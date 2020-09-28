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

import io.epirus.console.openapi.options.OpenApiProjectOptions
import io.epirus.console.project.InteractiveOptions
import io.epirus.console.project.utils.InputVerifier
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess

abstract class AbstractOpenApiCommand : Callable<Int> {

    @CommandLine.Mixin
    protected val projectOptions = OpenApiProjectOptions()

    @CommandLine.Spec
    protected lateinit var spec: CommandLine.Model.CommandSpec

    private val interactiveOptions: InteractiveOptions = InteractiveOptions(System.`in`, System.out)
    private val inputVerifier: InputVerifier = InputVerifier(System.out)

    override fun call(): Int {
        if (inputIsNotValid(projectOptions.packageName, projectOptions.projectName))
            exitProcess(1)

        val projectFolder = Paths.get(
            projectOptions.outputDir,
            projectOptions.projectName
        ).toFile().apply {
            if (!exists() || interactiveOptions.overrideExistingProject()) {
                deleteRecursively()
                mkdirs()
            } else {
                exitProcess(1)
            }
        }

        return try {
            generate(projectFolder)
            CommandLine.ExitCode.OK
        } catch (e: Exception) {
            if (!projectOptions.dev) projectFolder.deleteOnExit() // FIXME project doesn't get deleted when there is an exception, try messing with the Mustache templates to reproduce
            e.printStackTrace()
            exitProcess(1)
        }
    }

    abstract fun generate(projectFolder: File)

    private fun inputIsNotValid(vararg requiredArgs: String): Boolean {
        return !(inputVerifier.requiredArgsAreNotEmpty(*requiredArgs) &&
            inputVerifier.classNameIsValid(projectOptions.projectName) &&
            inputVerifier.packageNameIsValid(projectOptions.packageName))
    }
}
