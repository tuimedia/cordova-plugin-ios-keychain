package com.humanpractice.cordova.keystorage;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

final class Encryption {
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String UTF_8 = "UTF-8";

    static String decrypt(byte[] value, PrivateKey privateKey) throws GeneralSecurityException, IOException {
        if (value == null || value.length == 0) {
          throw new IOException("Cannot decrypt a null or empty value");
        }

        Cipher decryption = Cipher.getInstance(RSA_ALGORITHM);
        decryption.init(Cipher.DECRYPT_MODE, privateKey);

        CipherInputStream inputStream = new CipherInputStream(new ByteArrayInputStream(value), decryption);

        ArrayList values = new ArrayList();
        int nextByte;
        while ((nextByte = inputStream.read()) != -1) {
            values.add(new Byte((byte) nextByte));
        }
        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = ((Byte) values.get(i)).byteValue();
        }

        return new String(bytes, 0, bytes.length, UTF_8);
    }

    static byte[] encrypt(String value, PublicKey publicKey) throws GeneralSecurityException, IOException {
        Cipher encryption = Cipher.getInstance(RSA_ALGORITHM);
        encryption.init(Cipher.ENCRYPT_MODE, publicKey);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CipherOutputStream cipherOutput = null;
        try {
            cipherOutput = new CipherOutputStream(output, encryption);
            cipherOutput.write(value.getBytes(UTF_8));
        } finally {
            if (cipherOutput != null) {
                try {
                    cipherOutput.close();
                } catch (IOException ignored) {
                }
            }
        }

        return output.toByteArray();
    }
}
