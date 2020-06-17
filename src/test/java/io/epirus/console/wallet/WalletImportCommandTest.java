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

import io.epirus.console.project.utils.Folders;
import io.epirus.console.utils.IODevice;
import io.epirus.console.wallet.subcommands.WalletImportCommand;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WalletImportCommandTest {

    static final char[] WALLET_PASSWORD = SampleKeys.PASSWORD.toCharArray();
    IODevice console = mock(IODevice.class);

    @Test
    public void testSpecifyPrivateKey() {
        prepareWalletCreation(
                SampleKeys.PRIVATE_KEY_STRING, Folders.tempBuildFolder().getAbsolutePath());
    }

    @Test
    public void testLoadPrivateKeyFromFile() {
        prepareWalletCreation(
                WalletImportCommandTest.class
                        .getResource("/keyfiles/" + "sample-private-key.txt")
                        .getFile(),
                Folders.tempBuildFolder().getAbsolutePath());
    }

    private void prepareWalletCreation(String input, String tempDirPath) {
        when(console.readLine(startsWith("Please enter the hex encoded private key")))
                .thenReturn(input);
        when(console.readPassword(contains("password")))
                .thenReturn(WALLET_PASSWORD, WALLET_PASSWORD);
        when(console.readLine(contains("Please enter a destination directory location")))
                .thenReturn(tempDirPath);

        new WalletImportCommand(console).run();

        verify(console).printf(contains("successfully created in"));
    }
}
