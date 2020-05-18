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
package io.epirus.console.project.testing;

import java.io.File;
import java.io.IOException;

import io.epirus.console.utils.OSUtils;

import org.web3j.codegen.Console;

public class ProjectTester {
    private static boolean setExecutable(final String pathToDirectory, final String gradlew) {
        final File f = new File(pathToDirectory + File.separator + gradlew);
        return f.setExecutable(true);
    }

    public static void main() throws IOException, InterruptedException {
        String currentDirPath = System.getProperty("user.dir");

        if (OSUtils.determineOS() == OSUtils.OS.WINDOWS) {
            setExecutable(currentDirPath, "gradlew.bat");
            runTests(new File(currentDirPath), new String[] {"cmd.exe", "/c", "gradlew.bat test"});
        } else if (OSUtils.determineOS() == OSUtils.OS.LINUX) {
            setExecutable(currentDirPath, "gradlew");
            runTests(new File(currentDirPath), new String[] {"bash", "-c", "./gradlew test"});
        }
    }

    private static void runTests(File workingDir, String[] command)
            throws IOException, InterruptedException {
        int exitCode =
                new ProcessBuilder(command)
                        .directory(workingDir)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            Console.exitError("Tests failed. For more details, see the test output.");
        } else {
            System.out.println("Epirus successfully tested your application.");
        }
    }
}
