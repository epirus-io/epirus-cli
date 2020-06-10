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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import io.epirus.console.project.templates.TemplateProvider;
import io.epirus.console.project.utils.ProgressCounter;
import io.epirus.console.project.wallet.ProjectWallet;

import org.web3j.codegen.Console;

public abstract class AbstractProject<T extends AbstractProject<T>> {
    private T project;

    protected final boolean withTests;
    protected final boolean withFatJar;
    protected final Optional<Map> withCredentials;
    protected final boolean withSampleCode;
    protected final String command;
    protected final String solidityImportPath;
    protected final ProjectStructure projectStructure;
    protected ProjectWallet projectWallet;
    protected ProgressCounter progressCounter = new ProgressCounter(true);

    protected abstract T getProjectInstance();

    protected AbstractProject(
            boolean withTests,
            boolean withFatJar,
            Optional<Map> withCredentials,
            boolean withSampleCode,
            String command,
            String solidityImportPath,
            ProjectStructure projectStructure) {
        this.withTests = withTests;
        this.withFatJar = withFatJar;
        this.withCredentials = withCredentials;
        this.withSampleCode = withSampleCode;
        this.command = command;
        this.solidityImportPath = solidityImportPath;
        this.projectStructure = projectStructure;
        this.project = getProjectInstance();
    }

    public ProjectStructure getProjectStructure() {
        return project.projectStructure;
    }

    public ProjectWallet getProjectWallet() {
        return project.projectWallet;
    }

    protected void buildGradleProject(final String pathToDirectory)
            throws IOException, InterruptedException {
        if (!isWindows()) {
            setExecutable(pathToDirectory, "gradlew");
            executeBuild(
                    new File(pathToDirectory), new String[] {"bash", "-c", "./gradlew build -q"});
        } else {
            setExecutable(pathToDirectory, "gradlew.bat");
            executeBuild(
                    new File(pathToDirectory),
                    new String[] {"cmd.exe", "/c", "gradlew.bat build -q"});
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    private void setExecutable(final String pathToDirectory, final String gradlew) {
        final File f = new File(pathToDirectory + File.separator + gradlew);
        final boolean isExecutable = f.setExecutable(true);
    }

    private void executeBuild(final File workingDir, final String[] command)
            throws InterruptedException, IOException {
        executeProcess(workingDir, command);
    }

    private void executeProcess(File workingDir, String[] command)
            throws InterruptedException, IOException {
        int exitCode =
                new ProcessBuilder(command)
                        .directory(workingDir)
                        .redirectErrorStream(true)
                        .redirectOutput(File.createTempFile("w3j", "cli"))
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            Console.exitError("Could not build project.");
        }
    }

    protected void createFatJar(String pathToDirectory) throws IOException, InterruptedException {
        if (!isWindows()) {
            executeProcess(
                    new File(pathToDirectory),
                    new String[] {"bash", "./gradlew", "shadowJar", "-q"});
        } else {
            executeProcess(
                    new File(pathToDirectory),
                    new String[] {"cmd.exe", "/c", "./gradlew.bat shadowJar", "-q"});
        }
    }

    protected void generateTopLevelDirectories(ProjectStructure projectStructure) {
        projectStructure.createMainDirectory();
        projectStructure.createTestDirectory();
        projectStructure.createSolidityDirectory();
        projectStructure.createWrapperDirectory();
    }

    public void createProject() throws IOException, InterruptedException {
        generateTopLevelDirectories(projectStructure);
        getTemplateProvider().generateFiles(projectStructure);
        progressCounter.processing("Creating " + projectStructure.projectName);
        buildGradleProject(projectStructure.getProjectRoot());

        if (withTests) {
            generateTests(projectStructure);
        }
        if (withFatJar) {
            createFatJar(projectStructure.getProjectRoot());
        }
        progressCounter.setLoading(false);
    }

    protected abstract TemplateProvider getTemplateProvider();

    protected abstract void generateTests(ProjectStructure projectStructure) throws IOException;
}
