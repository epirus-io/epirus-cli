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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import com.google.gson.Gson;
import io.epirus.console.account.AccountManager;
import io.epirus.console.config.CliConfig;
import io.epirus.console.project.ProjectCreator;
import io.epirus.console.project.utils.ClassExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.web3j.crypto.Credentials;
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
    static CliConfig cliConfig;

    @BeforeEach
    public void createEpirusProject() throws IOException, InterruptedException {
        final String[] args = {"--java", "-p", "org.com", "-n", "Test", "-o" + workingDirectory};
        int exitCode =
                executeClassAsSubProcessAndReturnProcess(
                                ProjectCreator.class,
                                Collections.emptyList(),
                                Arrays.asList(args),
                                true)
                        .start()
                        .waitFor();
        cliConfig =
                new CliConfig(
                        "4.6.0-SNAPSHOT",
                        "https://auth.epirus.io",
                        "test-token",
                        "4.6.0-SNAPSHOT",
                        null,
                        null);
        String jsonToWrite = new Gson().toJson(cliConfig);
        new File(workingDirectory + File.separator + ".epirus").mkdirs();
        Path path = Paths.get(workingDirectory.getPath(), ".epirus", ".config");
        Files.write(path, jsonToWrite.getBytes(Charset.defaultCharset()));
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
        doNothing().when(accountManager).checkIfAccountIsConfirmed();
        DeployRunner deployRunner =
                spy(
                        new DeployRunner(
                                Network.RINKEBY,
                                accountManager,
                                web3j,
                                Paths.get(workingDirectory + File.separator + "Test")));
        doNothing().when(deployRunner).deploy();
        deployRunner.deploy();
        verify(deployRunner, times(1)).deploy();
    }
}
