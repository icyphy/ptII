# Makefile for the Ptolemy II documentation directory
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-1999 The Regents of the University of California.
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

ME =		ptII/doc

# Sub directories to run make in.
DIRS =		 coding #internals #usersGuides

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

EXTRA_SRCS = \
	demos.html \
	index.html \
	install.html \
	ptII0.1release.html

# The files in design and img are not under sccs because they are binaries.
MISC_FILES =	$(DIRS) design img

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	codeDoc

ALLJSRCS = 		ptolemy.plot \
			ptolemy.kernel \
			ptolemy.kernel.event \
			ptolemy.kernel.util \
			ptolemy.actor \
			ptolemy.actor.process \
			ptolemy.actor.sched \
			ptolemy.actor.util \
			ptolemy.domains.csp.kernel \
			ptolemy.domains.csp.lib \
			ptolemy.domains.ct.kernel \
			ptolemy.domains.ct.kernel.util \
			ptolemy.domains.ct.lib \
			ptolemy.domains.de.kernel \
			ptolemy.domains.de.lib \
			ptolemy.domains.od.kernel \
			ptolemy.domains.od.lib \
			ptolemy.domains.pn.kernel \
			ptolemy.domains.pn.lib \
			ptolemy.domains.sc.kernel \
			ptolemy.domains.sc.lib \
			ptolemy.domains.sdf.kernel \
			ptolemy.domains.sdf.lib \
			ptolemy.domains.sdf.lib.vq \
			ptolemy.data \
			ptolemy.data.expr \
			ptolemy.graph \
			ptolemy.math \
			ptolemy.media

all: suball codeDoc/tree.html
javadocs: codeDoc/tree.html
codeDoc/tree.html: 	../ptolemy/plot/*.java \
			../ptolemy/kernel/*.java \
			../ptolemy/kernel/event/*.java \
			../ptolemy/kernel/util/*.java \
			../ptolemy/actor/*.java \
			../ptolemy/actor/process/*.java \
			../ptolemy/actor/sched/*.java \
			../ptolemy/actor/util/*.java \
			../ptolemy/domains/csp/lib/*.java \
			../ptolemy/domains/csp/kernel/*.java \
			../ptolemy/domains/ct/lib/*.java \
			../ptolemy/domains/ct/kernel/*.java \
			../ptolemy/domains/ct/kernel/util/*.java \
			../ptolemy/domains/de/kernel/*.java \
			../ptolemy/domains/de/lib/*.java \
			../ptolemy/domains/od/kernel/*.java \
			../ptolemy/domains/od/lib/*.java \
			../ptolemy/domains/pn/kernel/*.java \
			../ptolemy/domains/pn/lib/*.java \
			../ptolemy/domains/sc/kernel/*.java \
			../ptolemy/domains/sc/lib/*.java \
			../ptolemy/domains/sdf/kernel/*.java \
			../ptolemy/domains/sdf/lib/*.java \
			../ptolemy/domains/sdf/lib/vq/*.java \
			../ptolemy/data/*.java \
			../ptolemy/data/expr/*.java \
			../ptolemy/graph/*.java \
			../ptolemy/math/*.java \
			../ptolemy/media/*.java
	if [ ! -d codeDoc ]; then mkdir -p codeDoc; fi
	-(cd ..; CLASSPATH=$(CLASSPATH):$(JAVAHOME)/lib/classes.zip:.. $(JAVADOC) $(JDOCFLAGS) -d doc/codeDoc $(ALLJSRCS) )
	@for x in codeDoc/*.html; do \
		echo "Fixing paths in $$x"; \
		sed -e 's|<a href="java|<a href="$(JAVAHTMLDIR)/java|g' \
		-e 's|<img src="images/|<img src="$(JAVAHTMLDIR)/images/|g' \
			$$x > $$x.bak; \
		mv $$x.bak $$x; \
	done

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk


