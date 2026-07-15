package com.yumg.starter.modules.resume.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumg.starter.common.api.ApiException;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ResumeDocumentValidator {

    private static final Set<String> SECTION_TYPES = Set.of("bullet-list", "timeline", "projects", "custom");
    private final ObjectMapper objectMapper;

    public ResumeDocumentValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(String content) {
        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (JsonProcessingException exception) {
            throw ApiException.invalidParameter();
        }
        require(root != null && root.isObject());
        requireText(root.path("profile").path("name"));
        JsonNode contacts = root.path("profile").path("contacts");
        require(contacts.isArray());
        for (JsonNode contact : contacts) {
            require(contact.isObject());
            requireText(contact.path("label"));
            requireText(contact.path("text"));
        }
        JsonNode sections = root.path("sections");
        require(sections.isArray() && !sections.isEmpty());
        for (JsonNode section : sections) {
            require(section.isObject());
            requireText(section.path("id"));
            requireText(section.path("type"));
            requireText(section.path("title"));
            require(section.path("items").isArray());
            String type = section.path("type").asText();
            require(SECTION_TYPES.contains(type));
            if ("custom".equals(type)) {
                require(section.path("payload").isObject());
            }
        }
    }

    private void requireText(JsonNode node) {
        require(node.isTextual() && !node.asText().isBlank());
    }

    private void require(boolean valid) {
        if (!valid) {
            throw ApiException.invalidParameter();
        }
    }
}
