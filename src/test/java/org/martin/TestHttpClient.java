package org.martin;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link HttpClient}
 */
@ExtendWith(MockitoExtension.class)
class TestHttpClient {
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    @Mock
    private HttpClientBuilder myBuilderMock;
    @Mock
    private CloseableHttpClient myClosableHttpClientMock;
    @Mock
    private CloseableHttpResponse myClosableHttpResponseMock;
    @Captor
    private ArgumentCaptor<HttpGet> myHttpGetCaptor;

    private HttpClient myHttpClient;

    @BeforeEach
    void setUp() throws IOException {
        when(myBuilderMock.build()).thenReturn(myClosableHttpClientMock);
        when(myClosableHttpClientMock.execute(myHttpGetCaptor.capture()))
                .thenReturn(myClosableHttpResponseMock);

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContentLength()).thenReturn(10L);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn(CONTENT_TYPE_TEXT_HTML);
        when(entity.getContentType()).thenReturn(header);
        when(myClosableHttpResponseMock.getStatusLine()).thenReturn(statusLine);
        when(myClosableHttpResponseMock.getEntity()).thenReturn(entity);

        myHttpClient = new HttpClient(myBuilderMock);
    }

    @Test
    void shouldPerformSuccessfulGet() {
        // Given
        String path = "http://mytest.com";

        // When
        ResponseEntity response = myHttpClient.getRequest(path);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContentType()).isEqualTo("text/html");
        assertThat(response.getRawData()).isNotEmpty();
    }
}