package com.tuimedia.cordova.keystorage;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

import org.apache.cordova.CordovaInterface;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

/**
 * Secure storage component
 */
public final class SecureStorage extends Logged {
    private static final String ANDROID = "AndroidKeyStore";
    private static final String BOUNCY_CASTLE = "AndroidKeyStoreBCWorkaround";
    private static final String OPEN_SSL = "AndroidOpenSSL";
    private static final int KEYPAIR_VALIDITY_IN_YEARS = 30;

    private final CordovaInterface cordova;
    private final FileStorage fileStorage;
    private final String keyStoreType;

    public SecureStorage(CordovaInterface cordova) {
        this.cordova = cordova; // this first, for getContext()
        this.fileStorage = new FileStorage(getContext());
        this.keyStoreType = getKeyStoreType();
    }

    /**
     * Returns the string value for the given key
     *
     * Will return unecrypted value from local cache if present,
     * otherwise retrieves encrypted value from shared preferences and unencrypts before returning it
     *
     * @param key   The key of the value to return
     * @return      The unencrypted value or null if not found
     */
    public String getValue(final String key) {
        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(key, null);
            return Encryption.decrypt(fileStorage.readValue(key), privateKey);
        } catch (IOException e) {
            logException(e);
            throw new ReportableException(e);
        } catch (GeneralSecurityException e) {
            logException(e);
            throw new ReportableException(e);
        }
    }

    /**
     * Sets the string value for the given key
     *
     * Encrypts the value and stores in shared preferences and stores the un-encrypted value
     * in the local cache
     *
     * @param key   The key of the value to set
     * @param value The value to set
     */
    public void setValue(final String key, final String value) {
        if (value.isEmpty()) {
            logDebug("Validation error: Input text is empty");
            return;
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null);
            fileStorage.writeValue(key, Encryption.encrypt(value, getPublicKey(key, keyStore)));
            logInfo("Key created and stored successfully");
        } catch (IOException e) {
            logException(e);
            throw new ReportableException(e);
        } catch (GeneralSecurityException e) {
            logException(e);
            throw new ReportableException(e);
        }
    }

    /**
     * Remove the value for the given key from shared preferences and the local cache
     *
     * @param key   The key of the value to remove
     */
    public void removeValue(final String key) {
        fileStorage.resetValue(key);
        logInfo("keys removed successfully");
    }

    private PublicKey createNewKey(String alias) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, KEYPAIR_VALIDITY_IN_YEARS);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(getContext()).setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", keyStoreType);
        generator.initialize(spec);

        // This also saves the keypair in the Android KeyStore
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair.getPublic();
    }

    private PublicKey getPublicKey(String key, KeyStore keyStore) throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        PublicKey publicKey = null;

        if (!keyStore.containsAlias(key)) {
            publicKey = createNewKey(key);
            logInfo("created new key pair");
        } else {
            publicKey = keyStore.getCertificate(key).getPublicKey();
        }
        return publicKey;
    }

    private static String getKeyStoreType() {
        try {
            KeyStore.getInstance(ANDROID);
            return ANDROID;
        } catch (KeyStoreException e) {
            try {
                KeyStore.getInstance(BOUNCY_CASTLE);
                return BOUNCY_CASTLE;
            } catch (KeyStoreException e1) {
                return OPEN_SSL;
            }
        }
    }

    private Context getContext() {
        return cordova.getActivity().getApplicationContext();
    }
}
