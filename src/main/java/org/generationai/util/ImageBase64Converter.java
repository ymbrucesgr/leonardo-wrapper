package org.generationai.util;

import java.io.*;
import java.util.Base64;

public class ImageBase64Converter {

    public static boolean getImgBase64ToImgFile(String imgBase64, String imgPath) {
        boolean flag = true;
        OutputStream outputStream = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(imgBase64);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
            }
            outputStream = new FileOutputStream(imgPath);
            outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

}
