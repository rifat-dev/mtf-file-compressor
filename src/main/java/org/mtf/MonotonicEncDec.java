package org.mtf;

import java.util.LinkedHashMap;
import java.util.Map;

// кодирование монотонным кодом
// формулу взял из книги Кудряшова Теория Информации стр. 130
public class MonotonicEncDec {

    public static String unaryEncode(int num) {
        return "1".repeat(Math.max(0, num)) + "0";
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
