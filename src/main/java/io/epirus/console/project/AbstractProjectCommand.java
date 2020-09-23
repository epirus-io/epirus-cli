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
package io.epirus.console.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.epirus.console.project.templates.TemplateReader;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import static org.web3j.codegen.Console.exitError;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

public abstract class AbstractProjectCommand {

    @CommandLine.Parameters(defaultValue = "NONE")
    protected TemplateType templateType = TemplateType.NONE;

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
            names = {"--address-length"},
            description = {"specify the address length."},
            defaultValue = "20")
    public int addressLength = 20;

    @CommandLine.Option(
            names = {"--jar"},
            description = {"generate the JAR (default: false)"},
            defaultValue = "false")
    public Boolean generateJar = false;

    protected final InteractiveOptions interactiveOptions;
    protected final InputVerifier inputVerifier;
    protected ProjectCreatorConfig projectCreatorConfig;

    public AbstractProjectCommand() {
        this(System.in, System.out);
    }

    public AbstractProjectCommand(InputStream inputStream, PrintStream outputStream) {
        this.interactiveOptions = new InteractiveOptions(inputStream, outputStream);
        this.inputVerifier = new InputVerifier(outputStream);
    }

    public void setup() {
        verifyInput();
        projectCreatorConfig =
                new ProjectCreatorConfig(projectName, packageName, outputDir, generateJar);
    }

    public void verifyInput() {
        if (inputIsValid(projectName, packageName)) {
            projectName = projectName.substring(0, 1).toUpperCase() + projectName.substring(1);
            if (new File(projectName).exists()) {
                if (interactiveOptions.overrideExistingProject()) {
                    ProjectUtils.deleteFolder(new File(projectName).toPath());
                } else {
                    exitError("Project creation was canceled.");
                }
            }
        }
    }

    private boolean inputIsValid(String... requiredArgs) {
        return inputVerifier.requiredArgsAreNotEmpty(requiredArgs)
                && inputVerifier.classNameIsValid(projectName)
                && inputVerifier.packageNameIsValid(packageName);
    }

    @NotNull
    private String prepareERC777Template() {
        final String buildPath = outputDir + File.separator + "build";
        final File buildDir = new File(buildPath);
        buildDir.mkdirs();

        final String contractPath = (buildPath + File.separator + projectName + ".sol");
        copyDependency("tokens/ERC777.template", contractPath);

        final String dependencyFilesPath = buildPath + File.separator + "erc777";
        final File dependencyDir = new File(dependencyFilesPath);
        dependencyDir.mkdirs();

        final String erc777ResourcePath = "tokens" + File.separator + "erc777" + File.separator;
        final String erc777OutputPath = buildPath + File.separator + "erc777" + File.separator;
        copyDependency(erc777ResourcePath + "Address.sol", erc777OutputPath + "Address.sol");
        copyDependency(erc777ResourcePath + "Context.sol", erc777OutputPath + "Context.sol");
        copyDependency(erc777ResourcePath + "ERC777.sol", erc777OutputPath + "ERC777.sol");
        copyDependency(erc777ResourcePath + "IERC20.sol", erc777OutputPath + "IERC20.sol");
        copyDependency(erc777ResourcePath + "IERC777.sol", erc777OutputPath + "IERC777.sol");
        copyDependency(
                erc777ResourcePath + "IERC777Recipient.sol",
                erc777OutputPath + "IERC777Recipient.sol");
        copyDependency(
                erc777ResourcePath + "IERC777Sender.sol", erc777OutputPath + "IERC777Sender.sol");
        copyDependency(
                erc777ResourcePath + "IERC1820Registry.sol",
                erc777OutputPath + "IERC1820Registry.sol");
        copyDependency(erc777ResourcePath + "SafeMath.sol", erc777OutputPath + "SafeMath.sol");
        return buildPath;
    }

    private void copyDependency(final String inputFolder, final String outputFolder) {
        try {
            final String erc777Dependencies = TemplateReader.readFile(inputFolder);
            Files.write(
                    Paths.get(outputDir + File.separator + outputFolder),
                    erc777Dependencies.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(final String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
