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
package io.epirus.console.account;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import io.epirus.console.project.InteractiveOptions;
import io.epirus.console.project.wallet.ProjectWallet;
import io.epirus.console.project.wallet.ProjectWalletUtils;

import org.web3j.codegen.Console;
import org.web3j.crypto.CipherException;

public class AccountUtils {

    public static void accountInit(AccountService accountService) {
        InteractiveOptions interactiveOptions = new InteractiveOptions();

        if (!interactiveOptions.isUserLoggedIn()
                && interactiveOptions.doesUserWantEpirusAccount()) {
            if (accountService.createAccount(interactiveOptions.getEmail())) {
                System.out.println(
                        "Account created successfully. You can now use Epirus Cloud. Please confirm your e-mail within 24 hours to continue using all features without interruption.");
            } else {
                Console.exitError(
                        "Server response did not contain the authentication token required to create an account.");
            }
        }
    }

    public static void accountDefaultWalletInit() {
        if (!ProjectWalletUtils.userHasGlobalWallet()) {
            try {
                ProjectWallet projectWallet =
                        new ProjectWallet("", ProjectWalletUtils.DEFAULT_WALLET_LOOKUP_PATH);
                boolean walletWasRenamed =
                        new File(
                                        projectWallet.getWalletPath()
                                                + File.separator
                                                + projectWallet.getWalletName())
                                .renameTo(
                                        new File(
                                                ProjectWalletUtils.DEFAULT_WALLET_LOOKUP_PATH
                                                        + File.separator
                                                        + ProjectWalletUtils.DEFAULT_WALLET_NAME));
                if (!walletWasRenamed) {
                    Console.exitError("Could not rename default test wallet.");
                }
                System.out.println("Default wallet was created successfully.");

            } catch (NoSuchAlgorithmException
                    | NoSuchProviderException
                    | InvalidAlgorithmParameterException
                    | CipherException
                    | IOException e) {
                Console.exitError("Could not create default wallet reason: " + e.getMessage());
            }
        } else {
            System.out.println("Account has default wallet.");
        }
    }
}
