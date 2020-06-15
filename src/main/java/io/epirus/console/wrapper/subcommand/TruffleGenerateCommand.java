package io.epirus.console.wrapper.subcommand;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.wrapper.TruffleFunctionWrapperGeneratorCommand;
import org.web3j.abi.datatypes.Address;
import org.web3j.codegen.Console;
import org.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.web3j.codegen.TruffleJsonFunctionWrapperGenerator;
import picocli.CommandLine;

import java.io.File;

import static org.web3j.codegen.Console.exitError;
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