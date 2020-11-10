/*
 * Copyright 2019 Web3 Labs Ltd.
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

import java.io.IOException;

import io.epirus.console.project.AbstractProject;
import io.epirus.console.project.Project;
import io.epirus.console.project.ProjectStructure;
import io.epirus.console.project.templates.java.JavaTemplateBuilder;
import io.epirus.console.project.templates.java.JavaTemplateProvider;

import org.web3j.commons.JavaVersion;

public class JavaProject extends AbstractProject<JavaProject> implements Project {

    public JavaProject(
            boolean withTests,
            boolean withFatJar,
            boolean withSampleCode,
            String command,
            String solidityImportPath,
            ProjectStructure projectStructure) {
        super(withTests, withFatJar, withSampleCode, command, solidityImportPath, projectStructure);
    }

    protected void generateTests(ProjectStructure projectStructure) throws IOException {
        new JavaTestCLIRunner(
                        projectStructure.getGeneratedJavaWrappers(),
                        projectStructure.getPathToTestDirectory())
                .generateJava();
    }

    @Override
    protected JavaProject getProjectInstance() {
        return this;
    }

    public JavaTemplateProvider getTemplateProvider() {
        JavaTemplateBuilder templateBuilder =
                new JavaTemplateBuilder()
                        .withProjectNameReplacement(projectStructure.projectName)
                        .withPackageNameReplacement(projectStructure.packageName)
                        .withGradleBatScript("project/gradlew.bat.template")
                        .withGradleScript("project/gradlew.template")
                        .withGradleSettings("project/settings.gradle.template")
                        .withWrapperGradleSettings("project/gradlew-wrapper.properties.template")
                        .withGradlewWrapperJar("project/gradle-wrapper.jar");

        if (command.equals("new")) {
            templateBuilder
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "project/build.gradle.template"
                                    : "project/build.gradleJava11.template")
                    .withSolidityProject("contracts/HelloWorld.sol");

        } else if (command.equals("import")) {
            templateBuilder
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "project/build.gradleImport.template"
                                    : "project/build.gradleImportJava11.template")
                    .withPathToSolidityFolder(solidityImportPath);
        }

        if (withSampleCode) {
            templateBuilder.withMainJavaClass("project/Java.template");
        } else {
            templateBuilder.withMainJavaClass("project/EmptyJava.template");
        }

        return templateBuilder.build();
    }
}
