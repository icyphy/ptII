# Makefile for Ptolemy Tycho system, java/doc
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997- The Regents of the University of California.
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
##########################################################################

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

# Sub directories to run make in.
DIRS =		 coding internals #usersGuides

EXTRA_SRCS = \
	demos.html \
	index.html \
	install.html \
	ptII0.1release.html

MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =	codeDoc
# 'make realclean' should remove tclIndex
REALCLEAN_STUFF =

ALLJSRCS = 		ptolemy/plot/*.java \
			ptolemy/kernel/*.java \
			ptolemy/kernel/util/*.java \
			ptolemy/actor/*.java \
			ptolemy/actor/util/*.java \
			ptolemy/data/*.java \
			ptolemy/data/expr/*.java

#			ptolemy/domains/pn/kernel/*.java \
#			ptolemy/domains/ct/kernel/*.java \
#			ptolemy/domains/pn/lib/*.java \
#			ptolemy/domains/ct/lib/*.java

all: suball codeDoc/tree.html

codeDoc/tree.html: 	../ptolemy/plot/*.java \
			../ptolemy/kernel/*.java \
			../ptolemy/kernel/util/*.java \
			../ptolemy/actor/*.java \
			../ptolemy/actor/util/*.java \
			../ptolemy/data/*.java \
			../ptolemy/data/expr/*.java
#			../ptolemy/domains/pn/kernel/*.java \
#			../ptolemy/domains/ct/kernel/*.java \
#			../ptolemy/domains/pn/lib/*.java \
#			../ptolemy/domains/ct/lib/*.java
	if [ ! -d codeDoc ]; then mkdir -p codeDoc; fi
	(cd ..; CLASSPATH=$(CLASSPATH):$(JAVAHOME)/lib/classes.zip:.. $(JAVADOC) $(JDOCFLAGS) -d doc/codeDoc $(ALLJSRCS) )
	@for x in codeDoc/*.html; do \
		echo "Fixing paths in $$x"; \
		sed -e 's|<a href="java|<a href="$(JAVAHTMLDIR)/java|g' \
		-e 's|<img src="images/|<img src="$(JAVAHTMLDIR)/images/|g' \
			$$x > $$x.bak; \
		mv $$x.bak $$x; \
	done

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/no-compile.mk


