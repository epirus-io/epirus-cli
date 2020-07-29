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
package io.epirus.console.wrapper;

import java.nio.file.Path;

import picocli.CommandLine;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

public class CredentialsOptions {
    @CommandLine.Option(
            names = {"-w", "--wallet-path"},
            description = "Wallet file path.",
            showDefaultValue = ALWAYS)
    private Path walletPath;

    @CommandLine.Option(
            names = {"-k", "--wallet-password"},
            description = "Wallet password.",
            showDefaultValue = ALWAYS)
    private String walletPassword = "";

    @CommandLine.Option(
            names = {"-r", "--private-key"},
            description = "Raw hex private key.",
            showDefaultValue = ALWAYS)
    private String rawKey = "";

    @CommandLine.Option(
            names = {"-j", "--wallet-json"},
            description = "JSON wallet.",
            showDefaultValue = ALWAYS)
    private String json = "";

    public Path getWalletPath() {
        return walletPath;
    }

    public String getWalletPassword() {
        return walletPassword;
    }

    public String getRawKey() {
        return rawKey;
    }

    public String getJson() {
        return json;
    }
}
