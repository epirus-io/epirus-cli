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
package io.epirus.console.update;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.epirus.console.utils.OSUtils;
import io.epirus.console.utils.Version;
import okhttp3.*;

import static io.epirus.console.config.ConfigManager.config;

public class Updater {
    private static final String DEFAULT_UPDATE_URL =
            "https://internal.services.web3labs.com/api/epirus/versions/latest";

    public static void promptIfUpdateAvailable() throws IOException {
        String version = Version.getVersion();
        if (!config.getLatestVersion().equals(version)) {
            System.out.println(
                    String.format(
                            "Your current Epirus version is: "
                                    + version
                                    + ". The latest Version is: "
                                    + config.getLatestVersion()
                                    + ". To update, run: %s",
                            config.getUpdatePrompt()));
        }
    }

    public static void onlineUpdateCheck() {
        onlineUpdateCheck(DEFAULT_UPDATE_URL);
    }

    public static void onlineUpdateCheck(String updateUrl) {
        OkHttpClient client = new OkHttpClient();

        RequestBody updateBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("os", OSUtils.determineOS().toString())
                        .addFormDataPart("clientId", config.getClientId())
                        .addFormDataPart("data", "update_check")
                        .build();

        Request updateCheckRequest =
                new okhttp3.Request.Builder().url(updateUrl).post(updateBody).build();

        try {
            Response sendRawResponse = client.newCall(updateCheckRequest).execute();
            JsonElement element;
            if (sendRawResponse.code() == 200
                    && sendRawResponse.body() != null
                    && (element = JsonParser.parseString(sendRawResponse.body().string())) != null
                    && element.isJsonObject()) {
                JsonObject rootObj = element.getAsJsonObject().get("latest").getAsJsonObject();
                String latestVersion = rootObj.get("version").getAsString();
                if (!latestVersion.equals(Version.getVersion())) {
                    config.setLatestVersion(latestVersion);
                    config.setUpdatePrompt(
                            rootObj.get(
                                            OSUtils.determineOS() == OSUtils.OS.WINDOWS
                                                    ? "install_win"
                                                    : "install_unix")
                                    .getAsString());
                }
            }
        } catch (Exception ignored) {
        }
    }
}
