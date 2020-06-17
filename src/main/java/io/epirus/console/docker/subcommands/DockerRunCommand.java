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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.docker.DockerOperations;
import org.apache.commons.lang3.ArrayUtils;
import picocli.CommandLine;

import org.web3j.codegen.Console;

import static io.epirus.console.config.ConfigManager.config;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_LOOKUP_PATH;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_NAME;

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

    @CommandLine.Option(names = {"-w", "--wallet-path"})
    Path walletPath = Paths.get(DEFAULT_WALLET_LOOKUP_PATH, DEFAULT_WALLET_NAME);

    @Override
    public void run() {
        String walletJson = null;
        try {
            walletJson = new String(Files.readAllBytes(walletPath));
        } catch (IOException e) {
            Console.exitError("Failed to read default wallet in home directory");
        }
        String[] args =
                new String[] {
                    "docker",
                    "run",
                    "--env",
                    String.format("EPIRUS_LOGIN_TOKEN=%s", config.getLoginToken()),
                    "--env",
                    String.format("EPIRUS_WALLET=%s", walletJson),
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

        try {
            executeDocker(args, walletPath);
        } catch (Exception e) {
            Console.exitError(e);
        }
    }
}
