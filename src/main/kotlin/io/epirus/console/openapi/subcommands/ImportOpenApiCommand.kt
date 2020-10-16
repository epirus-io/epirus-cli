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
import io.epirus.console.openapi.project.OpenApiProjectBuildUtils
import io.epirus.console.openapi.project.OpenApiProjectStructure
import io.epirus.console.openapi.project.OpenApiTemplateProvider
import io.epirus.console.openapi.utils.PrettyPrinter
import io.epirus.console.project.utils.ProgressCounter
import io.epirus.console.project.utils.ProjectCreationUtils
import org.apache.commons.lang.StringUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.io.InputStream
import java.io.PrintStream

@Command(
    name = "import",
    description = ["Import existing Solidity contracts into a new Web3j-OpenAPI Project."],
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
class ImportOpenApiCommand(
    input: InputStream = System.`in`,
    output: PrintStream = System.out
) : AbstractOpenApiCommand(input, output) {

    @Option(
        names = ["-s", "--solidity-path"],
        description = ["Path to Solidity file/folder"]
    )
    var solidityImportPath: String? = null

    override fun generate(projectFolder: File) {
        if (solidityImportPath == null) {
            solidityImportPath = interactiveOptions.solidityProjectPath
        }

        val contextPath = if (projectOptions.contextPath != null) {
            StringUtils.removeEnd(projectOptions.contextPath, "/")
        } else {
            projectOptions.projectName
        }

        val progressCounter = ProgressCounter(true)
        progressCounter.processing("Creating and Building ${projectOptions.projectName} project ... Subsequent builds will be faster")

        createImportProject(contextPath)

        progressCounter.setLoading(false)
        PrettyPrinter.onProjectSuccess()
    }

    private fun createImportProject(contextPath: String) {
        val projectStructure = OpenApiProjectStructure(
            projectOptions.outputDir,
            projectOptions.packageName,
            projectOptions.projectName
        )
        ProjectCreationUtils.generateTopLevelDirectories(projectStructure)
        OpenApiTemplateProvider(
            "",
            solidityImportPath!!,
            "project/build.gradleImportOpenApi.template",
            "project/settings.gradle.template",
            "project/gradlew-wrapper.properties.template",
            "project/gradlew.bat.template",
            "project/gradlew.template",
            "project/gradle-wrapper.jar",
            projectOptions.packageName,
            projectOptions.projectName,
            contextPath,
            (projectOptions.addressLength * 8).toString(),
            "project/README.openapi.md"
        ).generateFiles(projectStructure)

        OpenApiProjectBuildUtils.generateOpenApiAndSwaggerUi(projectStructure.projectRoot)
        OpenApiProjectBuildUtils.runGradleClean(projectStructure.projectRoot)
        OpenApiProjectBuildUtils.generateOpenApiAndSwaggerUi(projectStructure.projectRoot)
    }
}
