package org.speechrecognizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author Jamie Reid
 */
public class GoogleTranslateUtil extends Thread {
    private final static Logger logger = LogManager.getLogger(GoogleTranslateUtil.class);

    /**
     * This method translates text from one language to another
     *
     * @param apiKey       The google API key
     * @param text         The text to translate
     * @param fromLanguage The language code to translate from
     * @param toLanguage   The language code to translate to
     * @return The translated text
     */
    public String translate(String apiKey, String text, String fromLanguage, String toLanguage) {
        StringBuilder result = new StringBuilder();
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlStr = "https://www.googleapis.com/language/translate/v2?key=" + apiKey + "&q=" + encodedText + "&target=" + toLanguage + "&source=" + fromLanguage + "&format=text";
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            InputStream stream;
            if (conn.getResponseCode() == 200) {//success
                stream = conn.getInputStream();
            } else {
                stream = conn.getErrorStream();

            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            int responseCode = conn.getResponseCode();
            if (conn.getResponseCode() != 400) {
                JsonObject translatedObject = JsonParser.parseString(result.toString()).getAsJsonObject();
                if (translatedObject.get("error") == null) {
                    logger.debug("Text translated correctly");
                    return translatedObject.get("data").getAsJsonObject()
                            .get("translations").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("translatedText").getAsString();
                }
            } else if (responseCode == 200) {
                logger.error(result);
            }
        } catch (IOException | JsonSyntaxException exception) {
            logger.fatal(exception.getMessage());
        }

        return null;
    }
}

