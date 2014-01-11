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

package org.eclipse.virgo.samples.formtags.sharedservices.domain;



/**

 * Models a {@link User User's} preferences.

 *


 */

public class Preferences {





    private boolean receiveNewsletter;

    private String[] interests;

    private String favouriteWord;





    public boolean isReceiveNewsletter() {

        return receiveNewsletter;

    }



    public void setReceiveNewsletter(boolean receiveNewsletter) {

        this.receiveNewsletter = receiveNewsletter;

    }



    public String[] getInterests() {

        return interests;

    }



    public void setInterests(String[] interests) {

        this.interests = interests;

    }



    public String getFavouriteWord() {

        return favouriteWord;

    }



    public void setFavouriteWord(String favouriteWord) {

        this.favouriteWord = favouriteWord;

    }



}

