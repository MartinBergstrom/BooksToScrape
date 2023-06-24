package org.martin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Scraper {
    private final String myBaseUrl;
    private final HttpClient myHttpClient;
    private final Set<URI> myVisited = new HashSet<>();

    public Scraper(String baseUrl) {
        myBaseUrl = baseUrl;
        myHttpClient = new HttpClient();
    }

    public void start() throws URISyntaxException {
        scrape(new URI(myBaseUrl));
    }

    private void scrape(URI baseURI) {
        ResponseEntity responseEntity = myHttpClient.getRequest(baseURI.toString());
        myVisited.add(baseURI);

        String contentType = responseEntity.getContentType();
        if (contentType.equals("text/html")) {
            String html = new String(responseEntity.getRawData());

            // TODO
            writeToFile(responseEntity.getContentType(), baseURI.toString());

            Document doc = Jsoup.parse(html);
            Set<String> hrefs = extractAllRefs(doc);

            Set<URI> resolvedURIs = resolveURI(baseURI, hrefs);
            resolvedURIs.removeAll(myVisited);

            for (List<URI> chunk : splitUpIntoChunks(resolvedURIs)) {
                chunk.forEach(this::scrape);
            }
        } else {
            handleNonHtmlContentType(baseURI, contentType, responseEntity);
        }
    }

    private Set<String> extractAllRefs(Document doc) {
        Set<String> aLinks = doc.select("a").stream().map(element -> element.attr("href")).collect(Collectors.toSet());
        Set<String> imgTags = doc.select("img").stream().map(element -> element.attr("src")).collect(Collectors.toSet());
        Set<String> srcLinks = doc.select("scr").stream().map(element -> element.attr("href")).collect(Collectors.toSet());

        Set<String> finalSet = new HashSet<>();
        finalSet.addAll(aLinks);
        finalSet.addAll(imgTags);
        finalSet.addAll(srcLinks);
        return finalSet;
    }

    private void handleNonHtmlContentType(URI baseURI, String contentType, ResponseEntity responseEntity) {
        writeToFile(contentType, baseURI.toString());
    }

    // TODO
    private void writeToFile(String type, String path) {
        System.out.println("Writing type of data: " + type + " to file on path: " + path);
    }

    private Set<URI> resolveURI(URI baseURI, Set<String> links) {
        return links.stream()
                .map(link -> {
                    try {
                        return new URI(link);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }).map(baseURI::resolve)
                .collect(Collectors.toSet());
    }


    private List<List<URI>> splitUpIntoChunks(Set<URI> myLinks) {
        int count = 0;
        List<List<URI>> result = new ArrayList<>();
        List<URI> batch = new ArrayList<>();

        for (URI link : myLinks) {
            batch.add(link);
            count++;

            if (count == 10) {
                result.add(batch);
                batch = new ArrayList<>();
                count = 0;
            }
        }
        return result;
    }
}
