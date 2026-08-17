package com.germanlearning.service.external;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Everything about the outside world in one place.
 *
 * All of it lives on the server: no key, URL or quota detail is ever shipped
 * to the browser. Swapping the public LanguageTool service for a self-hosted
 * one is a property change, not a code change.
 */
@Component
@ConfigurationProperties(prefix = "lingualearn.external")
public class ExternalApiProperties {

    /** Sent to every third party API so they can identify this app. */
    private String userAgent = "LinguaLearn/1.0 (portfolio project; contact via repository)";

    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;

    private final Tatoeba tatoeba = new Tatoeba();
    private final Dictionary dictionary = new Dictionary();
    private final LanguageTool languageTool = new LanguageTool();

    public static class Tatoeba {
        private boolean enabled = true;
        private String baseUrl = "https://tatoeba.org/en/api_v0";
        /** Sentences kept per lookup after filtering. */
        private int maxResults = 4;
        private int minLength = 12;
        private int maxLength = 120;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }
    }

    public static class Dictionary {
        private boolean enabled = true;
        /** German Wiktionary through the standard MediaWiki action API. */
        private String baseUrl = "https://de.wiktionary.org/w/api.php";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class LanguageTool {
        private boolean enabled = true;
        /**
         * The public endpoint is rate limited and meant for interactive use, so
         * the writing coach only ever runs when a learner presses the button.
         * Point this at a self-hosted server for anything heavier.
         */
        private String baseUrl = "https://api.languagetool.org/v2";
        private String language = "de-DE";
        private int maxTextLength = 1500;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getMaxTextLength() {
            return maxTextLength;
        }

        public void setMaxTextLength(int maxTextLength) {
            this.maxTextLength = maxTextLength;
        }
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Tatoeba getTatoeba() {
        return tatoeba;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public LanguageTool getLanguageTool() {
        return languageTool;
    }
}
