# Makefile for Ptolemy JNLP Wrapper classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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

ME =		ptolemy/actor/gui/jnlp

DIRS =

# Root of the Java directory
ROOT =		../../../..

# Include diva.jar here in case we are building an VergilApplication
# does not yet exist.
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	jnlp
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	DSPApplication.java \
	FullApplication.java \
	HyVisualApplication.java \
	MenuApplication.java \
	PtinyApplication.java \
	PtinySandboxApplication.java \
	VisualSenseApplication.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(OTHER_JARS)

JCLASS = $(JSRCS:%.java=%.class)

OTHER_JARS = \
	DSPApplication.jar \
	FullApplication.jar \
	HyVisualApplication.jar \
	PtinyApplication.jar \
	PtinySandboxApplication.jar \
	VisualSenseApplication.jar

all: jclass $(OTHER_JARS)
install: all
DSPApplication.jar: DSPApplication.class MenuApplication.class
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp $< MenuApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

FullApplication.jar: FullApplication.class MenuApplication.class
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp $< MenuApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

HyVisualApplication.jar: HyVisualApplication.class MenuApplication.class
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp $< MenuApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

PtinyApplication.jar: PtinyApplication.class MenuApplication.class
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp $< MenuApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)


PtinySandboxApplication.jar: PtinySandboxApplication.class MenuApplication.class
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp $< MenuApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

VisualSenseApplication.jar: VisualSenseApplication.class MenuApplication.class
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp $< MenuApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
