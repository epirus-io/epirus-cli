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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

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
public class ImportProjectCommand extends AbstractProjectCommand implements Runnable {

    @Option(
            names = {"-s", "--solidity-path"},
            description = "Path to Solidity file/folder.")
    public String solidityImportPath;

    @Override
    public void run() {
        setupProject();
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

    private void buildInteractively() {
        solidityImportPath = interactiveOptions.getSolidityProjectPath();
    }
}
