package org.mtf;

import java.io.*;

public class EncMTF {

    public static boolean writeEncBytesToFile(byte[] inBytes, String outputFile) throws IOException {
        BinToFile bitWriter = new BinToFile(outputFile);
        double lengthSymAll = 0;
        for (byte b : inBytes) {
            int num = Byte.toUnsignedInt(b);
            String monCode = MonotonicEncDec.monotonicEncode(num);
            lengthSymAll += monCode.length();
            bitWriter.writeBits(monCode);
        }
        double cost = lengthSymAll / inBytes.length;
        System.out.println("Сред. длинна символа " + cost);
        bitWriter.close();
        return cost < 8;
    }

    // Пример запуска: java -jar enc.jar ./dataset/ ./out/
    // первый аргумент это путь к файлу или папке, второй опциональный - это в какую папку сохранить
    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                String inPath = args[0];
                String outDir = args[1];
                processFileOrDir(inPath, outDir);
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

                processFileOrDir(inPath, outDir);
            } else {
                throw new IllegalArgumentException("В качестве параметра на вход кодера подать имя сжимаемого файла.");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Ошибка FileNotFound: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка I/O: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка IllegalArgument: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void processFileOrDir(String inPath, String outputDir) throws IOException {
        File inFileDir = new File(inPath);
        File outDir = new File(outputDir);
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        if (inFileDir.isDirectory()) {
            File[] files = inFileDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().equalsIgnoreCase(".DS_Store")) {
                        compressFile(file, outDir);
                    }
                }
            }
        } else {
            compressFile(inFileDir, outDir);
        }
    }

    private static byte[] encode(byte[] input) {
        byte[] encOutput = new byte[input.length];
        byte[] dict = new byte[256];

        for (int i = 0; i < 256; i++) {
            dict[i] = (byte) i;
        }

        for (int i = 0; i < input.length; i++) {
            byte curByte = input[i];
            int pos = 0;
            for (int j = 0; j < 256; j++) {
                if (dict[j] == curByte) {
                    pos = j;
                    break;
                }
            }
            encOutput[i] = (byte) pos;

            System.arraycopy(dict, 0, dict, 1, pos);
            dict[0] = curByte;
        }
        return encOutput;
    }


    private static void compressFile(File inFile, File outDir) throws IOException {
        System.out.println("Сжатие файла: " + inFile.getName());
        String inputFilePath = inFile.getAbsolutePath();
        String outputFilePath = new File(outDir, inFile.getName() + ".csb").getAbsolutePath();

        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(inputFilePath));
        System.out.println("Размер файла в байтах: " + bytes.length);
        byte[] encBytes = encode(bytes);
        boolean useful = writeEncBytesToFile(encBytes, outputFilePath);
        GetInfoOfFile.getStatCompress(bytes, outputFilePath);
        if (!useful) {
            // Если средняя длина кода больше 8, то файл станет весить больше, чем был
            System.out.println("Файл не сжался, но закодирован " + inFile.getName() + ". Сохранён " + outputFilePath);
        } else {
            System.out.println("Файл сжат " + inFile.getName() + ". Сохранён " + outputFilePath);
        }
        System.out.println();
    }
}
