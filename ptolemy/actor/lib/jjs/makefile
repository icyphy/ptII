# Makefile for Nashorn-based Ptolemy II JavaScript actors
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2014-2016 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/jjs

# node_modules has no makefiles, so don't add it to DIRS.
DIRS = 		demo external modules node test #node_modules

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTJAVASCRIPT_JAR)$(CLASSPATHSEPARATOR)$(PTOAUTHCLIENT_JAR)$(CLASSPATHSEPARATOR)$(PTOAUTHCOMMON_JAR)$(CLASSPATHSEPARATOR)$(PTSOCKETIO_JAR)$(PTVERTX_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jjs
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AccessorOrchestrator.java \
	HelperBase.java \
	JavaScript.java \
	JSUtils.java \
	NashornAccessorHostApplication.java \
	RestrictedJavaScriptInterface.java \
	VertxHelperBase.java

OTHER_FILES_TO_BE_JARED = \
	capeCodeHost.js \
	commonHost.js \
	console.js \
	JavaScriptIcon.xml \
	jjs-license.htm \
	js.xml \
	localFunctions.js \
	nashornHost.js

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	external modules node test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	node_modules \
	node_files \
	'HelperBase$$1.class' \
	'HelperBase$$2.class' \
	'JavaScript$$1.class' \
	'JavaScript$$2.class' \
	'JavaScript$$DeferredSend.class' \
	'JavaScript$$ParameterProxy.class' \
	'JavaScript$$PortOrParameterProxy.class' \
	'JavaScript$$PortProxy.class' \
	'JavaScript$$PtolemyJavaScript.class' \
	'NashornAccessorHostApplication$$1.class' \
	'NashornAccessorHostApplication$$ActorSubstitute$$1.class' \
	'NashornAccessorHostApplication$$ActorSubstitute$$2.class' \
	'NashornAccessorHostApplication$$ActorSubstitute$$3.class' \
	'NashornAccessorHostApplication$$ActorSubstitute.class' \
	'VertxHelperBase$$1.class' \
	'VertxHelperBase$$2.class' \
	'VertxHelperBase$$AccessorVerticle.class' \
	'VertxHelperBase$$DATA_TYPE.class' \
	'VertxHelperBase$$HandlerInvocation.class' \
	$(PTCLASSJAR) 

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
