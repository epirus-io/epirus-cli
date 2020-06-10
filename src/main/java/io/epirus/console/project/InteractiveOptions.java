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
package io.epirus.console.project;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import io.epirus.console.account.AccountUtils;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import io.epirus.console.project.wallet.ProjectWalletUtils;

import static io.epirus.console.config.ConfigManager.config;
import static java.io.File.separator;
import static org.web3j.codegen.Console.exitError;

public class InteractiveOptions {
    private Scanner scanner;
    private PrintStream writer;

    public InteractiveOptions() {
        scanner = new Scanner(System.in);
        writer = System.out;
    }

    public InteractiveOptions(InputStream inputStream, PrintStream printStream) {
        scanner = new Scanner(inputStream);
        writer = printStream;
    }

    public String getProjectName() {
        print("Please enter the project name [Web3App]:");
        String projectName = getUserInput();
        if (projectName.trim().isEmpty()) {
            return "Web3App";
        }
        while (!InputVerifier.classNameIsValid(projectName)) {
            projectName = getUserInput();
        }
        return projectName;
    }

    public String getPackageName() {
        print("Please enter the package name for your project [io.epirus]:");
        String packageName = getUserInput();
        if (packageName.trim().isEmpty()) {
            return "io.epirus";
        }
        while (!InputVerifier.packageNameIsValid(packageName)) {
            packageName = getUserInput();
        }
        return packageName;
    }

    public Optional<String> getProjectDestination(final String projectName) {
        print(
                "Please enter the destination of your project ["
                        + System.getProperty("user.dir")
                        + "]: ");
        final String projectDest = getUserInput();
        final String projectPath = projectDest + separator + projectName;
        if (new File(projectPath).exists()) {
            if (overrideExistingProject()) {
                Path path = new File(projectPath).toPath();
                ProjectUtils.deleteFolder(path);
                return Optional.of(projectDest);
            } else {
                exitError("Project creation was canceled.");
            }
        }
        return projectDest.isEmpty() ? Optional.empty() : Optional.of(projectDest);
    }

    public Map<String, String> getWalletLocation() {
        Map<String, String> walletCredentials = new HashMap<>();
        ProjectWalletUtils walletUtils = new ProjectWalletUtils();
        if (userAnsweredYes("Would you like to use a global wallet [Y/n] ?")) {
            if (walletUtils.userHasGlobalWallets()) {
                print("Please choose your wallet:");
                int i = displayGlobalWallets(walletUtils);
                int walletNumber = Integer.parseInt(getUserInput());
                if (walletNumber >= 0 && walletNumber <= i) {
                    print("Please enter your wallet password.");
                    String walletPassword = getUserInput();
                    walletCredentials.put(
                            "path", walletUtils.getListOfGlobalWallets().get(walletNumber));
                    walletCredentials.put("password", walletPassword);
                    return walletCredentials;

                } else {
                    print("Wallet number is not valid. Exiting ...");
                    System.exit(1);
                }
            } else {
                if (userAnsweredYes(
                        "Looks like you don't have any global wallets. Would you like to generate one [Y/n] ?")) {
                    AccountUtils.accountDefaultWalletInit();
                    walletCredentials.put("path", ProjectWalletUtils.getGlobalWalletAbsolutePath());
                    walletCredentials.put("password", "");
                    return walletCredentials;
                }
            }
        } else {
            if (userAnsweredYes("Would you like to use an existing wallet [Y/n] ?")) {
                print("Please enter your wallet path: ");
                String walletPath = getUserInput();
                print(
                        "Please enter your wallet password (Leave empty if your wallet is not password protected)");
                String walletPassword = getUserInput();
                walletCredentials.put("path", walletPath);
                walletCredentials.put("password", walletPassword);
                return walletCredentials;
            } else {
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    private int displayGlobalWallets(ProjectWalletUtils walletUtils) {
        int i = 0;
        for (String walletName : walletUtils.getListOfGlobalWallets()) {
            print("[" + i++ + "] : " + walletName);
        }
        return i;
    }

    private boolean userAnsweredYes(String message) {
        print(message);
        String answer = getUserInput();
        return answer.trim().isEmpty()
                || answer.trim().toLowerCase().equals("y")
                || answer.trim().toLowerCase().equals("yes");
    }

    public Optional<String> getGeneratedWrapperLocation() {
        print(
                "Please enter the path of the generated contract wrappers ["
                        + String.join(
                                separator,
                                System.getProperty("user.dir"),
                                "build",
                                "generated",
                                "source",
                                "web3j",
                                "main",
                                "java")
                        + "]");
        String pathToTheWrappers = getUserInput();
        return pathToTheWrappers.isEmpty()
                ? Optional.of(
                        String.join(
                                separator,
                                System.getProperty("user.dir"),
                                "build",
                                "generated",
                                "source",
                                "web3j",
                                "main",
                                "java"))
                : Optional.of(pathToTheWrappers);
    }

    public Optional<String> setGeneratedTestLocationJava() {
        print(
                "Where would you like to save your tests ["
                        + String.join(
                                separator, System.getProperty("user.dir"), "src", "test", "java")
                        + "]");
        String outputPath = getUserInput();
        return outputPath.isEmpty()
                ? Optional.of(
                        String.join(
                                separator, System.getProperty("user.dir"), "src", "test", "java"))
                : Optional.of(outputPath);
    }

    public Optional<String> setGeneratedTestLocationKotlin() {
        print(
                "Where would you like to save your tests ["
                        + String.join(
                                separator, System.getProperty("user.dir"), "src", "test", "kotlin")
                        + "]");
        String outputPath = getUserInput();
        return outputPath.isEmpty()
                ? Optional.of(
                        String.join(
                                separator, System.getProperty("user.dir"), "src", "test", "kotlin"))
                : Optional.of(outputPath);
    }

    public boolean userWantsTests() {
        print("Would you like to generate unit test for your solidity contracts [Y/n] ? ");
        String userAnswer = getUserInput();
        return userAnswer.trim().toLowerCase().equals("y") || userAnswer.trim().equals("");
    }

    public String getSolidityProjectPath() {
        print("Please enter the path to your solidity file/folder [Required Field]: ");
        return getUserInput();
    }

    public boolean overrideExistingProject() {
        print("Looks like the project exists. Would you like to overwrite it [y/N] ?");
        String userAnswer = getUserInput();
        return userAnswer.toLowerCase().equals("y");
    }

    public boolean isUserLoggedIn() {
        return config.getClientId() != null && config.getClientId().length() > 0;
    }

    public boolean doesUserWantEpirusAccount() {
        print("It looks like you don’t have a Web3j account, would you like to create one?");
        print("This will provide free access to the Ethereum network [Y/n]");
        String userAnswer = getUserInput();
        return userAnswer.toLowerCase().equals("y") || userAnswer.trim().equals("");
    }

    public String getEmail() {
        print("Please enter your email address: ");

        return getUserInput();
    }

    private String getUserInput() {
        return scanner.nextLine();
    }

    private void print(final String text) {
        System.out.println(text);
    }
}
