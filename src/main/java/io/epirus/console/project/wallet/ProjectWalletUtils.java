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
package io.epirus.console.project.wallet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.io.File.separator;

public class ProjectWalletUtils {

    public static final String DEFAULT_WALLET_LOOKUP_PATH =
            System.getProperty("user.home") + separator + ".epirus" + separator + "keystore";
    public static final String DEFAULT_WALLET_NAME = "TEST_WALLET.json";
    private final String customWalletPath;
    private final String customWalletName;
    private List<String> customWallets;

    public ProjectWalletUtils(String walletPath, String customWalletName) {
        this.customWalletPath = walletPath;
        this.customWalletName = customWalletName;
    }

    public ProjectWalletUtils() {
        this.customWalletPath = DEFAULT_WALLET_LOOKUP_PATH;
        this.customWalletName = DEFAULT_WALLET_NAME;
    }

    public List<String> getListOfGlobalWallets() {
        try {
            return Files.walk(Paths.get(customWalletPath))
                    .map(Path::toString)
                    .filter(f -> f.endsWith("json"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public boolean userHasGlobalWallets() {
        customWallets = getListOfGlobalWallets();
        return !customWallets.isEmpty();
    }

    public static boolean userHasGlobalWallet() {
        return new File(DEFAULT_WALLET_LOOKUP_PATH + separator + DEFAULT_WALLET_NAME).exists();
    }

    public static String getGlobalWalletAbsolutePath() {
        if (userHasGlobalWallet()) {
            return new File(DEFAULT_WALLET_LOOKUP_PATH + separator + DEFAULT_WALLET_NAME)
                    .getAbsolutePath();
        } else {
            return "";
        }
    }
}
