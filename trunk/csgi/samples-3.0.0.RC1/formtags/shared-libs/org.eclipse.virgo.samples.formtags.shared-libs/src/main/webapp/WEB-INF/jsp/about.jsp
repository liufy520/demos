<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>



<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <title>Virgo Server, form tags showcase application</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link rel="icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />
    <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />

    <link href="<c:url value="/css/style.css"/>" rel="stylesheet" type="text/css"/>

  </head>

<body>
  <div id="main">

    <div id="topmenu">
      <a href="http://www.eclipse.org/virgo/">Virgo</a><a href="http://www.eclipse.org/forums/index.php?t=thread&frm_id=159">Virgo Forums</a><a href="http://forum.springframework.org">Spring forum</a><a href="http://www.dzone.com">Dzone.com</a><a href="http://www.infoq.com">Infoq.com</a>
    </div>

    <div id="header">
      <span class="title">Virgo Server <span class="darktitle">3.0</span> form tags</span><br/>
    </div>


    <div id="content">
       <div id="block">
          <p>
The Spring MVC form tags application showcases the new form
tag library introduced in Spring.</p>

<p>The web application is *very* simplistic, because the intent is
to convey the essence of the new form tags themselves and nothing
else.</p>

      </div>

    </div>

  <div class="lefty">
    <div class="menu">
      <c:forEach items="${userList}" var="user">
        <a href="form.htm?id=<c:out value="${user.id}"/>"><c:out value="${user.lastName}"/>, <c:out value="${user.firstName}"/></a>
      </c:forEach>

    </div>
    <div class="menu">
       <a href="<c:url value="/list.htm"/>">Home</a>
    </div>
  </div>

</div>

</body>
</html>
