/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module de.ubleipzig.metadata.renderer {
    exports de.ubleipzig.metadata.renderer;
    requires de.ubleipzig.metadata.templates;
    requires de.ubleipzig.metadata.processor;
    requires slf4j.api;
    requires jsonld.java;
    requires org.apache.commons.io;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires camel.core;
    requires org.apache.commons.rdf.api;
    requires org.apache.jena.core;
    requires org.apache.commons.rdf.jena;
    requires org.apache.jena.arq;
    requires java.naming;

    requires httpcore;
    requires httpclient;
    requires io;
    requires kernel;
    requires layout;
}