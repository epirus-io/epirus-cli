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

import org.web3j.openapi.codegen.GenerateOpenApi
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.codegen.utils.GeneratorUtils.loadContractConfigurations
import org.web3j.openapi.console.utils.GradleUtils.runGradleTask
import picocli.CommandLine.Command
import java.io.File
import java.util.concurrent.Callable

@Command(
        name = "new",
        showDefaultValues = true,
        description = ["Generates a whole Web3j OpenAPI project."]
)
class NewCommand : AbstractCommand(), Callable<Int> {

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

        GenerateOpenApi(generatorConfiguration).generateAll()
        runGradleTask(projectFolder, "completeSwaggerUiGeneration", "Generating SwaggerUI...")

        println("Done.")
    }
}
