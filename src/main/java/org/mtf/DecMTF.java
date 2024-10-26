package org.mtf;

import java.io.*;
import java.util.*;

import static org.mtf.MonotonicEncDec.getPrefixCodeMap;

public class DecMTF {

    private static String reverseByte(int b) {
        StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(b));

        while (binaryString.length() < 8) {
            binaryString.insert(0, "0");
        }

        return binaryString.reverse().toString();
    }

    // Пример запуска: java -jar dec.jar ./out/ ./dec-out/
    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                String inPath = args[0];
                String outDir = args[1];
                processFileDir(inPath, outDir);
            } else if (args.length == 1) {
                String inPath = args[0];
                File inFileOrDir = new File(inPath);

                if (!inFileOrDir.exists()) {
                    throw new FileNotFoundException("не существует: " + inPath);
                }

                String outDir = "";
                if (inFileOrDir.isFile()) {
                    outDir = inFileOrDir.getParent();
                } else {
                    outDir = inFileOrDir.getPath() + "/";
                }

                processFileDir(inPath, outDir);
            } else {
                throw new IllegalArgumentException("Неверное количество аргументов.");
            }
        } catch (FileNotFoundException | IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processFileDir(String inputPath, String outputDir) throws IOException {
        File inputPathFileOrDir = new File(inputPath);
        File outputPathDir = new File(outputDir);

        if (!outputPathDir.exists()) {
            outputPathDir.mkdirs();
        }

        if (inputPathFileOrDir.isDirectory()) {
            File[] files = inputPathFileOrDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().equalsIgnoreCase(".DS_Store")) {
                        decodeFileBin(file, outputPathDir);
                    }
                }
            }
        } else {
            decodeFileBin(inputPathFileOrDir, outputPathDir);
        }
    }

    private static void decodeFileBin(File inputFile, File outputDir) throws IOException {
        System.out.println("Начало декодирования: " + inputFile.getName());
        String inputFilePath = inputFile.getAbsolutePath();
        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(inputFilePath));

        Map<String, Integer> prefixCodeMap = getPrefixCodeMap(257);
        List<Integer> decodedValues = new ArrayList<>();
        StringBuilder bitBuffer = new StringBuilder();
        int preLastByte = Byte.toUnsignedInt(bytes[bytes.length - 2]);
        int lastByte = Byte.toUnsignedInt(bytes[bytes.length - 1]);

        for (int i = 0; i < bytes.length - 2; i++) {
            int currentByte = Byte.toUnsignedInt(bytes[i]);
            String reversedBinStr = reverseByte(currentByte);
            char[] charSeq = reversedBinStr.toCharArray();
            for (char bit : charSeq) {
                bitBuffer.append(bit);
                String currentBitSequence = bitBuffer.toString();
                if (i < bytes.length - 2 && prefixCodeMap.containsKey(currentBitSequence)) {
                    decodedValues.add(prefixCodeMap.get(currentBitSequence));
                    bitBuffer.setLength(0);
                }
            }
        }

        if (lastByte == 0) {
            bitBuffer.setLength(0);
        } else {
            String preLastBinary = reverseByte(preLastByte);
            char[] preLastBits = preLastBinary.toCharArray();
            for (int i = 0; i < lastByte; i++) {
                bitBuffer.append(preLastBits[i]);
                String curBitSeq = bitBuffer.toString();
                if (prefixCodeMap.containsKey(curBitSeq)) {
                    decodedValues.add(prefixCodeMap.get(curBitSeq));
                    bitBuffer.setLength(0);
                }
            }
        }

        int[] intArray = decodedValues.stream().mapToInt(Integer::intValue).toArray();
        byte[] byteArr = new byte[decodedValues.size()];
        for (int i = 0; i < intArray.length; i++) {
            byteArr[i] = (byte) intArray[i];
        }

        byte[] decDyte = decode(byteArr);
        String outputFileName = inputFile.getName().replace(".csb", "");
        String outputFilePath = new File(outputDir, outputFileName).getAbsolutePath();

        try (FileOutputStream fileOutStream = new FileOutputStream(outputFilePath)) {
            fileOutStream.write(decDyte);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Размер файла после декодирования в байтах: " + decDyte.length);
        System.out.println("Декодирование завершено и сохранено: " + outputFilePath + "\n");
    }

    private static byte[] decode(byte[] inBytes) {
        byte[] output = new byte[inBytes.length];
        byte[] dict = new byte[256];

        for (int i = 0; i < 256; i++) {
            dict[i] = (byte) i;
        }

        for (int i = 0; i < inBytes.length; i++) {
            byte j = inBytes[i];
            output[i] = dict[Byte.toUnsignedInt(j)];
            for (int k = Byte.toUnsignedInt(j) - 1; k >= 0; k--) {
                dict[k + 1] = dict[k];
            }
            dict[0] = output[i];
        }
        return output;
    }
}
