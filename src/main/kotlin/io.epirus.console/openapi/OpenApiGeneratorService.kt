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

import io.epirus.console.openapi.utils.GradleUtils
import io.epirus.console.project.templates.TemplateReader
import org.apache.commons.io.FileUtils
import org.web3j.openapi.codegen.GenerateOpenApi
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.codegen.utils.GeneratorUtils
import org.web3j.sokt.SolcArguments
import org.web3j.sokt.SolidityFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class OpenApiGeneratorService(
    private val openApiGeneratorServiceConfiguration: OpenApiGeneratorServiceConfiguration
) {

    fun generate() {
        if (openApiGeneratorServiceConfiguration.abis.isEmpty()) {
            generateWithHelloWorldTemplate()
        } else {
            generateInternal(openApiGeneratorServiceConfiguration.abis, openApiGeneratorServiceConfiguration.bins)
        }

        println("Done.")
    }

    private fun generateWithHelloWorldTemplate() {
        val erc777Template = TemplateReader.readFile("project/HelloWorld.sol")

        val buildPath = openApiGeneratorServiceConfiguration.outputDir + File.separator + "build"
        File(buildPath).mkdirs()

        val contractPath = (buildPath +
            File.separator +
            "HelloWorld.sol")
        Files.write(
            Paths.get(
                contractPath),
            erc777Template.toByteArray())
        val fileName = contractPath.substringAfterLast("/")
        val solidityFile = SolidityFile(contractPath)
        val compilerInstance = solidityFile.getCompilerInstance(redirectOutput = true)

        println("Using solidity compiler ${compilerInstance.solcRelease.version} for $fileName")

        compilerInstance.execute(
            SolcArguments.OUTPUT_DIR.param { buildPath },
            SolcArguments.ABI,
            SolcArguments.BIN,
            SolcArguments.OVERWRITE
        )

        generateInternal(listOf(File(buildPath + File.separator + "HelloWorld.abi")), listOf(File(buildPath + File.separator + "HelloWorld.bin")))

        FileUtils.deleteDirectory(File(buildPath))
    }

    private fun generateInternal(abis: List<File>, bins: List<File>) {
        GenerateOpenApi(GeneratorConfiguration(
            projectName = openApiGeneratorServiceConfiguration.projectName,
            packageName = openApiGeneratorServiceConfiguration.packageName,
            outputDir = if (openApiGeneratorServiceConfiguration.outputDir.endsWith(openApiGeneratorServiceConfiguration.projectName))
                openApiGeneratorServiceConfiguration.outputDir
            else "${openApiGeneratorServiceConfiguration.outputDir}${File.separator}${openApiGeneratorServiceConfiguration.projectName}",
            contracts = GeneratorUtils.loadContractConfigurations(abis, bins),
            addressLength = openApiGeneratorServiceConfiguration.addressLength,
            contextPath = openApiGeneratorServiceConfiguration.contextPath,
            withWrappers = openApiGeneratorServiceConfiguration.withWrappers,
            withSwaggerUi = openApiGeneratorServiceConfiguration.withSwaggerUi,
            withGradleResources = openApiGeneratorServiceConfiguration.withGradleResources,
            withServerBuildFile = openApiGeneratorServiceConfiguration.withServerBuildFile,
            withCoreBuildFile = openApiGeneratorServiceConfiguration.withCoreBuildFile
        )).generate()
    }
}
