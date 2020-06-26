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
import io.epirus.console.project.java.JavaProjectCreatorRunner;
import io.epirus.console.project.kotlin.KotlinProjectCreatorRunner;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import picocli.CommandLine;

import org.web3j.codegen.Console;

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

    @CommandLine.Option(
            names = {"--java"},
            description = "Whether java code should be generated.")
    public boolean isJava;

    @CommandLine.Option(
            names = {"--kotlin"},
            description = "Whether kotlin code should be generated.")
    public boolean isKotlin;

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
        if (isJava && isKotlin) {
            Console.exitError("Must only use one of -java or -kotlin");
        }
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

            if (!isJava) {
                new KotlinProjectCreatorRunner(projectCreatorConfig).run();
            } else {
                new JavaProjectCreatorRunner(projectCreatorConfig).run();
            }
        }
    }

    private void buildInteractively() {
        projectName = interactiveOptions.getProjectName();
        packageName = interactiveOptions.getPackageName();

        interactiveOptions
                .getProjectDestination(projectName)
                .ifPresent(projectDest -> outputDir = projectDest);

        // FIXME - not sure what this is for
        //        AccountUtils.accountInit(new AccountService());
    }

    private boolean inputIsValid(String... requiredArgs) {
        return inputVerifier.requiredArgsAreNotEmpty(requiredArgs)
                && inputVerifier.classNameIsValid(projectName)
                && inputVerifier.packageNameIsValid(packageName);
    }
}
