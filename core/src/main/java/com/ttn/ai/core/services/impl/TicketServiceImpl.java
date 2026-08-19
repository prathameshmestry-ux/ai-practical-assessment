package com.ttn.ai.core.services;

import static com.ttn.ai.core.constants.TicketConstants.COMMENTS_NODE_NAME;
import static com.ttn.ai.core.constants.TicketConstants.PN_ASSIGNEE;
import static com.ttn.ai.core.constants.TicketConstants.PN_AUTHOR;
import static com.ttn.ai.core.constants.TicketConstants.PN_COMMENT_ID;
import static com.ttn.ai.core.constants.TicketConstants.PN_CREATED;
import static com.ttn.ai.core.constants.TicketConstants.PN_DESCRIPTION;
import static com.ttn.ai.core.constants.TicketConstants.PN_LAST_MODIFIED;
import static com.ttn.ai.core.constants.TicketConstants.PN_PRIORITY;
import static com.ttn.ai.core.constants.TicketConstants.PN_REQUESTER;
import static com.ttn.ai.core.constants.TicketConstants.PN_STATUS;
import static com.ttn.ai.core.constants.TicketConstants.PN_TEXT;
import static com.ttn.ai.core.constants.TicketConstants.PN_TICKET_ID;
import static com.ttn.ai.core.constants.TicketConstants.PN_TITLE;
import static com.ttn.ai.core.constants.TicketConstants.PRIORITY_HIGH;
import static com.ttn.ai.core.constants.TicketConstants.PRIORITY_LOW;
import static com.ttn.ai.core.constants.TicketConstants.PRIORITY_MEDIUM;
import static com.ttn.ai.core.constants.TicketConstants.RT_TICKET;
import static com.ttn.ai.core.constants.TicketConstants.RT_TICKETS_ROOT;
import static com.ttn.ai.core.constants.TicketConstants.SERVICE_USER_SUBSERVICE;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_OPEN;
import static com.ttn.ai.core.constants.TicketConstants.TICKET_ROOT_PATH;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.ttn.ai.core.config.AssigneeConfig;
import com.ttn.ai.core.services.dto.CommentDto;
import com.ttn.ai.core.services.dto.TicketDto;
import com.ttn.ai.core.services.dto.TicketListResult;

/**
 * JCR-backed ticket service using a service resource resolver.
 */
@Component(service = TicketService.class)
@Designate(ocd = TicketServiceImpl.Config.class)
public class TicketServiceImpl implements TicketService {

    private static final Logger LOG = LoggerFactory.getLogger(TicketServiceImpl.class);
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_INSTANT;

    @ObjectClassDefinition(name = "Support Ticket Service Configuration")
    public @interface Config {

        @AttributeDefinition(name = "Ticket root path")
        String ticketRootPath() default TICKET_ROOT_PATH;

        @AttributeDefinition(name = "Default page size")
        int defaultPageSize() default 25;

        @AttributeDefinition(name = "Max page size")
        int maxPageSize() default 100;
    }

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private TicketStatusTransitionValidator statusValidator;

    @Reference
    private AssigneeConfig assigneeConfig;

    private String ticketRootPath;
    private int defaultPageSize;
    private int maxPageSize;

    @Activate
    protected void activate(Config config) {
        ticketRootPath = config.ticketRootPath();
        defaultPageSize = config.defaultPageSize();
        maxPageSize = config.maxPageSize();
    }

    @Override
    public TicketDto createTicket(String title, String description, String priority, String requester)
            throws TicketValidationException {
        validateTitle(title);
        validateDescription(description);
        String normalizedPriority = normalizePriority(priority);
        if (requester == null || requester.isBlank()) {
            throw new TicketValidationException("Requester is required");
        }

        String ticketId = "ticket-" + UUID.randomUUID().toString();
        try (ResourceResolver resolver = getServiceResolver()) {
            Resource root = ensureTicketRoot(resolver);
            Map<String, Object> props = new HashMap<>();
            props.put("jcr:primaryType", "nt:unstructured");
            props.put("sling:resourceType", RT_TICKET);
            Resource ticketResource = resolver.create(root, ticketId, props);
            ModifiableValueMap properties = ticketResource.adaptTo(ModifiableValueMap.class);
            if (properties == null) {
                throw new TicketValidationException("Unable to create ticket");
            }
            Calendar now = nowCalendar();
            properties.put(PN_TICKET_ID, ticketId);
            properties.put(PN_TITLE, title.trim());
            properties.put(PN_DESCRIPTION, description.trim());
            properties.put(PN_PRIORITY, normalizedPriority);
            properties.put(PN_STATUS, STATUS_OPEN);
            properties.put(PN_REQUESTER, requester);
            properties.put(PN_CREATED, now);
            properties.put(PN_LAST_MODIFIED, now);
            resolver.create(ticketResource, COMMENTS_NODE_NAME,
                    Map.of("jcr:primaryType", "nt:unstructured"));
            resolver.commit();
            return toTicketDto(ticketResource, false);
        } catch (LoginException | PersistenceException e) {
            LOG.error("Failed to create ticket", e);
            throw new TicketValidationException("Failed to create ticket");
        }
    }

    @Override
    public TicketDto getTicket(String ticketId) throws TicketNotFoundException {
        try (ResourceResolver resolver = getServiceResolver()) {
            Resource ticketResource = getTicketResource(resolver, ticketId);
            if (ticketResource == null) {
                throw new TicketNotFoundException(ticketId);
            }
            return toTicketDto(ticketResource, true);
        } catch (LoginException e) {
            LOG.error("Failed to read ticket {}", ticketId, e);
            throw new TicketNotFoundException(ticketId);
        }
    }

    @Override
    public TicketListResult listTickets(int offset, int limit) {
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(0, offset);
        TicketListResult result = new TicketListResult();
        result.setOffset(safeOffset);
        result.setLimit(safeLimit);

        try (ResourceResolver resolver = getServiceResolver()) {
            Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                result.setTotal(0);
                return result;
            }
            Map<String, String> predicates = new HashMap<>();
            predicates.put("path", ticketRootPath);
            predicates.put("type", "nt:unstructured");
            predicates.put("property", PN_TICKET_ID);
            predicates.put("property.operation", "exists");
            predicates.put("orderby", "@lastModified");
            predicates.put("orderby.sort", "desc");
            predicates.put("p.offset", String.valueOf(safeOffset));
            predicates.put("p.limit", String.valueOf(safeLimit));
            predicates.put("p.guessTotal", "true");

            Query query = queryBuilder.createQuery(PredicateGroup.create(predicates), session);
            SearchResult searchResult = query.getResult();
            result.setTotal(searchResult.getTotalMatches());
            List<TicketDto> tickets = new ArrayList<>();
            for (Hit hit : searchResult.getHits()) {
                Resource resource = hit.getResource();
                if (resource != null) {
                    tickets.add(toTicketDto(resource, false));
                }
            }
            result.setTickets(tickets);
            return result;
        } catch (LoginException | RepositoryException e) {
            LOG.error("Failed to list tickets", e);
            result.setTotal(0);
            result.setTickets(Collections.emptyList());
            return result;
        }
    }

    @Override
    public TicketDto updateTicket(String ticketId, String title, String description, String priority, String assignee)
            throws TicketNotFoundException, TicketValidationException {
        try (ResourceResolver resolver = getServiceResolver()) {
            Resource ticketResource = getTicketResource(resolver, ticketId);
            if (ticketResource == null) {
                throw new TicketNotFoundException(ticketId);
            }
            ModifiableValueMap properties = ticketResource.adaptTo(ModifiableValueMap.class);
            if (properties == null) {
                throw new TicketValidationException("Unable to update ticket");
            }
            if (title != null) {
                validateTitle(title);
                properties.put(PN_TITLE, title.trim());
            }
            if (description != null) {
                validateDescription(description);
                properties.put(PN_DESCRIPTION, description.trim());
            }
            if (priority != null) {
                properties.put(PN_PRIORITY, normalizePriority(priority));
            }
            if (assignee != null) {
                if (assignee.isBlank()) {
                    properties.remove(PN_ASSIGNEE);
                } else if (!assigneeConfig.isValidAssignee(assignee)) {
                    throw new TicketValidationException("Invalid assignee");
                } else {
                    properties.put(PN_ASSIGNEE, assignee);
                }
            }
            properties.put(PN_LAST_MODIFIED, nowCalendar());
            resolver.commit();
            return toTicketDto(ticketResource, true);
        } catch (LoginException | PersistenceException e) {
            LOG.error("Failed to update ticket {}", ticketId, e);
            throw new TicketValidationException("Failed to update ticket");
        }
    }

    @Override
    public CommentDto addComment(String ticketId, String text, String author)
            throws TicketNotFoundException, TicketValidationException {
        validateCommentText(text);
        if (author == null || author.isBlank()) {
            throw new TicketValidationException("Author is required");
        }
        try (ResourceResolver resolver = getServiceResolver()) {
            Resource ticketResource = getTicketResource(resolver, ticketId);
            if (ticketResource == null) {
                throw new TicketNotFoundException(ticketId);
            }
            Resource commentsFolder = ticketResource.getChild(COMMENTS_NODE_NAME);
            if (commentsFolder == null) {
                commentsFolder = resolver.create(ticketResource, COMMENTS_NODE_NAME,
                        Map.of("jcr:primaryType", "nt:unstructured"));
            }
            String commentId = "comment-" + UUID.randomUUID().toString();
            Resource commentResource = resolver.create(commentsFolder, commentId,
                    Map.of("jcr:primaryType", "nt:unstructured"));
            ModifiableValueMap properties = commentResource.adaptTo(ModifiableValueMap.class);
            if (properties == null) {
                throw new TicketValidationException("Unable to add comment");
            }
            Calendar now = nowCalendar();
            properties.put(PN_COMMENT_ID, commentId);
            properties.put(PN_TEXT, text.trim());
            properties.put(PN_AUTHOR, author);
            properties.put(PN_CREATED, now);

            ModifiableValueMap ticketProps = ticketResource.adaptTo(ModifiableValueMap.class);
            if (ticketProps != null) {
                ticketProps.put(PN_LAST_MODIFIED, now);
            }
            resolver.commit();
            return toCommentDto(commentResource);
        } catch (LoginException | PersistenceException e) {
            LOG.error("Failed to add comment on ticket {}", ticketId, e);
            throw new TicketValidationException("Failed to add comment");
        }
    }

    @Override
    public TicketDto updateStatus(String ticketId, String newStatus)
            throws TicketNotFoundException, InvalidStatusTransitionException {
        try (ResourceResolver resolver = getServiceResolver()) {
            Resource ticketResource = getTicketResource(resolver, ticketId);
            if (ticketResource == null) {
                throw new TicketNotFoundException(ticketId);
            }
            ValueMap valueMap = ticketResource.getValueMap();
            String currentStatus = valueMap.get(PN_STATUS, String.class);
            statusValidator.validateTransition(currentStatus, newStatus);
            ModifiableValueMap properties = ticketResource.adaptTo(ModifiableValueMap.class);
            if (properties != null) {
                properties.put(PN_STATUS, newStatus);
                properties.put(PN_LAST_MODIFIED, nowCalendar());
            }
            resolver.commit();
            return toTicketDto(ticketResource, true);
        } catch (LoginException | PersistenceException e) {
            LOG.error("Failed to update status for ticket {}", ticketId, e);
            throw new TicketNotFoundException(ticketId);
        }
    }

    private Resource ensureTicketRoot(ResourceResolver resolver) throws PersistenceException {
        Resource root = resolver.getResource(ticketRootPath);
        if (root == null) {
            Resource parent = resolver.getResource("/content/ai-practical-assessment");
            if (parent == null) {
                throw new PersistenceException("Parent content path missing");
            }
            Map<String, Object> props = new HashMap<>();
            props.put("jcr:primaryType", "nt:unstructured");
            props.put("sling:resourceType", RT_TICKETS_ROOT);
            root = resolver.create(parent, "support-tickets", props);
            resolver.commit();
        }
        return root;
    }

    private Resource getTicketResource(ResourceResolver resolver, String ticketId) {
        return resolver.getResource(ticketRootPath + "/" + ticketId);
    }

    private ResourceResolver  getServiceResolver() throws LoginException {
        Map<String, Object> authInfo = Map.of(ResourceResolverFactory.SUBSERVICE, SERVICE_USER_SUBSERVICE);
        return resourceResolverFactory.getServiceResourceResolver(authInfo);
    }

    private TicketDto toTicketDto(Resource ticketResource, boolean includeComments) {
        ValueMap valueMap = ticketResource.getValueMap();
        TicketDto dto = new TicketDto();
        dto.setTicketId(valueMap.get(PN_TICKET_ID, String.class));
        dto.setPath(ticketResource.getPath());
        dto.setTitle(valueMap.get(PN_TITLE, String.class));
        dto.setDescription(valueMap.get(PN_DESCRIPTION, String.class));
        dto.setPriority(valueMap.get(PN_PRIORITY, String.class));
        dto.setStatus(valueMap.get(PN_STATUS, String.class));
        dto.setRequester(valueMap.get(PN_REQUESTER, String.class));
        dto.setAssignee(valueMap.get(PN_ASSIGNEE, String.class));
        dto.setCreated(formatDate(valueMap.get(PN_CREATED, Calendar.class)));
        dto.setLastModified(formatDate(valueMap.get(PN_LAST_MODIFIED, Calendar.class)));
        if (includeComments) {
            Resource commentsFolder = ticketResource.getChild(COMMENTS_NODE_NAME);
            if (commentsFolder != null) {
                List<CommentDto> comments = new ArrayList<>();
                for (Resource child : commentsFolder.getChildren()) {
                    if (!ResourceUtil.isNonExistingResource(child)) {
                        comments.add(toCommentDto(child));
                    }
                }
                comments.sort((a, b) -> a.getCreated().compareTo(b.getCreated()));
                dto.setComments(comments);
            }
        }
        return dto;
    }

    private CommentDto toCommentDto(Resource commentResource) {
        ValueMap valueMap = commentResource.getValueMap();
        CommentDto dto = new CommentDto();
        dto.setCommentId(valueMap.get(PN_COMMENT_ID, String.class));
        dto.setText(valueMap.get(PN_TEXT, String.class));
        dto.setAuthor(valueMap.get(PN_AUTHOR, String.class));
        dto.setCreated(formatDate(valueMap.get(PN_CREATED, Calendar.class)));
        return dto;
    }

    private String formatDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return ISO_FORMAT.format(calendar.toInstant());
    }

    private Calendar nowCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Instant.now().toEpochMilli());
        return calendar;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return defaultPageSize;
        }
        return Math.min(limit, maxPageSize);
    }

    private void validateTitle(String title) throws TicketValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new TicketValidationException("Title is required");
        }
        if (title.length() > 200) {
            throw new TicketValidationException("Title exceeds 200 characters");
        }
    }

    private void validateDescription(String description) throws TicketValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new TicketValidationException("Description is required");
        }
        if (description.length() > 5000) {
            throw new TicketValidationException("Description exceeds 5000 characters");
        }
    }

    private void validateCommentText(String text) throws TicketValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new TicketValidationException("Comment text is required");
        }
        if (text.length() > 4000) {
            throw new TicketValidationException("Comment exceeds 4000 characters");
        }
    }

    private String normalizePriority(String priority) throws TicketValidationException {
        if (priority == null || priority.isBlank()) {
            return PRIORITY_MEDIUM;
        }
        String normalized = priority.trim().toLowerCase();
        if (!PRIORITY_LOW.equals(normalized) && !PRIORITY_MEDIUM.equals(normalized)
                && !PRIORITY_HIGH.equals(normalized)) {
            throw new TicketValidationException("Invalid priority");
        }
        return normalized;
    }
}
