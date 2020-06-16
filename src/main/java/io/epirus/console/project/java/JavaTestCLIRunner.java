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
package io.epirus.console.project.java;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.project.InteractiveOptions;
import picocli.CommandLine;

import org.web3j.codegen.Console;
import org.web3j.codegen.unit.gen.ClassProvider;
import org.web3j.codegen.unit.gen.java.JavaClassGenerator;

@CommandLine.Command(
        name = "java",
        description = "Generate Java tests for a Web3j Java smart contract wrapper",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class JavaTestCLIRunner implements Runnable {
    @CommandLine.Option(
            names = {"-i", "--java-wrapper-directory"},
            description = "The class path of your generated wrapper.",
            required = false)
    public String javaWrapperDir;

    @CommandLine.Option(
            names = {"-o", "--output-directory"},
            description = "The path where the unit tests will be generated.",
            required = false)
    public String unitTestOutputDir;

    @VisibleForTesting
    public JavaTestCLIRunner(final String javaWrapperDir, final String unitTestOutputDir) {

        this.javaWrapperDir = javaWrapperDir;
        this.unitTestOutputDir = unitTestOutputDir;
    }

    @VisibleForTesting
    public JavaTestCLIRunner() {}

    @Override
    public void run() {
        if (javaWrapperDir == null && unitTestOutputDir == null) {
            buildInteractively();
        }
        try {
            generateJava();
            System.out.println(
                    "Unit tests were generated successfully at location: " + unitTestOutputDir);
        } catch (IOException e) {
            Console.exitError(e);
        }
    }

    private void buildInteractively() {
        InteractiveOptions interactiveOptions = new InteractiveOptions();
        interactiveOptions
                .getGeneratedWrapperLocation()
                .ifPresent(wrappersPath -> javaWrapperDir = wrappersPath);
        interactiveOptions
                .setGeneratedTestLocationJava()
                .ifPresent(outputPath -> unitTestOutputDir = outputPath);
    }

    @VisibleForTesting
    public void generateJava() throws IOException {
        List<Class> compiledClasses = new ClassProvider(new File(javaWrapperDir)).getClasses();
        compiledClasses.forEach(
                compiledClass -> {
                    try {
                        new JavaClassGenerator(
                                        compiledClass,
                                        compiledClass
                                                .getCanonicalName()
                                                .substring(
                                                        0,
                                                        compiledClass
                                                                .getCanonicalName()
                                                                .lastIndexOf(".")),
                                        unitTestOutputDir)
                                .writeClass();
                    } catch (IOException e) {
                        Console.exitError(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
