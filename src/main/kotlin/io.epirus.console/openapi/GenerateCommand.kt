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

import io.epirus.console.EpirusVersionProvider
import org.web3j.openapi.codegen.GenerateOpenApi
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.codegen.utils.GeneratorUtils.loadContractConfigurations
import org.web3j.openapi.console.options.ProjectOptions
import org.web3j.openapi.console.utils.GradleUtils.runGradleTask
import picocli.CommandLine.Command
import picocli.CommandLine.ExitCode
import picocli.CommandLine.Mixin
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Callable

@Command(
        name = "generate",
        description = ["Generate a new OpenAPI Kotlin code"],
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider::class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = ["Epirus CLI is licensed under the Apache License 2.0"])
class GenerateCommand : Callable<Int> {

    @Spec
    private lateinit var spec: CommandSpec

    override fun generate(projectFolder: File) {

        val generatorConfiguration = GeneratorConfiguration(
            projectName = projectOptions.projectName,
            packageName = packageName,
            outputDir = projectFolder.path,
            contracts = loadContractConfigurations(abis, bins),
            addressLength = addressLength,
            contextPath = projectOptions.contextPath?.removeSuffix("/") ?: projectOptions.projectName,
            version = OpenApiCommand.VersionProvider.versionName
        )

        GenerateOpenApi(generatorConfiguration).apply {
            generateCore()
            generateServer()
            generateWrappers()
        }
        println("Done.")
    }
}
