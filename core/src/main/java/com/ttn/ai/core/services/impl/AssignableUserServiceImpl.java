package com.ttn.ai.core.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ttn.ai.core.constants.TicketConstants;
import com.ttn.ai.core.services.AssignableUserService;
import com.ttn.ai.core.services.dto.AssignableUserDto;

/**
 * Assignable users = direct members of the {@code devs} group (UserManager).
 */
@Component(service = AssignableUserService.class)
public class AssignableUserServiceImpl implements AssignableUserService {

    private static final Logger LOG = LoggerFactory.getLogger(AssignableUserServiceImpl.class);

    @Override
    public List<AssignableUserDto> listUsers(ResourceResolver resolver) {
        List<AssignableUserDto> users = new ArrayList<>();
        if (resolver == null) {
            return users;
        }
        UserManager userManager = resolver.adaptTo(UserManager.class);
        if (userManager == null) {
            return users;
        }
        try {
            Authorizable groupAuthorizable = userManager.getAuthorizable(TicketConstants.ASSIGNEE_GROUP_ID);
            if (!(groupAuthorizable instanceof Group)) {
                LOG.warn("Assignee group '{}' not found or not a group", TicketConstants.ASSIGNEE_GROUP_ID);
                return users;
            }
            Group group = (Group) groupAuthorizable;
            Iterator<Authorizable> members = ((Group) groupAuthorizable).getMembers();
            while (members.hasNext()) {
                Authorizable member = members.next();
                String path = member.getPath();

                // Ensure it is a user (not a nested group) and not in the system path
                if (!member.isGroup() && !path.startsWith("/home/users/system")) {
                    String userId = member.getID();
                    User user = (User) member;
                    if (path != null && path.startsWith(TicketConstants.HOME_USERS_SYSTEM_PATH)) {
                        continue;
                    }
                    users.add(toDto(user));
                }
            }
        } catch (RepositoryException e) {
            LOG.error("Failed to list assignable users from group '{}'", TicketConstants.ASSIGNEE_GROUP_ID, e);
        }
        return users;
    }

    private AssignableUserDto toDto(User user) throws RepositoryException {
        String id = user.getID();
        String displayName = id;
        if (user.getProperty("profile/givenName") != null && user.getProperty("profile/givenName").length > 0) {
            displayName = user.getProperty("profile/givenName")[0].getString();
        } else if (user.getProperty("profile/familyName") != null && user.getProperty("profile/familyName").length > 0) {
            displayName = user.getProperty("profile/familyName")[0].getString();
        }
        return new AssignableUserDto(id, displayName);
    }
}
