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
package io.epirus.console.project.kotlin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.epirus.console.project.ProjectCreator;
import io.epirus.console.project.ProjectCreatorCLIRunner;
import picocli.CommandLine;

import static io.epirus.console.project.ProjectCreator.COMMAND_KOTLIN;
import static io.epirus.console.project.ProjectCreator.COMMAND_NEW;

@CommandLine.Command(
        name = COMMAND_KOTLIN,
        mixinStandardHelpOptions = true,
        version = "4.0",
        sortOptions = false)
public class KotlinProjectCreatorCLIRunner extends ProjectCreatorCLIRunner {

    protected void createProject() {
        Map<String, String> walletCredentials = new HashMap<>();
        walletCredentials.put("path", walletPath);
        walletCredentials.put("password", walletPassword);
        new ProjectCreator(outputDir, packageName, projectName)
                .generateKotlin(
                        true,
                        Optional.empty(),
                        Optional.of(walletCredentials),
                        true,
                        true,
                        COMMAND_NEW);
    }
}
