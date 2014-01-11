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
 * Simple enumeration for common colors.
 *
 */
public enum Colour {

	RED(0, "RED"),
	
	GREEN(1, "GREEN"),
	
	BLUE(2, "BLUE");
	
	private final int code;
	
	private final String label;
	
    private Colour(int code, String label) {
        this.code = code;
        this.label = label;
    }

	public String getLabel() {
		return this.label;
	}

	public int getCode() {
		return this.code;
	}
	
	public static Colour getColour(int code){
		Colour[] values = Colour.values();
		for (Colour colour : values) {
			if(colour.getCode() == code){
				return colour;
			}
		}
		return null;
	}

}

