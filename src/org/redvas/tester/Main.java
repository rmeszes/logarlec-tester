package org.redvas.tester;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

    private static int testsRun = 0;

    private static int testsSuccessful = 0;

    public static void main(String[] args) throws IOException, InterruptedException {

        File toDelete = new File("results.txt"); //delete previous results
        if(toDelete.exists()){
            toDelete.delete();
        }

        if(args.length > 0) {
            runTest(args[0]);
        } else {
            File testsFolder = new File("./test_inputs");
            for(File file : Objects.requireNonNull(testsFolder.listFiles())) {
                String filename = file.getName();
                if(filename.contains(".txt")) {
                    runTest(filename.replaceFirst(".txt",""));
                }
            }
        }

        try(FileWriter resultFile = new FileWriter("results.txt",true)) {
            resultFile.append("\nTests success/Tests ran: ").append(String.valueOf(testsSuccessful)).append(String.valueOf('/')).append(String.valueOf(testsRun));
        }

        System.out.println("Tests success/Tests ran: " + testsSuccessful + '/' + testsRun);

        if(testsSuccessful != testsRun) System.exit(1);
    }

    private static void runTest(String testNum) throws IOException, InterruptedException {
        testsRun++;

        String command = "java -jar target/logarlec-proto-1.0.jar";

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("cmd", "/c", command);

        File outputFile = new File( "test_outputs/" + testNum + ".txt");
        processBuilder.redirectOutput(outputFile);
        processBuilder.redirectError(outputFile);
        processBuilder.redirectInput(new File("test_inputs/" + testNum + ".txt"));

        Process process = processBuilder.start();

        /*int exitCode = */process.waitFor(); //meg kell várni, mert nem fog fájlba írni

        //System.out.println("test " + testNum +" exited with code: " + exitCode);

        //This is where test ends, time for checks.

        runChecks(testNum);
    }

    private static void runChecks(String testNum) throws IOException {
        HashMap<String,Boolean> checks = HashMap.newHashMap(40);

        try(BufferedReader reader = new BufferedReader(new FileReader("test_checks/" + testNum + ".txt"))) {
            String line;
            while((line = reader.readLine()) != null) {
                checks.put(line, line.charAt(0) != '!');
            } //fills hashmap with checks
        }

        StringBuilder outputFile = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader("test_outputs/" + testNum + ".txt"))) {

            String line;
            while((line = reader.readLine()) != null) {
                outputFile.append(line);
            }
        }


        try(FileWriter resultFile = new FileWriter("results.txt",true)) {

            for (Map.Entry<String, Boolean> check : checks.entrySet()) {
                if (check.getValue().equals(true) && !outputFile.toString().contains(check.getKey())) {
                    System.out.println("test " + testNum + " failed");
                    resultFile.append("test ").append(testNum).append(" failed").append('\n');
                    System.out.println("Check: " + check.getKey());
                    resultFile.append("Check: ").append(check.getKey()).append('\n');
                    return;
                } else if (check.getValue().equals(false) && outputFile.toString().contains(check.getKey().replaceFirst("!", ""))) {
                    System.out.println("test " + testNum + " failed");
                    resultFile.append("test ").append(testNum).append(" failed").append('\n');
                    System.out.println("Check: " + check.getKey());
                    resultFile.append("Check: ").append(check.getKey()).append('\n');
                    return;
                }
            }

            testsSuccessful++;
            System.out.println("test " + testNum + " success");
            resultFile.append("test ").append(testNum).append(" success").append('\n');
        }
    }
}
