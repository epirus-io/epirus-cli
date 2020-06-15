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
import java.util.Map;

import io.epirus.console.account.AccountCommand;
import io.epirus.console.account.subcommands.LoginCommand;
import io.epirus.console.account.subcommands.LogoutCommand;
import io.epirus.console.config.ConfigManager;
import io.epirus.console.docker.DockerCommand;
import io.epirus.console.project.ImportProjectCommand;
import io.epirus.console.project.NewProjectCommand;
import io.epirus.console.project.testing.ProjectTestCommand;
import io.epirus.console.security.ContractAuditCommand;
import io.epirus.console.wallet.WalletCommand;
import io.epirus.console.web.services.Telemetry;
import io.epirus.console.web.services.Updater;
import io.epirus.console.wrapper.SolidityFunctionWrapperGeneratorCommand;
import io.epirus.console.wrapper.TruffleFunctionWrapperGeneratorCommand;
import picocli.CommandLine;

import org.web3j.codegen.Console;

import static io.epirus.console.config.ConfigManager.config;
import static org.web3j.codegen.Console.exitSuccess;

/** Main entry point for running command line utilities. */
@CommandLine.Command(
        name = "epirus",
        subcommands = {
            CommandLine.HelpCommand.class,
            WalletCommand.class,
            DockerCommand.class,
            SolidityFunctionWrapperGeneratorCommand.class,
            TruffleFunctionWrapperGeneratorCommand.class,
            ContractAuditCommand.class,
            NewProjectCommand.class,
            ImportProjectCommand.class,
            AccountCommand.class,
            LoginCommand.class,
            LogoutCommand.class,
            ProjectTestCommand.class
        },
        showDefaultValues = true,
        abbreviateSynopsis = true,
        description = "Run Epirus CLI commands",
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class EpirusCommand {

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

    private final Map<String, String> environmentVariables;

    public EpirusCommand(final Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public int parse(final String[] args) {
        final CommandLine commandLine = new CommandLine(this);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        System.out.println(LOGO);
        try {
            ConfigManager.setProduction();
            Updater.promptIfUpdateAvailable();
        } catch (IOException e) {
            Console.exitError("Failed to initialise the CLI");
        }

        return commandLine.execute(args);
    }

    // FIXME: not sure where this goes
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
