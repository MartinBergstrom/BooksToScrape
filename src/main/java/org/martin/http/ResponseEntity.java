package org.martin.http;

public class ResponseEntity {
    private final String myContentType;
    private final byte[] myRawData;

    public ResponseEntity(String contentType, byte[] rawData) {
        myContentType = contentType;
        myRawData = rawData;
    }

    public String getContentType() {
        return myContentType;
    }

    public byte[] getRawData() {
        return myRawData;
    }
}
