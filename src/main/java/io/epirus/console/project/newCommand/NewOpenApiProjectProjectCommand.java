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
package io.epirus.console.project.newCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.openapi.OpenApiGeneratorService;
import io.epirus.console.openapi.OpenApiGeneratorServiceConfiguration;
import io.epirus.console.project.AbstractProjectCommand;
import org.apache.commons.lang.StringUtils;
import picocli.CommandLine;

@CommandLine.Command(
        name = "openapi",
        description = "Create a new Web3j OpenAPI Project",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class NewOpenApiProjectProjectCommand extends AbstractProjectCommand implements Runnable {

    @CommandLine.Option(
            names = {"-a", "--abi"},
            description = {"input ABI files and folders."},
            arity = "1..*")
    public List<File> abis = new ArrayList<>();

    @CommandLine.Option(
            names = {"-b", "--bin"},
            description = {"input BIN files and folders."},
            arity = "1..*")
    public List<File> bins = new ArrayList<>();

    @CommandLine.Option(
            names = {"--context-path"},
            description = {"set the API context path (default: the project name)"})
    public String contextPath;

    @Override
    public void run() {
        setup();
        switch (templateType) {
            case NONE:
            case HELLOWORLD:
                new OpenApiGeneratorService(
                                new OpenApiGeneratorServiceConfiguration(
                                        projectName,
                                        packageName,
                                        outputDir,
                                        abis,
                                        bins,
                                        addressLength,
                                        contextPath != null
                                                ? StringUtils.removeEnd(contextPath, "/")
                                                : projectName,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true))
                        .generate();
                break;
        }
    }
}
