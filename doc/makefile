# Makefile for the Ptolemy II documentation directory
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2000 The Regents of the University of California.
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
DIRS =		 coding design #internals #usersGuides

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

EXTRA_SRCS = \
	acks.htm \
	banner.htm \
	body.htm \
	buttons.htm \
	contents.css \
	contentsheader.htm \
	default.css \
	docbody.htm \
	docbuttons.htm \
	docs.htm \
	doctoc.htm \
	domains.htm \
	footer.htm \
	help.htm \
	index.htm \
	install.htm \
	main.htm \
	mainheader.htm \
	ptII0.4.release.htm \
	ptIIfaq.htm \
	toc.css \
	toc.htm \
	troubleshooting.htm \
	types.htm \
	ui.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) img tutorial

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	codeDoc uml

ALLJSRCS =		ptolemy.plot \
			ptolemy.plot.plotml \
			ptolemy.plot.compat \
			ptolemy.kernel \
			ptolemy.kernel.event \
			ptolemy.kernel.util \
			ptolemy.actor \
			ptolemy.actor.event \
			ptolemy.actor.gui \
			ptolemy.actor.lib \
			ptolemy.actor.process \
			ptolemy.actor.sched \
			ptolemy.actor.util \
			ptolemy.domains.csp.gui \
			ptolemy.domains.csp.kernel \
			ptolemy.domains.csp.lib \
			ptolemy.domains.ct.gui \
			ptolemy.domains.ct.kernel \
			ptolemy.domains.ct.kernel.solver \
			ptolemy.domains.ct.kernel.util \
			ptolemy.domains.ct.lib \
			ptolemy.domains.dde.gui \
			ptolemy.domains.dde.kernel \
			ptolemy.domains.dde.lib \
			ptolemy.domains.de.gui \
			ptolemy.domains.de.kernel \
			ptolemy.domains.de.lib \
			ptolemy.domains.fsm.kernel \
			ptolemy.domains.pn.gui \
			ptolemy.domains.pn.kernel \
			ptolemy.domains.sdf.gui \
			ptolemy.domains.sdf.kernel \
			ptolemy.domains.sdf.lib \
			ptolemy.domains.sdf.lib.vq \
			ptolemy.data \
			ptolemy.data.expr \
			ptolemy.data.type \
			ptolemy.graph \
			ptolemy.gui \
			ptolemy.math \
			ptolemy.media \
			ptolemy.moml

all: suball codeDoc/tree.html
install: all
docs: codeDoc/tree.html
javadocs: codeDoc/tree.html
codeDoc/tree.html:	../ptolemy/plot/*.java \
			../ptolemy/plot/compat/*.java \
			../ptolemy/plot/plotml/*.java \
			../ptolemy/kernel/*.java \
			../ptolemy/kernel/event/*.java \
			../ptolemy/kernel/util/*.java \
			../ptolemy/actor/*.java \
			../ptolemy/actor/event/*.java \
			../ptolemy/actor/gui/*.java \
			../ptolemy/actor/lib/*.java \
			../ptolemy/actor/process/*.java \
			../ptolemy/actor/sched/*.java \
			../ptolemy/actor/util/*.java \
			../ptolemy/domains/csp/gui/*.java \
			../ptolemy/domains/csp/kernel/*.java \
			../ptolemy/domains/csp/lib/*.java \
			../ptolemy/domains/ct/gui/*.java \
			../ptolemy/domains/ct/kernel/*.java \
			../ptolemy/domains/ct/kernel/solver/*.java \
			../ptolemy/domains/ct/kernel/util/*.java \
			../ptolemy/domains/ct/lib/*.java \
			../ptolemy/domains/dde/gui/*.java \
			../ptolemy/domains/dde/kernel/*.java \
			../ptolemy/domains/dde/lib/*.java \
			../ptolemy/domains/de/gui/*.java \
			../ptolemy/domains/de/kernel/*.java \
			../ptolemy/domains/de/lib/*.java \
			../ptolemy/domains/fsm/kernel/*.java \
			../ptolemy/domains/pn/gui/*.java \
			../ptolemy/domains/pn/kernel/*.java \
			../ptolemy/domains/sdf/gui/*.java \
			../ptolemy/domains/sdf/kernel/*.java \
			../ptolemy/domains/sdf/lib/*.java \
			../ptolemy/domains/sdf/lib/vq/*.java \
			../ptolemy/data/*.java \
			../ptolemy/data/expr/*.java \
			../ptolemy/data/type/*.java \
			../ptolemy/graph/*.java \
			../ptolemy/gui/*.java \
			../ptolemy/math/*.java \
			../ptolemy/media/*.java \
			../ptolemy/moml/*.java
	if [ ! -d codeDoc ]; then mkdir -p codeDoc; fi
	-(cd ..; CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR).$(CLASSPATHSEPARATOR)$(PTJAVA_DIR)/lib/classes.zip$(CLASSPATHSEPARATOR).." $(JAVADOC) $(JDOCFLAGS) -d doc/codeDoc $(ALLJSRCS) )


# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk
