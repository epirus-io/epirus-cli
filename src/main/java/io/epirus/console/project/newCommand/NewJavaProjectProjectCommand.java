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

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.project.AbstractProjectCommand;
import io.epirus.console.project.java.JavaProjectCreatorRunner;
import picocli.CommandLine;

@CommandLine.Command(
        name = "java",
        description = "Create a new Web3j Java Project",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class NewJavaProjectProjectCommand extends AbstractProjectCommand implements Runnable {
    @Override
    public void run() {
        setup();
        switch (templateType) {
            case NONE:
            case HELLOWORLD:
                new JavaProjectCreatorRunner(projectCreatorConfig).run();
                break;
            case ERC777:
                System.out.println("Generating ERC777 Java project is currently unsupported");
                //                        final String templatePath =
                // prepareERC777Template();
                //                        final ProjectImporterConfig projectImporterConfig
                // =
                //                                new ProjectImporterConfig(
                //                                        projectName, packageName,
                // outputDir, templatePath, true);
                //                        new
                // KotlinProjectImporterRunner(projectImporterConfig).run();
                //                        deleteFile(templatePath);
                break;
        }
    }
}
