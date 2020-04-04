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
package io.epirus.console.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.google.gson.Gson;

public class ConfigManager {
    protected static final Path DEFAULT_EPIRUS_CONFIG_PATH =
            Paths.get(System.getProperty("user.home"), ".epirus", ".config");

    public static CliConfig config;

    static {
        try {
            config = getConfig(DEFAULT_EPIRUS_CONFIG_PATH.toFile());
        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }

    private static CliConfig initializeDefaultConfig(File configFile) throws IOException {
        File epirusHome = new File(configFile.getParent());
        if (!epirusHome.exists() && !epirusHome.mkdirs()) {
            throw new IOException("Failed to create Epirus home directory");
        }
        return new CliConfig(UUID.randomUUID().toString(), null, null, null, false);
    }

    private static CliConfig getSavedConfig(File configFile) throws IOException {
        String configContents = new String(Files.readAllBytes(configFile.toPath()));
        return new Gson().fromJson(configContents, CliConfig.class);
    }

    private static CliConfig getConfig(File configFile) throws IOException {
        if (configFile.exists()) {
            return getSavedConfig(configFile);
        } else {
            return initializeDefaultConfig(configFile);
        }
    }
}
