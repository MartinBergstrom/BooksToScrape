package org.martin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ScrapedDataFileWriter implements Runnable {
    private final BlockingQueue<ScrapedFile> myQueue;
    private final String BASE_DIR = "scraped";

    public ScrapedDataFileWriter(BlockingQueue<ScrapedFile> queue) {
        myQueue = queue;
        File file = new File(BASE_DIR);
        file.mkdir();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ScrapedFile file = myQueue.take();
                writeData(file.getRawData(), file.getRelativePath());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void writeData(byte[] rawData, String fileName) {
        System.out.println("Writing to disc file: " + fileName);
        File file = new File(BASE_DIR + File.separator + fileName);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(rawData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
