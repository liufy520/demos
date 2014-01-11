/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package liufy.demos.servlet3;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author liufengyu
 * @date 2014年1月11日
 */
@WebServlet("/FirstServlet")
public class FirstServlet extends HttpServlet {
    
    final static Logger LOG = LoggerFactory.getLogger(FirstServlet.class);
    
    private static final long serialVersionUID = 6833688147345340845L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.info("hello");
        PrintWriter out = response.getWriter();
        out.print("first servlet");
        out.flush();
        out.close();
    }
}
