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



import org.eclipse.virgo.samples.formtags.sharedlibs.service.UserManager;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.servlet.mvc.AbstractController;



import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;



/**

 * Simple {@link org.springframework.web.servlet.mvc.Controller} implementation

 * that pretty much locates (and thus allows a {@link org.springframework.web.servlet.View}

 * to render a list) of all of the {@link org.eclipse.virgo.samples.formtags.formtags.domain.User Users}

 * in the application.

 *


 */

public class ListController extends AbstractController {





    private UserManager userManager;

    private String viewName;



    public void setViewName(String viewName) {

        this.viewName = viewName;

    }



    /**

     * Sets the {@link UserManager} that to which this presentation component delegates

     * in order to perform complex business logic.

     *

     * @param userManager the {@link UserManager} that to which this presentation component delegates

     *                    in order to perform complex business logic

     */

    public void setUserManager(UserManager userManager) {

        this.userManager = userManager;

    }





    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        return new ModelAndView(viewName).addObject(this.userManager.findAll());

    }



}

