package org.martin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Scraper {
    private final URI myBaseURI;
    private final HttpClient myHttpClient;
    private final Set<String> myVisited = ConcurrentHashMap.newKeySet();
    private final ExecutorService myExecutorService;

    public Scraper(String baseUrl) throws URISyntaxException {
        myBaseURI = new URI(baseUrl);
        myHttpClient = new HttpClient();
        myExecutorService = Executors.newFixedThreadPool(10);

        Runtime.getRuntime().addShutdownHook(new Thread(myExecutorService::shutdown));
    }

    public void start() {
        myExecutorService.submit(() -> scrape(myBaseURI));
    }

    private void scrape(URI baseURI) {
        ResponseEntity responseEntity = myHttpClient.getRequest(baseURI.toString());
        myVisited.add(baseURI.toString());

        String contentType = responseEntity.getContentType();
        if (contentType.equals("text/html")) {
            String html = new String(responseEntity.getRawData());

            // TODO
            writeToFile(responseEntity, baseURI.toString());

            Document doc = Jsoup.parse(html);
            Set<String> hrefs = extractAllRefs(doc);

            Set<URI> resolvedURIs = resolveURI(baseURI, hrefs);
            resolvedURIs.removeIf(uri -> myVisited.contains(uri.toString()));

            resolvedURIs.forEach(resolvedURI -> myExecutorService.submit(() -> scrape(resolvedURI)));
        } else {
            handleNonHtmlContentType(baseURI, responseEntity);
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

    private void handleNonHtmlContentType(URI baseURI, ResponseEntity responseEntity) {
        writeToFile(responseEntity, baseURI.toString());
    }

    // TODO
    private void writeToFile(ResponseEntity responseEntity, String path) {
        double sizeInKiloBytes = (double) responseEntity.getRawData().length / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("#");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        String roundedValue = decimalFormat.format(sizeInKiloBytes);

        System.out.println("Writing type of data: " + responseEntity.getContentType() +
                " to file on path: " + path + " , size: " + roundedValue + "kB");
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
                .collect(Collectors.toCollection(HashSet::new));
    }


}
