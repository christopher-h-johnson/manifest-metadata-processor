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
package de.ubleipzig.metadata.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {


    private String baseUrl;
    
    private String katalogUrl;
    
    private String manifestFilename;
    
    private Boolean isUBLImageService;
    
    private String imageServiceBaseUrl;
    
    private String imageServiceImageDirPrefix;
    
    private String imageServiceFileExtension;
    
    private String imageServiceContext;
    
    private String imageServiceProfile;
    
    private String sequenceContext;
    
    private String canvasContext;
    
    private String annotationContext;
    
    private String rangeContext;
    private String license;
    private String attributionLicenseNote;
    private String attributionKey;
    private String resourceType;
    private String resourceFormat;
    private String resourceFileExtension;
    private String viewingHint;

    /**
     * @return baseUrl String
     */
    @JsonProperty
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl String
     */
    @JsonProperty
    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return imageServiceBaseUrl String
     */
    @JsonProperty
    public String getImageServiceBaseUrl() {
        return imageServiceBaseUrl;
    }

    /**
     * @param imageServiceBaseUrl String
     */
    @JsonProperty
    public void setImageServiceBaseUrl(final String imageServiceBaseUrl) {
        this.imageServiceBaseUrl = imageServiceBaseUrl;
    }

    /**
     * @return imageServiceContext
     */
    @JsonProperty
    public String getImageServiceContext() {
        return imageServiceContext;
    }

    /**
     * @param imageServiceContext String
     */
    @JsonProperty
    public void setImageServiceContext(final String imageServiceContext) {
        this.imageServiceContext = imageServiceContext;
    }

    /**
     * @return imageServiceProfile
     */
    @JsonProperty
    public String getImageServiceProfile() {
        return imageServiceProfile;
    }

    /**
     * @param imageServiceProfile String
     */
    @JsonProperty
    public void setImageServiceProfile(final String imageServiceProfile) {
        this.imageServiceProfile = imageServiceProfile;
    }

    /**
     * @return katalogUrl String
     */
    @JsonProperty
    public String getKatalogUrl() {
        return katalogUrl;
    }

    /**
     * @param katalogUrl String
     */
    @JsonProperty
    public void setKatalogUrl(final String katalogUrl) {
        this.katalogUrl = katalogUrl;
    }

    /**
     * @return manifestFilename
     */
    @JsonProperty
    public String getManifestFilename() {
        return manifestFilename;
    }

    /**
     * @param manifestFilename String
     */
    @JsonProperty
    public void setManifestFilename(final String manifestFilename) {
        this.manifestFilename = manifestFilename;
    }

    /**
     *
     * @return isUBLImageService
     */
    @JsonProperty
    public Boolean getIsUBLImageService() {
        return isUBLImageService;
    }

    /**
     *
     * @param isUBLImageService Boolean
     */
    @JsonProperty
    public void setIsUBLImageService(final Boolean isUBLImageService) {
        this.isUBLImageService = isUBLImageService;
    }

    /**
     * @return imageServiceUriPrefix
     */
    @JsonProperty
    public String getImageServiceImageDirPrefix() {
        return imageServiceImageDirPrefix;
    }

    /**
     * @param imageServiceImageDirPrefix String
     */
    @JsonProperty
    public void setImageServiceImageDirPrefix(final String imageServiceImageDirPrefix) {
        this.imageServiceImageDirPrefix = imageServiceImageDirPrefix;
    }

    /**
     * @return imageServiceFileExtension
     */
    @JsonProperty
    public String getImageServiceFileExtension() {
        return imageServiceFileExtension;
    }

    /**
     * @param imageServiceFileExtension String
     */
    @JsonProperty
    public void setImageServiceFileExtension(final String imageServiceFileExtension) {
        this.imageServiceFileExtension = imageServiceFileExtension;
    }

    /**
     * @return defaultSequenceId
     */
    @JsonProperty
    public String getSequenceContext() {
        return sequenceContext;
    }

    /**
     * @param sequenceContext String
     */
    @JsonProperty
    public void setSequenceContext(final String sequenceContext) {
        this.sequenceContext = sequenceContext;
    }

    /**
     * @return canvasContext
     */
    @JsonProperty
    public String getCanvasContext() {
        return canvasContext;
    }

    /**
     * @param canvasContext String
     */
    @JsonProperty
    public void setCanvasContext(final String canvasContext) {
        this.canvasContext = canvasContext;
    }

    /**
     * @return canvasContext
     */
    @JsonProperty
    public String getAnnotationContext() {
        return annotationContext;
    }

    /**
     * @param annotationContext String
     */
    @JsonProperty
    public void setAnnotationContext(final String annotationContext) {
        this.annotationContext = annotationContext;
    }

    /**
     * @return rangeContext
     */
    @JsonProperty
    public String getRangeContext() {
        return rangeContext;
    }

    /**
     * @param rangeContext String
     */
    @JsonProperty
    public void setRangeContext(final String rangeContext) {
        this.rangeContext = rangeContext;
    }

    /**
     * @return license
     */
    @JsonProperty
    public String getLicense() {
        return license;
    }

    /**
     * @param license String
     */
    @JsonProperty
    public void setLicense(final String license) {
        this.license = license;
    }

    /**
     * @return attributionLicenseNote
     */
    @JsonProperty
    public String getAttributionLicenseNote() {
        return attributionLicenseNote;
    }

    /**
     * @param attributionLicenseNote String
     */
    @JsonProperty
    public void setAttributionLicenseNote(final String attributionLicenseNote) {
        this.attributionLicenseNote = attributionLicenseNote;
    }

    /**
     * @return attributionKey
     */
    @JsonProperty
    public String getAttributionKey() {
        return attributionKey;
    }

    /**
     * @param attributionKey String
     */
    @JsonProperty
    public void setAttributionKey(final String attributionKey) {
        this.attributionKey = attributionKey;
    }

    /**
     * @return resourceType
     */
    @JsonProperty
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceType String
     */
    @JsonProperty
    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * @return resourceFormat
     */
    @JsonProperty
    public String getResourceFormat() {
        return resourceFormat;
    }

    /**
     * @param resourceFormat String
     */
    @JsonProperty
    public void setResourceFormat(final String resourceFormat) {
        this.resourceFormat = resourceFormat;
    }

    /**
     * @return resourceFileExtension
     */
    @JsonProperty
    public String getResourceFileExtension() {
        return resourceFileExtension;
    }

    /**
     * @param resourceFileExtension String
     */
    @JsonProperty
    public void setResourceFileExtension(final String resourceFileExtension) {
        this.resourceFileExtension = resourceFileExtension;
    }

    /**
     * @return viewingHint
     */
    @JsonProperty
    public String getViewingHint() {
        return viewingHint;
    }

    /**
     * @param viewingHint String
     */
    @JsonProperty
    public void setViewingHint(final String viewingHint) {
        this.viewingHint = viewingHint;
    }

}
