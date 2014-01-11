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

package org.eclipse.virgo.samples.formtags.par.domain;



/**
 * Models a country.
 *
 */
public class Country implements Comparable<Country> {

    private String code;

    private String name;

    /**
     * Creates a new instance of this {@link Country} class.
     */
    public Country() {
    }

    /**
     * Creates a new instance of this {@link Country} class.
     *
     * @param code the country code
     * @param name the name of the country
     */
    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }

    public int compareTo(Country c) {
        return this.code.compareTo(c.code);
    }

}
