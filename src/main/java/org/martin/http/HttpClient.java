package org.martin.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {
    private static final PoolingHttpClientConnectionManager connectionManager;
    private static final CloseableHttpClient myHttpClient;

    static {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(200);
        myHttpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    public ResponseEntity getRequest(String path) {

        HttpGet request = new HttpGet(path);

        try (CloseableHttpResponse response = myHttpClient.execute(request)) {

            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                String contentType = response.getEntity().getContentType().getValue();
                byte[] rawData = EntityUtils.toByteArray(response.getEntity());
                EntityUtils.consume(response.getEntity());
                return new ResponseEntity(contentType, rawData);
            } else {
                System.err.println("Response code for request: " + path + " was: " + responseCode);
                throw new RuntimeException();
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception during request", e);
        }
    }
}
