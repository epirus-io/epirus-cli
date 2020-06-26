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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import io.epirus.console.config.ConfigManager;
import io.epirus.console.project.utils.ClassExecutor;
import io.epirus.console.project.utils.Folders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportProjectCommandTest extends ClassExecutor {
    static String tempDirPath;
    private String formattedPath =
            new File(String.join(separator, "src", "test", "resources", "Solidity"))
                    .getAbsolutePath();

    @BeforeAll
    public static void setUpStreams() {
        tempDirPath = Folders.tempBuildFolder().getAbsolutePath();
    }

    @Test
    public void testWhenCorrectArgsArePassedProjectStructureCreated() {
        final String[] args = {"-p=org.com", "-n=Test", "-o=" + tempDirPath, "-s=" + tempDirPath};
        final ImportProjectCommand importProjectCommand = new ImportProjectCommand();
        new CommandLine(importProjectCommand).parseArgs(args);
        assertEquals("Test", importProjectCommand.projectName);
        assertEquals("org.com", importProjectCommand.packageName);
        assertEquals(tempDirPath, importProjectCommand.solidityImportPath);
    }

    @Test
    public void testWhenNonDefinedArgsArePassed() {
        final ImportProjectCommand importProjectCommand = new ImportProjectCommand();
        final String[] args = {"-t=org.org", "-b=test", "-z=" + tempDirPath};
        final CommandLine commandLine = new CommandLine(importProjectCommand);
        Assertions.assertThrows(
                CommandLine.ParameterException.class, () -> commandLine.parseArgs(args));
    }

    @Test
    public void testWhenDuplicateArgsArePassed() {
        final ImportProjectCommand importProjectCommand = new ImportProjectCommand();
        final String[] args = {
            "-p=org.org", "-n=test", "-n=OverrideTest", "-o=" + tempDirPath, "-s=test"
        };
        final CommandLine commandLine = new CommandLine(importProjectCommand);
        Assertions.assertThrows(
                CommandLine.OverwrittenOptionException.class, () -> commandLine.parseArgs(args));
    }

    @Test
    public void testWithPicoCliWhenArgumentsAreCorrect() {
        final String[] args = {
            "--java", "-p=org.com", "-n=Test5", "-o=" + tempDirPath, "-s=" + formattedPath, "-t"
        };
        int exitCode = new CommandLine(ImportProjectCommand.class).execute(args);
        assertEquals(0, exitCode);

        String pathToTests =
                String.join(
                        separator,
                        tempDirPath,
                        "Test5",
                        "src",
                        "test",
                        "java",
                        "org",
                        "com",
                        "generated",
                        "contracts",
                        "Test2Test.java");
        assertTrue(new File(pathToTests).exists());
    }

    @Disabled
    @Test
    public void testWithPicoCliWhenArgumentsAreEmpty() throws IOException {
        ConfigManager.setDevelopment();
        final String[] args = {"--java", "-n=", "-p=", "-s="};
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(outputStream);

        new CommandLine(
                        ImportProjectCommand.class,
                        FactoryHarness.getFactory(
                                new ByteArrayInputStream("".getBytes()), printStream))
                .execute(args);

        assertTrue(
                outputStream
                        .toString()
                        .contains("Please make sure the required parameters are not empty."));
    }

    @Test
    public void testWhenInteractiveAndArgumentsAreCorrect() throws IOException {
        final String[] args = {"--java"};
        final String input =
                "Test1" + "\n" + "org.com" + "\n" + tempDirPath + "\n" + tempDirPath + "\n" + "n";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        final PrintStream printStream = new PrintStream(new ByteArrayOutputStream());
        int exitCode =
                new CommandLine(
                                ImportProjectCommand.class,
                                FactoryHarness.getFactory(inputStream, printStream))
                        .execute(args);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> stringList = reader.lines().collect(Collectors.toList());
            stringList.forEach(string -> System.out.println(string + "\n"));
        }

        assertEquals(0, exitCode);
    }
}
