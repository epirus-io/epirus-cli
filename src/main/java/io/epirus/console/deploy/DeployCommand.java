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
package io.epirus.console.deploy;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.diogonunes.jcdp.color.api.Ansi;
import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.account.AccountService;
import io.epirus.console.account.AccountUtils;
import io.epirus.console.account.subcommands.LoginCommand;
import io.epirus.console.project.utils.ProjectUtils;
import io.epirus.console.wallet.Faucet;
import io.epirus.console.wallet.subcommands.WalletFundCommand;
import io.epirus.web3j.Epirus;
import picocli.CommandLine;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;

import static io.epirus.console.config.ConfigManager.config;
import static io.epirus.console.project.utils.ProjectUtils.uploadSolidityMetadata;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_LOOKUP_PATH;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_NAME;
import static io.epirus.console.utils.PrinterUtilities.*;
import static org.web3j.utils.Convert.Unit.ETHER;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "deploy",
        description = "Deploy your project to an Ethereum network",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class DeployCommand implements Runnable {
    private Path workingDirectory;
    private Network network;
    private AccountService accountService;

    private Credentials credentials;

    private Web3j web3j;

    @CommandLine.Option(
            names = {"-w", "--wallet-path"},
            description = "Path to your wallet file")
    public String walletPath = DEFAULT_WALLET_LOOKUP_PATH + File.separator + DEFAULT_WALLET_NAME;

    @CommandLine.Option(
            names = {"-k", "--wallet-password"},
            description = "Wallet password",
            showDefaultValue = ALWAYS)
    public String walletPassword = "";

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "network",
            description = "Ethereum network [rinkeby/kovan]",
            arity = "1")
    String deployNetwork;

    public DeployCommand(
            Network network,
            AccountService accountService,
            Web3j web3j,
            Path workingDirectory,
            String walletPath) {
        this.workingDirectory = workingDirectory;
        this.network = network;
        this.walletPath = walletPath;
        this.credentials = ProjectUtils.createCredentials(Paths.get(walletPath), walletPassword);
        this.accountService = accountService;
        this.web3j = web3j;
    }

    public DeployCommand(
            Network network, AccountService accountService, Credentials credentials, Web3j web3j) {
        this.workingDirectory = Paths.get(System.getProperty("user.dir"));
        this.network = network;
        this.credentials = credentials;
        this.accountService = accountService;
        this.web3j = web3j;
    }

    public DeployCommand() {}

    @Override
    public void run() {
        Web3j web3j = null;
        if (config.getLoginToken() == null || config.getLoginToken().length() == 0) {
            System.out.println(
                    "You aren't currently logged in to the Epirus Platform. Please create an account if you don't have one (https://portal.epirus.io/account/signup). If you do have an account, you can log in below:");
            new LoginCommand().run();
        }

        String walletJson = System.getenv("WALLET_JSON");
        if (walletJson == null || walletJson.isEmpty()) {
            this.credentials =
                    ProjectUtils.createCredentials(Paths.get(walletPath), walletPassword);
        } else {
            this.credentials = ProjectUtils.createCredentials(walletJson, walletPassword);
        }

        try {
            web3j = Epirus.buildWeb3j(Network.valueOf(deployNetwork.toUpperCase()));
        } catch (Exception e) {
            printErrorAndExit(e.getMessage());
        }
        try {
            new DeployCommand(
                            Network.valueOf(deployNetwork.toUpperCase()),
                            new AccountService(),
                            credentials,
                            web3j)
                    .deploy();
        } catch (Exception e) {
            printErrorAndExit(
                    "Epirus failed to deploy the project. For more information please see the log file.");
        }
    }

    public void deploy() throws Exception {
        coloredPrinter.println("Preparing to deploy your Web3App");
        System.out.print(System.lineSeparator());
        AccountUtils.accountInit(accountService);
        if (accountService.checkIfAccountIsConfirmed(20)) {
            printInformationPairWithStatus("Account status", 20, "ACTIVE ", Ansi.FColor.GREEN);
            System.out.print(System.lineSeparator());
        } else {
            printErrorAndExit(
                    "Please check your email and activate your account in order to take advantage our features. Once your account is activated you can re-run the command.");
        }
        fundWallet();
        uploadSolidityMetadata(network, workingDirectory);
        System.out.print(System.lineSeparator());
        coloredPrinter.println("Deploying your Web3App");
        System.out.print(System.lineSeparator());
        runGradle(workingDirectory);
    }

    private void fundWallet() {
        BigInteger accountBalance = accountService.getAccountBalance(credentials, web3j);
        printInformationPair(
                "Wallet balance",
                20,
                Convert.fromWei(String.valueOf(accountBalance), ETHER) + " ETH",
                Ansi.FColor.GREEN);
        try {
            if (accountBalance.equals(BigInteger.ZERO)) {
                String result =
                        WalletFundCommand.fundWallet(
                                credentials.getAddress(),
                                Faucet.valueOf(network.getNetworkName().toUpperCase()),
                                this.accountService.getLoginToken());
                printInformationPair("Funding wallet with", 20, "0.2 ETH", Ansi.FColor.GREEN);
                waitForBalanceUpdate(result);
            }

        } catch (Exception e) {
            printErrorAndExit("Could not fund wallet: " + e.getMessage());
        }
    }

    private void waitForBalanceUpdate(String txHash) {
        coloredPrinter.println(
                "Waiting for balance update",
                Ansi.Attribute.CLEAR,
                Ansi.FColor.YELLOW,
                Ansi.BColor.BLACK);
        try {
            System.out.printf("Waiting for transaction %s to be mined...\n", txHash);

            BigInteger accountBalance =
                    accountService.pollForAccountBalance(credentials, network, web3j, 5);

        } catch (Exception e) {
            printErrorAndExit(e.getMessage());
        }
    }

    private void runGradle(Path runLocation) throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            executeProcess(
                    new File(File.separator, runLocation.toString()),
                    new String[] {"cmd.exe", "/c", "./gradlew.bat run", "-q"});
        } else {
            executeProcess(
                    new File(File.separator, runLocation.toString()),
                    new String[] {"bash", "-c", "./gradlew run -q"});
        }
    }

    private void executeProcess(File workingDir, String[] command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("DEPLOY_NETWORK", network.getNetworkName());
        int exitCode =
                processBuilder
                        .directory(workingDir)
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            printErrorAndExit("Could not build project.");
        } else {
            printInformationPair(
                    "Wallet address",
                    20,
                    String.format(
                            "https://%s.epirus.io/accounts/%s",
                            network.getNetworkName(), credentials.getAddress()),
                    Ansi.FColor.BLUE);
        }
    }
}
