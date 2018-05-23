## Manifest Metadata Processor

This provides an Camel Jetty API that can be crawled by an httpclient to get indexable JSON documents containing mapped metadata fields
 sourced from IIIF manifests.

 ```bash
 http://localhost:9098/extractor?type=extract&manifest={$remote_manifest_URI}
 ```

The client can then use the elasticsearch API and PUT the response bodies into an index for discovery.
See `IndexerCrawlerTest` for an example.

### Elasticsearch

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