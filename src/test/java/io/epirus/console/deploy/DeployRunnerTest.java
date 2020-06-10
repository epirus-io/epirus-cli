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
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collections;

import io.epirus.console.account.AccountManager;
import io.epirus.console.project.ProjectCreator;
import io.epirus.console.project.utils.ClassExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeployRunnerTest extends ClassExecutor {

    @TempDir static File workingDirectory;
    private String absoluteWalletPath;

    @BeforeEach
    public void createEpirusProject()
            throws IOException, InterruptedException, NoSuchAlgorithmException,
                    NoSuchProviderException, InvalidAlgorithmParameterException, CipherException {
        final File testWalletDirectory =
                new File(workingDirectory.getPath() + File.separator + "keystore");
        testWalletDirectory.mkdirs();
        absoluteWalletPath =
                testWalletDirectory
                        + File.separator
                        + WalletUtils.generateNewWalletFile("", testWalletDirectory);
        final String[] args = {
            "--java",
            "-p",
            "org.com",
            "-n",
            "Test",
            "-o" + workingDirectory,
            "-w",
            absoluteWalletPath
        };
        executeClassAsSubProcessAndReturnProcess(
                        ProjectCreator.class, Collections.emptyList(), Arrays.asList(args), true)
                .start()
                .waitFor();
    }

    @Test
    public void testAccountDeployment() throws Exception {
        AccountManager accountManager = mock(AccountManager.class);
        Web3j web3j = mock(Web3j.class);
        when(accountManager.pollForAccountBalance(
                        any(Credentials.class),
                        any(Network.class),
                        any(Web3j.class),
                        any(int.class)))
                .thenReturn(BigInteger.TEN);
        when(accountManager.checkIfAccountIsConfirmed(20)).thenReturn(true);
        DeployRunner deployRunner =
                spy(
                        new DeployRunner(
                                Network.RINKEBY,
                                accountManager,
                                web3j,
                                Paths.get(workingDirectory + File.separator + "Test"),
                                absoluteWalletPath));
        doNothing().when(deployRunner).deploy();
        deployRunner.deploy();
        verify(deployRunner, times(1)).deploy();
    }
}
