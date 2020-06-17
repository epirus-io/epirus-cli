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
package io.epirus.console;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collections;

import io.epirus.console.project.utils.ClassExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

public class ProjectTest extends ClassExecutor {
    @TempDir protected static File workingDirectory;
    protected String absoluteWalletPath;

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
            "new",
            "java",
            "-p",
            "org.com",
            "-n",
            "Test",
            "-o" + workingDirectory,
            "-w",
            absoluteWalletPath
        };
        int result =
                executeClassAsSubProcessAndReturnProcess(
                                Epirus.class, Collections.emptyList(), Arrays.asList(args), true)
                        .start()
                        .waitFor();
        if (result != 0) {
            throw new RuntimeException("Failed to generate test project");
        }
    }
}
