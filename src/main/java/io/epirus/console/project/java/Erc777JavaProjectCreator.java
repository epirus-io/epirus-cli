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
package io.epirus.console.project.java;

import io.epirus.console.openapi.utils.PrettyPrinter;
import io.epirus.console.openapi.utils.SimpleFileLogger;
import io.epirus.console.project.Erc777ProjectCreatorConfig;
import io.epirus.console.project.ProjectRunner;
import io.epirus.console.project.templates.java.erc777.Erc777JavaTemplateBuilder;
import io.epirus.console.project.utils.ProgressCounter;
import io.epirus.console.project.utils.ProjectCreationUtils;
import io.epirus.console.token.erc777.ERC777Utils;

import org.web3j.commons.JavaVersion;

public class Erc777JavaProjectCreator extends ProjectRunner {

    Erc777ProjectCreatorConfig erc777ProjectCreatorConfig;

    public Erc777JavaProjectCreator(Erc777ProjectCreatorConfig erc777ProjectCreatorConfig) {
        super(erc777ProjectCreatorConfig);
        this.erc777ProjectCreatorConfig = erc777ProjectCreatorConfig;
    }

    @Override
    protected void createProject() {
        ProgressCounter progressCounter = new ProgressCounter(true);
        progressCounter.processing(
                "Creating and building ERC777 project ... Subsequent builds will be faster");
        JavaProjectStructure projectStructure =
                new JavaProjectStructure(outputDir, packageName, projectName);
        ProjectCreationUtils.generateTopLevelDirectories(projectStructure);
        try {
            new Erc777JavaTemplateBuilder()
                    .withTokenName(erc777ProjectCreatorConfig.getTokenName())
                    .withTokenSymbol(erc777ProjectCreatorConfig.getTokenSymbol())
                    .withInitialSupply(erc777ProjectCreatorConfig.getInitialSupply())
                    .withDefaultOperators(erc777ProjectCreatorConfig.getDefaultProviders())
                    .withProjectNameReplacement(projectName)
                    .withPackageNameReplacement(packageName)
                    .withGradleBatScript("project/gradlew.bat.template")
                    .withGradleScript("project/gradlew.template")
                    .withGradleSettings("project/settings.gradle.template")
                    .withWrapperGradleSettings("project/gradlew-wrapper.properties.template")
                    .withGradlewWrapperJar("gradle-wrapper.jar")
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "project/build.gradleErc777.template"
                                    : "project/build.gradleErc777Java11.template")
                    .withMainJavaClass("project/JavaErc777.template")
                    .build()
                    .generateFiles(projectStructure);

            ERC777Utils.INSTANCE.copy(projectStructure.getSolidityPath());
            ProjectCreationUtils.generateWrappers(projectStructure.getProjectRoot());
            if (withTests) {
                new JavaTestCLIRunner(
                                projectStructure.getGeneratedJavaWrappers(),
                                projectStructure.getPathToTestDirectory())
                        .generateJava();
            }
            if (withJar) {
                ProjectCreationUtils.createFatJar(projectStructure.getProjectRoot());
            }

            progressCounter.setLoading(false);
            JavaProjectRunner.onSuccess(
                    new JavaProject(withTests, withJar, true, "new", "", projectStructure), "java");
        } catch (Exception e) {
            e.printStackTrace(SimpleFileLogger.INSTANCE.getFilePrintStream());
            PrettyPrinter.INSTANCE.onFailed();
            System.exit(1);
        }
    }
}
