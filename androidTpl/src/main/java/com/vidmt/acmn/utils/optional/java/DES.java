package com.vidmt.acmn.utils.optional.java;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DES {

    /**
     * DES加解密
     * 
     * @param plainText
     *            要处理的byte[]
     * @param key
     *            密钥
     * @param mode
     *            模式
     * @return
     */
    private static byte[] coderByDES(byte[] plainText, String key, int mode)
            throws InvalidKeyException, InvalidKeySpecException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException,
            UnsupportedEncodingException {
        SecureRandom sr = new SecureRandom();
        byte[] resultKey = makeKey(key);
        DESKeySpec desSpec = new DESKeySpec(resultKey);
        SecretKey secretKey = SecretKeyFactory.getInstance("DES")
                .generateSecret(desSpec);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(mode, secretKey, sr);
        return cipher.doFinal(plainText);
    }

    /**
     * 生产8位的key
     * 
     * @param key
     *            字符串形式
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] makeKey(String key)
            throws UnsupportedEncodingException {
        byte[] keyByte = new byte[8];
        byte[] keyResult = key.getBytes("UTF-8");
        for (int i = 0; i < keyResult.length && i < keyByte.length; i++) {
            keyByte[i] = keyResult[i];
        }
        return keyByte;
    }

    /**
     * DES加密
     * 
     * @param plainText
     *            明文
     * @param key
     *            密钥
     * @return
     */
    public static String encoderByDES(String plainText, String key) {
        try {
            byte[] result = coderByDES(plainText.getBytes("UTF-8"), key,
                    Cipher.ENCRYPT_MODE);
            
            return HexUtil.toHexString(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * DES解密
     * 
     * @param secretText
     *            密文
     * @param key
     *            密钥
     * @return
     */
    public static String decoderByDES(String secretText, String key) {
        try {
            byte[] result = coderByDES(HexUtil.toByteArray(secretText), key,
                    Cipher.DECRYPT_MODE);
            return new String(result, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
    
    public static void main(String[] args){
        String key = "^&Dur78mn&8j23@#";
        String pass = "123456";
        String secrete = DES.encoderByDES(pass, key);
        String plain = DES.decoderByDES(secrete, key);
        System.out.println(plain);
    }
}
