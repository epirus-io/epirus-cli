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
import java.nio.file.Paths;

import io.epirus.console.ProjectTest;
import io.epirus.console.account.AccountService;
import org.junit.jupiter.api.Test;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeployCommandTest extends ProjectTest {

    @Test
    public void testAccountDeployment() throws Exception {
        AccountService accountService = mock(AccountService.class);
        Web3j web3j = mock(Web3j.class);
        when(accountService.pollForAccountBalance(
                        any(Credentials.class),
                        any(Network.class),
                        any(Web3j.class),
                        any(int.class)))
                .thenReturn(BigInteger.TEN);
        when(accountService.checkIfAccountIsConfirmed(20)).thenReturn(true);
        DeployCommand deployCommand =
                spy(
                        new DeployCommand(
                                Network.RINKEBY,
                                accountService,
                                web3j,
                                Paths.get(workingDirectory + File.separator + "Test"),
                                absoluteWalletPath));
        doNothing().when(deployCommand).deploy();
        deployCommand.deploy();
        verify(deployCommand, times(1)).deploy();
    }
}
