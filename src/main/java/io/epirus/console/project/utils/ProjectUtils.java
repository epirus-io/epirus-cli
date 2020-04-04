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
package io.epirus.console.project.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import com.diogonunes.jcdp.color.api.Ansi;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.RandomStringUtils;

import org.web3j.codegen.Console;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Network;

import static io.epirus.console.utils.PrinterUtilities.printErrorAndExit;
import static io.epirus.console.utils.PrinterUtilities.printInformationPairWithStatus;

public class ProjectUtils {

    public static void deleteFolder(Path directoryToDeleted) {
        try {
            Files.walk(directoryToDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(ProjectUtils::deleteFile);
        } catch (IOException e) {
            Console.exitError(e);
        }
    }

    static void deleteFile(File fileToDelete) {
        if (!fileToDelete.delete()) {
            Console.exitError(
                    "Could not delete "
                            + fileToDelete.getName()
                            + " at location "
                            + fileToDelete.getPath());
        }
    }

    public static String generateWalletPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    public static Optional<Path> loadProjectWalletFile(Path path) {
        String pathToProjectResources =
                String.join(File.separator, path.toString(), "src", "test", "resources", "wallet");
        try {
            return Optional.of(
                    Files.list(Paths.get(pathToProjectResources))
                            .filter(f -> f.getFileName().toString().endsWith("json"))
                            .collect(Collectors.toList())
                            .get(0));
        } catch (IOException e) {
            Console.exitError(
                    "Could not load wallet file. Make sure you are in the right directory.");
            return Optional.empty();
        }
    }

    public static Optional<Path> loadProjectPasswordWalletFile(Path path) {
        String pathToProjectResources =
                String.join(File.separator, path.toString(), "src", "test", "resources", "wallet");
        try {
            return Optional.of(
                    Files.list(Paths.get(pathToProjectResources))
                            .filter(f -> f.getFileName().toString().endsWith("-Password"))
                            .collect(Collectors.toList())
                            .get(0));
        } catch (IOException e) {
            Console.exitError(
                    "Could not load wallet password file. Make sure you are in the right directory: "
                            + e.getMessage());

            return Optional.empty();
        }
    }

    public static Credentials createCredentials(Path walletPath, Path walletPasswordPath) {
        String walletPassword = null;
        try {
            walletPassword = new String(Files.readAllBytes(walletPasswordPath));
        } catch (IOException e) {
            Console.exitError("Could not read password file: " + e.getMessage());
        }
        try {
            return WalletUtils.loadCredentials(walletPassword, walletPath.toFile());
        } catch (IOException e) {
            Console.exitError("Could not create credentials: " + e.getMessage());
        } catch (CipherException e) {
            Console.exitError(e.getMessage());
        }
        return null;
    }

    public static void uploadSolidityMetadata(Network network, Path workingDirectory) {
        File pathToMetadata =
                new File(
                        String.join(
                                File.separator,
                                workingDirectory.toString(),
                                "build",
                                "resources",
                                "main",
                                "solidity"));
        if (pathToMetadata.exists()) {
            Arrays.stream(pathToMetadata.listFiles())
                    .filter(file -> file.getName().contains("_meta"))
                    .forEach(
                            file -> {
                                try {
                                    uploadFile(file, network);
                                } catch (IOException e) {
                                    printErrorAndExit(e.getMessage());
                                }
                            });
            printInformationPairWithStatus("Uploading metadata", 20, "DONE", Ansi.FColor.GREEN);
            System.out.print(System.lineSeparator());
        } else {
            printInformationPairWithStatus("Uploading metadata", 20, "FAILED", Ansi.FColor.RED);
            System.out.print(System.lineSeparator());
            printErrorAndExit(
                    "Could not find the metadata files in :" + pathToMetadata.getAbsolutePath());
        }
    }

    private static void uploadFile(File file, Network network) throws IOException {
        String uploadURL = "https://" + network.getNetworkName() + ".api.epirus.io/metadata";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "file",
                                file.getName(),
                                RequestBody.create(file, MediaType.parse("application/json")))
                        .build();
        Request request = new Request.Builder().url(uploadURL).post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.execute();
    }
}
