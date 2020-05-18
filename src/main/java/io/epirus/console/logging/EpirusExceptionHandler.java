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
package io.epirus.console.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpirusExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(EpirusExceptionHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.info(
                "Epirus encountered an unexpected error. Please see the log file for more details.");
    }
}
