package org.martin;

import org.apache.commons.io.FileUtils;
import org.martin.scraping.ScrapedFile;
import org.martin.scraping.Scraper;
import org.martin.writing.ScrapedDataFileWriter;

import java.io.File;
import java.util.concurrent.*;

public class Main {
    private static long startTime;

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
            long totalFileSize = FileUtils.sizeOfDirectory(new File("scraped"));
            double averageFileSizePerSecondKB = (totalFileSize / 1024.0) / durationSeconds;
            System.out.println("Application stopped. Duration: " + formatDuration(duration));
            System.out.println("Total dir size: " + totalFileSize / 1024 + " KB");
            System.out.println("Average write per second: " + averageFileSizePerSecondKB + " KB/second");
        }));

        startTime = System.currentTimeMillis();
        System.out.println("Launching...");
        BlockingQueue<ScrapedFile> blockingQueue = new LinkedBlockingQueue<>(100);

        new Scraper("http://books.toscrape.com/", blockingQueue, 200).start();

        int fileWriterThreads = 2;
        try (ExecutorService executorService = Executors.newFixedThreadPool(fileWriterThreads)) {
            for (int i = 0; i < fileWriterThreads; i++) {
                executorService.submit(new ScrapedDataFileWriter(blockingQueue));
            }
        }
    }

    private static String formatDuration(long duration) {
        long minutes = (duration / (1000 * 60)) % 60;
        long seconds = (duration / 1000) % 60;
        long milliseconds = duration % 1000;

        return String.format("%d minutes %d seconds %d milliseconds", minutes, seconds, milliseconds);
    }
}