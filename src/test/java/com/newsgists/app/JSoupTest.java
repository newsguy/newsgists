package com.newsgists.app;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by sulabh on 7/14/16.
 */
public class JSoupTest {

    public static void main(String[] args) {

        System.out.println("Gets all google top stories ...");


        try {
            Document doc = Jsoup.connect("https://news.google.com").get();
            Elements headings = doc.getElementsByAttributeValue("class", "esc-lead-article-title");
            for (Element e: headings) {
                String title = e.text();
                String link = e.getElementsByAttribute("href").attr("href");
                System.out.println("Title: " + title);
                System.out.println("Link: " + link);
                String articleBody = null;
                try {
                    Document cdoc = Jsoup.connect(link).get();
                    articleBody = cdoc.body().text();
                    System.out.println("Article Text: " + articleBody);
                } catch (Exception e1) {
                    System.err.println("Failed to get the article for: " + title);;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get headings");
        }
        System.out.println("Done");
    }
}
