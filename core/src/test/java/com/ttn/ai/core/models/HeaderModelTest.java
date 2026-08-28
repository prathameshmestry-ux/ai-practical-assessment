package com.ttn.ai.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.ttn.ai.core.servlets.ServletAuth;
import com.ttn.ai.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class HeaderModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private Page page;

    @BeforeEach
    void setUp() {
        page = context.create().page("/content/mypage");
    }

    @Test
    void exposesLogoPathAndWebsiteName() {
        Resource resource = context.create().resource(page, "header",
                "sling:resourceType", "ai-practical-assessment/components/header",
                "logoPath", "/content/dam/ai-practical-assessment/logo.png",
                "websiteName", "AI Capability");
        context.currentPage(page);
        context.currentResource(resource);

        HeaderModel model = context.request().adaptTo(HeaderModel.class);

        assertNotNull(model);
        assertEquals("/content/dam/ai-practical-assessment/logo.png", model.getLogoPath());
        assertEquals("AI Capability", model.getWebsiteName());
    }

    @Test
    void anonymousUserLoginIncludesCurrentPageResource() {
        Resource resource = context.create().resource(page, "empty-header",
                "sling:resourceType", "ai-practical-assessment/components/header");
        context.currentPage(page);
        context.currentResource(resource);

        HeaderModel model = context.request().adaptTo(HeaderModel.class);

        assertNotNull(model);
        assertFalse(model.isLoggedIn());
        assertNull(model.getUserFullName());
        assertEquals("/content/mypage.html", model.getCurrentPagePath());
        assertEquals(ServletAuth.buildLoginPath("/content/mypage.html"), model.getLoginPath());
        assertEquals(ServletAuth.LOGOUT_PATH, model.getLogoutPath());
    }
}
