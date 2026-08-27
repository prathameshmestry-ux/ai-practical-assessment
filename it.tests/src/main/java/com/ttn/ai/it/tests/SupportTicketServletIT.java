package com.ttn.ai.it.tests;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.junit.rules.CQAuthorPublishClassRule;
import com.adobe.cq.testing.junit.rules.CQRule;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration tests for Support Ticket JSON endpoints (requires local AEM author on 4502).
 */
public class SupportTicketServletIT {

    private static final String TICKETS_ROOT = "/var/ai-practical-assessment/tickets";
    private static final String SEARCH_PATH = "/bin/ai-practical-assessment/search";

    @ClassRule
    public static final CQAuthorPublishClassRule cqBaseClassRule = new CQAuthorPublishClassRule();

    @Rule
    public CQRule cqBaseRule = new CQRule(cqBaseClassRule.authorRule, cqBaseClassRule.publishRule);

    static CQClient adminAuthor;

    @BeforeClass
    public static void beforeClass() throws ClientException {
        adminAuthor = cqBaseClassRule.authorRule.getAdminClient(CQClient.class);
    }

    @Test
    public void testCreateAndListTickets() throws ClientException {
        String createBody = "{\"title\":\"IT Ticket\",\"description\":\"Integration test\",\"priority\":\"medium\"}";
        StringEntity entity = new StringEntity(createBody, ContentType.APPLICATION_JSON);
        SlingHttpResponse createResponse = adminAuthor.doPost(
                TICKETS_ROOT + ".ticket.json",
                entity,
                201);
        createResponse.checkContentContains("\"success\":true");

        SlingHttpResponse listResponse = adminAuthor.doGet(TICKETS_ROOT + ".list.json", 200);
        listResponse.checkContentContains("\"success\":true");
    }

    @Test
    public void testSearchTicketsByTitlePrefix() throws ClientException {
        String uniqueTitle = "Permission reset " + System.currentTimeMillis();
        String createBody = String.format(
                "{\"title\":\"%s\",\"description\":\"Search integration test\",\"priority\":\"medium\"}",
                uniqueTitle);
        StringEntity entity = new StringEntity(createBody, ContentType.APPLICATION_JSON);
        adminAuthor.doPost(TICKETS_ROOT + ".ticket.json", entity, 201);

        SlingHttpResponse searchResponse = adminAuthor.doGet(
                SEARCH_PATH + ".json?keyword=permis",
                200);
        searchResponse.checkContentContains("\"success\":true");
        searchResponse.checkContentContains(uniqueTitle);
    }

    @Test
    public void testSearchRejectsBlankKeyword() throws ClientException {
        adminAuthor.doGet(SEARCH_PATH + ".json?keyword=", 400);
    }

    @Test
    public void testSearchByStatusOnly() throws ClientException {
        SlingHttpResponse searchResponse = adminAuthor.doGet(
                SEARCH_PATH + ".json?status=open",
                200);
        searchResponse.checkContentContains("\"success\":true");
    }
}
