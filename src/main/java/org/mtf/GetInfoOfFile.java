package org.mtf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetInfoOfFile {
    public static double getEntropy(byte[] input) {
        Map<Byte, Integer> freqMap = new HashMap<>();

        for (byte b : input) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
        }

        double entropy = 0.0;
        int symbLength = input.length;

        for (int freq : freqMap.values()) {
            double prob = (double) freq / symbLength;
            entropy -= prob * (Math.log(prob) / Math.log(2));
        }

        return entropy;
    }

    public static double getCondEntropy(byte[] input) {
        Map<String, Integer> pairFreqMap = new HashMap<>();
        Map<Byte, Integer> singleFreqMap = new HashMap<>();
        for (int i = 0; i < input.length - 1; i++) {
            byte first = input[i];
            byte second = input[i + 1];
            String pair = first + "," + second;

            pairFreqMap.put(pair, pairFreqMap.getOrDefault(pair, 0) + 1);
            singleFreqMap.put(first, singleFreqMap.getOrDefault(first, 0) + 1);
        }

        double condEntropy = 0.0;
        int symLength = input.length - 1;

        for (Map.Entry<String, Integer> entry : pairFreqMap.entrySet()) {
            String[] pair = entry.getKey().split(",");
            byte first = Byte.parseByte(pair[0]);
            double jProb = (double) entry.getValue() / symLength;
            double condProb = jProb / ((double) singleFreqMap.get(first) / symLength);
            condEntropy -= jProb * (Math.log(condProb) / Math.log(2));
        }

        return condEntropy;
    }

    public static double getSecondCondEntropy(byte[] input) {
        Map<String, Integer> triplFreqMap = new HashMap<>();
        Map<String, Integer> pairFreqMap = new HashMap<>();

        for (int i = 0; i < input.length - 2; i++) {
            byte first = input[i];
            byte second = input[i + 1];
            byte third = input[i + 2];
            String triplet = first + "," + second + "," + third;
            String pair = first + "," + second;

            triplFreqMap.put(triplet, triplFreqMap.getOrDefault(triplet, 0) + 1);
            pairFreqMap.put(pair, pairFreqMap.getOrDefault(pair, 0) + 1);
        }

        double condEntropy = 0.0;
        int symbLength = input.length - 2;

        for (Map.Entry<String, Integer> entry : triplFreqMap.entrySet()) {
            String[] triplet = entry.getKey().split(",");
            String pair = triplet[0] + "," + triplet[1];
            double jProb = (double) entry.getValue() / symbLength;
            double condProb = jProb / ((double) pairFreqMap.get(pair) / symbLength);
            condEntropy -= jProb * (Math.log(condProb) / Math.log(2));
        }

        return condEntropy;
    }

    public static void getStatCompress(byte[] input, String outputFilePath) throws IOException {
        byte[] encoded = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(outputFilePath));
        double entropy = getEntropy(input);
        double entropyX = getCondEntropy(input);
        double entropyXX = getSecondCondEntropy(input);

        System.out.println("Энтропии исходного файла:");
        System.out.println("H(X) = " +  entropy);
        System.out.println("H(X|X) = " + entropyX);
        System.out.println("H(X|XX) = " + entropyXX);

        System.out.println("Размер закодированного файла " + encoded.length);
    }
}
