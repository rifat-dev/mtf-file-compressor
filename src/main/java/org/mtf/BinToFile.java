package org.mtf;

import java.io.FileOutputStream;
import java.io.IOException;

public class BinToFile {
    private final FileOutputStream file;
    private int buf = 0;
    private int bitPos = 0;

    public BinToFile(String filePath) throws IOException {
        this.file = new FileOutputStream(filePath);
    }

    public void writeBits(String bits) throws IOException {
        char[] arrBits = bits.toCharArray();
        for (char arrBit : arrBits) {
            int intVal = (arrBit == '1') ? 1 : 0;
            buf |= intVal << bitPos;
            bitPos++;

            if (bitPos == 8) {
                file.write(buf);
                buf = 0;
                bitPos = 0;
            }
        }
    }

    public void close() throws IOException {
        file.write(buf);
        //System.out.println("close " + Integer.toBinaryString(buf) + " bit pos: " + bitPos);
        buf = 0;
        // тут я в последний байт записываю, сколько символов нужно считать с предпоследнего байта декодеру
        file.write(bitPos);
        bitPos = 0;
        file.close();
    }
}
