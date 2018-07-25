package de.ubleipzig.metadata.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class Config {

    @NotNull
    private String baseUrl;
    @NotNull
    private String katalogUrl;
    @NotNull
    private String manifestFilename;
    @NotNull
    private Boolean isUBLImageService;
    @NotNull
    private String imageServiceBaseUrl;
    @NotNull
    private String imageServiceImageDirPrefix;
    @NotNull
    private String imageServiceFileExtension;
    @NotNull
    private String imageServiceContext;
    @NotNull
    private String imageServiceProfile;
    @NotNull
    private String sequenceContext;
    @NotNull
    private String canvasContext;
    @NotNull
    private String annotationContext;
    @NotNull
    private String rangeContext;
    private String license;
    private String attributionLicenseNote;
    private String attributionKey;

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

}
