package com.qrcontactshare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VCardParser {

    private final String text;

    public VCardParser(InputStream is) throws IOException {
        text = readStreamToString(is);
    }
    private static String readStreamToString(InputStream is) throws IOException {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean skippingPhoto = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PHOTO")) {
                    skippingPhoto = true;
                    continue;
                }
                if (skippingPhoto) {
                    if (line.startsWith(" ") || line.startsWith("\t")) {
                        // continuation of base64 photo data, skip
                        continue;
                    } else {
                        skippingPhoto = false; // end of PHOTO field
                    }
                }
                builder.append(line).append("\n");
            }
        }
        return builder.toString();
    }

    public String getText() {
        return text;
    }

}
