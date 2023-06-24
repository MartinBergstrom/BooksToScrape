package org.martin;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {
    private final HttpClientBuilder myHttpClientBuilder;

    public HttpClient() {
        this(HttpClients.custom());
    }

    // Visible for testing
    HttpClient(HttpClientBuilder httpClientBuilder) {
        myHttpClientBuilder = httpClientBuilder;
    }


    public ResponseEntity getRequest(String path) {
        HttpGet request = new HttpGet(path);

        try (CloseableHttpClient client = myHttpClientBuilder.build();
             CloseableHttpResponse response = client.execute(request)) {

            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                String contentType = response.getEntity().getContentType().getValue();
                byte[] rawData = EntityUtils.toByteArray(response.getEntity());
                return new ResponseEntity(contentType, rawData);
            } else {
                System.err.println("Response code for request: " + path +
                        " was: " + responseCode);
                throw new RuntimeException();
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception during request", e);
        }
    }
}
