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

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;

import com.diogonunes.jcdp.color.api.Ansi;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.epirus.console.project.InteractiveOptions;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.web3j.codegen.Console;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Network;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import static io.epirus.console.config.ConfigManager.config;
import static io.epirus.console.utils.PrinterUtilities.printErrorAndExit;
import static io.epirus.console.utils.PrinterUtilities.printInformationPairWithStatus;
import static org.web3j.codegen.Console.exitError;

public class AccountManager implements Closeable {
    private static final String USAGE = "account login"; // soon we'll add logout|create
    public static final String DEFAULT_CLOUD_URL = "https://auth.epirus.io";
    private static final String DEFAULT_REALM = "EpirusPortal";
    private final String realm;
    private final String cloudURL;
    private final OkHttpClient client;

    public static void main(final String[] args) {
        if (args.length > 0 && "create".equals(args[0])) {
            String email = new InteractiveOptions().getEmail();
            AccountManager accountManager = new AccountManager(new OkHttpClient());
            accountManager.createAccount(email);
            accountManager.close();
        } else {
            exitError(USAGE);
        }
    }

    @VisibleForTesting
    public AccountManager(OkHttpClient client) {
        this.client = client;
        this.cloudURL = DEFAULT_CLOUD_URL;
        this.realm = DEFAULT_REALM;
    }

    @VisibleForTesting
    public AccountManager(OkHttpClient client, String cloudURL, String realm) {
        this.client = client;
        this.cloudURL = cloudURL;
        this.realm = realm;
    }

    public void createAccount(String email) {
        RequestBody requestBody = createRequestBody(email);
        Request newAccountRequest = createRequest(requestBody);
        try {
            Response sendRawResponse = executeClientCall(newAccountRequest);
            ResponseBody body;
            if (sendRawResponse.code() == 200 && (body = sendRawResponse.body()) != null) {
                String rawResponse = body.string();
                JsonObject responseJsonObj = JsonParser.parseString(rawResponse).getAsJsonObject();
                if (responseJsonObj.get("token") == null) {
                    String tokenError = responseJsonObj.get("tokenError").getAsString();
                    if (tokenError == null || tokenError.isEmpty()) {
                        Console.exitError("Could not retrieve token. Try again later.");
                    } else {

                        printErrorAndExit(tokenError);
                    }
                    return;
                }
                String token = responseJsonObj.get("token").getAsString();
                config.setLoginToken(token);
                System.out.println(
                        "Account created successfully. You can now use Epirus Cloud. Please confirm your e-mail within 24 hours to continue using all features without interruption.");
            } else {
                printErrorAndExit("Account creation failed. Please try again later.");
            }

        } catch (IOException e) {
            printErrorAndExit("Could not connect to the server.\nReason:" + e.getMessage());
        }
    }

    private final Response executeClientCall(Request newAccountRequest) throws IOException {
        return client.newCall(newAccountRequest).execute();
    }

    final RequestBody createRequestBody(String email) {

        return new FormBody.Builder().add("email", email).build();
    }

    final Request createRequest(RequestBody accountBody) {

        return new Request.Builder()
                .url(String.format("%s/auth/realms/%s/web3j-token/create", cloudURL, realm))
                .post(accountBody)
                .build();
    }

    public void checkIfAccountIsConfirmed() throws IOException, InterruptedException {
        Request request =
                new Request.Builder()
                        .url(
                                cloudURL
                                        + "/auth/realms/EpirusPortal/web3j-token/status/"
                                        + config.getLoginToken())
                        .get()
                        .build();
        int tries = 10;
        while (tries-- > 0) {
            if (userConfirmedAccount(request)) {
                printInformationPairWithStatus("Account status", 20, "ACTIVE ", Ansi.FColor.GREEN);
                System.out.print(System.lineSeparator());

                return;
            } else {
                printInformationPairWithStatus(
                        "Account status", 20, "PENDING ", Ansi.FColor.YELLOW);
            }

            Thread.sleep(5000);
        }
        printErrorAndExit(
                "Please check your email and activate your account in order to take advantage our features. Once your account is activated you can re-run the command.");
    }

    private boolean userConfirmedAccount(Request request) throws IOException {

        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        if (response.code() != 200 || responseBody == null) {
            Console.exitError(response.message());
        }
        String responseBodyString = responseBody.string();
        if (responseBodyString.equals("Invalid request")) {
            printErrorAndExit("Could not check if account has been confirmed");
        }
        JsonObject responseJsonObj = JsonParser.parseString(responseBodyString).getAsJsonObject();
        return responseJsonObj.get("active").getAsBoolean();
    }

    public BigInteger getAccountBalance(Credentials credentials, Web3j web3j) {
        int count = 0;
        int maxTries = 5;
        while (true) {
            try {
                EthGetBalance accountBalance =
                        web3j.ethGetBalance(
                                        credentials.getAddress(), DefaultBlockParameterName.LATEST)
                                .send();
                if (accountBalance.getError() == null) {
                    return accountBalance.getBalance();
                }

            } catch (Exception e) {
                if (++count == maxTries) {
                    printErrorAndExit(e.getMessage());
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public BigInteger pollForAccountBalance(
            Credentials credentials, Network network, Web3j web3j, int numberOfBlocksToCheck)
            throws IOException {
        BigInteger accountBalance = null;
        BigInteger startBlock = web3j.ethBlockNumber().send().getBlockNumber();
        BigInteger stopBlock = startBlock.add(BigInteger.valueOf(numberOfBlocksToCheck));
        while (web3j.ethBlockNumber().send().getBlockNumber().compareTo(stopBlock) < 0) {
            try {
                accountBalance =
                        Web3j.build(Network.valueOf(network.getNetworkName().toUpperCase()))
                                .ethGetBalance(
                                        credentials.getAddress(), DefaultBlockParameterName.LATEST)
                                .send()
                                .getBalance();
                if (accountBalance.compareTo(BigInteger.ZERO) > 0) {
                    return accountBalance;
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                printErrorAndExit("Could not check the account balance." + e.getMessage());
            }
        }
        return accountBalance;
    }

    public String getLoginToken() {
        return config.getLoginToken();
    }

    @Override
    public void close() {
        this.client.dispatcher().executorService().shutdown();
        this.client.connectionPool().evictAll();
    }
}
