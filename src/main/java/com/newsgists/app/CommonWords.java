package com.newsgists.app;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhinav on 11/7/16.
 */
public class CommonWords {

    public static final String COMMON_WORDS_FILENAME = "src/main/resources/commonwords.txt";

    private final List<String> commonWords = new ArrayList<>();

    private static final Logger LOG = Logger.getLogger(CommonWords.class);

    public CommonWords() {
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(COMMON_WORDS_FILENAME), charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                commonWords.add(line);
            }
        } catch (IOException x) {
            LOG.error("IOException: %s%n", x);
        }
    }

    public boolean contains(String word) {
        return commonWords.contains(word);
    }

}
