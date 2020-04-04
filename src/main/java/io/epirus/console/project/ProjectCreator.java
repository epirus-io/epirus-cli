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
package io.epirus.console.project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import io.epirus.console.account.AccountManager;
import io.epirus.console.account.AccountUtils;
import io.epirus.console.project.java.JavaBuilder;
import io.epirus.console.project.java.JavaProjectCreatorCLIRunner;
import io.epirus.console.project.kotlin.KotlinBuilder;
import io.epirus.console.project.kotlin.KotlinProjectCreatorCLIRunner;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import static org.web3j.codegen.Console.exitError;
import static org.web3j.utils.Collection.tail;

public class ProjectCreator {

    public static final String COMMAND_NEW = "new";
    public static final String COMMAND_JAVA = "--java";
    public static final String COMMAND_KOTLIN = "kotlin";
    public static final String USAGE = "new java|kotlin";
    private final String root;
    private final String packageName;
    private final String projectName;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectCreator.class);

    public ProjectCreator(final String root, final String packageName, final String projectName) {
        this.projectName = projectName;
        this.packageName = packageName;
        this.root = root;
    }

    public static void main(String[] args) throws IOException {
        final List<String> stringOptions = new ArrayList<>();
        if (args.length > 0 && args[0].toLowerCase().equals(COMMAND_JAVA)) {
            args = tail(args);
            args = getValues(args, stringOptions);
            new CommandLine(new JavaProjectCreatorCLIRunner()).execute(args);
        } else {
            args = getValues(args, stringOptions);
            new CommandLine(new KotlinProjectCreatorCLIRunner()).execute(args);
        }
    }

    @NotNull
    private static String[] getValues(String[] args, List<String> stringOptions)
            throws IOException {
        String projectName;
        if (args.length == 0) {
            InteractiveOptions interactiveOptions = new InteractiveOptions();
            stringOptions.add("-n");
            projectName = interactiveOptions.getProjectName();
            stringOptions.add(projectName);
            stringOptions.add("-p");
            stringOptions.add(interactiveOptions.getPackageName());
            interactiveOptions
                    .getProjectDestination(projectName)
                    .ifPresent(
                            projectDest -> {
                                stringOptions.add("-o");
                                stringOptions.add(projectDest);
                            });
            AccountUtils.accountInit(new AccountManager(new OkHttpClient()));
            args = stringOptions.toArray(new String[0]);
        }
        return args;
    }

    public void generateJava(
            boolean withTests,
            Optional<File> solidityFile,
            boolean withWalletProvider,
            boolean withFatJar,
            boolean withSampleCode,
            String command) {
        try {
            JavaBuilder javaBuilder =
                    new JavaBuilder()
                            .withProjectName(this.projectName)
                            .withRootDirectory(this.root)
                            .withPackageName(this.packageName)
                            .withTests(withTests)
                            .withWalletProvider(withWalletProvider)
                            .withCommand(command)
                            .withSampleCode(withSampleCode)
                            .withFatJar(withFatJar);
            solidityFile.map(File::getAbsolutePath).ifPresent(javaBuilder::withSolidityFile);
            Project javaProject = javaBuilder.build();
            javaProject.createProject();
            onSuccess(javaProject, "java");
        } catch (final Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            exitError("\nCould not generate project reason: \n" + sw.toString());
        }
    }

    public void generateKotlin(
            boolean withTests,
            Optional<File> solidityFile,
            boolean withWalletProvider,
            boolean withFatJar,
            boolean withSampleCode,
            String command) {
        try {
            KotlinBuilder kotlinBuilder =
                    new KotlinBuilder()
                            .withProjectName(this.projectName)
                            .withRootDirectory(this.root)
                            .withPackageName(this.packageName)
                            .withTests(withTests)
                            .withWalletProvider(withWalletProvider)
                            .withCommand(command)
                            .withSampleCode(withSampleCode)
                            .withFatJar(withFatJar);
            solidityFile.map(File::getAbsolutePath).ifPresent(kotlinBuilder::withSolidityFile);
            Project kotlinProject = kotlinBuilder.build();
            kotlinProject.createProject();
            onSuccess(kotlinProject, "kotlin");
        } catch (final Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            exitError("\nCould not generate project reason: \n" + sw.toString());
        }
    }

    private void onSuccess(Project project, String projectType) {
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
        instructionPrinter.print(String.format("%-20s", gradleCommand + " run"));
        commandPrinter.println("Runs your application");
        instructionPrinter.print(String.format("%-20s", gradleCommand + " test"));
        commandPrinter.println("Test your application");
        instructionPrinter.print(String.format("%-20s", "epirus deploy"));
        commandPrinter.println("Deploys your application");
    }
}
