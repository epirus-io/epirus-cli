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

import org.web3j.openapi.console.options.ProjectOptions
import picocli.CommandLine
import java.io.File
import java.nio.file.Path

abstract class AbstractCommand {

    @CommandLine.Spec
    protected lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(
            names = ["-o", "--output"],
            description = ["project output directory."],
            defaultValue = "."
    )
    protected lateinit var outputDirectory: File

    @CommandLine.Option(
            names = ["-a", "--abi"],
            description = ["input ABI files and folders."],
            arity = "1..*",
            required = true
    )
    protected lateinit var abis: List<File>

    @CommandLine.Option(
            names = ["-b", "--bin"],
            description = ["input BIN files and folders."],
            arity = "1..*",
            required = true
    )
    protected lateinit var bins: List<File>

    @CommandLine.Mixin
    protected val projectOptions = ProjectOptions()

    @CommandLine.Option(
            names = ["-p", "--package-name"],
            description = ["generated package name."],
            required = true
    )
    protected lateinit var packageName: String

    @CommandLine.Option(
            names = ["--dev"],
            description = ["not delete the failed build files."],
            defaultValue = "false"
    )
    protected var dev: Boolean = false

    @CommandLine.Option(
            names = ["--address-length"],
            description = ["specify the address length."],
            defaultValue = "20"
    )
    protected var addressLength: Int = 20

    fun call(): Int {
        val projectFolder = Path.of(
                outputDirectory.canonicalPath,
                projectOptions.projectName
        ).toFile().apply {
            deleteRecursively()
            mkdirs()
        }

        return try {
            generate(projectFolder)
            CommandLine.ExitCode.OK
        } catch (e: Exception) {
            if (!dev) projectFolder.deleteOnExit() // FIXME project doesn't get deleted when there is an exception, try messing with the Mustache templates to reproduce
            e.printStackTrace()
            CommandLine.ExitCode.SOFTWARE
        }
    }

    abstract fun generate(projectFolder: File)
}
