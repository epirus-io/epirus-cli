package io.epirus.console.wallet;

import picocli.CommandLine;

public abstract class SubCommand implements Runnable {
    @Override
    public void run() {
        if (isHelpRequired()) {
            CommandLine.usage(this, System.out);
        }
    }

    public boolean isHelpRequired() {
        return true;
    }
}
