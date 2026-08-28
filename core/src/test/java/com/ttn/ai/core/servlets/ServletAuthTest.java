package com.ttn.ai.core.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;

import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.Test;

class ServletAuthTest {

    @Test
    void anonymousUserIsNotAuthenticated() {
        ResourceResolver resolver = mock(ResourceResolver.class);
        when(resolver.getUserID()).thenReturn("anonymous");

        assertFalse(ServletAuth.isAuthenticated(resolver));
        assertEquals("anonymous", ServletAuth.getUserId(resolver));
        assertNull(ServletAuth.resolveUserFullName(resolver));
    }

    @Test
    void requireAuthenticatedReturnsFalseForAnonymous() throws Exception {
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
        ResourceResolver resolver = mock(ResourceResolver.class);
        StringWriter buffer = new StringWriter();

        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.getUserID()).thenReturn("anonymous");
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(buffer));

        assertFalse(ServletAuth.requireAuthenticated(request, response));
        assertEquals("{}", buffer.toString());
    }

    @Test
    void resolvesFullNameFromProfile() throws Exception {
        ResourceResolver resolver = mock(ResourceResolver.class);
        UserManager userManager = mock(UserManager.class);
        User user = mock(User.class);
        Value given = mock(Value.class);
        Value family = mock(Value.class);

        when(resolver.getUserID()).thenReturn("jdoe");
        when(resolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("jdoe")).thenReturn(user);
        when(user.hasProperty(ServletAuth.PROFILE_GIVEN_NAME)).thenReturn(true);
        when(user.hasProperty(ServletAuth.PROFILE_FAMILY_NAME)).thenReturn(true);
        when(user.getProperty(ServletAuth.PROFILE_GIVEN_NAME)).thenReturn(new Value[] { given });
        when(user.getProperty(ServletAuth.PROFILE_FAMILY_NAME)).thenReturn(new Value[] { family });
        when(given.getString()).thenReturn("Jane");
        when(family.getString()).thenReturn("Doe");

        assertTrue(ServletAuth.isAuthenticated(resolver));
        assertEquals("Jane Doe", ServletAuth.resolveUserFullName(resolver));
    }
}
