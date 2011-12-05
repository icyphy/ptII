<!-- The Java Spring view for the SensorController

 Copyright (c) 1997-2011 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 -->

<%@  page  language="java"  contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<!DOCTYPE html>
<html>
<head>

<!-- Load jQuery and jQueryMobile script stylesheets and libraries.  
	 Note that jQuery must come before jQueryMobile 
	 These statements load them from online, but it is also fine to load from
	 a local copy.  A local copy is included for reference - both the 
	 human-readable version and the .min.js version for smaller footprint --> 
<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0rc2/jquery.mobile-1.0rc2.min.css" />
<script src="http://code.jquery.com/jquery-1.7.min.js"></script>
<script src="http://code.jquery.com/mobile/1.0rc2/jquery.mobile-1.0rc2.min.js"></script>

<!-- Load custom Javascript functions -->
<script type-"text/javascript" src="/websensor/static/js/themes/std/view/sensorReading.js"></script>

<title>Websensor </title>

<!-- Recommended by jQueryMobile for screen resizing -->
<meta name="viewport" content="width=device-width, initial-scale=1">

</head>

<body>
<div data-role="page"> 
	<div data-role="header">
		<!-- The 'type' variable is added by the controller -->
		<h1> ${type} </h1>
	</div> 
	<div data-role="content"> 
		<!-- The 'readings' variable is added by the controller -->
		<div> ${readings} </div>
	</div>
</body>
</html> 