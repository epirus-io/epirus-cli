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
import org.web3j.openapi.codegen.GenerateOpenApi
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.codegen.utils.GeneratorUtils
import java.io.File
import java.nio.file.Paths

class OpenApiGeneratorService(
    val projectName: String,
    val packageName: String,
    val outputDir: String,
    val abis: List<File>,
    val bins: List<File>,
    val addressLength: Int,
    val contextPath: String,
    val isCodeOnly: Boolean
) {

    fun generate() {
        GenerateOpenApi(GeneratorConfiguration(
                projectName = projectName,
                packageName = packageName,
                outputDir = outputDir,
                contracts = GeneratorUtils.loadContractConfigurations(abis, bins),
                addressLength = addressLength,
                contextPath = contextPath
        )).apply {
            generateCore()
            generateServer()
            generateWrappers()
        }

        if (!isCodeOnly) GradleUtils.runGradleTask(Paths.get(outputDir).toFile(), "completeSwaggerUiGeneration", "Generating SwaggerUI...")

        println("Done.")
    }
}
