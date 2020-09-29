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
package io.epirus.console.openapi.project

import io.epirus.console.project.ProjectStructure
import io.epirus.console.project.ProjectWriter
import io.epirus.console.project.templates.TemplateProvider
import io.epirus.console.project.templates.TemplateReader
import java.io.File

class OpenApiTemplateProvider(
    private val solidityContract: String,
    private val pathToSolidityFolder: String,
    private val gradleBuild: String,
    private val gradleSettings: String,
    private val gradlewWrapperSettings: String,
    private val gradlewBatScript: String,
    private val gradlewScript: String,
    private val gradlewJar: String,
    private val packageName: String,
    private val projectName: String,
    private val contextPath: String,
    private val addressLength: String
) : TemplateProvider {
    fun loadGradleBuild(): String {
        return TemplateReader.readFile(gradleBuild)
            .replace("<package_name>".toRegex(), packageName)
            .replace("<project_name>".toRegex(), projectName)
            .replace("<context_path>".toRegex(), contextPath)
            .replace("<address_length>".toRegex(), addressLength)
    }

    fun loadSolidityContract(): String {
        return TemplateReader.readFile(solidityContract)
    }

    fun loadGradleSettings(): String {
        return TemplateReader.readFile(gradleSettings)
            .replace("<project_name>".toRegex(), projectName)
    }

    fun loadGradlewWrapperSettings(): String {
        return TemplateReader.readFile(gradlewWrapperSettings)
    }

    fun loadGradlewBatScript(): String {
        return TemplateReader.readFile(gradlewBatScript)
    }

    fun loadGradlewScript(): String {
        return TemplateReader.readFile(gradlewScript)
    }

    override fun generateFiles(projectStructure: ProjectStructure) {
        ProjectWriter.writeResourceFile(
            loadGradleBuild(), "build.gradle", projectStructure.projectRoot)
        ProjectWriter.writeResourceFile(
            loadGradleSettings(), "settings.gradle", projectStructure.projectRoot)
        if (solidityContract.isNotEmpty()) ProjectWriter.writeResourceFile(
            loadSolidityContract(), "HelloWorld.sol", projectStructure.solidityPath)
        if (pathToSolidityFolder.isNotEmpty()) {
            ProjectWriter.importSolidityProject(
                File(pathToSolidityFolder), projectStructure.solidityPath)
        }
        ProjectWriter.writeResourceFile(
            TemplateReader.readFile("project/Dockerfile.template"),
            "Dockerfile",
            projectStructure.projectRoot)
        ProjectWriter.writeResourceFile(
            loadGradlewWrapperSettings(),
            "gradle-wrapper.properties",
            projectStructure.wrapperPath)
        ProjectWriter.writeResourceFile(
            loadGradlewScript(), "gradlew", projectStructure.projectRoot)
        ProjectWriter.writeResourceFile(
            loadGradlewBatScript(), "gradlew.bat", projectStructure.projectRoot)
        ProjectWriter.copyResourceFile(
            gradlewJar,
            projectStructure.wrapperPath + File.separator + "gradle-wrapper.jar")
    }
}
