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
package io.epirus.console.project.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.project.InteractiveOptions;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import static io.epirus.console.project.ImportProjectCommand.COMMAND_IMPORT;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "java",
        description = "Import existing solidity contracts into a new Java Web3j Project",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class JavaProjectImporterCLIRunner extends JavaProjectCLIRunner {
    @Option(
            names = {"-s", "--solidity-path"},
            description = "Path to solidity file/folder",
            required = true)
    public String solidityImportPath;

    @Option(
            names = {"-t", "--generate-tests"},
            description = "Generate unit tests for the contract wrappers",
            required = false,
            showDefaultValue = ALWAYS)
    boolean generateTests = false;

    protected void createProject() {
        Map<String, String> walletCredentials = new HashMap<>();
        walletCredentials.put("path", walletPath);
        walletCredentials.put("password", walletPassword);
        generateJava(
                generateTests,
                Optional.of(new File(solidityImportPath)),
                Optional.of(walletCredentials),
                false,
                false,
                COMMAND_IMPORT);
    }

    @NotNull
    protected void buildInteractively() {
        InteractiveOptions interactiveOptions = new InteractiveOptions();
        projectName = interactiveOptions.getProjectName();
        packageName = interactiveOptions.getPackageName();
        solidityImportPath = interactiveOptions.getSolidityProjectPath();

        final Map<String, String> walletCredentials = interactiveOptions.getWalletLocation();
        walletPath = walletCredentials.get("path");
        walletPassword = walletCredentials.get("password");

        interactiveOptions
                .getProjectDestination(projectName)
                .ifPresent(projectDest -> outputDir = projectDest);

        generateTests = interactiveOptions.userWantsTests();
    }
}
