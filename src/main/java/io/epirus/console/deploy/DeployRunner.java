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

import static io.epirus.console.project.utils.ProjectUtils.uploadSolidityMetadata;

public class DeployRunner {
    public static final String USAGE = "epirus deploy <network>";
    private final Path workingDirectory;
    private final Network network;
    private final Credentials credentials;
    private final AccountManager accountManager;

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            new DeployRunner(
                            Network.valueOf(args[0].toUpperCase()),
                            new AccountManager(
                                    CliConfig.getConfig(
                                            CliConfig.getDefaultEpirusConfigPath().toFile()),
                                    new OkHttpClient()))
                    .deploy();
        } else {
            Console.exitError(USAGE);
        }
    }

    public DeployRunner(Network network, AccountManager accountManager, Path workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.network = network;
        Path walletPath = ProjectUtils.loadProjectWalletFile(workingDirectory).get();
        Path walletPasswordPath =
                ProjectUtils.loadProjectPasswordWalletFile(workingDirectory).get();
        this.credentials = ProjectUtils.createCredentials(walletPath, walletPasswordPath);
        this.accountManager = accountManager;
    }

    public DeployRunner(Network network, AccountManager accountManager) {
        this.workingDirectory = Paths.get(System.getProperty("user.dir"));
        this.network = network;
        Path walletPath = ProjectUtils.loadProjectWalletFile(workingDirectory).get();
        Path walletPasswordPath =
                ProjectUtils.loadProjectPasswordWalletFile(workingDirectory).get();
        this.credentials = ProjectUtils.createCredentials(walletPath, walletPasswordPath);
        this.accountManager = accountManager;
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
        int tries = 5;
        while (tries-- > 0) {
            try {
                BigInteger accountBalance = accountManager.getAccountBalance(credentials, network);
                if (accountBalance.equals(BigInteger.ZERO)) {
                    WalletFunder.fundWallet(
                            credentials.getAddress(),
                            Faucet.valueOf(network.getNetworkName().toUpperCase()),
                            "alex");
                }
                return;
            } catch (Exception e) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void waitForBalanceUpdate() {
        System.out.println("Checking the account balance...");
        int tries = 5;
        while (tries-- > 0) {
            try {
                BigInteger accountBalance = accountManager.getAccountBalance(credentials, network);
                if (!accountBalance.equals(BigInteger.ZERO)) {
                    return;
                } else {
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void runGradle(Path runLocation) throws Exception {
        executeProcess(
                new File(File.separator, runLocation.toString()),
                new String[] {"bash", "-c", "./gradlew run -q"});
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
            System.out.println("Project deployed successfully");
        }
    }
}
