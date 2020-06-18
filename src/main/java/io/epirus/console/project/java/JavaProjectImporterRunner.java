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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.epirus.console.project.InteractiveOptions;
import io.epirus.console.project.ProjectImporterConfig;
import org.jetbrains.annotations.NotNull;

public class JavaProjectImporterRunner extends JavaProjectRunner {

    private String outputDir;
    private String walletPath;
    private String walletPassword;
    private String projectName;
    private String packageName;
    private String solidityImportPath;
    private boolean shouldGenereteTests;

    public JavaProjectImporterRunner(final ProjectImporterConfig projectImporterConfig) {
        super(projectImporterConfig);
        this.walletPath = projectImporterConfig.getWalletPath();
        this.walletPassword = projectImporterConfig.getWalletPassword();
        this.projectName = projectImporterConfig.getProjectName();
        this.packageName = projectImporterConfig.getPackageName();
        this.outputDir = projectImporterConfig.getOutputDir();
        this.solidityImportPath = projectImporterConfig.getSolidityImportPath();
        this.shouldGenereteTests = projectImporterConfig.shouldGenerateTests();
    }

    protected void createProject() {
        Map<String, String> walletCredentials = new HashMap<>();
        walletCredentials.put("path", walletPath);
        walletCredentials.put("password", walletPassword);
        generateJava(
                shouldGenereteTests,
                Optional.of(new File(solidityImportPath)),
                Optional.of(walletCredentials),
                false,
                false,
                "import");
    }

    @NotNull
    protected void buildInteractively() {
        InteractiveOptions interactiveOptions = new InteractiveOptions();
        projectName = interactiveOptions.getProjectName();
        packageName = interactiveOptions.getPackageName();
        solidityImportPath = interactiveOptions.getSolidityProjectPath();

        final Map<String, String> walletCredentials = interactiveOptions.getWalletLocation();
        walletPath = walletCredentials.get("path");
        walletPassword = walletCredentials.get("password");

        interactiveOptions
                .getProjectDestination(projectName)
                .ifPresent(projectDest -> outputDir = projectDest);

        shouldGenereteTests = interactiveOptions.userWantsTests();
    }
}
