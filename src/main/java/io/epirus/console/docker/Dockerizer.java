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
package io.epirus.console.docker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.ArrayUtils;
import picocli.CommandLine;

import org.web3j.codegen.Console;

import static io.epirus.console.config.ConfigManager.config;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_LOOKUP_PATH;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_NAME;
import static io.epirus.console.utils.PrinterUtilities.printErrorAndExit;

@CommandLine.Command(name = "docker")
public class Dockerizer implements Runnable {
    @CommandLine.Option(names = {"-l", "--local"})
    boolean localMode;

    @CommandLine.Parameters(index = "0", description = "The docker command to run.")
    private String command;

    private static final String USAGE = "docker build|run";

    private void dockerBuild() throws IOException, InterruptedException {
        executeDocker(new String[] {"docker", "build", "-t", "web3app", "."});
    }

    private void dockerRun() throws IOException, InterruptedException {
        String walletJson =
                new String(
                        Files.readAllBytes(
                                Paths.get(DEFAULT_WALLET_LOOKUP_PATH, DEFAULT_WALLET_NAME)));
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

        args = ArrayUtils.addAll(args, "web3app", "-j", walletJson);

        executeDocker(args);
    }

    private void executeDocker(String[] command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        System.out.println(String.join(" ", command));
        int exitCode =
                processBuilder
                        .directory(Paths.get(System.getProperty("user.dir")).toFile())
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            printErrorAndExit("Could not run project.");
        }
    }

    @Override
    public void run() {
        try {
            switch (command) {
                case "build":
                    dockerBuild();
                    break;
                case "run":
                    dockerRun();
                    break;
                default:
                    Console.exitError(USAGE);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
