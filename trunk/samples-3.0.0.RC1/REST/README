Spring REST sample project
==========================

This sample is a simple REST service implemented as an OSGi standard Web Application
Bundle and using Spring MVC with annotations. For more information, refer to:

org.eclipse.virgo.samples.rest/src/main/java/org/eclipse/virgo/samples/rest/RestController.java

It was tested with Virgo Server for Apache Tomcat 3.6.0.RELEASE on Java 6.

You can build and deploy it from the command line or in Eclipse.


Building and Deploying from the Command Line
============================================

Pre-req's: Java 6 or later and Ant 1.8.x or later (tested with 1.8.2).

Build the sample by changing directory to org.eclipse.virgo.samples.rest and
issuing the command:

$ ant clean jar

If the build is successful, copy:

target/artifacts/org.eclipse.virgo.samples.rest.jar

to Virgo's pickup directory and start the server. You should see the following
messages:

... <DE0004I> Starting bundle 'org.eclipse.virgo.samples.rest' version '1.0.0'. 
... <WE0000I> Starting web bundle 'org.eclipse.virgo.samples.rest' version '1.0.0' with context path '/rest'. 
... <WE0001I> Started web bundle 'org.eclipse.virgo.samples.rest' version '1.0.0' with context path '/rest'. 
... <DE0005I> Started bundle 'org.eclipse.virgo.samples.rest' version '1.0.0'.


Building and Deploying from Eclipse
===================================

Pre-req's: Java 6 or later and Eclipse Java EE Juno or later (tested with Juno SR1)

Install the Virgo IDE tooling as described here:

http://wiki.eclipse.org/Virgo/Tooling#Installation

Import the project org.eclipse.virgo.samples.rest into Eclipse. Define a Virgo
server for Virgo Server for Apache Tomcat 3.6.0.RELEASE or later. For help, refer
to the Virgo Tooling Guide at:

http://www.eclipse.org/virgo/documentation/

Start the server. Drag and drop the project org.eclipse.virgo.samples.rest onto
the server. You should see the following messages on the console:

... <DE0004I> Starting bundle 'org.eclipse.virgo.samples.rest' version '1.0.0'. 
... <WE0000I> Starting web bundle 'org.eclipse.virgo.samples.rest' version '1.0.0' with context path '/rest'. 
... <WE0001I> Started web bundle 'org.eclipse.virgo.samples.rest' version '1.0.0' with context path '/rest'. 
... <DE0005I> Started bundle 'org.eclipse.virgo.samples.rest' version '1.0.0'.


Using the Spring REST Sample
============================

From a new terminal, you can now drive the sample using curl, e.g.:

$ curl -i -H "Accept: application/json" http://localhost:8080/rest/users/roy
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=utf-8
Content-Length: 79
Date: Fri, 25 Jan 2013 11:22:59 GMT

{ "name" : "Roy T. Fielding", "invention" : "Representational State Transfer" }

$ curl -i -X PUT http://localhost:8080/rest/users/rod/Rod%20Johnson/Spring%20Framework
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Length: 0
Date: Fri, 25 Jan 2013 11:24:35 GMT

$ curl -i -H "Accept: application/json" http://localhost:8080/rest/users/rod
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=utf-8
Content-Length: 60
Date: Fri, 25 Jan 2013 11:25:11 GMT

{ "name" : "Rod Johnson", "invention" : "Spring Framework" }

$ curl -i -H "Accept: application/json" http://localhost:8080/rest/users
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=utf-8
Content-Length: 34
Date: Fri, 25 Jan 2013 11:25:37 GMT

[/rest/users/rod, /rest/users/roy]
