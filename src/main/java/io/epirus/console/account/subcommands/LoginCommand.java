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
package io.epirus.console.account.subcommands;

import io.epirus.console.EpirusVersionProvider;
import io.epirus.console.account.AccountService;
import io.epirus.console.project.InteractiveOptions;
import io.epirus.console.utils.ConsoleDevice;
import io.epirus.console.utils.IODevice;
import picocli.CommandLine;

import org.web3j.codegen.Console;

@CommandLine.Command(
        name = "login",
        description = "Login to an Epirus account",
        showDefaultValues = true,
        abbreviateSynopsis = true,
        mixinStandardHelpOptions = true,
        versionProvider = EpirusVersionProvider.class,
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        optionListHeading = "%nOptions:%n",
        footerHeading = "%n",
        footer = "Epirus CLI is licensed under the Apache License 2.0")
public class LoginCommand implements Runnable {
    private static final IODevice console = new ConsoleDevice();

    @Override
    public void run() {
        String email = new InteractiveOptions().getEmail();
        AccountService accountService = new AccountService();
        String password = String.valueOf(console.readPassword("Please enter your password: "));
        if (accountService.authenticate(email, password)) {
            System.out.println("Successfully logged in");
        } else {
            Console.exitError(
                    "Server response did not contain the authentication token required to log in.");
        }
    }
}
