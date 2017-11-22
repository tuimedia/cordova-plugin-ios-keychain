package com.tuimedia.cordova.keystorage;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// read/write/remove the file associated with a key

final class FileStorage extends Logged {
    private static final String FILENAME = "CORDOVA_PLUGIN_KEY_STORAGE_FILE";

    private Context context;

    FileStorage(Context context) {
        this.context = context;
    }

    byte[] readValue(String key) {
        FileInputStream stream = null;

        try {
            stream = context.openFileInput(FILENAME + key);

            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            while ((bytesRead = stream.read(buffer)) != -1) {
                bytes.write(buffer, 0, bytesRead);
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            logException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return new byte[0];
    }

    void writeValue(String key, byte[] value) {
        FileOutputStream output = null;
        try {
            output = context.openFileOutput(FILENAME + key, context.MODE_PRIVATE);
            output.write(value);
        } catch (IOException e) {
            logException(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    void resetValue(String key) {
        try {
            context.deleteFile(FILENAME + key);
        } catch (Exception e) {
            logException(e);
        }
    }
}
