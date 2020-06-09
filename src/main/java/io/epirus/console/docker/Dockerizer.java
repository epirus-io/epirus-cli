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
import java.nio.file.Paths;

import org.web3j.codegen.Console;

import static io.epirus.console.config.ConfigManager.config;
import static io.epirus.console.utils.PrinterUtilities.printErrorAndExit;

public class Dockerizer {
    private static final String USAGE = "docker build|run";

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            Console.exitError(USAGE);
        } else {
            switch (args[0]) {
                case "build":
                    dockerBuild();
                    break;
                case "run":
                    dockerRun();
                    break;
                default:
                    Console.exitError(USAGE);
            }
        }
    }

    private static void dockerBuild() throws IOException, InterruptedException {
        executeDocker(new String[] {"docker", "build", "-t", "web3app", "."});
    }

    private static void dockerRun() throws IOException, InterruptedException {
        executeDocker(
                new String[] {
                    "docker",
                    "run",
                    "--env",
                    String.format("EPIRUS_LOGIN_TOKEN=%s", config.getLoginToken()),
                    "web3app"
                });
    }

    private static void executeDocker(String[] command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        int exitCode =
                processBuilder
                        .directory(Paths.get(System.getProperty("user.dir")).toFile())
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            printErrorAndExit("Could not build project.");
        }
    }
}
