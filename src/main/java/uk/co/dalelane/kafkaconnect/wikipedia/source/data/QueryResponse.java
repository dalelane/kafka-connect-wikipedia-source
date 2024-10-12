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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * API response from Wikipedia. Represents a list of Wikipedia edits.
 *
 *  cf. https://www.mediawiki.org/wiki/API:RecentChanges
 */
public class QueryResponse {

    @SerializedName("recentchanges")
    @Expose
    private List<Change> recentchanges;

    public List<Change> getRecentChanges() {
        return recentchanges;
    }
}
