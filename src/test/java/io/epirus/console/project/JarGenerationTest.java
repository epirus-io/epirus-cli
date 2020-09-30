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
package io.epirus.console.project;

import java.io.File;

import io.epirus.console.openapi.subcommands.JarOpenApiCommand;
import io.epirus.console.project.utils.Folders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JarGenerationTest {
    private static String tempDirPath;

    @BeforeAll
    static void setUpStreams() {
        tempDirPath = Folders.tempBuildFolder().getAbsolutePath();
    }

    @Test
    public void testCorrectArgsOpenApiJarGeneration() {
        final String[] args = {"-p", "org.com", "-n", "Test", "-o" + tempDirPath};
        int exitCode = new CommandLine(JarOpenApiCommand.class).execute(args);
        assertEquals(0, exitCode);

        final File jarFile = new File(String.join(separator, tempDirPath, "Test-server-all.jar"));
        assertTrue(jarFile.exists());
    }
}
