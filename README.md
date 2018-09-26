# Manifest Metadata Processor

This is a collection of APIs for the manipulation of IIIF Manifests

## Requirements
JDK 11

## Reserializer

This is an api to change/correct previously published manifests.  It can also produce version 3 manifests from version 2 data.
If new metadata is available via a JSON API, it can modify existing labels and values.

### Parameters
| Name | Options | Description |
| ---- | ------- | ------- |
| type | reserialize | processor type |
| m | (none) | the URI of a IIIF (v.2.1) manifest |
| version | 2 or 3   | the IIIF output specification |

```bash
 http://localhost:9098/extractor?type=reserialize&version=3&m={$remote_manifest_URI}
 ```

## Renderer

This is an api that can produce a zip file or PDF of a range of images from a IIIF manifest:

### Parameters
| Name | Options | Description |
| ---- | ------- | ------- |
| type | pdf, image | serialization options |
| manifest | (none) | the URI of a IIIF (v.2.1) manifest |
| from | (none)    | the first index of the image sequence |
| to | (none)    | the last index of the image sequence |
| pct | (none)    | the scale of the images (as a percentage)|

#### Example
```bash
http://localhost:9099/renderer?type=pdf&manifest=http://iiif.ub.uni-leipzig.de/0000009000/manifest.json&from=1&to=30&pct=25`
```

#### Preflight for Image Count
A client can get the image count in a manifest with this request:
```bash
http://localhost:9099/renderer?type=count&manifest=http://iiif.ub.uni-leipzig.de/0000009000/manifest.json
```

The response is a JSON Object
```json
{
  "imageServiceCount" : 316
}
```

#### Docker Compose
The service can be deployed with this:
```bash
$ cd src/main/resources/renderer
docker-compose up
```

## Extractor
This provides an Camel Jetty API that can be crawled by an httpclient to get indexable JSON documents containing mapped metadata fields
 sourced from IIIF manifests.

 ```bash
 http://localhost:9098/extractor?type=extract&manifest={$remote_manifest_URI}
 ```

The client can then use the elasticsearch API and PUT the response bodies into an index for discovery.
See `IndexerCrawlerTest` for an example.

## Indexer
(WIP)

## ElasticSearch

See [docs](https://github.com/ub-leipzig/manifest-metadata-processor/blob/master/docs) for sample analysis data from Elastic.

This gets all manifest URIs and their title from the index.

```bash
curl -XGET "http://localhost:9100/m1/_search" -H 'Content-Type: application/json' -d'
{
    "_source": ["metadataMap.@id", "metadataMap.Title"],
    "from" : 0, "size" : 10000,
    "query" : {"match_all" : {}}
}'
```

Host requires this:
```bash
$ sudo sysctl -w vm.max_map_count=262144
```
* Start [elasticsearch-compose](https://github.com/ub-leipzig/manifest-metadata-processor/blob/master/src/main/resources/docker-compose.yml)

### Kibana
* The Kibana interface can be accessed at `http://localhost:5601`