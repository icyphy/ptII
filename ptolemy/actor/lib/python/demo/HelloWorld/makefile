# Makefile for Jython HelloWorld Applet
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/python/demo/HelloWorld

# Root of the Java directory
ROOT =		../../../../../..

CLASSPATH =     $(ROOT)$(CLASSPATHSEPARATOR)$(JYTHON_DIR)/jython.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	HelloWorld
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	$(JSRCS) \
	Ptolemnizer.xml

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) \
	HelloWorld.htm \
	HelloWorld.py \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass HelloWorldApplet.jar
	@echo "To run all the demos, run 'make demo'"

install: all $(PTCLASSJAR)

JYTHON_HOME =	$(ROOT)/vendors/jython/jython-2.1
JYTHONC_PY =	$(JYTHON_HOME)/Tools/jythonc/jythonc.py 
HelloWorldApplet.jar: HelloWorld.py makefile
	@if [ ! -f $(JYTHONC_PY) ]; then \
		echo "WARNING: Cannot find $(JYTHONC_PY)"; \
		echo "Perhaps Jython sources are not installed?"; \
		echo "Try downloading from"; \
		echo "   http://www.jython.org/download.html"; \
		echo "and then doing "; \
		echo "   java -classpath . jython-21"; \
		echo "and installing in \$$PTII/vendors/jython/jython-2.1"; \
		exit 2; \
	fi
	CLASSPATH="$(CLASSPATH)" \
	"$(JAVA)" "-Dpython.home=$(JYTHON_HOME)" \
		org.python.util.jython \
		$(JYTHON_HOME)/Tools/jythonc/jythonc.py \
		--compiler javac \
		--core --deep --jar $@ HelloWorld.py 

demo: demo_applet

demo_applet:
	CLASSPATH="$(CLASSPATH)" appletviewer HelloWorld.htm

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
