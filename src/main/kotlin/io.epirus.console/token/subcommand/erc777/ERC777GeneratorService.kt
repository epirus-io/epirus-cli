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
import org.web3j.openapi.codegen.utils.GeneratorUtils
import org.web3j.openapi.console.utils.GradleUtils.runGradleTask
import org.web3j.sokt.SolcArguments
import org.web3j.sokt.SolidityFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class ERC777GeneratorService(private val erc777Config: ERC777Config) {
    fun generate() {
        try {
            val erc777Template = TemplateReader.readFile("tokens/ERC777.template")
                    .replace("<TOKEN_NAME>".toRegex(), erc777Config.tokenName)
                    .replace("<TOKEN_SYMBOL>".toRegex(), erc777Config.symbol)
                    .replace("<INITIAL_SUPPLY>".toRegex(), erc777Config.initialSupply)
            val contractPath = (erc777Config.outputDir +
                    File.separator +
                    erc777Config.tokenName +
                    ".sol")
            Files.write(
                    Paths.get(
                            contractPath),
                    erc777Template.toByteArray())
            val dependencyDir = File(erc777Config.outputDir +
                    File.separator +
                    "erc777")
            dependencyDir.mkdirs()
            val erc777ResourcePath = "tokens" + File.separator + "erc777" + File.separator
            val erc777OutputPath = "erc777" + File.separator
            copyDependency(erc777ResourcePath + "Address.sol", erc777OutputPath + "Address.sol")
            copyDependency(erc777ResourcePath + "Context.sol", erc777OutputPath + "Context.sol")
            copyDependency(erc777ResourcePath + "ERC777.sol", erc777OutputPath + "ERC777.sol")
            copyDependency(erc777ResourcePath + "IERC20.sol", erc777OutputPath + "IERC20.sol")
            copyDependency(erc777ResourcePath + "IERC777.sol", erc777OutputPath + "IERC777.sol")
            copyDependency(erc777ResourcePath + "IERC777Recipient.sol", erc777OutputPath + "IERC777Recipient.sol")
            copyDependency(erc777ResourcePath + "IERC777Sender.sol", erc777OutputPath + "IERC777Sender.sol")
            copyDependency(erc777ResourcePath + "IERC1820Registry.sol", erc777OutputPath + "IERC1820Registry.sol")
            copyDependency(erc777ResourcePath + "SafeMath.sol", erc777OutputPath + "SafeMath.sol")

            val fileName = contractPath.substringAfterLast("/")
            val solidityFile = SolidityFile(contractPath)
            val compilerInstance = solidityFile.getCompilerInstance()

            println("Using solidity compiler ${compilerInstance.solcRelease.version} for $fileName")

            File(erc777Config.outputDir + File.separator + "build").mkdirs()

            val result = compilerInstance.execute(
                    SolcArguments.OUTPUT_DIR.param { erc777Config.outputDir + File.separator + "build" },
                    SolcArguments.ABI,
                    SolcArguments.BIN,
                    SolcArguments.OVERWRITE
            )

            val generatorConfiguration = GeneratorConfiguration(
                    projectName = erc777Config.tokenName,
                    packageName = "io.epirus",
                    outputDir = erc777Config.outputDir + File.separator + erc777Config.tokenName,
                    contracts = GeneratorUtils.loadContractConfigurations(
                            listOf(
                                    File(erc777Config.outputDir + File.separator + "build" + File.separator + erc777Config.tokenName + ".abi")),
                            listOf(
                                    File(erc777Config.outputDir + File.separator + "build" + File.separator + erc777Config.tokenName + ".bin"))),
                    addressLength = 20,
                    contextPath = erc777Config.tokenName
            )

            File(erc777Config.outputDir +
                    File.separator +
                    erc777Config.tokenName).mkdirs()

            GenerateOpenApi(generatorConfiguration).generateAll()
            runGradleTask(File(erc777Config.outputDir + File.separator + erc777Config.tokenName), "completeSwaggerUiGeneration", "Generating SwaggerUI...")

            println("Done.")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyDependency(inputFolder: String, outputFolder: String) {
        val erc777Dependencies = TemplateReader.readFile(inputFolder)
        Files.write(
                Paths.get(
                        erc777Config.outputDir +
                                File.separator +
                                outputFolder),
                erc777Dependencies.toByteArray())
    }
}
