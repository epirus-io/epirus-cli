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
package io.epirus.console.wrapper.subcommand;

import java.io.File;

import io.epirus.console.EpirusVersionProvider;
import picocli.CommandLine;

import org.web3j.codegen.Console;
import org.web3j.codegen.TruffleJsonFunctionWrapperGenerator;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "generate",
        description = "Generate Java smart contract wrappers from truffle json",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class TruffleGenerateCommand implements Runnable {

    static final String JAVA_TYPES_ARG = "--javaTypes";
    static final String SOLIDITY_TYPES_ARG = "--solidityTypes";

    @CommandLine.Option(
            names = {"-t", "--truffle-json"},
            description = "abi file with contract definition.",
            required = true)
    private File jsonFileLocation;

    @CommandLine.Option(
            names = {"-o", "--outputDir"},
            description = "destination base directory.",
            required = true)
    private File destinationDirLocation;

    @CommandLine.Option(
            names = {"-p", "--package"},
            description = "base package name.",
            required = true)
    private String basePackageName;

    @CommandLine.Option(
            names = {"-jt", JAVA_TYPES_ARG},
            description = "use native Java types.",
            required = false,
            showDefaultValue = ALWAYS)
    private boolean javaTypes = true;

    @CommandLine.Option(
            names = {"-st", SOLIDITY_TYPES_ARG},
            description = "use solidity types.",
            required = false)
    private boolean solidityTypes;

    @Override
    public void run() {

        boolean useJavaNativeTypes = useJavaNativeTypes();

        try {
            new TruffleJsonFunctionWrapperGenerator(
                            jsonFileLocation.getAbsolutePath(),
                            destinationDirLocation.getAbsolutePath(),
                            basePackageName,
                            useJavaNativeTypes)
                    .generate();
        } catch (Exception e) {
            Console.exitError(e);
        }
    }

    private boolean useJavaNativeTypes() {
        boolean useJavaNativeTypes = true;
        if ((solidityTypes == false && javaTypes == false)
                || (solidityTypes == true && javaTypes == true)) {
            Console.exitError(
                    "Invalid project type. Expecting one of "
                            + SOLIDITY_TYPES_ARG
                            + " or "
                            + JAVA_TYPES_ARG);
        }
        if (solidityTypes) {
            useJavaNativeTypes = false;
        }
        return useJavaNativeTypes;
    }
}
