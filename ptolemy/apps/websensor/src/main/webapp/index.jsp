<!-- The welcome file for the websensor web app

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

<!DOCTYPE html>
<html>
	<head>
	
	<!-- Load jQuery and jQueryMobile script stylesheets and libraries.  
		 Note that jQuery must come before jQueryMobile 
		 Use online version to avoid problems rendering icons.  The files are 
		 included in the repository for reference.  The local version could be used
		 so that an internet connection is not required if testing from localhost.
		 Both the human-readable version and the .min.js version for smaller 
		 footprint are in the repository, though the web app only needs one version 
		 to run.
		 Note that jQuery 1.7 is available, but there are currently problems with
		 that version and jQueryMobile.  jQuery 1.6.4 is the latest version 
		 supported. --> 
	
	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0/jquery.mobile-1.0.min.css" />
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.4.min.js"></script>
	<script type="text/javascript" src="http://code.jquery.com/mobile/1.0/jquery.mobile-1.0.min.js"></script>
	
	<title>Websensor </title>
	
	<!-- Recommended by jQueryMobile for screen resizing -->
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<body>
		<div data-role="page"> 
			<div data-role="header">
				<h1> Websensor </h1>
			</div> 
			
			<div data-role="content">
				<ul data-role="listview"> 
					<li> <a href="./temperature"> Temperature </a> </li>
		 			<li> <a href="./temperature.json"> Temperature (JSON) </a> </li>
		 			<li> <a href="./humidity"> Humidity </a> </li>
		 			<li> <a href="./humidity.json"> Humidity (JSON) </a> </li>
		 			<li> <a href="./pressure"> Pressure </a> </li>
		 			<li> <a href="./pressure.json"> Pressure (JSON) </a> </li>
				<ul>
			</div>
		</div>
	</body>
</html>
