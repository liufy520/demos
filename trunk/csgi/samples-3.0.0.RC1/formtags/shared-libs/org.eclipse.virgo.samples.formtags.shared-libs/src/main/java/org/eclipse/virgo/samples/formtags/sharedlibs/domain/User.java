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
package org.eclipse.virgo.samples.formtags.sharedlibs.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a user.
 *
 */
public class User implements Cloneable {

    private Integer id;

    private String firstName;

    private String lastName;

    private String notes;

    private String house;

    private Country country = new Country();

    private Colour favouriteColour = Colour.RED;

    private List<String> skills = new ArrayList<String>();

    private char sex;

    private String password;

    private Preferences preferences = new Preferences();

    private String secretWord;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Colour getFavouriteColour() {
        return favouriteColour;
    }

    public void setFavouriteColour(Colour favouriteColour) {
        this.favouriteColour = favouriteColour;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretWord() {
        return secretWord;
    }
    
    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
