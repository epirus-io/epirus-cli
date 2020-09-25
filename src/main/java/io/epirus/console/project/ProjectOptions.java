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
package io.epirus.console.project;

import picocli.CommandLine;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

public class ProjectOptions {
    @CommandLine.Option(
            names = {"-n", "--project-name"},
            description = "Project name.",
            showDefaultValue = ALWAYS)
    public String projectName = "Web3App";

    @CommandLine.Option(
            names = {"-p", "--package"},
            description = "Base package name.",
            showDefaultValue = ALWAYS)
    public String packageName = "io.epirus";

    @CommandLine.Option(
            names = {"-o", "--output-dir"},
            description = "Destination base directory.",
            showDefaultValue = ALWAYS)
    public String outputDir = ".";

    @CommandLine.Option(
            names = {"--address-length"},
            description = {"specify the address length."},
            showDefaultValue = ALWAYS)
    public int addressLength = 20;

    @CommandLine.Option(
            names = {"-t", "--generate-tests"},
            description = "Generate unit tests for the contract wrappers.",
            showDefaultValue = ALWAYS)
    public Boolean generateTests = true;

    @CommandLine.Option(
            names = {"--jar"},
            description = {"generate the JAR"},
            showDefaultValue = ALWAYS)
    public Boolean generateJar = false;

    @CommandLine.Option(
            names = {"--kotlin"},
            description = "Generate Kotlin code instead of Java.",
            showDefaultValue = ALWAYS)
    public Boolean isKotlin = false;
}
