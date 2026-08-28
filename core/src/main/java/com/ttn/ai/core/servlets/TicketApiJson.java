package com.ttn.ai.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 * JSON response helpers for ticket API servlets.
 */
final class TicketApiJson {

    private TicketApiJson() {
    }

    static void writeSuccess(HttpServletResponse response, int status, JSONObject data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject envelope = new JSONObject();
        try {
            envelope.put("success", true);
            envelope.put("data", data);
            envelope.put("error", JSONObject.NULL);
        } catch (JSONException e) {
            throw new IOException(e);
        }
        writeJson(response, envelope);
    }

    static void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("{}");
        writer.flush();
    }

    static void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject envelope = new JSONObject();
        try {
            envelope.put("success", false);
            envelope.put("data", JSONObject.NULL);
            JSONObject error = new JSONObject();
            error.put("code", code);
            error.put("message", message);
            envelope.put("error", error);
        } catch (JSONException e) {
            throw new IOException(e);
        }
        writeJson(response, envelope);
    }

    private static void writeJson(HttpServletResponse response, JSONObject json) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(json.toString());
        writer.flush();
    }
}
