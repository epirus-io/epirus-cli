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

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.project.java.JavaProjectImporterRunner;
import io.epirus.console.project.kotlin.KotlinProjectImporterRunner;
import picocli.CommandLine;

import org.web3j.codegen.Console;

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

    @CommandLine.Option(
            names = {"-s", "--solidity-path"},
            description = "Path to solidity file/folder",
            required = true)
    public String solidityImportPath;

    @CommandLine.Option(
            names = {"-t", "--generate-tests"},
            description = "Generate unit tests for the contract wrappers",
            showDefaultValue = ALWAYS)
    boolean generateTests = false;

    @Override
    public void run() {
        if (isJava && isKotlin) {
            Console.exitError("Must only use one of -java or -kotlin");
        }
        final ProjectImporterConfig projectImporterConfig =
                new ProjectImporterConfig(
                        projectName, packageName, outputDir, solidityImportPath, generateTests);

        if (!isJava) {
            new KotlinProjectImporterRunner(projectImporterConfig).run();
        } else {
            new JavaProjectImporterRunner(projectImporterConfig).run();
        }
    }
}
