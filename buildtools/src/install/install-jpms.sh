#!/usr/bin/env bash
./gradlew --version
./gradlew --stacktrace --warning-mode=all processor:build
./gradlew --stacktrace --warning-mode=all templates:build
./gradlew --stacktrace --warning-mode=all extractor:build
./gradlew --stacktrace --warning-mode=all renderer:build