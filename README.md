# Kafka Connect source connector for Wikipedia

## Overview

Creates events for a Kafka topic to represent page creations and edits on the English Wikipedia.

## Config

|    | Notes |
| -- | ----- |
| `wikipedia.pollinterval.secs` | How frequently to check for updates |
| `wikipedia.topic` |  Kafka topic to deliver events to

### Example output

With the JSON converter, events will look like:

```json
{
    "type": "edit",
    "title": "Religious use of incense",
    "user": "Grafen",
    "userid": 307602,
    "timestamp": 1728240621000
}

{
    "type": "edit",
    "title": "Joker: Folie à Deux",
    "user": "WesleyTRV",
    "userid": 46034537,
    "timestamp": 1728240565000
}
```

This is a subset of data available from the Wikipedia API. Refer to [MediaWiki API docs](https://www.mediawiki.org/wiki/API:RecentChanges) for an explanation of individual fields.
