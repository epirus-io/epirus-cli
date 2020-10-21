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
package io.epirus.console.openapi

import io.epirus.console.openapi.subcommands.GenerateOpenApiCommand
import io.epirus.console.openapi.subcommands.ImportOpenApiCommand
import io.epirus.console.openapi.subcommands.JarOpenApiCommand
import io.epirus.console.openapi.subcommands.NewOpenApiCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Paths

class OpenApiProjectTest {
    //    private val tempDirPath = Folders.tempBuildFolder().absolutePath
    private val tempDirPath = Files.createTempDirectory("openapi").toFile().absolutePath

    @Test
    fun testCorrectArgsOpenApiEndpointsGeneration() {
        val args = arrayOf("-o", tempDirPath, "-n", "generationTest")
        val exitCode = CommandLine(GenerateOpenApiCommand::class.java).execute(*args)
        assertEquals(0, exitCode)
    }

    @Test
    fun testCorrectArgsOpenApiJarGeneration() {
        val args = arrayOf("-p", "org.com", "-n", "Test", "-o", tempDirPath)
        val exitCode = CommandLine(JarOpenApiCommand::class.java).execute(*args)
        assertEquals(0, exitCode)
        val jarFile = Paths.get(tempDirPath, "Test-server-all.jar").toFile()
        assertTrue(jarFile.exists())
    }

    @Test
    fun testCorrectArgsOpenApiNew() {
        val args = arrayOf("-p", "org.com", "-n", "Test", "-o", tempDirPath)
        val exitCode = CommandLine(NewOpenApiCommand::class.java).execute(*args)
        assertEquals(0, exitCode)
    }

    @Test
    fun testCorrectArgsOpenApiImport() {
        val soliditySource = Paths.get("src", "test", "resources", "Solidity", "TestContract.sol").toFile()
        val userInput = ByteArrayInputStream(soliditySource.absolutePath.toByteArray())
        val args = arrayOf("-p", "org.com", "-n", "Test", "-o", tempDirPath)
        val exitCode = CommandLine(ImportOpenApiCommand(userInput, System.out)).execute(*args)
        assertEquals(0, exitCode)
    }
}
