package com.ttn.ai.core.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketListModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketService ticketService;
    private Page page;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        context.registerService(TicketService.class, ticketService);
        page = context.create().page("/content/dashboard");
    }

    @Test
    void anonymousUserIsNotLoggedInAndDoesNotLoadTickets() {
        Resource resource = context.create().resource(page, "dashboard",
                "sling:resourceType", "ai-practical-assessment/components/ticket-dashboard");
        context.currentResource(resource);

        TicketListModel model = context.request().adaptTo(TicketListModel.class);

        assertNotNull(model);
        assertFalse(model.isLoggedIn());
        assertTrue(model.isEmpty());
        verify(ticketService, never()).listTickets(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }
}
