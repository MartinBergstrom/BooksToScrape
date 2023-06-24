package org.martin;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Launching...");
        BlockingQueue<ScrapedFile> blockingQueue = new LinkedBlockingQueue<>(50);

        new Scraper("http://books.toscrape.com/", blockingQueue).start();
        new Thread(new ScrapedDataFileWriter(blockingQueue)).start();
    }
}