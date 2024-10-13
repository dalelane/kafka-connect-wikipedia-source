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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkaconnect.wikipedia.source.data.Change;


public class WikipediaRecordFactory {

    private static Logger log = LoggerFactory.getLogger(WikipediaRecordFactory.class);

    private static final Schema CHANGE_SCHEMA = SchemaBuilder.struct()
        .name("change")
        .version(1)
            .field("type", Schema.STRING_SCHEMA)
            .field("title", Schema.STRING_SCHEMA)
            .field("user", Schema.OPTIONAL_STRING_SCHEMA)
            .field("userid", Schema.OPTIONAL_INT32_SCHEMA)
            .field("timestamp", Timestamp.SCHEMA)
        .build();

    private String topic;

    public WikipediaRecordFactory(AbstractConfig config) {
        topic = config.getString(WikipediaConfig.TOPIC);
    }

    public SourceRecord createSourceRecord(Change data) {
        Integer partition = null;
        Schema keySchema = null;
        Object key = null;
        return new SourceRecord(createSourcePartition(),
                                createSourceOffset(data),
                                topic, partition,
                                keySchema, key,
                                CHANGE_SCHEMA, createStruct(data),
                                data.getTimestampAsInstant().toEpochMilli());
    }

    private Map<String, Object> createSourcePartition() {
        return null;
    }

    private static final String SOURCE_OFFSET = "timestamp";

    private Map<String, Object> createSourceOffset(Change data) {
        return Collections.singletonMap(SOURCE_OFFSET, data.getTimestampAsInstant().toEpochMilli());
    }

    private Struct createStruct(Change data) {
        Struct response = new Struct(CHANGE_SCHEMA);
        response.put("type", data.getType());
        response.put("title", data.getTitle());
        response.put("user", data.getUser());
        response.put("userid", data.getUserId());
        response.put("timestamp", Date.from(data.getTimestampAsInstant()));
        return response;
    }

    public Instant getPersistedOffset(OffsetStorageReader offsetReader) {
        log.debug("retrieving persisted offset for previously produced events");

        Instant defaultOffset = Instant
            .now()
            .truncatedTo(ChronoUnit.SECONDS)
            .minus(1L, ChronoUnit.DAYS);

        if (offsetReader == null) {
            log.debug("no offset reader available");
            return defaultOffset;
        }

        Map<String, Object> sourcePartition = createSourcePartition();
        Map<String, Object> persistedOffsetInfo = offsetReader.offset(sourcePartition);
        if (persistedOffsetInfo == null || !persistedOffsetInfo.containsKey(SOURCE_OFFSET)) {
            log.debug("no persisted offset");
            return defaultOffset;
        }

        Long offsetEpoch = (Long) persistedOffsetInfo.get(SOURCE_OFFSET);
        Instant offset = Instant.ofEpochMilli(offsetEpoch);
        log.info("previous offset {}", offset);
        return offset;
    }
}
