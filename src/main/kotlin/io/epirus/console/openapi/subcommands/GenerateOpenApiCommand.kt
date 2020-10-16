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
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Visibility.ALWAYS
import java.io.File

@Command(
        name = "generate",
        description = ["Generate REST endpoints from existing Solidity contracts."],
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

    @Mixin
    val preCompiledContractOptions = PreCompiledContractOptions()

    @Option(
        names = ["--with-implementations"],
        description = ["Generate the interfaces implementations."],
        showDefaultValue = ALWAYS
    )
    var withImplementations: Boolean = true

    override fun generate(projectFolder: File) {
        val progressCounter = ProgressCounter(true)
        progressCounter.processing("Generating REST endpoints ...")

        OpenApiGeneratorService(
            OpenApiGeneratorServiceConfiguration(
                projectName = projectOptions.projectName,
                packageName = projectOptions.packageName,
                outputDir = projectFolder.path,
                abis = preCompiledContractOptions.abis,
                addressLength = projectOptions.addressLength,
                contextPath = projectOptions.contextPath?.removeSuffix("/") ?: projectOptions.projectName,
                withImplementations = withImplementations
            )
        ).generate()

        progressCounter.setLoading(false)
        PrettyPrinter.onSuccess()
    }
}
