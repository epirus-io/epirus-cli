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
package io.epirus.console.token.subcommand.erc777

import io.epirus.console.openapi.OpenApiCommand
import io.epirus.console.project.templates.TemplateReader
import org.web3j.openapi.codegen.GenerateOpenApi
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.console.utils.GradleUtils.runGradleTask
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class ERC777GeneratorService(private val erc777Config: ERC777Config) {
    fun generate() {
        try {
            val erc777Template = TemplateReader.readFile("tokens/ERC777.template")
                    .replace("<TOKEN_NAME>".toRegex(), erc777Config.projectName)
                    .replace("<TOKEN_SYMBOL>".toRegex(), erc777Config.symbol)
            Files.write(
                    Paths.get(
                            erc777Config.outputDir
                                    + File.separator
                                    + erc777Config.projectName
                                    + ".sol"),
                    erc777Template.toByteArray())
            File(erc777Config.outputDir
                    + File.separator
                    + "erc777").mkdirs()
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "Address.sol", "erc777" + File.separator + "Address.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "Context.sol", "erc777" + File.separator + "Context.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "ERC777.sol", "erc777" + File.separator + "ERC777.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "IERC20.sol", "erc777" + File.separator + "IERC20.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "ERC777.sol", "erc777" + File.separator + "ERC777.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "IERC777Recipient.sol", "erc777" + File.separator + "IERC777Recipient.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "IERC777Sender.sol", "erc777" + File.separator + "IERC777Sender.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "IERC1820Registry.sol", "erc777" + File.separator + "IERC1820Registry.sol")
            copyDependency("tokens" + File.separator + "erc777" + File.separator + "SafeMath.sol", "erc777" + File.separator + "SafeMath.sol")

            val generatorConfiguration = GeneratorConfiguration(
                    projectName = erc777Config.projectName,
                    packageName = "io.epirus",
                    outputDir = erc777Config.outputDir + File.separator + erc777Config.projectName,
                    contracts = emptyList(),
                    addressLength = 20,
                    contextPath = erc777Config.projectName,
                    version = OpenApiCommand.VersionProvider.versionName
        )

            GenerateOpenApi(generatorConfiguration).generateAll()
            runGradleTask(File(erc777Config.outputDir + File.separator + erc777Config.projectName), "completeSwaggerUiGeneration", "Generating SwaggerUI...")

            println("Done.");
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyDependency(inputFolder: String, outputFolder: String) {
        val erc777Dependencies = TemplateReader.readFile(inputFolder)
        Files.write(
                Paths.get(
                        erc777Config.outputDir
                                + File.separator
                                + outputFolder),
                erc777Dependencies.toByteArray())
    }

}