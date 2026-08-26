package com.ttn.ai.core.services;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;

import com.ttn.ai.core.services.dto.AssignableUserDto;

/**
 * Lists AEM users assignable to tickets from the {@code devs} group.
 */
public interface AssignableUserService {

    List<AssignableUserDto> listUsers(ResourceResolver resolver);
}
