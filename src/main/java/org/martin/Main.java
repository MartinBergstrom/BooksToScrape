package org.martin;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Launching...");
        new Scraper("http://books.toscrape.com/").start();
    }
}