/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.epirus.console.docker.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.core.DockerClientBuilder;
import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.docker.DockerCommand;
import io.epirus.console.docker.DockerOperations;
import io.epirus.console.project.InteractiveOptions;
import io.epirus.console.wrapper.CredentialsOptions;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import org.web3j.codegen.Console;

import static io.epirus.console.config.ConfigManager.config;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "run",
        description = "Run project in docker",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class DockerRunCommand implements DockerOperations, Runnable {
    @CommandLine.Option(names = {"-l", "--local"})
    boolean localMode;

    @CommandLine.Mixin CredentialsOptions credentialsOptions;

    @CommandLine.Option(
            names = {"-d", "--directory"},
            description = "Directory to run docker in.",
            showDefaultValue = ALWAYS)
    Path directory = Paths.get(System.getProperty("user.dir"));

    @CommandLine.Option(names = {"-p", "--print"})
    boolean print;

    @Override
    public void run() {

        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withShowAll(true);

        if (listContainersCmd.exec().stream().noneMatch(i -> i.getImage().equals("web3app"))) {
            if (new InteractiveOptions()
                    .userAnsweredYes(
                            "It seems that no Docker container has yet been built. Would you like to build a Dockerized version of your app now?")) {
                new CommandLine(new DockerCommand())
                        .execute(
                                "build",
                                "-d",
                                Paths.get(System.getProperty("user.dir"))
                                        .toAbsolutePath()
                                        .toString());
            }
        }

        String[] args =
                new String[] {
                    "docker",
                    "run",
                    "--env",
                    String.format("EPIRUS_LOGIN_TOKEN=%s", config.getLoginToken()),
                    setCredentials()
                };

        if (localMode) {
            args =
                    ArrayUtils.addAll(
                            args,
                            "-v",
                            String.format(
                                    "%s/.epirus:/root/.epirus", System.getProperty("user.home")));
        }

        args = ArrayUtils.addAll(args, "web3app");

        if (print) {
            System.out.println(String.join(" ", args));
            return;
        }

        try {
            executeDocker(args, directory);
        } catch (Exception e) {
            Console.exitError(e);
        }
    }

    private String setCredentials() {
        if (credentialsOptions.getWalletPath() != null) {
            return getWalletEnvironment(credentialsOptions.getWalletPath());
        } else if (credentialsOptions.getRawKey() != null) {
            return "--env "
                    + String.format("EPIRUS_PRIVATE_KEY=%s", credentialsOptions.getRawKey());
        } else if (credentialsOptions.getJson() != null) {
            return "--env " + String.format("EPIRUS_WALLET_JSON=%s", credentialsOptions.getJson());
        }
        return getWalletEnvironment(Paths.get(config.getDefaultWalletPath()));
    }

    @NotNull
    private String getWalletEnvironment(final Path path) {
        String walletEnvironment =
                "--env "
                        + String.format(
                                "EPIRUS_WALLET_PATH=%s",
                                "/root/key/" + path.getFileName().toString())
                        + " -v "
                        + path.getParent().toAbsolutePath().toString()
                        + ":/root/key";
        if (credentialsOptions.getWalletPassword() != null) {
            walletEnvironment +=
                    " --env "
                            + String.format(
                                    "EPIRUS_WALLET_PASSWORD=%s",
                                    credentialsOptions.getWalletPassword());
        }
        return walletEnvironment;
    }
}
