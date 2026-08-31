package com.ttn.ai.core.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TicketApiJsonTest {

    @Test
    void writeUnauthorizedReturnsEmptyJsonObject() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StringWriter buffer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(buffer));

        TicketApiJson.writeUnauthorized(response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals("{}", buffer.toString());
    }
}
