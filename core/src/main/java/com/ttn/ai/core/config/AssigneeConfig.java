package com.ttn.ai.core.config;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * OSGi configuration for allowed ticket assignees.
 */
@Component(service = AssigneeConfig.class)
@Designate(ocd = AssigneeConfig.Config.class)
public class AssigneeConfig {

    @ObjectClassDefinition(name = "Support Ticket Assignee Configuration")
    public @interface Config {

        @AttributeDefinition(name = "Assignees", description = "Allowed assignee user IDs")
        String[] assignees() default {"admin"};
    }

    private List<String> assignees;

    @Activate
    protected void activate(Config config) {
        if (config.assignees() == null) {
            assignees = Collections.emptyList();
        } else {
            assignees = Arrays.asList(config.assignees());
        }
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public boolean isValidAssignee(String assigneeId) {
        return assigneeId != null && assignees.contains(assigneeId);
    }
}
