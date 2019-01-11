package com.ood.libraryManagementSystem.service;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.apache.commons.codec.binary.Hex.decodeHex;

@Service
public class KeyService {
    private Cipher ecipher;
    private Cipher dcipher;
    private SecretKey key;
    private String keyString = "37c1b0989851cea2";

    private static final Logger logger = LoggerFactory.getLogger(KeyService.class);

    @Autowired
    public KeyService() {
        try {
            key = new SecretKeySpec(decodeHex(keyString.toCharArray()), "DES");

            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");

            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            logger.error("Something happened in construction: " + e.toString());
        }
    }

    public String encrypt(String str) {
        try {
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            enc = BASE64EncoderStream.encode(enc);
            return new String(enc);
        } catch (Exception e) {
            logger.error("Something happened in encryption: " + e.toString());
        }

        return "";
    }

    public String decrypt(String str) {
        try {
            byte[] dec = BASE64DecoderStream.decode(str.getBytes());
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, "UTF8");
        } catch (Exception e) {
            logger.error("Something happened in decryption: " + e.toString());
        }

        return "";
    }
}
