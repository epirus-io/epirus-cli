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
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import io.epirus.console.utils.IODevice;

import org.web3j.codegen.Console;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

/** Simple class for creating a wallet file. */
public class WalletCreator extends WalletManager {

    public WalletCreator() {}

    public WalletCreator(IODevice console) {
        super(console);
    }

    public static void main(String[] args) {
        new WalletCreator().run();
    }

    static void main(IODevice console) {
        new WalletCreator(console).run();
    }

    private void run() {
        String password = getPassword("Please enter a wallet file password: ");
        String destinationDir = getDestinationDir();
        File destination = createDir(destinationDir);

        try {
            String walletFileName = WalletUtils.generateFullNewWalletFile(password, destination);
            console.printf(
                    "Wallet file "
                            + walletFileName
                            + " successfully created in: "
                            + destinationDir
                            + "\n");
        } catch (CipherException
                | IOException
                | InvalidAlgorithmParameterException
                | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            Console.exitError(e);
        }
    }
}
