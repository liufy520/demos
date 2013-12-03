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

import java.beans.PropertyEditorSupport;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.virgo.samples.formtags.sharedlibs.domain.Colour;
import org.eclipse.virgo.samples.formtags.sharedlibs.domain.Country;
import org.eclipse.virgo.samples.formtags.sharedlibs.domain.User;
import org.eclipse.virgo.samples.formtags.sharedlibs.service.UserManager;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * The central form controller for this showcase application.
 *
 */
public class FormController extends SimpleFormController {

	private UserManager userManager;

	/**
	 * Sets the {@link UserManager} to which this presentation component
	 * delegates in order to perform complex business logic.
	 * @param userManager the {@link UserManager} to which this presentation
	 *                    component delegatesin order to perform complex business logic
	 */
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Country.class, new CountryEditor(this.userManager));
        binder.registerCustomEditor(Colour.class, new PropertyEditorSupport() {
            public void setAsText(String string) throws IllegalArgumentException {
            	Integer code = new Integer(string);
                setValue(Colour.getColour(code));
            }
        });
    }

    protected Map<?, ?> referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        return new ModelMap(this.userManager.findAllCountries())
            .addAttribute("skills", getSkills())
            .addAttribute(this.userManager.findAll());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        return this.userManager.findById(new Integer(id));
    }

    protected void doSubmitAction(Object managedResource) throws Exception {
        this.userManager.save((User) managedResource);
    }

    private String[] getSkills() {
        return new String[]{
                "Potions",
                "Herbology",
                "Quidditch"
        };
    }
    
}
