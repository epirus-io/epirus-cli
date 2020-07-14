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
import io.epirus.console.deploy.DeployCommand;
import io.epirus.console.docker.DockerCommand;
import io.epirus.console.openapi.OpenApiCommand;
import io.epirus.console.project.ImportProjectCommand;
import io.epirus.console.project.InteractiveOptions;
import io.epirus.console.project.NewProjectCommand;
import io.epirus.console.project.UnitTestCommand;
import io.epirus.console.project.testing.ProjectTestCommand;
import io.epirus.console.security.ContractAuditCommand;
import io.epirus.console.token.TokenCommand;
import io.epirus.console.wallet.WalletCommand;
import io.epirus.console.web.services.Telemetry;
import io.epirus.console.web.services.Updater;
import io.epirus.console.wrapper.SolidityFunctionWrapperGeneratorCommand;
import io.epirus.console.wrapper.TruffleFunctionWrapperGeneratorCommand;
import picocli.CommandLine;

import org.web3j.codegen.Console;

import static io.epirus.console.config.ConfigManager.config;
import static java.io.File.separator;
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
            ProjectTestCommand.class,
            UnitTestCommand.class,
            DeployCommand.class,
            OpenApiCommand.class,
            TokenCommand.class,
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
public class EpirusCommand implements Runnable {

    public static final String DEFAULT_WALLET_FOLDER =
            System.getProperty("user.home") + separator + ".epirus" + separator + "keystore";

    private static final String LOGO =
            "  ______       _                \n"
                    + " |  ____|     (_)               \n"
                    + " | |__   _ __  _ _ __ _   _ ___ \n"
                    + " |  __| | '_ \\| | '__| | | / __|\n"
                    + " | |____| |_) | | |  | |_| \\__ \\\n"
                    + " |______| .__/|_|_|   \\__,_|___/\n"
                    + "        | |                     \n"
                    + "        |_|                     ";

    private final CommandLine commandLine;
    private final Map<String, String> environmentVariables;
    private final String[] args;

    @CommandLine.Option(
            names = {"--telemetry"},
            description = "Whether to perform analytics.",
            defaultValue = "false")
    public boolean telemetry;

    public EpirusCommand(final Map<String, String> environmentVariables, String[] args) {
        this.commandLine = new CommandLine(this);
        this.environmentVariables = environmentVariables;
        this.args = args;
    }

    public int parse() {
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        System.out.println(LOGO);
        try {
            ConfigManager.setProduction();
            maybeCreateDefaultWallet();
            Updater.promptIfUpdateAvailable();
        } catch (IOException e) {
            Console.exitError("Failed to initialise the CLI");
        }

        return commandLine.execute(args);
    }

    @Override
    public void run() {
        performTelemetryUpload();
    }

    private void maybeCreateDefaultWallet() {
        if (config.getDefaultWalletPath() == null || config.getDefaultWalletPath().isEmpty()) {
            final String walletPath =
                    new InteractiveOptions().createWallet(DEFAULT_WALLET_FOLDER, "");
            config.setDefaultWalletPath(walletPath);
        }
    }

    private void performTelemetryUpload() {
        if (args.length == 0) {
            commandLine.usage(System.out);
        }
        if (telemetry) {
            Telemetry.uploadTelemetry(args);
            Updater.onlineUpdateCheck();
            exitSuccess();
        } else if (!config.isTelemetryDisabled()) {
            try {
                Telemetry.invokeTelemetryUpload(args);
            } catch (URISyntaxException | IOException e) {
                Console.exitError("Failed to invoke telemetry upload");
            }
        }
    }
}
