package com.ttn.ai.core.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 * Shared servlet authentication and JSON parsing helpers.
 */
final class ServletAuth {

    private ServletAuth() {
    }

    static boolean isAuthenticated(SlingHttpServletRequest request) {
        String userId = request.getResourceResolver().getUserID();
        return userId != null && !"anonymous".equals(userId);
    }

    static JSONObject readJsonBody(SlingHttpServletRequest request) throws IOException, JSONException {
        try (InputStreamReader reader = new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)) {
            return new JSONObject(reader);
        }
    }
}
