package com.germanlearning.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.germanlearning.dto.GrammarFeedbackDto;
import com.germanlearning.dto.GrammarFeedbackDto.GrammarIssueDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.util.ArrayList;
import java.util.List;

/**
 * The Writing Coach: checks a learner's German with LanguageTool.
 *
 * Called only from the backend, only when the learner presses "Check my
 * German", and never on a schedule — the public LanguageTool endpoint is rate
 * limited (roughly 20 requests a minute per IP) and is not meant for automated
 * bulk traffic. That is fine for interactive use in a portfolio project; for
 * anything heavier, point {@code lingualearn.external.language-tool.base-url}
 * at a self-hosted LanguageTool server. No configuration reaches the browser.
 */
@Service
public class GrammarCheckService {

    private static final Logger log = LoggerFactory.getLogger(GrammarCheckService.class);

    private final RestClient restClient;
    private final ExternalApiProperties properties;

    public GrammarCheckService(RestClient externalRestClient, ExternalApiProperties properties) {
        this.restClient = externalRestClient;
        this.properties = properties;
    }

    public GrammarFeedbackDto check(String text) {
        ExternalApiProperties.LanguageTool config = properties.getLanguageTool();

        if (text == null || text.isBlank()) {
            return GrammarFeedbackDto.unavailable("", "Write something first.");
        }
        if (!config.isEnabled()) {
            return GrammarFeedbackDto.unavailable(text, "The writing coach is switched off.");
        }
        if (text.length() > config.getMaxTextLength()) {
            return GrammarFeedbackDto.unavailable(text,
                    "That is longer than " + config.getMaxTextLength() + " characters.");
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("text", text);
            form.add("language", config.getLanguage());

            JsonNode response = restClient.post()
                    .uri(config.getBaseUrl() + "/check")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);

            List<GrammarIssueDto> issues = toIssues(response, text);
            return new GrammarFeedbackDto(true, text, issues.size(), issues, summaryFor(issues.size()));
        } catch (Exception e) {
            log.warn("LanguageTool check failed: {}", e.toString());
            return GrammarFeedbackDto.unavailable(text,
                    "The grammar checker could not be reached. Your answer can still be submitted.");
        }
    }

    private List<GrammarIssueDto> toIssues(JsonNode response, String text) {
        List<GrammarIssueDto> issues = new ArrayList<>();
        if (response == null || !response.hasNonNull("matches")) {
            return issues;
        }

        for (JsonNode match : response.get("matches")) {
            int offset = match.path("offset").asInt(0);
            int length = match.path("length").asInt(0);

            List<String> suggestions = new ArrayList<>();
            for (JsonNode replacement : match.path("replacements")) {
                String value = replacement.path("value").asText(null);
                if (value != null && !value.isBlank()) {
                    suggestions.add(value);
                }
                if (suggestions.size() == 3) {
                    break; // three options is plenty for a learner
                }
            }

            issues.add(new GrammarIssueDto(
                    match.path("message").asText(""),
                    match.path("shortMessage").asText(null),
                    match.path("rule").path("category").path("name").asText(null),
                    excerpt(text, offset, length),
                    offset,
                    length,
                    suggestions));
        }

        return issues;
    }

    private String excerpt(String text, int offset, int length) {
        if (offset < 0 || length <= 0 || offset + length > text.length()) {
            return null;
        }
        return text.substring(offset, offset + length);
    }

    private String summaryFor(int issueCount) {
        if (issueCount == 0) {
            return "No mistakes found. Nice work!";
        }
        return issueCount == 1
                ? "One thing to look at."
                : issueCount + " things to look at.";
    }
}
