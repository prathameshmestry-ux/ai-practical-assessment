package com.ttn.ai.core.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 * Shared servlet authentication, user profile, and JSON parsing helpers.
 */
public final class ServletAuth {

    static final String ANONYMOUS_USER_ID = "anonymous";
    static final String PROFILE_GIVEN_NAME = "./profile/givenName";
    static final String PROFILE_FAMILY_NAME = "./profile/familyName";
    public static final String LOGOUT_PATH = "/system/sling/logout.html";
    public static final String LOGIN_PATH = "/libs/granite/core/content/login.html";

    public static String buildLoginPath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return LOGIN_PATH;
        }
        return LOGIN_PATH + "?resource=" + URLEncoder.encode(resourcePath, StandardCharsets.UTF_8);
    }

    private ServletAuth() {
    }

    public static boolean isAuthenticated(SlingHttpServletRequest request) {
        return isAuthenticated(request.getResourceResolver());
    }

    public static boolean requireAuthenticated(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws IOException {
        if (isAuthenticated(request)) {
            return true;
        }
        TicketApiJson.writeUnauthorized(response);
        return false;
    }

    public static boolean isAuthenticated(ResourceResolver resourceResolver) {
        String userId = getUserId(resourceResolver);
        return userId != null && !ANONYMOUS_USER_ID.equals(userId);
    }

    public static String getUserId(ResourceResolver resourceResolver) {
        if (resourceResolver == null) {
            return null;
        }
        return resourceResolver.getUserID();
    }

    public static String resolveUserFullName(ResourceResolver resourceResolver) {
        if (!isAuthenticated(resourceResolver)) {
            return null;
        }
        String userId = getUserId(resourceResolver);
        try {
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            if (userManager == null) {
                return userId;
            }
            Authorizable authorizable = userManager.getAuthorizable(userId);
            if (!(authorizable instanceof User)) {
                return userId;
            }
            User user = (User) authorizable;
            String givenName = readProfileProperty(user, PROFILE_GIVEN_NAME);
            String familyName = readProfileProperty(user, PROFILE_FAMILY_NAME);
            String fullName = joinNames(givenName, familyName);
            return fullName.isEmpty() ? userId : fullName;
        } catch (RepositoryException e) {
            return userId;
        }
    }

    static JSONObject readJsonBody(SlingHttpServletRequest request) throws IOException, JSONException {
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new JSONObject(body);
    }

    private static String readProfileProperty(User user, String propertyPath) throws RepositoryException {
        if (!user.hasProperty(propertyPath)) {
            return "";
        }
        Value[] values = user.getProperty(propertyPath);
        if (values == null || values.length == 0) {
            return "";
        }
        return values[0].getString();
    }

    private static String joinNames(String givenName, String familyName) {
        String given = givenName == null ? "" : givenName.trim();
        String family = familyName == null ? "" : familyName.trim();
        if (given.isEmpty()) {
            return family;
        }
        if (family.isEmpty()) {
            return given;
        }
        return given + " " + family;
    }
}
