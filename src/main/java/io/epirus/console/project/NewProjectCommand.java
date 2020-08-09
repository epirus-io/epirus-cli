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
import java.util.ArrayList;
import java.util.List;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.openapi.OpenApiGeneratorService;
import io.epirus.console.project.java.JavaProjectCreatorRunner;
import io.epirus.console.project.kotlin.KotlinProjectCreatorRunner;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import io.epirus.console.token.erc777.ERC777GeneratorService;
import org.apache.commons.lang.StringUtils;
import picocli.CommandLine;

import static org.web3j.codegen.Console.exitError;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "new",
        description = "Create a new Web3j Project",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class NewProjectCommand implements Runnable {

    @CommandLine.ArgGroup() ProjectType projectType = new ProjectType();

    @CommandLine.Parameters(defaultValue = "NONE") TemplateType templateType = TemplateType.NONE;

    @CommandLine.Option(
            names = {"-n", "--project-name"},
            description = "Project name.")
    public String projectName;

    @CommandLine.Option(
            names = {"-p", "--package"},
            description = "Base package name.")
    public String packageName;

    @CommandLine.Option(
            names = {"-o", "--output-dir"},
            description = "Destination base directory.",
            showDefaultValue = ALWAYS)
    public String outputDir = ".";

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
    public List<File> abis = new ArrayList<>();

    @CommandLine.Option(
            names = {"-b", "--bin"},
            description = {"input BIN files and folders."},
            arity = "1..*")
    public List<File> bins = new ArrayList<>();

    private final InteractiveOptions interactiveOptions;
    private final InputVerifier inputVerifier;

    public NewProjectCommand() {
        this(System.in, System.out);
    }

    public NewProjectCommand(InputStream inputStream, PrintStream outputStream) {
        this.interactiveOptions = new InteractiveOptions(inputStream, outputStream);
        this.inputVerifier = new InputVerifier(outputStream);
    }

    @Override
    public void run() {
        if (projectName == null && packageName == null) {
            buildInteractively();
        }
        if (inputIsValid(projectName, packageName)) {
            if (new File(projectName).exists()) {
                if (interactiveOptions.overrideExistingProject()) {
                    ProjectUtils.deleteFolder(new File(projectName).toPath());
                } else {
                    exitError("Project creation was canceled.");
                }
            }
            final ProjectCreatorConfig projectCreatorConfig =
                    new ProjectCreatorConfig(projectName, packageName, outputDir);

            if (projectType.isOpenApi) {
                switch (templateType) {
                    case NONE:
                    case HELLOWORLD:

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
                                false)
                                .generate();
                        break;
                    case ERC777:
                        new ERC777GeneratorService(projectName, packageName, outputDir).generate();
                        break;
                }
            } else if (projectType.isJava) {
                new JavaProjectCreatorRunner(projectCreatorConfig).run();
            } else {
                new KotlinProjectCreatorRunner(projectCreatorConfig).run();
            }
        }
    }

    private void buildInteractively() {
        projectName = interactiveOptions.getProjectName();
        packageName = interactiveOptions.getPackageName();

        interactiveOptions
                .getProjectDestination(projectName)
                .ifPresent(projectDest -> outputDir = projectDest);
    }

    private boolean inputIsValid(String... requiredArgs) {
        return inputVerifier.requiredArgsAreNotEmpty(requiredArgs)
                && inputVerifier.classNameIsValid(projectName)
                && inputVerifier.packageNameIsValid(packageName);
    }
}
