package com.ttn.ai.core.servlets;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import com.ttn.ai.core.services.dto.CommentDto;
import com.ttn.ai.core.services.dto.TicketDto;
import com.ttn.ai.core.services.dto.TicketListResult;

/**
 * Maps ticket DTOs to JSON objects for API responses.
 */
final class TicketJsonMapper {

    private TicketJsonMapper() {
    }

    static JSONObject toJson(TicketDto ticket, boolean includeComments) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ticketId", ticket.getTicketId());
        json.put("path", ticket.getPath());
        json.put("title", ticket.getTitle());
        json.put("description", ticket.getDescription());
        json.put("priority", ticket.getPriority());
        json.put("status", ticket.getStatus());
        json.put("requester", ticket.getRequester());
        json.put("assignee", ticket.getAssignee());
        json.put("created", ticket.getCreated());
        json.put("lastModified", ticket.getLastModified());
        if (includeComments && ticket.getComments() != null) {
            JSONArray comments = new JSONArray();
            for (CommentDto comment : ticket.getComments()) {
                comments.put(toJson(comment));
            }
            json.put("comments", comments);
        }
        return json;
    }

    static JSONObject toJson(CommentDto comment) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("commentId", comment.getCommentId());
        json.put("text", comment.getText());
        json.put("author", comment.getAuthor());
        json.put("created", comment.getCreated());
        return json;
    }

    static JSONObject toJson(TicketListResult result) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("total", result.getTotal());
        json.put("offset", result.getOffset());
        json.put("limit", result.getLimit());
        JSONArray tickets = new JSONArray();
        for (TicketDto ticket : result.getTickets()) {
            tickets.put(toJson(ticket, false));
        }
        json.put("tickets", tickets);
        return json;
    }
}
