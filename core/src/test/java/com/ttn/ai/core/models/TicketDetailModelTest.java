package com.ttn.ai.core.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.TicketStatusTransitionValidator;
import com.ttn.ai.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketDetailModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketService ticketService;
    private Page page;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        context.registerService(TicketService.class, ticketService);
        context.registerService(TicketStatusTransitionValidator.class,
                mock(TicketStatusTransitionValidator.class));
        page = context.create().page("/content/ticket");
    }

    @Test
    void anonymousUserIsNotLoggedInAndDoesNotLoadTicket() throws Exception {
        Resource resource = context.create().resource(page, "detail",
                "sling:resourceType", "ai-practical-assessment/components/ticket-detail");
        context.currentResource(resource);
        context.request().setQueryString("ticketId=TKT-001");

        TicketDetailModel model = context.request().adaptTo(TicketDetailModel.class);

        assertNotNull(model);
        assertFalse(model.isLoggedIn());
        assertFalse(model.isNotFound());
        verify(ticketService, never()).getTicket(org.mockito.ArgumentMatchers.anyString());
    }
}
