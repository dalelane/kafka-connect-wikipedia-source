/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.co.dalelane.kafkaconnect.wikipedia.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.common.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uk.co.dalelane.kafkaconnect.wikipedia.source.data.Change;
import uk.co.dalelane.kafkaconnect.wikipedia.source.data.WikipediaEdits;


public class WikipediaDataFetcher implements Runnable {

    private static Logger log = LoggerFactory.getLogger(WikipediaDataFetcher.class);

    private final List<Change> fetchedChanges = Collections.synchronizedList(new ArrayList<Change>());

    // how many edits to fetch in each API call to Wikipedia
    //  The API supports setting this to up to 500, however values over
    //  100 periodically result in errors.
    private final static int CHANGES_TO_REQUEST = 100;
    // maximum number of fetches to make before waiting for the poll interval
    private final static int MAX_FETCHES = 80;

    // cf. https://www.mediawiki.org/wiki/API:RecentChanges
    private static final String BASE_API_URL = "https://en.wikipedia.org/w/api.php" + "?" +
        "format=json" + "&" +
        "action=query" + "&" +
        "list=recentchanges" + "&" +
        "rcnamespace=0" + "&" +
        "rcprop=" + URLEncoder.encode("user|userid|flags|timestamp|title", Charset.forName("UTF-8")) + "&" +
        "rctype=" + URLEncoder.encode("new|edit", Charset.forName("UTF-8")) + "&" +
        "rclimit=" + CHANGES_TO_REQUEST + "&" +
        "rcdir=newer" + "&";

    // JSON parser for Wikipedia API responses
    //  ignoring the java.time.Instant value which cannot be handled
    private final Gson apiResponseParser = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    // timestamp of the latest edit received from the API
    private Instant offset;


    public WikipediaDataFetcher(AbstractConfig config, Instant offset) {
        log.info("Creating a data fetcher starting from offset {}", offset);
        this.offset = offset;
    }


    @Override
    public void run() {
        try {
            boolean fetching = true;
            int fetches = 0;
            while (fetching && fetches++ < MAX_FETCHES) {
                List<Change> recentChanges = fetchNewChanges();
                synchronized (fetchedChanges) {
                    for (Change change : recentChanges) {
                        fetchedChanges.add(change);
                        offset = change.getTimestampAsInstant();
                    }
                }

                fetching = (recentChanges.size() == CHANGES_TO_REQUEST);
            }
        }
        catch (Throwable e) {
            log.error("Failed to fetch changes", e);
        }
    }

    public List<Change> getChanges() {
        synchronized (fetchedChanges) {
            List<Change> copy = new ArrayList<>(fetchedChanges);
            fetchedChanges.clear();
            return copy;
        }
    }

    private List<Change> fetchNewChanges() throws IOException {
        String url = BASE_API_URL +
            "rcstart=" + offset.plusSeconds(1).toString();
        log.info("fetching {}", url);

        InputStream apiStream = URI.create(url)
            .toURL()
            .openConnection()
            .getInputStream();
        Reader apiReader = new InputStreamReader(apiStream, StandardCharsets.UTF_8);

        return apiResponseParser.fromJson(apiReader, WikipediaEdits.class)
            .getQuery()
            .getRecentChanges();
    }
}
