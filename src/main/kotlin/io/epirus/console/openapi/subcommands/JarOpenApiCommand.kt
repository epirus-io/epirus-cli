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
package io.epirus.console.openapi.subcommands

import io.epirus.console.EpirusVersionProvider
import io.epirus.console.openapi.OpenApiGeneratorService
import io.epirus.console.openapi.OpenApiGeneratorServiceConfiguration
import io.epirus.console.openapi.options.PreCompiledContractOptions
import io.epirus.console.openapi.utils.GradleUtils
import io.epirus.console.openapi.utils.PrettyPrinter
import io.epirus.console.openapi.utils.SimpleFileLogger
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@CommandLine.Command(
    name = "jar",
    description = ["Generate an executable Web3j-OpenAPI JAR."],
    abbreviateSynopsis = true,
    showDefaultValues = true,
    mixinStandardHelpOptions = true,
    versionProvider = EpirusVersionProvider::class,
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    optionListHeading = "%nOptions:%n",
    footerHeading = "%n",
    footer = ["Epirus CLI is licensed under the Apache License 2.0"]
)
class JarOpenApiCommand : AbstractOpenApiCommand() {
    @CommandLine.Mixin
    val preCompiledContractOptions = PreCompiledContractOptions()

    override fun generate(projectFolder: File) {
        if (preCompiledContractOptions.abis.isEmpty()) {
            print("\nGenerating Hello World OpenAPI JAR ...\n")
        } else {
            print("\nGenerating OpenAPI JAR ...\n")
        }
        SimpleFileLogger.startLogging()
        val tempFolderPath = Paths.get(projectFolder.toString(), projectOptions.projectName)

        OpenApiGeneratorService(
            OpenApiGeneratorServiceConfiguration(
                projectName = projectOptions.projectName,
                packageName = projectOptions.packageName,
                outputDir = tempFolderPath.toString(),
                abis = preCompiledContractOptions.abis,
                bins = preCompiledContractOptions.bins,
                addressLength = projectOptions.addressLength,
                contextPath = projectOptions.contextPath?.removeSuffix("/") ?: projectOptions.projectName
            )
        ).generate()

        GradleUtils.runGradleTask(
            tempFolderPath.toFile(),
            "shadowJar",
            emptyList(),
            "Generating the Jar ...",
            System.out
        )

        Files.copy(
            getJarFile(tempFolderPath.toFile()).toPath(),
            File(projectOptions.outputDir, "${projectOptions.projectName}$JARSUFFIX").toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        PrettyPrinter.onJarSuccess()
    }

    private fun getJarFile(outputProjectFolder: File): File {
        return File(
            Paths.get(
                outputProjectFolder.toString(),
                "server",
                "build",
                "libs"
            ).toString()
        ).listFiles()!!.first { it.name.endsWith("-all.jar") }
    }
}
