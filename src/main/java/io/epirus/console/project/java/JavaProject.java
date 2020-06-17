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
import java.util.Map;
import java.util.Optional;

import io.epirus.console.project.AbstractProject;
import io.epirus.console.project.Project;
import io.epirus.console.project.ProjectStructure;
import io.epirus.console.project.templates.java.JavaTemplateBuilder;
import io.epirus.console.project.templates.java.JavaTemplateProvider;

import org.web3j.commons.JavaVersion;

public class JavaProject extends AbstractProject<JavaProject> implements Project {

    protected JavaProject(
            boolean withTests,
            boolean withFatJar,
            Optional<Map> withCredentials,
            boolean withSampleCode,
            String command,
            String solidityImportPath,
            ProjectStructure projectStructure) {
        super(
                withTests,
                withFatJar,
                withCredentials,
                withSampleCode,
                command,
                solidityImportPath,
                projectStructure);
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
                        .withGradleBatScript("gradlew.bat.template")
                        .withGradleScript("gradlew.template")
                        .withGradleSettings("settings.gradle.template")
                        .withWrapperGradleSettings("gradlew-wrapper.properties.template")
                        .withGradlewWrapperJar("gradle-wrapper.jar");

        if (withCredentials.isPresent()) {
            templateBuilder.withWalletNameReplacement((String) withCredentials.get().get("path"));
            templateBuilder.withPasswordFileName((String) withCredentials.get().get("password"));
        }
        if (command.equals("new")) {
            templateBuilder
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "build.gradle.template"
                                    : "build.gradleJava11.template")
                    .withSolidityProject("HelloWorld.sol");

        } else if (command.equals("import")) {
            templateBuilder
                    .withGradleBuild(
                            JavaVersion.getJavaVersionAsDouble() < 11
                                    ? "build.gradleImport.template"
                                    : "build.gradleImportJava11.template")
                    .withPathToSolidityFolder(solidityImportPath);
        }

        if (withSampleCode) {
            templateBuilder.withMainJavaClass("Java.template");
        } else {
            templateBuilder.withMainJavaClass("EmptyJava.template");
        }

        return templateBuilder.build();
    }
}
