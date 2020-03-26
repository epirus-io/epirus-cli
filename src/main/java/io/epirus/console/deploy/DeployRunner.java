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

import io.epirus.console.Faucet;
import io.epirus.console.WalletFunder;
import io.epirus.console.account.AccountManager;
import io.epirus.console.account.AccountUtils;
import io.epirus.console.config.CliConfig;
import io.epirus.console.project.utils.ProjectUtils;
import okhttp3.OkHttpClient;

import org.web3j.account.LocalWeb3jAccount;
import org.web3j.codegen.Console;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;

import static io.epirus.console.project.utils.ProjectUtils.uploadSolidityMetadata;

public class DeployRunner {
    public static final String USAGE = "epirus deploy <network>";
    private final Path workingDirectory;
    private final Network network;
    private final Credentials credentials;
    private final AccountManager accountManager;
    private final Web3j web3j;

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            Web3j web3j = null;
            try {
                web3j = Web3j.build(Network.valueOf(args[0].toUpperCase()));
            } catch (Exception e) {
                Console.exitError(e.getMessage());
            }
            new DeployRunner(
                            Network.valueOf(args[0].toUpperCase()),
                            new AccountManager(
                                    CliConfig.getConfig(
                                            CliConfig.getDefaultEpirusConfigPath().toFile()),
                                    new OkHttpClient()),
                            web3j)
                    .deploy();
        } else {
            Console.exitError(USAGE);
        }
    }

    public DeployRunner(
            Network network, AccountManager accountManager, Web3j web3j, Path workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.network = network;
        Path walletPath = ProjectUtils.loadProjectWalletFile(workingDirectory).get();
        Path walletPasswordPath =
                ProjectUtils.loadProjectPasswordWalletFile(workingDirectory).get();
        this.credentials = ProjectUtils.createCredentials(walletPath, walletPasswordPath);
        this.accountManager = accountManager;
        this.web3j = web3j;
    }

    public DeployRunner(Network network, AccountManager accountManager, Web3j web3j) {
        this.workingDirectory = Paths.get(System.getProperty("user.dir"));
        this.network = network;
        Path walletPath = ProjectUtils.loadProjectWalletFile(workingDirectory).get();
        Path walletPasswordPath =
                ProjectUtils.loadProjectPasswordWalletFile(workingDirectory).get();
        this.credentials = ProjectUtils.createCredentials(walletPath, walletPasswordPath);
        this.accountManager = accountManager;
        this.web3j = web3j;
    }

    public void deploy() throws Exception {
        AccountUtils.accountInit(accountManager);
        this.accountManager.checkIfAccountIsConfirmed();
        fundWallet();
        waitForBalanceUpdate();
        uploadSolidityMetadata(network, workingDirectory);
        runGradle(workingDirectory);
    }

    private void fundWallet() {
        try {
            BigInteger accountBalance =
                    accountManager.getAccountBalance(credentials, network, web3j);
            if (accountBalance.equals(BigInteger.ZERO)) {
                WalletFunder.fundWallet(
                        credentials.getAddress(),
                        Faucet.valueOf(network.getNetworkName().toUpperCase()),
                        this.accountManager.getLoginToken());
            }
        } catch (Exception e) {
            Console.exitError("Could not fund wallet: " + e.getMessage());
        }
    }

    private void waitForBalanceUpdate() {
        System.out.println("Checking the account balance...");
        try {
            BigInteger accountBalance =
                    accountManager.pollForAccountBalance(credentials, network, web3j, 5);

        } catch (Exception e) {
            Console.exitError(e.getMessage());
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
        String NODE_RPC_ENDPOINT = "https://%s-eth.epirus.io/%s";

        String httpEndpoint =
                String.format(
                        NODE_RPC_ENDPOINT,
                        network.getNetworkName(),
                        LocalWeb3jAccount.readConfigAsJson().get("loginToken").asText());
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("NODE_URL", httpEndpoint);
        int exitCode =
                processBuilder
                        .directory(workingDir)
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            Console.exitError("Could not build project.");
        } else {
            String epirus =
                    String.format(
                            "https://%s.epirus.io/accounts/%s",
                            network.getNetworkName(), credentials.getAddress());
            System.out.println("Project deployed successfully at: " + epirus);
        }
    }
}
