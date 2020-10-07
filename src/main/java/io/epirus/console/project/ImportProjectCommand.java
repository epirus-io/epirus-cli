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
package io.epirus.console.project;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.project.java.JavaProjectImporterRunner;
import io.epirus.console.project.kotlin.KotlinProjectImporterRunner;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import static org.web3j.codegen.Console.exitError;

@Command(
        name = "import",
        description = "Import existing Solidity contracts into a new Web3j Project",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class ImportProjectCommand implements Runnable {

    @Mixin public ProjectOptions projectOptions = new ProjectOptions();

    @Option(
            names = {"-s", "--solidity-path"},
            description = "Path to Solidity file/folder")
    public String solidityImportPath;

    private final InteractiveOptions interactiveOptions;
    private final InputVerifier inputVerifier;

    public ImportProjectCommand() {
        this(System.in, System.out);
    }

    public ImportProjectCommand(InputStream inputStream, PrintStream outputStream) {
        this(new InteractiveOptions(inputStream, outputStream), new InputVerifier(outputStream));
    }

    public ImportProjectCommand(
            InteractiveOptions interactiveOptions, InputVerifier inputVerifier) {
        this.interactiveOptions = interactiveOptions;
        this.inputVerifier = inputVerifier;
    }

    @Override
    public void run() {
        if (inputIsValid(projectOptions.projectName, projectOptions.packageName)) {
            projectOptions.projectName =
                    projectOptions.projectName.substring(0, 1).toUpperCase()
                            + projectOptions.projectName.substring(1);
            if (new File(projectOptions.projectName).exists()) {
                if (interactiveOptions.overrideExistingProject()) {
                    ProjectUtils.deleteFolder(new File(projectOptions.projectName).toPath());
                } else {
                    exitError("Project creation was canceled.");
                }
            }

            if (solidityImportPath == null) {
                buildInteractively();
            }

            final ProjectImporterConfig projectImporterConfig =
                    new ProjectImporterConfig(
                            projectOptions.projectName,
                            projectOptions.packageName,
                            projectOptions.outputDir,
                            solidityImportPath,
                            projectOptions.generateTests);

            if (projectOptions.isKotlin) {
                new KotlinProjectImporterRunner(projectImporterConfig).run();
            } else {
                new JavaProjectImporterRunner(projectImporterConfig).run();
            }
        }
    }

    private void buildInteractively() {
        solidityImportPath = interactiveOptions.getSolidityProjectPath();
    }

    private boolean inputIsValid(String... requiredArgs) {
        return inputVerifier.requiredArgsAreNotEmpty(requiredArgs)
                && inputVerifier.classNameIsValid(projectOptions.projectName)
                && inputVerifier.packageNameIsValid(projectOptions.packageName);
    }
}
