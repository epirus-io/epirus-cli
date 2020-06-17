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
package io.epirus.console.wallet.subcommands;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.utils.IODevice;
import io.epirus.console.wallet.WalletManager;
import picocli.CommandLine;

import org.web3j.codegen.Console;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

/** Simple class for creating a wallet file. */
@CommandLine.Command(
        name = "create",
        description = "Create wallets for Ethereum",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class WalletCreateCommand extends WalletManager implements Runnable {

    public WalletCreateCommand() {
        super();
    }

    public WalletCreateCommand(IODevice console) {
        super(console);
    }

    @Override
    public void run() {
        String password = getPassword("Please enter a wallet file password: ");
        String destinationDir = getDestinationDir();
        File destination = createDir(destinationDir);

        try {
            String walletFileName = WalletUtils.generateFullNewWalletFile(password, destination);
            notify(
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
