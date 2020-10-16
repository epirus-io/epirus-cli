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
import io.epirus.console.openapi.utils.PrettyPrinter
import io.epirus.console.project.utils.ProgressCounter
import io.epirus.console.project.utils.ProjectCreationUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Command(
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
    @Mixin
    val preCompiledContractOptions = PreCompiledContractOptions()

    override fun generate(projectFolder: File) {
        val progressCounter = ProgressCounter(true)
        progressCounter.processing("Creating and Building ${projectOptions.projectName} JAR ... Subsequent builds will be faster")

        val tempFolderPath = Paths.get(projectFolder.toString(), projectOptions.projectName)

        OpenApiGeneratorService(
            OpenApiGeneratorServiceConfiguration(
                projectName = projectOptions.projectName,
                packageName = projectOptions.packageName,
                outputDir = tempFolderPath.toString(),
                abis = preCompiledContractOptions.abis,
                addressLength = projectOptions.addressLength,
                contextPath = projectOptions.contextPath?.removeSuffix("/") ?: projectOptions.projectName
            )
        ).generate()

        ProjectCreationUtils.createFatJar(tempFolderPath.toString())

        Files.copy(
            getJarFile(tempFolderPath.toFile()).toPath(),
            File(projectOptions.outputDir, "${projectOptions.projectName}$JAR_SUFFIX").toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        progressCounter.setLoading(false)
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
