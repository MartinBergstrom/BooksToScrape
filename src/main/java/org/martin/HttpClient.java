package org.martin;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class HttpClient {
    private static final String BASE_URL = "http://books.toscrape.com/";

    public HttpEntity getRequest(String path) {
        String requestURI = BASE_URL + path;
        HttpGet request = new HttpGet(requestURI);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {

            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                return response.getEntity();
            } else {
                System.err.println("Response code for request: " + requestURI +
                        " was: " + responseCode);
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception during request", e);
        }
        return null;
    }
}
