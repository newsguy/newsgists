package com.newsgists.app;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuhnAutoAbstracter implements AutoAbstracter {

    private final CommonWords commonWords;

    private static final Logger LOG = Logger.getLogger(LuhnAutoAbstracter.class);

    public LuhnAutoAbstracter() {
        this.commonWords = new CommonWords();
    }

    public static void main(String[] args) {
        LuhnAutoAbstracter summarizer = new LuhnAutoAbstracter();
        String text = summarizer.text("./test.txt");
        String autoAbstract = summarizer.summarize(text);
        LOG.info("Generated abstract: " + autoAbstract);
    }

    @Override
    public String summarize(String text) {
        List<String> frequentWords = frequentWords(text.toLowerCase());
        List<String> sentences = breakIntoSentences(text);
        // sentences.stream().forEach(sentence -> LOG.info(sentence));
        Map<String, Double> sentenceScores = scoreSentences(sentences, frequentWords);
        List<String> byRankSentences = byRank(sentenceScores);

        return byAppearanceSentences(byRankSentences, sentences);
    }

    private String byAppearanceSentences(List<String> byRankSentences, List<String> sentences) {
        List<String> byAppearanceSentences = new ArrayList<>();
        sentences.stream()
                .filter(sentence -> byRankSentences.contains(sentence))
                .forEach(sentence -> byAppearanceSentences.add(sentence));

        return String.join(". ", byAppearanceSentences);
    }

    private Map<String, Double> scoreSentences(List<String> sentences, List<String> frequentWords) {
        Map<String, Double> sentenceScores = new HashMap<>();

        for (String sentence : sentences) {
            int freqCount = 0, beg = 0, end = 0;
            boolean beginning = false;
            List<String> words = Arrays.asList(Pattern.compile("\\,|;|- ").matcher(sentence.toLowerCase()).replaceAll("").split(" "));
            for (String word : words) {
                if (frequentWords.contains(word)) {
                    if (!beginning) {
                        beg = words.indexOf(word);
                        beginning = true;
                    }
                    freqCount++;
                    end = words.indexOf(word);
                }
            }
            List<String> sentenceSlice = words.subList(beg, end + 1);
            List<String> finalSentenceSlice = new ArrayList<>();
            sentenceSlice.stream()
                    .filter(word -> !commonWords.contains(word))
                    .forEach(word -> finalSentenceSlice.add(word));
            sentenceScores.put(sentence, Math.pow(new Double(freqCount), 2) / new Double(finalSentenceSlice.size()));
        }

        return sentenceScores;
    }

    private List<String> byRank(Map<String, Double> sentenceScores) {
        final List<String> topRanked = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        sentenceScores.values().stream().forEach(score -> scores.add(score));
        Collections.sort(scores, Collections.reverseOrder());

        scores.stream().forEach(score -> {
            sentenceScores.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == score)
                    .forEach(entry -> topRanked.add(entry.getKey()));
        });
        int topTen = new Double(topRanked.size() * 0.05D).intValue();

        return topRanked.subList(0, topTen);
    }

    private List<String> frequentWords(String lowercaseText) {
        Map<String, Integer> frequentWords = new HashMap<>();
        Pattern pattern = Pattern.compile("\\.|\\,|;|\n|- ");
        Matcher matcher = pattern.matcher(lowercaseText);
        lowercaseText = matcher.replaceAll("");
        List<String> words = Arrays.asList(lowercaseText.split(" "));
        LOG.info("Before filtering common words: " + words);
        words.stream().filter(word -> !commonWords.contains(word)).forEach(word -> {
            if (frequentWords.containsKey(word))
                frequentWords.put(word, frequentWords.get(word) + 1);
            else
                frequentWords.put(word, 1);
        });

        List<Integer> occurrenceCounts = new ArrayList<>();
        List<String> topFrequent = new ArrayList<>();

        frequentWords.values().stream().forEach(count -> occurrenceCounts.add(count));
        Collections.sort(occurrenceCounts, Collections.reverseOrder());
        int topTen = new Double(occurrenceCounts.size() * 0.1D + 0.5D).intValue();

        occurrenceCounts.subList(0, topTen)
                .stream()
                .forEach(count -> {
                    frequentWords.entrySet().stream()
                            .filter(entry -> entry.getValue() == count)
                            .filter(entry -> !topFrequent.contains(entry.getKey()))
                            .forEach(entry -> topFrequent.add(entry.getKey()));
                });

        LOG.info("Top ten frequent words: " + topFrequent);
        return topFrequent;
    }

    private List<String> breakIntoSentences(String text) {
        Pattern pattern = Pattern.compile("\\n|\\. ");
        return Arrays.asList(pattern.split(text));
    }

    private String text(String filename) {
        StringBuilder buffer = new StringBuilder();
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename), charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        String text = buffer.toString();
        text = text.replaceAll("- ", "");

        return text;
    }

}
