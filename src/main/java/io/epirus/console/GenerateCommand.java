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
package io.epirus.console;

import io.epirus.console.project.UnitTestCommand;
import io.epirus.console.wrapper.subcommand.SolidityGenerateCommand;
import io.epirus.console.wrapper.subcommand.TruffleGenerateCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "generate",
        subcommands = {
            CommandLine.HelpCommand.class,
            TruffleGenerateCommand.class,
            UnitTestCommand.class,
            SolidityGenerateCommand.class,
        },
        showDefaultValues = true,
        abbreviateSynopsis = true,
        description = "Generate Wrappers or Tests",
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class GenerateCommand extends SubCommand {}
