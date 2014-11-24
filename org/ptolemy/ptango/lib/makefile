# Makefile for Ptango - Internet of Things - Actors
#
# @Authors: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2012-2014 The Regents of the University of California.
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

ME =		org/ptolemy/ptango/lib

# PTXMPP_DIR is set to xmpp if smack.jar and smackx.jar are found by configure.
DIRS =		http img $(PTXMPP_DIR) webSocket

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTANGO_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	lib
PTCLASSALLJAR = lib.jar
# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	http/http.jar \
	img/img.jar \
	webSocket/webSocket.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	HTMLModelExporter.java \
	HttpActor.java \
	HttpGet.java \
	HttpPost.java \
	HttpPut.java \
	HttpRequest.java \
	HttpResponse.java \
	HttpService.java \
	RecordToQueryString.java \
	WebServer.java \
	WebApplicationInfo.java \
	WebServerManager.java \
	WebServerUtilities.java

OTHER_FILES_TO_BE_JARED = \
	ptango.xml \
	HTMLModelExporterIcon.xml \
	HttpActorIcon.xml \
	HttpGetIcon.xml \
	HttpPostIcon.xml \
	WebServerIcon.xml

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Non-source files such as README files and subdirectories
MISC_FILES = img

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	manifest.tmp \
	$(PTCLASSALLJAR) \
	$(PTAUXJAR) \
	websocket \
	'HTMLModelExporter$$1.class' \
	'HttpActor$$ActorServlet.class' \
	'HttpRequest$$Method.class' \
	'HttpActor$$HttpRequestItems.class' \
	'HttpActor$$HttpResponseItems.class' \
	'HttpResponse$$ErrorSource.class' \
	'WebServer$$1.class' \
	'WebServer$$RunnableServer.class' \
	'HttpActor$$HttpRequest.class' \
	'HttpActor$$HttpResponse.class' \
	'WebServerUtilities$$1.class' \
	'WebServerUtilities$$RunnableServer.class'

KRUFT =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: all jars 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
