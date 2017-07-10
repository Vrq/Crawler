package com.vrq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jtomasik on 09/07/2017.
 */
public class JSLibCrawler {
    private final String GOOGLE_URL = "http://www.google.com/search?q=";
    private final String CHARSET = "UTF-8";
    private final String USER_AGENT = "User-Agent";
    private final String AGENT_SPEC = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

    public void runCrawler() throws Exception {

        String searchTerm = getSearchTermFromStdInput();
        BufferedReader searchReader = getGoogleResultReaderFor(searchTerm);
        List<String> URLsFromSearch = getURLsFromSearch(searchReader);
        Map<String, Integer> JSLibraries = getJSLibrariesFrom(URLsFromSearch);
        printMostFrequentLibs(JSLibraries);
    }

    private String getSearchTermFromStdInput() {
        System.out.println("Please type your search term and confirm with Enter");
        Scanner inputScanner = new Scanner(System.in);
        String searchTerm = inputScanner.nextLine();
        return searchTerm;
    }

    private BufferedReader getGoogleResultReaderFor(String searchTerm) throws Exception {
        URL url = new URL(GOOGLE_URL + URLEncoder.encode(searchTerm, CHARSET));
        return getBufferedReaderForWebsite(url);
    }

    private List<String> getURLsFromSearch(BufferedReader bufferedReader) throws Exception {
        String line;
        List<String> resultURLs = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            String URLMatcher = "a href=\"/url?q=";
            while (line.contains(URLMatcher)) {
                int indexOfSearchResult = line.indexOf(URLMatcher);
                String searchResultEntry = line.substring(indexOfSearchResult);
                int indexOfURLStart = searchResultEntry.indexOf("http");
                int indexOfURLEnd = searchResultEntry.indexOf("&amp");
                String entryURL = searchResultEntry.substring(indexOfURLStart, indexOfURLEnd);
                resultURLs.add(entryURL);
                line = line.substring(indexOfSearchResult + 1);
            }
        }
        return resultURLs;
    }

    private Map<String, Integer> getJSLibrariesFrom(List<String> resultURLs) throws Exception {
        Map<String, Integer> JSLibraries = new HashMap<String, Integer>();
        for (String resultURL : resultURLs) {
            URL url = new URL(resultURL);
            BufferedReader bufferedReader = getBufferedReaderForWebsite(url);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                while (line.contains(".js")) {
                    int indexOfDotJS = line.indexOf(".js");
                    int JSLibNameStart = line.lastIndexOf("/", indexOfDotJS);
                    if (JSLibNameStart != -1 && JSLibNameStart < indexOfDotJS) {
                        String libraryName = line.substring(JSLibNameStart + 1, indexOfDotJS + 3);
                        if (JSLibraries.containsKey(libraryName)) {
                            JSLibraries.put(libraryName, JSLibraries.get(libraryName) + 1);
                        } else {
                            JSLibraries.put(libraryName, 1);
                        }
                    }
                    line = line.substring(indexOfDotJS + 1);
                }
            }
        }
        return JSLibraries;
    }

    private BufferedReader getBufferedReaderForWebsite(URL url) throws Exception {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(USER_AGENT, AGENT_SPEC);
        Reader reader = new InputStreamReader(conn.getInputStream(), CHARSET);
        return new BufferedReader(reader);
    }

    private void printMostFrequentLibs(Map<String, Integer> libs) {
        Map<String, Integer> topFiveLibs =
                libs.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        topFiveLibs.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(System.out::println);
    }


}
