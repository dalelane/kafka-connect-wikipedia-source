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
package uk.co.dalelane.kafkaconnect.wikipedia.source.data;

import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * API response from Wikipedia. Represents a change to a Wikipedia page.
 *
 *  cf. https://www.mediawiki.org/wiki/API:RecentChanges
 */
public class Change {

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("user")
    @Expose
    private String user;

    @SerializedName("userid")
    @Expose
    private Integer userid;

    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }

    public Integer getUserId() {
        return userid;
    }

    public String getTimestamp() {
        return timestamp;
    }


    private Instant timestampParsed = null;

    public Instant getTimestampAsInstant() {
        if (timestampParsed != null) {
            return timestampParsed;
        }
        timestampParsed = Instant.parse(timestamp);
        return timestampParsed;
    }
}
