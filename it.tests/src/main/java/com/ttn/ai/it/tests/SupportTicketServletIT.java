package com.ttn.ai.it.tests;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.junit.rules.CQAuthorPublishClassRule;
import com.adobe.cq.testing.junit.rules.CQRule;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Integration tests for Support Ticket JSON endpoints (requires local AEM author on 4502).
 */
public class SupportTicketServletIT {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SupportTicketServletIT.class);

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
        LOG.info("Initialized admin author client for Support Ticket servlet ITs");
    }

    @Test
    public void testCreateAndListTickets() throws ClientException {
        LOG.info("Creating ticket via POST {}", TICKETS_ROOT + ".ticket.json");
        String createBody = "{\"title\":\"IT Ticket\",\"description\":\"Integration test\",\"priority\":\"medium\"}";
        StringEntity entity = new StringEntity(createBody, ContentType.APPLICATION_JSON);
        SlingHttpResponse createResponse = adminAuthor.doPost(
                TICKETS_ROOT + ".ticket.json",
                entity,
                201);
        createResponse.checkContentContains("\"success\":true");

        LOG.info("Listing tickets via GET {}", TICKETS_ROOT + ".list.json");
        SlingHttpResponse listResponse = adminAuthor.doGet(TICKETS_ROOT + ".list.json", 200);
        listResponse.checkContentContains("\"success\":true");
    }

    @Test
    public void testSearchTicketsByTitlePrefix() throws ClientException {
        String uniqueTitle = "Permission reset " + System.currentTimeMillis();
        LOG.info("Creating ticket with unique title: {}", uniqueTitle);
        String createBody = String.format(
                "{\"title\":\"%s\",\"description\":\"Search integration test\",\"priority\":\"medium\"}",
                uniqueTitle);
        StringEntity entity = new StringEntity(createBody, ContentType.APPLICATION_JSON);
        adminAuthor.doPost(TICKETS_ROOT + ".ticket.json", entity, 201);

        LOG.info("Searching tickets via GET {} with keyword=Permis", SEARCH_PATH + ".json");
        SlingHttpResponse searchResponse = adminAuthor.doGet(
                SEARCH_PATH + ".json",
                Collections.singletonList(new BasicNameValuePair("keyword", "Permis")),
                200);
            
        searchResponse.checkContentContains("\"success\":true");
        searchResponse.checkContentContains(uniqueTitle);
    }

    @Test
    public void testSearchRejectsBlankKeyword() throws ClientException {
        LOG.info("Expecting 400 for blank keyword search via GET {}", SEARCH_PATH + ".json");
        adminAuthor.doGet(
                SEARCH_PATH + ".json",
                Collections.singletonList(new BasicNameValuePair("keyword", "")),
                400);
    }

    @Test
    public void testSearchByStatusOnly() throws ClientException {
        LOG.info("Searching tickets by status via GET {} with status=open", SEARCH_PATH + ".json");
        SlingHttpResponse searchResponse = adminAuthor.doGet(
                SEARCH_PATH + ".json",
                Collections.singletonList(new BasicNameValuePair("status", "open")),
                200);
        
        searchResponse.checkContentContains("\"success\":true");
    }
}
