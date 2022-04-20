package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Strings {
    public static String readString(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        String line;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.US_ASCII))) {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            System.err.println("readString error");
        }

        return builder.toString();
    }
}
