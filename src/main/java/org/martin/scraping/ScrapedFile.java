package org.martin.scraping;

public class ScrapedFile {
    private final String myRelativePath;
    private final byte[] myRawData;

    public ScrapedFile(String relativePath, byte[] rawData) {
        this.myRelativePath = relativePath;
        this.myRawData = rawData;
    }

    public String getRelativePath() {
        return myRelativePath;
    }

    public byte[] getRawData() {
        return myRawData;
    }
}
