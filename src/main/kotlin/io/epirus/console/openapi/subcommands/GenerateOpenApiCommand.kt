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
import io.epirus.console.openapi.utils.SimpleFileLogger
import picocli.CommandLine
import picocli.CommandLine.Command
import java.io.File

@Command(
        name = "generate",
        description = ["Generate REST endpoints from existing solidity contracts."],
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider::class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = ["Epirus CLI is licensed under the Apache License 2.0"])
class GenerateOpenApiCommand : AbstractOpenApiCommand() {

    @CommandLine.Mixin
    val preCompiledContractOptions = PreCompiledContractOptions()

    @CommandLine.Option(
        names = ["--with-implementations"],
        description = ["Generate the interfaces implementations."],
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    var withImplementations: Boolean = true

    override fun generate(projectFolder: File) {
        if (preCompiledContractOptions.abis.isEmpty()) {
            print("\nGenerating Hello World REST endpoints ...\n")
        } else {
            print("\nGenerating contracts REST endpoints ...\n")
        }
        SimpleFileLogger.startLogging()

        OpenApiGeneratorService(
            OpenApiGeneratorServiceConfiguration(
                projectName = projectOptions.projectName,
                packageName = projectOptions.packageName,
                outputDir = projectFolder.path,
                abis = preCompiledContractOptions.abis,
                bins = preCompiledContractOptions.bins,
                addressLength = projectOptions.addressLength,
                contextPath = projectOptions.contextPath?.removeSuffix("/") ?: projectOptions.projectName,
                withSwaggerUi = false,
                withGradleResources = false,
                withWrappers = withImplementations, // So that we don't generate the wrappers with only the interfaces
                withServerBuildFile = true,
                withCoreBuildFile = true,
                withImplementations = withImplementations
            )
        ).generate()

        PrettyPrinter.onSuccess()
    }
}
