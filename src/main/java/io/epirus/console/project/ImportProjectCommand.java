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
import io.epirus.console.project.java.JavaProjectImporterCLIRunner;
import io.epirus.console.project.kotlin.KotlinProjectImporterCLIRunner;
import picocli.CommandLine;

@CommandLine.Command(
        name = "import",
        description = "Import existing solidity contracts into a new Web3j Project",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        subcommands = {JavaProjectImporterCLIRunner.class, KotlinProjectImporterCLIRunner.class},
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class ImportProjectCommand {
    public static final String COMMAND_IMPORT = "import";
}
