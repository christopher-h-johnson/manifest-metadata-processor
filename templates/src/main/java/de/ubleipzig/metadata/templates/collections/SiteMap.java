package de.ubleipzig.metadata.templates.collections;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class SiteMap {

    @JacksonXmlElementWrapper(localName = "urlset")
    private List<Location> url;

    public void setUrl(final List<Location> url) {
        this.url = url;
    }

    public List<Location> getUrl() {
        return url;
    }

    public static class Location {
        String loc;

        public void setLoc(final String loc) {
            this.loc = loc;
        }

        public String getLoc() {
            return loc;
        }
    }
}
