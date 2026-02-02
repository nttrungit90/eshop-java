/**
 * Converted from: src/Catalog.API/CatalogOptions.cs
 * .NET Class: eShop.Catalog.API.CatalogOptions
 *
 * Configuration options for the catalog service.
 */
package com.eshop.catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "catalog")
public class  CatalogOptions {

    /**
     * Path to the product pictures directory.
     */
    private String picsPath = "pics";

    /**
     * Whether to use customization data.
     */
    private boolean useCustomizationData = false;

    /**
     * AI configuration.
     */
    private AiOptions ai = new AiOptions();

    public String getPicsPath() {
        return picsPath;
    }

    public void setPicsPath(String picsPath) {
        this.picsPath = picsPath;
    }

    public boolean isUseCustomizationData() {
        return useCustomizationData;
    }

    public void setUseCustomizationData(boolean useCustomizationData) {
        this.useCustomizationData = useCustomizationData;
    }

    public AiOptions getAi() {
        return ai;
    }

    public void setAi(AiOptions ai) {
        this.ai = ai;
    }

    public static class AiOptions {
        /**
         * Whether AI features are enabled.
         */
        private boolean enabled = false;

        /**
         * Embedding model to use (e.g., "text-embedding-ada-002").
         */
        private String embeddingModel;

        /**
         * Ollama API URL for local embeddings.
         */
        private String ollamaUrl;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public String getOllamaUrl() {
            return ollamaUrl;
        }

        public void setOllamaUrl(String ollamaUrl) {
            this.ollamaUrl = ollamaUrl;
        }
    }
}
