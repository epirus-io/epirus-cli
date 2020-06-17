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
package io.epirus.console.docker;

import java.nio.file.Paths;

import io.epirus.console.ProjectTest;
import io.epirus.console.account.AccountUtils;
import io.epirus.console.config.ConfigManager;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DockerCommandTest extends ProjectTest {

    @BeforeEach
    public void setupWallet() {
        AccountUtils.accountDefaultWalletInit();
        ConfigManager.setDevelopment("", "", "", "<login_token>", true);
    }

    @Disabled("must have a login token & docker installed")
    @Test
    @Order(1)
    public void testDockerBuild() {
        new CommandLine(new DockerCommand())
                .execute(
                        "build",
                        "-w",
                        Paths.get(workingDirectory.getAbsolutePath(), "Test").toString());
    }

    @Disabled("must have a login token & docker installed")
    @Test
    @Order(2)
    public void testDockerRun() {
        new CommandLine(new DockerCommand()).execute("run", "-w", System.getProperty("user.home"));
    }
}
