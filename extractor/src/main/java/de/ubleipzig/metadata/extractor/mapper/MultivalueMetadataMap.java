package de.ubleipzig.metadata.extractor.mapper;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultivalueMetadataMap {

    Map<String, Object> metadataMap;
}
