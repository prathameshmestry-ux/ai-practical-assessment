package com.ttn.ai.core.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.ttn.ai.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketCreateModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private Page page;

    @BeforeEach
    void setUp() {
        page = context.create().page("/content/create-ticket");
    }

    @Test
    void anonymousUserIsNotLoggedIn() {
        Resource resource = context.create().resource(page, "create",
                "sling:resourceType", "ai-practical-assessment/components/ticket-create");
        context.currentResource(resource);

        TicketCreateModel model = context.request().adaptTo(TicketCreateModel.class);

        assertNotNull(model);
        assertFalse(model.isLoggedIn());
    }
}
