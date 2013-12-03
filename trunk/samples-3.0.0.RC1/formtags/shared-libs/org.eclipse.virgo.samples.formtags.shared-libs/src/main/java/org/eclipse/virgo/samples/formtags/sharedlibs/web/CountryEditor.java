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

package org.eclipse.virgo.samples.formtags.sharedlibs.web;




import org.eclipse.virgo.samples.formtags.sharedlibs.domain.Country;
import org.eclipse.virgo.samples.formtags.sharedlibs.service.UserManager;



import java.beans.PropertyEditorSupport;



/**

 * Simple {@link java.beans.PropertyEditor} for the {@link org.eclipse.org.eclipse.virgo.samples.formtags.formtags.Country} class.

 * 


 */

public class CountryEditor extends PropertyEditorSupport {



    private UserManager userManager;





    /**

     * Creates a new instance of the {@link org.eclipse.virgo.samples.formtags.sharedlibs.web.CountryEditor} class.

     *

     * @param userManager the service object that is to be used to resolve country codes

     */

    public CountryEditor(UserManager userManager) {

        this.userManager = userManager;

    }





    public void setAsText(String text) throws IllegalArgumentException {

        setValue(this.userManager.findCountry(text));

    }



    public String getAsText() {

        if (getValue() == null) {

            return "";

        }

        return ((Country) getValue()).getCode();

    }



}

