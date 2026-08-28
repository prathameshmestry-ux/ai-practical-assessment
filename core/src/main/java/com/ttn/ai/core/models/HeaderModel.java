package com.ttn.ai.core.models;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.ttn.ai.core.servlets.ServletAuth;

@Model(adaptables = SlingHttpServletRequest.class)
public class HeaderModel {

    @Self
    private SlingHttpServletRequest request;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ScriptVariable
    private Page currentPage;

    private String logoPath;
    private String websiteName;
    private boolean loggedIn;
    private String userFullName;
    private String userId;
    private String currentPagePath;

    @PostConstruct
    private void init() {
        Resource resource = request.getResource();
        if (resource != null) {
            logoPath = resource.getValueMap().get("logoPath", String.class);
            websiteName = resource.getValueMap().get("websiteName", String.class);
        }
        userId = ServletAuth.getUserId(resourceResolver);
        loggedIn = ServletAuth.isAuthenticated(resourceResolver);
        if (loggedIn) {
            userFullName = ServletAuth.resolveUserFullName(resourceResolver);
        }
        currentPagePath = resolveCurrentPagePath();
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public String getUserId() {
        return userId;
    }

    public String getLogoutPath() {
        return ServletAuth.LOGOUT_PATH;
    }

    public String getCurrentPagePath() {
        return currentPagePath;
    }

    public String getLoginPath() {
        return ServletAuth.buildLoginPath(currentPagePath);
    }

    private String resolveCurrentPagePath() {
        if (currentPage != null) {
            return toHtmlPagePath(currentPage.getPath());
        }
        String pathInfo = request.getPathInfo();
        if (isViewableContentPath(pathInfo)) {
            return toHtmlPagePath(pathInfo);
        }
        Resource resource = request.getResource();
        if (resource != null) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            if (pageManager != null) {
                Page page = pageManager.getContainingPage(resource);
                if (page != null && !page.getPath().startsWith("/content/experience-fragments")) {
                    return toHtmlPagePath(page.getPath());
                }
            }
        }
        return null;
    }

    private static boolean isViewableContentPath(String path) {
        return path != null && path.startsWith("/content") && !path.startsWith("/content/experience-fragments");
    }

    private static String toHtmlPagePath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        return path.endsWith(".html") ? path : path + ".html";
    }
}
