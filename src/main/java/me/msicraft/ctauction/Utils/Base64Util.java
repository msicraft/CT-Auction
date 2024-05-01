package me.msicraft.ctauction.Utils;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class Base64Util {

    public static String byteArrayToString(byte[] bytes) {
        return Base64Coder.encodeLines(bytes);
    }

    public static byte[] stringToByteArray(String byteString) {
        return Base64Coder.decodeLines(byteString);
    }

}
