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
import picocli.CommandLine.Command
import java.io.File
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
class GenerateCommand : Callable<Int>, AbstractCommand() {

    override fun generate(projectFolder: File) {

        OpenApiGeneratorService(OpenApiGeneratorServiceConfiguration(
            projectName = projectOptions.projectName,
            packageName = packageName,
            outputDir = projectFolder.path,
            abis = abis,
            bins = bins,
            addressLength = addressLength,
            contextPath = projectOptions.contextPath?.removeSuffix("/") ?: projectOptions.projectName,
            withSwaggerUi = false,
            withGradleResources = false,
            withWrappers = true,
            withServerBuildFile = true,
            withCoreBuildFile = true
        )
        ).generate()
    }
}
