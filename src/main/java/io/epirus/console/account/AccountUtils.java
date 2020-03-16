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
package io.epirus.console.account;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.epirus.console.config.CliConfig;
import io.epirus.console.project.InteractiveOptions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.web3j.codegen.Console;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class AccountUtils {

    static OkHttpClient okHttpClient = new OkHttpClient();

    public static void accountInit() throws IOException {
        if (InteractiveOptions.configFileExists()) {
            if (!InteractiveOptions.userHasEpirusAccount()) {
                if (InteractiveOptions.userWantsEpirusAccount()) {
                    AccountManager.main(
                            CliConfig.getConfig(CliConfig.getEpirusConfigPath().toFile()),
                            new String[] {"create"});
                }
            }
        } else {
            if (InteractiveOptions.userWantsEpirusAccount()) {
                AccountManager.main(
                        CliConfig.getConfig(CliConfig.getEpirusConfigPath().toFile()),
                        new String[] {"create"});
            }
        }
    }

    public static void checkIfUserConfirmedAccount() throws IOException, InterruptedException {
        CliConfig cliConfig =
                CliConfig.getConfig(
                        new File(
                                String.valueOf(
                                        Paths.get(
                                                System.getProperty("user.home"),
                                                ".epirus",
                                                ".config"))));
        Request request =
                new Request.Builder()
                        .url(
                                AccountManager.CLOUD_URL
                                        + "/auth/realms/EpirusPortal/web3j-token/status/"
                                        + cliConfig.getLoginToken())
                        .get()
                        .build();
        System.out.println("Checking if the account is activated...");
        int tries = 5;
        while (tries-- > 0) {
            if (userConfirmedAccount(request)) {
                return;
            } else {
                Thread.sleep(5000);
            }
        }
    }

    private static boolean userConfirmedAccount(Request request) throws IOException {
        Response response = okHttpClient.newCall(request).execute();
        if (response.code() != 200) {
            Console.exitError(response.message());
        }
        ResponseBody responseBody = response.body();
        assert responseBody != null;
        JsonObject responseJsonObj =
                JsonParser.parseString(responseBody.string()).getAsJsonObject();
        return responseJsonObj.get("active").getAsBoolean();
    }

    public static BigInteger getAccountBalance(Credentials credentials, Network network)
            throws Exception {
        BigInteger accountBalance =
                Web3j.build(Network.valueOf(network.getNetworkName().toUpperCase()))
                        .ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                        .send()
                        .getBalance();
        if (accountBalance == null) {
            return BigInteger.ZERO;
        }
        return accountBalance;
    }
}
