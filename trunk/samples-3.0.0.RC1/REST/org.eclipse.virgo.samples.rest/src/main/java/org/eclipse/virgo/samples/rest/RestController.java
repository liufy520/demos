/*******************************************************************************
 * Copyright (c) 2013 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.samples.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * {@link RestController} uses Spring MVC to implement a REST service which associates names and inventions with
 * "user ids".
 * <p/>
 * GET requests may specify a user id, e.g.:
 * 
 * <pre>
 * GET /rest/users/roy HTTP/1.1
 * Accept: application/json
 * </pre>
 * 
 * or may be used to inquire all users, e.g.:
 * 
 * <pre>
 * GET /rest/users HTTP/1.1
 * Accept: application/json
 * </pre>
 * 
 * whereas PUT requests specify a user id, a name, and an invention, e.g.:
 * 
 * <pre>
 * PUT /rest/users/james/Sir%20James%20Dyson/Cyclonic%20Separation HTTP/1.1
 * </pre>
 * 
 * </p>
 * You can use curl to drive this program as follows:
 * 
 * <pre>
 * curl -i -H "Accept: application/json" http://localhost:8080/rest/users/roy
 * curl -i -X PUT http://localhost:8080/rest/users/james/Sir%20James%20Dyson/Cyclonic%20Separation
 * curl -i -H "Accept: application/json" http://localhost:8080/rest/users/james
 * curl -i -H "Accept: application/json" http://localhost:8080/rest/users
 * </pre>
 * 
 * The implementation is deliberately primitive.
 * <p/>
 * Please consult the following for more information on REST and its support in Spring:
 * <p/>
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Representational_state_transfer">Representational State Transfer</a>
 * (Wikipedia article)
 * <li><a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm">Architectural Styles and the Design of
 * Network-based Software Architectures</a> (Roy Fielding's REST dissertation)</li>
 * <li><a href="http://static.springsource.org/spring/docs/3.1.0.RELEASE/reference/html/mvc.html">Spring Web MVC
 * framework<a/></li>
 * <li><a href="http://static.springsource.org/spring-roo/reference/html/base-json.html">Spring Roo JSON Add-On</a></li>
 * </ul>
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
@Controller
public final class RestController {

    private Map<String, Info> model = Collections.synchronizedMap(new HashMap<String, Info>());

    public RestController() {
        this.model.put("roy", new Info("Roy T. Fielding", "Representational State Transfer"));
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getUsers() {
        return createResponseEntity(toJson(), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getUser(@PathVariable("userId") String userId) {
        Info info = model.get(userId);
        if (info != null) {
            return createResponseEntity(info.toJson(), HttpStatus.OK);
        } else {
            return createResponseEntity("", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/users/{userId}/{name}/{invention}", method = RequestMethod.PUT)
    public void putUser(@PathVariable("userId") String userId, @PathVariable("name") String name, @PathVariable("invention") String invention,
        HttpServletResponse httpServletResponse) {
        this.model.put(userId, new Info(name, invention));
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    }

    public ResponseEntity<String> createResponseEntity(String json, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(json + "\n", headers, status);
    }
    
    private String toJson() {
        StringBuffer json = new StringBuffer();
        boolean first = true;
        json.append("[");
        for (String name : this.model.keySet()) {
            if (first) {
                first = false;
            } else {
                json.append(", ");
            }
            json.append("/rest/users/" + name);
        }
        json.append("]");
        return json.toString();
    }

    private class Info {

        private String name;

        private String invention;

        Info(String name, String invention) {
            this.name = name;
            this.invention = invention;
        }

        String toJson() {
            return "{ \"name\" : \"" + this.name + "\", \"invention\" : \"" + this.invention + "\" }";
        }
    }
}
