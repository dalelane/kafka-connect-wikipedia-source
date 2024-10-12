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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikipediaSourceTask extends SourceTask {

    private static Logger log = LoggerFactory.getLogger(WikipediaSourceTask.class);

    // fires at regular poll intervals
    private ScheduledExecutorService fetchTimer;
    // invoked by the timer to call the wikipedia API
    private WikipediaDataFetcher dataFetcher;

    // factory class for creating Connect records from the API responses
    private WikipediaRecordFactory recordFactory;


    @Override
    public void start(Map<String, String> properties) {
        log.info("Starting task {}", properties);

        AbstractConfig config = new AbstractConfig(WikipediaConfig.CONFIG_DEF, properties);
        log.debug("Config {}", config);

        recordFactory = new WikipediaRecordFactory(config);
        Instant offset = recordFactory.getPersistedOffset(getOffsetStorageReader());
        dataFetcher = new WikipediaDataFetcher(config, offset);

        fetchTimer = Executors.newScheduledThreadPool(1);
        int pollIntervalSecs = config.getInt(WikipediaConfig.POLL_INTERVAL);
        fetchTimer.scheduleWithFixedDelay(dataFetcher, 0, pollIntervalSecs, TimeUnit.SECONDS);
    }


    @Override
    public void stop() {
        log.info("Stopping task");

        if (fetchTimer != null) {
            fetchTimer.shutdownNow();
        }
        fetchTimer = null;
        dataFetcher = null;
        recordFactory = null;
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return dataFetcher.getChanges()
            .stream()
            .map(r -> recordFactory.createSourceRecord(r))
            .collect(Collectors.toList());
    }

    @Override
    public String version() {
        return WikipediaSourceConnector.VERSION;
    }

    private OffsetStorageReader getOffsetStorageReader () {
        if (context == null) {
            log.debug("No context - assuming that this is the first time the Connector has run");
            return null;
        }
        else if (context.offsetStorageReader() == null) {
            log.debug("No offset reader - assuming that this is the first time the Connector has run");
            return null;
        }
        else {
            return context.offsetStorageReader();
        }
    }
}
