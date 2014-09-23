# Makefile for Ptango - Internet of Things - Actors for http
#
# @Authors: Christopher Brooks
#
# @Version: $Id: makefile 69602 2014-07-30 14:20:15Z cxh $
#
# @Copyright (c) 2014 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		org/ptolemy/ptango/lib/http

DIRS =		img

# Root of the Ptolemy II directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTANGO_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	http
PTCLASSALLJAR = http.jar
# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		img/img.jar

# Java Sources, keep these alphabetical
JSRCS = \
	HttpPost.java \
	HttpPut.java \
	HttpRequest.java \
	HttpRequestHandler.java \
	HttpResponse.java \
	HttpService.java \
	HttpSink.java \
	WebApplicationInfo.java \
	WebServer.java \
	WebServerManager.java \
	WebServerUtilities.java

OTHER_FILES_TO_BE_JARED = \
	WebServerIcon.xml

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Non-source files such as README files and subdirectories
MISC_FILES = img

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	$(PTCLASSALLJAR) \
	$(PTAUXJAR) \
	'HttpRequest$$Method.class' \
	'HttpRequestHandler$$ActorServlet.class' \
	'HttpRequestHandler$$HttpRequestItems.class' \
	'HttpRequestHandler$$HttpResponseItems.class' \
	'HttpResponse$$ErrorSource.class' \
	'WebServerUtilities$$1.class' \
	'WebServerUtilities$$RunnableServer.class'

KRUFT =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: all jars 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
