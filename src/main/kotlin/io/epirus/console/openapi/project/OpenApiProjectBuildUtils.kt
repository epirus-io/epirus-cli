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

import io.epirus.console.project.utils.ProjectCreationUtils.setExecutable
import io.epirus.console.project.utils.ProjectCreationUtils.executeBuild
import io.epirus.console.project.utils.ProjectCreationUtils.isWindows
import java.io.File
import java.io.IOException

internal object OpenApiProjectBuildUtils {
    @Throws(IOException::class, InterruptedException::class)
    fun generateOpenApiAndSwaggerUi(pathToDirectory: String?) {
        if (!isWindows()) {
            setExecutable(pathToDirectory, "gradlew")
            executeBuild(
                File(pathToDirectory!!), arrayOf("bash", "-c", "./gradlew generateWeb3jSwaggerUI"))
        } else {
            setExecutable(pathToDirectory, "gradlew.bat")
            executeBuild(
                File(pathToDirectory!!), arrayOf("cmd", "/c", ".\\gradlew.bat generateWeb3jSwaggerUI"))
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    fun runGradleClean(pathToDirectory: String?) {
        if (!isWindows()) {
            setExecutable(pathToDirectory, "gradlew")
            executeBuild(
                File(pathToDirectory!!), arrayOf("bash", "-c", "./gradlew clean"))
        } else {
            setExecutable(pathToDirectory, "gradlew.bat")
            executeBuild(
                File(pathToDirectory!!), arrayOf("cmd", "/c", ".\\gradlew.bat clean"))
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    fun generateShadowJar(pathToDirectory: String?) {
        if (!isWindows()) {
            setExecutable(pathToDirectory, "gradlew")
            executeBuild(
                File(pathToDirectory!!), arrayOf("bash", "-c", "./gradlew clean"))
        } else {
            setExecutable(pathToDirectory, "gradlew.bat")
            executeBuild(
                File(pathToDirectory!!), arrayOf("cmd", "/c", ".\\gradlew.bat clean"))
        }
    }
}