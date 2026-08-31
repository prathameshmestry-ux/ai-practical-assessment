package com.ttn.ai.core.services;

import static com.ttn.ai.core.constants.TicketConstants.PN_STATUS;
import static com.ttn.ai.core.constants.TicketConstants.PN_TICKET_ID;
import static com.ttn.ai.core.constants.TicketConstants.PN_TITLE;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.ttn.ai.core.services.dto.TicketDto;
import com.ttn.ai.core.services.impl.TicketServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jcr.Session;

@ExtendWith(AemContextExtension.class)
class TicketServiceImplTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        context.create().resource("/var/ai-practical-assessment",
                "jcr:primaryType", "sling:Folder");
        QueryBuilder queryBuilder = mock(QueryBuilder.class);
        Query query = mock(Query.class);
        SearchResult searchResult = mock(SearchResult.class);
        when(queryBuilder.createQuery(any(PredicateGroup.class), any(Session.class))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getTotalMatches()).thenReturn(0L);
        when(searchResult.getHits()).thenReturn(Collections.emptyList());
        context.registerService(QueryBuilder.class, queryBuilder);
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

    @Test
    void searchTicketsRequiresKeywordOrStatus() {
        assertThrows(TicketValidationException.class, () -> ticketService.searchTickets("  ", null, 0, 25));
        assertThrows(TicketValidationException.class, () -> ticketService.searchTickets(null, null, 0, 25));
    }

    @Test
    void searchTicketsAcceptsStatusOnly() throws Exception {
        ticketService.searchTickets(null, STATUS_OPEN, 0, 25);
    }

    @Test
    void buildSearchPredicatesUsesTitlePrefix() throws Exception {
        Map<String, String> predicates = invokeBuildSearchPredicates("permis", null);
        assertEquals(PN_TITLE, predicates.get("1_property"));
        assertEquals("permis%", predicates.get("1_property.value"));
        assertEquals("like", predicates.get("1_property.operation"));
    }

    @Test
    void buildSearchPredicatesUsesTicketIdPrefix() throws Exception {
        Map<String, String> predicates = invokeBuildSearchPredicates("ticket-8fde", null);
        assertEquals(PN_TICKET_ID, predicates.get("1_property"));
        assertEquals("ticket-8fde%", predicates.get("1_property.value"));
        assertEquals("like", predicates.get("1_property.operation"));
    }

    @Test
    void buildSearchPredicatesAddsStatusFilter() throws Exception {
        Map<String, String> predicates = invokeBuildSearchPredicates(null, STATUS_OPEN);
        assertEquals(PN_STATUS, predicates.get("1_property"));
        assertEquals(STATUS_OPEN, predicates.get("1_property.value"));
    }

    @Test
    void buildSearchPredicatesCombinesKeywordAndStatus() throws Exception {
        Map<String, String> predicates = invokeBuildSearchPredicates("permis", STATUS_OPEN);
        assertEquals(PN_TITLE, predicates.get("1_property"));
        assertEquals("permis%", predicates.get("1_property.value"));
        assertEquals(PN_STATUS, predicates.get("2_property"));
        assertEquals(STATUS_OPEN, predicates.get("2_property.value"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> invokeBuildSearchPredicates(String keyword, String status) throws Exception {
        Method method = TicketServiceImpl.class.getDeclaredMethod("buildSearchPredicates", String.class, String.class);
        method.setAccessible(true);
        return (Map<String, String>) method.invoke(ticketService, keyword, status);
    }
}
