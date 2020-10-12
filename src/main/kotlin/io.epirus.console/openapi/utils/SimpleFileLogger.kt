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
package io.epirus.console.openapi.utils

import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

class SimpleFileLogger {
    companion object {
        val logFile = File("logs")
        val filePrintStream = PrintStream(logFile)

        init {
            logFile.createNewFile()
        }

        fun startLogging() {
            System.setOut(filePrintStream)
            System.setErr(filePrintStream)
            switchToConsole()
        }

        fun switchToConsole() {
            System.setOut(PrintStream(FileOutputStream(FileDescriptor.out)))
        }

        fun deleteLogging(): Boolean {
            return logFile.delete()
        }

        fun switchTo(p: PrintStream) {
            System.setOut(p)
        }
    }
}
