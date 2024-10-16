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

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;


public class WikipediaConfig {

    public static final String TOPIC = "wikipedia.topic";
    public static final String POLL_INTERVAL = "wikipedia.pollinterval.secs";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(TOPIC,
                Type.STRING,
                "wikipedia",
                new ConfigDef.NonEmptyString(),
                Importance.HIGH,
                "Topic to deliver messages to")
        .define(POLL_INTERVAL,
                Type.INT,
                30,
                ConfigDef.Range.atLeast(10),
                Importance.HIGH,
                "How frequently to check for new updates");
}
