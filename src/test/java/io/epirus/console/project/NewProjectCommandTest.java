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

import io.epirus.console.project.utils.ClassExecutor;
import io.epirus.console.project.utils.Folders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewProjectCommandTest extends ClassExecutor {
    private static String tempDirPath;

    @BeforeAll
    static void setUpStreams() {
        tempDirPath = Folders.tempBuildFolder().getAbsolutePath();
    }

    @Test
    public void testCorrectArgs() {
        final String[] args = {"-p=org.com", "-n=Test", "-o=" + tempDirPath};
        final NewProjectCommand newProjectCommand = new NewProjectCommand();
        new CommandLine(newProjectCommand).parseArgs(args);
        assertEquals("org.com", newProjectCommand.projectOptions.packageName);
        assertEquals("Test", newProjectCommand.projectOptions.projectName);
        assertEquals(tempDirPath, newProjectCommand.projectOptions.outputDir);
    }

    @Test
    public void testWhenNonDefinedArgsArePassed() {
        final String[] args = {"-u=org.org", "-b=test", "-z=" + tempDirPath};
        final CommandLine commandLine = new CommandLine(NewProjectCommand.class);
        Assertions.assertThrows(
                CommandLine.UnmatchedArgumentException.class, () -> commandLine.parseArgs(args));
    }

    @Test
    public void testWhenDuplicateArgsArePassed() {
        final String[] args = {"-p=org.org", "-n=test", "-n=OverrideTest", "-o=" + tempDirPath};
        final CommandLine commandLine = new CommandLine(NewProjectCommand.class);
        Assertions.assertThrows(
                CommandLine.OverwrittenOptionException.class, () -> commandLine.parseArgs(args));
    }

    @Test
    public void testCorrectArgsJavaProjectGeneration() {
        final String[] args = {"-p", "org.com", "-n", "Test", "-o", tempDirPath};
        int exitCode = new CommandLine(NewProjectCommand.class).execute(args);
        assertEquals(0, exitCode);
        final File pathToTests =
                new File(
                        String.join(
                                separator,
                                tempDirPath,
                                "Test",
                                "src",
                                "test",
                                "java",
                                "org",
                                "com",
                                "generated",
                                "contracts",
                                "HelloWorldTest.java"));
        assertTrue(pathToTests.exists());
    }

    @Test
    public void testCorrectArgsJavaErc777ProjectGeneration() throws IOException {
        final String[] args = {"-p", "org.com", "-n", "TestErc777", "-o", tempDirPath, "ERC777"};

        final String input = "ERC777Test" + "\n" + "erc777" + "\n" + "1000" + "\n" + "";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        int exitCode =
                new CommandLine(
                                NewProjectCommand.class,
                                FactoryHarness.getFactory(
                                        inputStream, new PrintStream(new ByteArrayOutputStream())))
                        .execute(args);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> stringList = reader.lines().collect(Collectors.toList());
            stringList.forEach(string -> System.out.println(string + "\n"));
        }

        assertEquals(0, exitCode);
    }
}
