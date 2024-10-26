package org.mtf;

import java.util.LinkedHashMap;
import java.util.Map;

// кодирование монотонным кодом
// формулу взял из книги Кудряшова Теория Информации стр. 130
public class MonotonicEncDec {

    public static String unaryEncode(int num) {
        // сделал под java 1.8
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append("1");
        }
        sb.append("0");
        //return "1".repeat(Math.max(0, num)) + "0"; // для java 11
        return sb.toString();
    }

    public static String monotonicEncode(int num) {
        String binary = Integer.toBinaryString(num + 1).substring(1);
        int len = binary.length();
        return unaryEncode(len) + binary;
    }

    // для декодера
    public static Map<String, Integer> getPrefixCodeMap(int maxNumber) {
        Map<String, Integer> prefixCodeMap = new LinkedHashMap<>();
        for (int i = 0; i <= maxNumber; i++) {
            prefixCodeMap.put(monotonicEncode(i), i);
        }
        return prefixCodeMap;
    }
}
