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
package org.eclipse.virgo.samples.formtags.sharedlibs.validation;

import org.eclipse.virgo.samples.formtags.sharedlibs.domain.Country;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;

import org.springframework.validation.Validator;

/**
 * Simple {@link Validator} implementation for {@link Country} instances.
 *
 */
public class CountryValidator implements Validator {

    public static final String DEFAULT_BAD_PLACEHOLDER_CODE = "-";

    private String badPlaceholderCode = DEFAULT_BAD_PLACEHOLDER_CODE;

    public void setBadPlaceholderCode(String badPlaceholderCode) {
        this.badPlaceholderCode = StringUtils.hasText(badPlaceholderCode)
                ? badPlaceholderCode : DEFAULT_BAD_PLACEHOLDER_CODE;
    }

    public boolean supports(Class<?> candidate) {
        return Country.class.isAssignableFrom(candidate);
    }

    public void validate(Object object, Errors errors) {
        Country country = (Country) object;
        if (country.getCode() == this.badPlaceholderCode) {
            errors.rejectValue("bad.country.selected", "Please select a valid country");
        }
    }

}
