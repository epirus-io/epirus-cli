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
package io.epirus.console.wallet;

import io.epirus.console.utils.ConsoleDevice;

import org.web3j.codegen.Console;

import static org.web3j.utils.Collection.tail;

/** Class for managing our wallet command line utilities. */
public class WalletRunner {
    private static final String USAGE = "wallet create|update|send|fromkey|fund";

    public static void main(String[] args) {
        if (args.length < 1) {
            Console.exitError(USAGE);
        } else {
            switch (args[0]) {
                case "create":
                    WalletCreator.main(new String[] {});
                    break;
                case "update":
                    WalletUpdater.main(tail(args));
                    break;
                case "send":
                    WalletSendFunds.main(tail(args));
                    break;
                case "fromkey":
                    KeyImporter.main(tail(args));
                    break;
                case "fund":
                    WalletFunder.main(new ConsoleDevice(), tail(args));
                    break;
                default:
                    Console.exitError(USAGE);
            }
        }
    }
}
