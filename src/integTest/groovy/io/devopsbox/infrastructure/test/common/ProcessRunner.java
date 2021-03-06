package io.devopsbox.infrastructure.test.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ProcessRunner {
    private static final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

    public int run(String... command) {
        return run(new HashMap<>(), command);
    }

    public int run(Map<String, String> environmentVariables, String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        if (environmentVariables != null) {
            processBuilder.environment().putAll(environmentVariables);
        }

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Cannot start process " + Arrays.toString(command), e);
        }

        try {
            log.debug("Waiting for external process to end...");
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Process " + Arrays.toString(command) + " interrupted!", e);
        }

        int exitValue = process.exitValue();
        log.debug("External process finished with exit code {}", exitValue);
        return exitValue;
    }

    public String runWithOutput(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Cannot start process " + Arrays.toString(command), e);
        }

        String lineSeparator = System.getProperty("line.separator");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringJoiner sj = new StringJoiner(lineSeparator);
        reader.lines().iterator().forEachRemaining(sj::add);
        String result = sj.toString();

        try {
            log.debug("Waiting for external process to end...");
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Process " + Arrays.toString(command) + " interrupted!", e);
        }

        int exitValue = process.exitValue();
        if (exitValue != 0) {
            throw new RuntimeException("External process error, exit code " + exitValue);
        }
        return result;
    }
}
