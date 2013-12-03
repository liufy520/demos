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
package org.eclipse.virgo.samples.formtags.par.service.internal;

import org.eclipse.virgo.samples.formtags.par.domain.Country;

import org.eclipse.virgo.samples.formtags.par.domain.User;

import org.eclipse.virgo.samples.formtags.par.service.UserManager;



import java.util.*;

/**
 * Stub {@link UserManager} implementation that maintains simple
 * in-memory state for users and countries.
 *
 */
public class StubUserManager implements UserManager {

    private Map<Integer, User> users = new TreeMap<Integer, User>();

    private Map<String, Country> countries = new TreeMap<String, Country>();

    /**
     * Creates a new instance of the {@link StubUserManager} class.
     */
    public StubUserManager() {
        loadCountries();
        loadUsers();
    }

    public void save(User user) {
        // passed in should be a clone - simply replace
        putUser(user);
    }

    public Collection<Country> findAllCountries() {
        return this.countries.values();
    }

    public Country findCountry(String code) {
        return (Country) this.countries.get(code);
    }

    public User findById(Integer id) {
        User user = (User) this.users.get(id);
        if (user != null) {
            return cloneUser(user);
        }
        return null;
    }

    public Collection<User> findAll() {
        List<User> userList = new ArrayList<User>();
        Iterator<User> itr = this.users.values().iterator();
        while (itr.hasNext()) {
            User user = (User) itr.next();
            userList.add(cloneUser(user));
        }
        return userList;
    }
    
    private void loadCountries() {
        putCountry(new Country("AT", "Austria"));
        putCountry(new Country("UK", "United Kingdom"));
        putCountry(new Country("US", "United States"));
    }

    private void loadUsers() {
        User u = new User();
        u.setId(new Integer(1));
        u.setFirstName("Harry");
        u.setLastName("Potter");
        u.setNotes("Promising Wizard...");
        u.setCountry(findCountry("UK"));
        u.setSex('M');
        u.setHouse("Gryffindor");
        u.getPreferences().setReceiveNewsletter(true);
        u.getPreferences().setInterests(new String[]{"Quidditch"});
        u.getPreferences().setFavouriteWord("Magic");
        u.setPassword("password");

        putUser(u);

        u = new User();
        u.setId(new Integer(2));
        u.setFirstName("Ronald");
        u.setLastName("Weasly");
        u.setNotes("Friends with Harry Potter.");
        u.setCountry(findCountry("UK"));
        u.setSex('M');
        u.setHouse("Gryffindor");
        u.setPassword("password");

        putUser(u);

        u = new User();
        u.setId(new Integer(3));
        u.setFirstName("Hermione");
        u.setLastName("Granger");
        u.setNotes("Friends with Harry Potter.");
        u.setCountry(findCountry("UK"));
        u.setSex('F');
        u.setHouse("Gryffindor");
        u.setPassword("password");

        putUser(u);
    }

    private void putUser(User user) {
        this.users.put(user.getId(), user);
    }

    private void putCountry(Country country) {
        this.countries.put(country.getCode(), country);
    }

    private User cloneUser(User user) {
        try {
            return (User) user.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Unable to clone user.");
        }
    }
    
}
