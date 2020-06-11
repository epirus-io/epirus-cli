/*
 * Copyright 2019 Web3 Labs Ltd.
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
package io.epirus.console;

import java.io.IOException;
import java.net.URISyntaxException;

import io.epirus.console.account.AccountManager;
import io.epirus.console.config.ConfigManager;
import io.epirus.console.deploy.DeployRunner;
import io.epirus.console.docker.Dockerizer;
import io.epirus.console.logging.EpirusExceptionHandler;
import io.epirus.console.project.ProjectCreator;
import io.epirus.console.project.ProjectImporter;
import io.epirus.console.project.UnitTestCreator;
import io.epirus.console.project.testing.ProjectTester;
import io.epirus.console.security.ContractAuditor;
import io.epirus.console.utils.CliVersion;
import io.epirus.console.wallet.WalletRunner;
import io.epirus.console.web.services.Telemetry;
import io.epirus.console.web.services.Updater;
import picocli.CommandLine;

import org.web3j.codegen.Console;
import org.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.web3j.codegen.TruffleJsonFunctionWrapperGenerator;

import static io.epirus.console.config.ConfigManager.config;
import static io.epirus.console.project.ProjectCreator.COMMAND_NEW;
import static io.epirus.console.project.ProjectImporter.COMMAND_IMPORT;
import static io.epirus.console.project.UnitTestCreator.COMMAND_GENERATE_TESTS;
import static org.web3j.codegen.Console.exitSuccess;
import static org.web3j.codegen.SolidityFunctionWrapperGenerator.COMMAND_SOLIDITY;
import static org.web3j.utils.Collection.tail;

/** Main entry point for running command line utilities. */
public class Runner {

    private static final String USAGE =
            "Usage: epirus version|wallet|solidity|new|import|generate-tests|audit|account|docker ...";

    private static final String LOGO =
            "  ______       _                \n"
                    + " |  ____|     (_)               \n"
                    + " | |__   _ __  _ _ __ _   _ ___ \n"
                    + " |  __| | '_ \\| | '__| | | / __|\n"
                    + " | |____| |_) | | |  | |_| \\__ \\\n"
                    + " |______| .__/|_|_|   \\__,_|___/\n"
                    + "        | |                     \n"
                    + "        |_|                     ";

    public static void main(String[] args) throws Exception {
        EpirusExceptionHandler globalExceptionHandler = new EpirusExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);

        System.out.println(LOGO);

        ConfigManager.setProduction();

        Updater.promptIfUpdateAvailable();

        if (args.length < 1) {
            Console.exitError(USAGE);
        } else {
            performStartupTasks(args);
            switch (args[0]) {
                case "docker":
                    new CommandLine(new Dockerizer()).execute(tail(args));
                    break;
                case "deploy":
                    DeployRunner.main(tail(args));
                    break;
                case "wallet":
                    WalletRunner.main(tail(args));
                    break;
                case COMMAND_SOLIDITY:
                    SolidityFunctionWrapperGenerator.main(tail(args));
                    break;
                case "truffle":
                    TruffleJsonFunctionWrapperGenerator.run(tail(args));
                    break;
                case COMMAND_NEW:
                    ProjectCreator.main(tail(args));
                    break;
                case COMMAND_IMPORT:
                    ProjectImporter.main(tail(args));
                    break;
                case "version":
                    System.out.println(
                            "Version: "
                                    + CliVersion.getVersion()
                                    + "\n"
                                    + "Build timestamp: "
                                    + CliVersion.getTimestamp());
                    break;
                case "audit":
                    ContractAuditor.main(tail(args));
                    break;
                case "account":
                    AccountManager.main(tail(args));
                    break;
                case "login":
                    AccountManager.main(new String[] {"login"});
                    break;
                case "logout":
                    config.setLoginToken("");
                    System.out.println("Logged out successfully");
                    break;
                case COMMAND_GENERATE_TESTS:
                    UnitTestCreator.main(tail(args));
                    break;
                case "test":
                    ProjectTester.main();
                    break;
                default:
                    Console.exitError(USAGE);
            }
        }
        exitSuccess();
    }

    private static void performStartupTasks(String[] args) throws IOException, URISyntaxException {
        if (args[0].equals("--telemetry")) {
            Telemetry.uploadTelemetry(args);
            Updater.onlineUpdateCheck();
            exitSuccess();
        } else if (!config.isTelemetryDisabled()) {
            Telemetry.invokeTelemetryUpload(args);
        }
    }
}
