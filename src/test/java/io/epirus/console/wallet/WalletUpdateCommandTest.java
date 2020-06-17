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
import io.epirus.console.wallet.subcommands.WalletUpdateCommand;
import org.junit.jupiter.api.Test;

import static io.epirus.console.wallet.WalletImportCommandTest.WALLET_PASSWORD;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WalletUpdateCommandTest {
    IODevice console = mock(IODevice.class);

    @Test
    public void testWalletUpdate() {
        when(console.readPassword(startsWith("Please enter your existing wallet file password")))
                .thenReturn(WALLET_PASSWORD);

        when(console.readPassword(contains("password")))
                .thenReturn(WALLET_PASSWORD, WALLET_PASSWORD);
        when(console.readLine(startsWith("Please enter a destination directory ")))
                .thenReturn(Folders.tempBuildFolder().getAbsolutePath());
        when(console.readLine(startsWith("Would you like to delete"))).thenReturn("N");

        new WalletUpdateCommand(
                        console,
                        WalletImportCommandTest.class
                                .getResource(
                                        "/keyfiles/"
                                                + "UTC--2016-11-03T05-55-06.340672473Z--ef678007d18427e6022059dbc264f27507cd1ffc")
                                .getFile())
                .run();

        verify(console).printf(contains("successfully created in"));
    }
}
