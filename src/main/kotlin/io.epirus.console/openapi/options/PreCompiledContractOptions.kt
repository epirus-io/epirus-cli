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
package io.epirus.console.openapi.options

import picocli.CommandLine
import java.io.File

class PreCompiledContractOptions {
    @CommandLine.Option(
        names = ["-a", "--abi"],
        description = ["input ABI files and folders."],
        arity = "1..*"
    )
    var abis: MutableList<File> = mutableListOf()

    @CommandLine.Option(
        names = ["-b", "--bin"],
        description = ["input BIN files and folders."],
        arity = "1..*"
    )
    var bins: MutableList<File> = mutableListOf()
}
