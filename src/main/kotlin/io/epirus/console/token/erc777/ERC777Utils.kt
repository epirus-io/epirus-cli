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
package io.epirus.console.token.erc777

import io.epirus.console.project.templates.TemplateReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ERC777Utils {
    /**
     * Copies ERC777 contract implementation and its dependencies
     */
    fun copy(outputDir: String) {
            val erc777ResourcePath = Paths.get("tokens", "erc777").toString()
            copyDependency(Paths.get(erc777ResourcePath, "ERC777Token.sol"), Paths.get(outputDir, "ERC777Token.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "Address.sol"), Paths.get(outputDir, "Address.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "Context.sol"), Paths.get(outputDir, "Context.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "ERC777.sol"), Paths.get(outputDir, "ERC777.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "IERC20.sol"), Paths.get(outputDir, "IERC20.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "IERC777.sol"), Paths.get(outputDir, "IERC777.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "IERC777Recipient.sol"), Paths.get(outputDir, "IERC777Recipient.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "IERC777Sender.sol"), Paths.get(outputDir, "IERC777Sender.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "IERC1820Registry.sol"), Paths.get(outputDir, "IERC1820Registry.sol"))
            copyDependency(Paths.get(erc777ResourcePath, "SafeMath.sol"), Paths.get(outputDir, "SafeMath.sol"))
    }

    @Throws(IOException::class)
    fun copyDependency(inputPath: Path, outputPath: Path) {
        val erc777Dependencies = TemplateReader.readFile(inputPath.toString())
        Files.write(
            outputPath,
            erc777Dependencies.toByteArray())
    }
}
