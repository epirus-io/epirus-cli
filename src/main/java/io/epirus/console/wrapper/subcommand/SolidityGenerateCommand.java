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

import org.web3j.abi.datatypes.Address;
import org.web3j.codegen.Console;
import org.web3j.codegen.SolidityFunctionWrapperGenerator;

import static org.web3j.codegen.Console.exitError;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(
        name = "generate",
        description = "Generate Java smart contract wrappers from solidity",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class SolidityGenerateCommand implements Runnable {

    static final String JAVA_TYPES_ARG = "--javaTypes";
    static final String SOLIDITY_TYPES_ARG = "--solidityTypes";
    static final String PRIMITIVE_TYPES_ARG = "--primitiveTypes";

    @CommandLine.Option(
            names = {"-a", "--abiFile"},
            description = "abi file with contract definition.",
            required = true)
    private File abiFile;

    @CommandLine.Option(
            names = {"-b", "--binFile"},
            description =
                    "bin file with contract compiled code "
                            + "in order to generate deploy methods.")
    private File binFile;

    @CommandLine.Option(
            names = {"-c", "--contractName"},
            description = "contract name (defaults to ABI file name).")
    private String contractName;

    @CommandLine.Option(
            names = {"-o", "--outputDir"},
            description = "destination base directory.",
            required = true)
    private File destinationFileDir;

    @CommandLine.Option(
            names = {"-p", "--package"},
            description = "base package name.",
            required = true)
    private String packageName;

    @CommandLine.Option(
            names = {"-al", "--addressLength"},
            description = "address length in bytes (defaults to 20).")
    private int addressLength = Address.DEFAULT_LENGTH / Byte.SIZE;

    @CommandLine.Option(
            names = {"-jt", JAVA_TYPES_ARG},
            description = "use native Java types.",
            showDefaultValue = ALWAYS)
    private boolean javaTypes = true;

    @CommandLine.Option(
            names = {"-st", SOLIDITY_TYPES_ARG},
            description = "use solidity types.")
    private boolean solidityTypes;

    @CommandLine.Option(
            names = {"-pt", PRIMITIVE_TYPES_ARG},
            description = "use Java primitive types.")
    private boolean primitiveTypes = false;

    @Override
    public void run() {
        try {
            boolean useJavaTypes = useJavaNativeTypes();

            if (contractName == null || contractName.isEmpty()) {
                contractName = getFileNameNoExtension(abiFile.getName());
            }

            new SolidityFunctionWrapperGenerator(
                            binFile,
                            abiFile,
                            destinationFileDir,
                            contractName,
                            packageName,
                            useJavaTypes,
                            primitiveTypes,
                            addressLength)
                    .generate();
        } catch (Exception e) {
            exitError(e);
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

    private String getFileNameNoExtension(String fileName) {
        String[] splitName = fileName.split("\\.(?=[^.]*$)");
        return splitName[0];
    }
}
