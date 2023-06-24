package org.martin.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.martin.http.ResponseEntity;
import org.martin.http.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Scraper {
    private final URI myBaseURI;
    private final HttpClient myHttpClient;
    private final Set<String> myVisited = ConcurrentHashMap.newKeySet();
    private final ExecutorService myExecutorService;
    private final BlockingQueue<ScrapedFile> myQueue;

    public Scraper(String baseUrl, BlockingQueue<ScrapedFile> blockingQueue) throws URISyntaxException {
        myQueue = blockingQueue;
        myBaseURI = new URI(baseUrl);
        myHttpClient = new HttpClient();
        myExecutorService = Executors.newFixedThreadPool(10);

        Runtime.getRuntime().addShutdownHook(new Thread(myExecutorService::shutdown));
    }

    public void start() {
        myExecutorService.submit(() -> scrape(myBaseURI.resolve("index.html")));
    }

    private void scrape(URI baseURI) {
        ResponseEntity responseEntity = myHttpClient.getRequest(baseURI.toString());
        myVisited.add(baseURI.toString());

        addToQueue(responseEntity, baseURI);

        String contentType = responseEntity.getContentType();
        if (contentType.equals("text/html")) {
            String html = new String(responseEntity.getRawData());

            Document doc = Jsoup.parse(html);
            Set<String> hrefs = extractAllRefs(doc);

            Set<URI> resolvedURIs = resolveURI(baseURI, hrefs);
            resolvedURIs.removeIf(uri -> myVisited.contains(uri.toString()));

            resolvedURIs.forEach(resolvedURI -> myExecutorService.submit(() -> scrape(resolvedURI)));
        }
    }

    private void addToQueue(ResponseEntity responseEntity, URI uri) {
        String baseUri = myBaseURI.toString();
        String relativePath = uri.toString().substring(baseUri.length());
        ScrapedFile scrapedFile = new ScrapedFile(relativePath, responseEntity.getRawData());

        myQueue.add(scrapedFile);
    }

    private Set<String> extractAllRefs(Document doc) {
        Set<String> aLinks = doc.select("a").stream().map(element -> element.attr("href")).collect(Collectors.toSet());
        Set<String> linkHrefs = doc.select("link").stream().map(element -> element.attr("href")).collect(Collectors.toSet());
        Set<String> imgTags = doc.select("img").stream().map(element -> element.attr("src")).collect(Collectors.toSet());
        Set<String> srcLinks = doc.select("scr").stream().map(element -> element.attr("href")).collect(Collectors.toSet());

        Set<String> finalSet = new HashSet<>();
        finalSet.addAll(aLinks);
        finalSet.addAll(linkHrefs);
        finalSet.addAll(imgTags);
        finalSet.addAll(srcLinks);
        return finalSet;
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
