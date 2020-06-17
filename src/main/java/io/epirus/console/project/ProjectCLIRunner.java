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

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import io.epirus.console.project.utils.InputVerifier;
import io.epirus.console.project.utils.ProjectUtils;
import picocli.CommandLine;

import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_LOOKUP_PATH;
import static io.epirus.console.project.wallet.ProjectWalletUtils.DEFAULT_WALLET_NAME;
import static org.web3j.codegen.Console.exitError;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

public abstract class ProjectCLIRunner implements Runnable {

    @CommandLine.Option(
            names = {"-n", "--project-name"},
            description = "Project name.")
    public String projectName;

    @CommandLine.Option(
            names = {"-p", "--package"},
            description = "Base package name.")
    public String packageName;

    @CommandLine.Option(
            names = {"-o", "--output-dir"},
            description = "Destination base directory.",
            showDefaultValue = ALWAYS)
    public String outputDir = ".";

    @CommandLine.Option(
            names = {"-w", "--wallet-path"},
            description = "Path to your wallet file")
    public String walletPath = DEFAULT_WALLET_LOOKUP_PATH + File.separator + DEFAULT_WALLET_NAME;

    @CommandLine.Option(
            names = {"-k", "--wallet-password"},
            description = "Wallet password",
            showDefaultValue = ALWAYS)
    public String walletPassword = "";

    @Override
    public void run() {
        if (projectName == null && packageName == null) {
            buildInteractively();
        }
        if (!inputIsValid(projectName, packageName)) {
            exitError("Input is not valid.");
        }
        if (InputVerifier.projectExists(new File(projectName))) {
            if (new InteractiveOptions().overrideExistingProject()) {
                ProjectUtils.deleteFolder(new File(projectName).toPath());
                createProject();
            } else {
                exitError("Project creation was canceled.");
            }
        } else {
            createProject();
        }
    }

    protected abstract void createProject();

    protected abstract void buildInteractively();

    protected void onSuccess(Project project, String projectType) {
        String gradleCommand =
                System.getProperty("os.name").toLowerCase().startsWith("windows")
                        ? "./gradlew.bat"
                        : "./gradlew";
        System.out.print(System.lineSeparator());
        ColoredPrinter cp =
                new ColoredPrinter.Builder(0, false)
                        .foreground(Ansi.FColor.WHITE)
                        .background(Ansi.BColor.GREEN)
                        .attribute(Ansi.Attribute.BOLD)
                        .build();
        ColoredPrinter instructionPrinter =
                new ColoredPrinter.Builder(0, false).foreground(Ansi.FColor.CYAN).build();
        ColoredPrinter commandPrinter =
                new ColoredPrinter.Builder(0, false).foreground(Ansi.FColor.GREEN).build();
        cp.println("Project Created Successfully");
        System.out.print(System.lineSeparator());

        if (project.getProjectWallet() != null) {
            instructionPrinter.println(
                    "Project information",
                    Ansi.Attribute.LIGHT,
                    Ansi.FColor.WHITE,
                    Ansi.BColor.BLACK);
            instructionPrinter.print(
                    String.format("%-20s", "Wallet Address"),
                    Ansi.Attribute.CLEAR,
                    Ansi.FColor.WHITE,
                    Ansi.BColor.BLACK);
            instructionPrinter.println(
                    project.getProjectWallet().getWalletAddress(),
                    Ansi.Attribute.BOLD,
                    Ansi.FColor.GREEN,
                    Ansi.BColor.BLACK);
            System.out.print(System.lineSeparator());
        }
        instructionPrinter.println(
                "Commands", Ansi.Attribute.LIGHT, Ansi.FColor.YELLOW, Ansi.BColor.BLACK);
        instructionPrinter.print(String.format("%-40s", gradleCommand + " run"));
        commandPrinter.println("Runs your application");
        instructionPrinter.print(String.format("%-40s", gradleCommand + " test"));
        commandPrinter.println("Test your application");
        instructionPrinter.print(String.format("%-40s", "epirus deploy rinkeby|ropsten"));
        commandPrinter.println("Deploys your application");
    }

    private boolean inputIsValid(String... requiredArgs) {
        return InputVerifier.requiredArgsAreNotEmpty(requiredArgs)
                && InputVerifier.classNameIsValid(projectName)
                && InputVerifier.packageNameIsValid(packageName);
    }
}
