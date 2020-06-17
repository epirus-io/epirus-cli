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
package io.epirus.console.wallet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import io.epirus.console.utils.ConsoleDevice;
import io.epirus.console.utils.IODevice;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import static org.web3j.codegen.Console.exitError;

/** Interactive wallet console */
public abstract class WalletManager {

    final IODevice console;

    public WalletManager() {
        console = new ConsoleDevice();
    }

    public WalletManager(IODevice console) {
        this.console = console;
    }

    protected String request(final String message, final Object... args) {
        return console.readLine(message, args).trim();
    }

    protected void notify(final String message, final Object... args) {
        console.printf(message, args);
    }

    protected String getPassword(String initialPrompt) {
        while (true) {
            char[] input1 = console.readPassword(initialPrompt);
            char[] input2 = console.readPassword("Please re-enter the password: ");

            if (Arrays.equals(input1, input2)) {
                return new String(input1);
            } else {
                console.printf("Sorry, passwords did not match\n");
            }
        }
    }

    protected String getDestinationDir() {
        String defaultDir = WalletUtils.getTestnetKeyDirectory();
        String destinationDir =
                console.readLine(
                        "Please enter a destination directory location [" + defaultDir + "]: ");
        if (destinationDir.equals("")) {
            return defaultDir;
        } else if (destinationDir.startsWith("~")) {
            return System.getProperty("user.home") + destinationDir.substring(1);
        } else {
            return destinationDir;
        }
    }

    protected File createDir(String destinationDir) {
        File destination = new File(destinationDir);

        if (!destination.exists()) {
            console.printf("Creating directory: " + destinationDir + " ...");
            if (!destination.mkdirs()) {
                exitError(
                        "Unable to create destination directory ["
                                + destinationDir
                                + "], exiting...");
            } else {
                console.printf("complete\n");
            }
        }

        return destination;
    }

    protected Credentials getCredentials(File walletFile) {
        if (!walletFile.exists() || !walletFile.isFile()) {
            exitError("Unable to read wallet file: " + walletFile);
        }
        return loadWalletFile(walletFile);
    }

    private Credentials loadWalletFile(File walletFile) {
        while (true) {
            char[] password =
                    console.readPassword("Please enter your existing wallet file password: ");
            String currentPassword = new String(password);
            try {
                return WalletUtils.loadCredentials(currentPassword, walletFile);
            } catch (CipherException e) {
                console.printf("Invalid password specified\n");
            } catch (IOException e) {
                exitError("Unable to load wallet file: " + walletFile + "\n" + e.getMessage());
            }
        }
    }
}
