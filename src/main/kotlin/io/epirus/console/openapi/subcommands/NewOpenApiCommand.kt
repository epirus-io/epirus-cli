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
import io.epirus.console.openapi.project.OpenApiProjectCreationUtils.generateOpenApiAndSwaggerUi
import io.epirus.console.openapi.project.OpenApiProjectCreationUtils.runGradleClean
import io.epirus.console.openapi.project.OpenApiProjectStructure
import io.epirus.console.openapi.project.OpenApiTemplateProvider
import io.epirus.console.openapi.utils.PrettyPrinter
import io.epirus.console.openapi.utils.SimpleFileLogger
import io.epirus.console.project.ProjectStructure
import io.epirus.console.project.TemplateType
import io.epirus.console.project.utils.ProjectCreationUtils
import io.epirus.console.token.erc777.ERC777Utils
import org.apache.commons.lang.StringUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File

@Command(
    name = "new",
    description = ["Create a new Web3j-OpenAPI project."],
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
class NewOpenApiCommand : AbstractOpenApiCommand() {

    @Parameters(description = ["HelloWorld, ERC777"], defaultValue = "HelloWorld")
    var templateType = TemplateType.HelloWorld

    override fun generate(projectFolder: File) {
        print("\nCreating ${projectOptions.projectName} project ...\n")
        SimpleFileLogger.startLogging()

        val contextPath = if (projectOptions.contextPath != null) {
            StringUtils.removeEnd(projectOptions.contextPath, "/")
        } else {
            projectOptions.projectName
        }

        when (templateType) {
            TemplateType.HelloWorld -> {
                val projectStructure = createNewProject(
                    OpenApiTemplateProvider(
                        "project/HelloWorld.sol",
                        "",
                        "project/build.gradleOpenApi.template",
                        "project/settings.gradle.template",
                        "project/gradlew-wrapper.properties.template",
                        "project/gradlew.bat.template",
                        "project/gradlew.template",
                        "gradle-wrapper.jar",
                        projectOptions.packageName,
                        projectOptions.projectName,
                        contextPath,
                        (projectOptions.addressLength * 8).toString(),
                        "project/README.openapi.md"
                    )
                )
                buildNewProject(projectStructure.projectRoot)
            }

            TemplateType.ERC777 -> {
                val projectStructure = createNewProject(
                    OpenApiTemplateProvider(
                        "",
                        "",
                        "project/build.gradleOpenApiErc777.template",
                        "project/settings.gradle.template",
                        "project/gradlew-wrapper.properties.template",
                        "project/gradlew.bat.template",
                        "project/gradlew.template",
                        "gradle-wrapper.jar",
                        projectOptions.packageName,
                        projectOptions.projectName,
                        contextPath,
                        (projectOptions.addressLength * 8).toString(),
                        "project/README.openapi.md"
                    )
                )
                copyErc777Contract(projectStructure.solidityPath)
                buildNewProject(projectStructure.projectRoot)
            }
        }

        PrettyPrinter.onProjectSuccess()
    }

    /**
     * Copies ERC777 contract implementation and its dependencies to the new project solidity folder
     */
    private fun copyErc777Contract(solidityPath: String) {
        ERC777Utils.copy(solidityPath)
    }

    /**
     * Creates a new OpenAPI project structure from a set of predefined contracts.
     *
     * @param openApiTemplateProvider: is the OpenApiTemplateProvider containing all parameters for the generation
     */
    private fun createNewProject(openApiTemplateProvider: OpenApiTemplateProvider): ProjectStructure {
        return OpenApiProjectStructure(
            projectOptions.outputDir,
            projectOptions.packageName,
            projectOptions.projectName
        ).apply {
            ProjectCreationUtils.generateTopLevelDirectories(this)
            openApiTemplateProvider.generateFiles(this)
        }
    }

    /**
     * Runs the necessary gradle tasks to have a working project.
     *
     * @param projectRoot: The project root directory containing the gradle executables
     */
    private fun buildNewProject(projectRoot: String) {
        generateOpenApiAndSwaggerUi(projectRoot)
        runGradleClean(projectRoot)
        generateOpenApiAndSwaggerUi(projectRoot)
    }
}
