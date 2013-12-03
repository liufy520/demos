/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.samples.formtags.sharedservices.service;

import org.eclipse.virgo.samples.formtags.sharedservices.domain.Country;
import org.eclipse.virgo.samples.formtags.sharedservices.domain.User;



import java.util.Collection;

/**
 * Central service interface for the application.
 *
 */
public interface UserManager {

    /**
     * Finds all of the {@link User Users} in the system.
     *
     * @return a {@link Collection} of all of the {@link User Users} in the system.
     */
    Collection<User> findAll();

    /**
     * Finds the specific {@link User} identified by the supplied <code>id</code>.
     *
     * @param id the value uniquely identifying a {@link User}
     * @return the located {@link User} or <code>null</code> if not found
     */
    User findById(Integer id);

    /**
     * Saves the supplied {@link User} to persistent storage.
     *
     * @param user the {@link User} to be so saved
     */
    void save(User user);

    /**
     * Finds all of the {@link Country Countries} in the system.
     *
     * @return all of the {@link Country Countries} in the system
     */
    Collection<Country> findAllCountries();

    /**
     * Finds the specific {@link Country} identified by the supplied (country) <code>code</code>.
     *
     * @param code the country code to be used to locate a specific {@link Country}
     * @return the specific {@link Country} identified by the supplied (country) <code>code</code>
     */
    Country findCountry(String code);
    
}
