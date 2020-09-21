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
import java.util.List;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.openapi.OpenApiGeneratorService;
import io.epirus.console.project.java.JavaProjectImporterRunner;
import io.epirus.console.project.kotlin.KotlinProjectImporterRunner;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import org.apache.commons.lang.StringUtils;
import picocli.CommandLine;

import static org.web3j.codegen.Console.exitError;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "import",
        description = "Import existing solidity contracts into a new Web3j Project",
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

    @CommandLine.ArgGroup() ProjectType projectType = new ProjectType();

    @CommandLine.Option(
            names = {"-n", "--project-name"},
            description = "Project name.",
            showDefaultValue = ALWAYS)
    public String projectName = "Web3App";

    @CommandLine.Option(
            names = {"-p", "--package"},
            description = "Base package name.",
            showDefaultValue = ALWAYS)
    public String packageName = "io.epirus";

    @CommandLine.Option(
            names = {"-o", "--output-dir"},
            description = "Destination base directory.",
            showDefaultValue = ALWAYS)
    public String outputDir = ".";

    @CommandLine.Option(
            names = {"-s", "--solidity-path"},
            description = "Path to solidity file/folder")
    public String solidityImportPath;

    @CommandLine.Option(
            names = {"-t", "--generate-tests"},
            description = "Generate unit tests for the contract wrappers",
            showDefaultValue = ALWAYS)
    boolean generateTests = true;

    @CommandLine.Option(
            names = {"--address-length"},
            description = {"specify the address length."},
            defaultValue = "20")
    public int addressLength = 20;

    @CommandLine.Option(
            names = {"--context-path"},
            description = {"set the API context path (default: the project name)"})
    public String contextPath;

    @CommandLine.Option(
            names = {"-a", "--abi"},
            description = {"input ABI files and folders."},
            arity = "1..*")
    public List<File> abis;

    @CommandLine.Option(
            names = {"-b", "--bin"},
            description = {"input BIN files and folders."},
            arity = "1..*")
    public List<File> bins;

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
        if (solidityImportPath == null) {
            buildInteractively();
        }
        if (inputIsValid(projectName, packageName)) {
            projectName = projectName.substring(0, 1).toUpperCase() + projectName.substring(1);
            if (new File(projectName).exists()) {
                if (interactiveOptions.overrideExistingProject()) {
                    ProjectUtils.deleteFolder(new File(projectName).toPath());
                } else {
                    exitError("Project creation was canceled.");
                }
            }
            final ProjectImporterConfig projectImporterConfig =
                    new ProjectImporterConfig(
                            projectName, packageName, outputDir, solidityImportPath, generateTests);

            if (projectType.isOpenApi) {
                new OpenApiGeneratorService(
                                projectName,
                                packageName,
                                outputDir,
                                abis,
                                bins,
                                addressLength,
                                contextPath != null
                                        ? StringUtils.removeEnd(contextPath, "/")
                                        : projectName,
                                true)
                        .generate();
            } else if (projectType.isKotlin) {
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
                && inputVerifier.classNameIsValid(projectName)
                && inputVerifier.packageNameIsValid(packageName);
    }
}
