package com.ttn.ai.core.services;

import static com.ttn.ai.core.constants.TicketConstants.STATUS_OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.search.QueryBuilder;
import com.ttn.ai.core.services.dto.TicketDto;
import com.ttn.ai.core.services.impl.TicketServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketServiceImplTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        context.create().resource("/content/ai-practical-assessment",
                "jcr:primaryType", "sling:Folder");
        context.registerService(QueryBuilder.class, mock(QueryBuilder.class));
        context.registerInjectActivateService(new TicketStatusTransitionValidator());
        ticketService = context.registerInjectActivateService(new TicketServiceImpl());
    }

    @Test
    void createsTicketWithOpenStatus() throws Exception {
        TicketDto ticket = ticketService.createTicket("Title", "Description", "high", "admin");
        assertNotNull(ticket.getTicketId());
        assertEquals(STATUS_OPEN, ticket.getStatus());
        assertEquals("admin", ticket.getRequester());
    }
}
