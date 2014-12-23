# Makefile for Java classes used to implement Ptolemy domains
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 1997-2014 The Regents of the University of California.
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

ME =		ptolemy/domains

# Domains we do not ship
EXPERIMENTAL_DOMAINS =

# Domains, in alphabetical order.  If you change this, change
# PTCLASSALLJARS below
# DT has to come after SDF, sorry.
# PTPSDF_DIR is set by configure in $PTII/mk/ptII.mk to psdf if
#   $PTII/lib/mapss.jar was found
# PTJAVA3D_DIR is set by configure in $PTII/mk/ptII.mk to gr if
#   Java 3D was found
# PTSPACE_DIR is set to space by configure in $PTII/mk/ptII.mk if
#   ojdbc6.jar was found.
# coroutine is on a separate line because it is not shipped and 
#   building the installer removes coroutine.
DOMAINS =	algebraic ca ci continuous \
		coroutine \
		csp curriculum \
	        dde ddf de hdf modal pn sdf dt giotto \
		$(PTMETROII_DIR) \
		ontologies \
		openmodelica \
		petrinet qss rendezvous sysml \
		$(PTPSDF_DIR) ptera pthales ptides ptinyos \
		scr $(PTSPACE_DIR) \
		sequence sr taskpt tdl tester tm $(PTJAVA3D_DIR) wireless

DIRS =		$(DOMAINS) $(EXPERIMENTAL_DOMAINS) test

# Root of Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	domains
PTCLASSJAR =

# Include these jars in PTAUXALLJAR

# PTGRDOMAIN_JAR is the name of the jar file that includes the GR
# domain if Java 3D was found.  PTGRDOMAIN_JAR is set by configure.

# PTSPACEDOMAIN_JAR is the name of the jar file that includes the space
# domain if ojdbc6.jar was found.  PTSPACEDOMAIN_JAR is set by configure.

PTAUXALLJARS = \
		algebraic/algebraic.jar \
		ca/ca.jar \
		ci/ci.jar \
		csp/csp.jar \
		curriculum/curriculum.jar \
		dde/dde.jar \
	        dt/dt.jar \
		giotto/giotto.jar \
		$(PTGRDOMAIN_JAR) \
		openmodelica/openmodelica.jar \
		petrinet/petrinet.jar \
		ptera/ptera.jar \
		pthales/pthales.jar \
		ptides/ptides.jar \
		qss/qss.jar \
		scr/scr.jar \
		$(PTSPACEDOMAIN_JAR) \
		sysml/sysml.jar \
		taskpt/taskpt.jar \
		tm/tm.jar \
		wireless/wireless.jar

# Are we shipping these domains?
#		metroII/metroII.jar
#		ptinyos/ptinyos.jar 
#		pbo/pbo.jar
#		petrinet/petrinet.jar
#		sysml/sysml.jar

# Experimental domains 
PTAUXALLJAR =	experimentalDomains.jar

# Include the .class files from these jars in PTCLASSALLJAR
# See ptolemy/configs/basicDirectors.xml 
# If you change this, change DIRS above
PTCLASSALLJARS = \
		continuous/continuous.jar \
		de/de.jar \
		ddf/ddf.jar \
		hdf/hdf.jar \
		modal/modal.jar \
		ontologies/ontologies.jar \
		pn/pn.jar \
		rendezvous/rendezvous.jar \
		sdf/sdf.jar \
		sr/sr.jar \
		tester/tester.jar


PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	domains.xml \
	ptinyDomains.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	$(DOMAINS) test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(EXPERIMENTAL_DOMAINS) \
	$(OTHER_FILES_TO_BE_JARED) \
	dfm \
	demo.jar \
	generic \
	gr \
	ptinyos

OTHER_JARS = demo.jar

DEMOJARS = \
	algebraic/demo/demo.jar \
	ca/demo/demo.jar \
	de/demo/demo.jar \
	de/doc/doc.jar \
	ddf/demo/demo.jar \
	ddf/doc/doc.jar \
	hdf/demo/demo.jar \
	hdf/doc/doc.jar \
	modal/doc/doc.jar \
	modal/doc/doc.jar \
	pn/demo/demo.jar \
	pn/doc/doc.jar \
	ptera/demo/demo.jar \
	ptera/doc/doc.jar \
	pthales/demo/demo.jar \
	pthales/doc/doc.jar \
	rendezvous/demo/demo.jar \
	rendezvous/doc/doc.jar \
	sdf/demo/demo.jar \
	sdf/doc/doc.jar \
	sr/demo/demo.jar \
	sr/doc/doc.jar

all: jclass
install: jclass jars

demo.jar:
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	for jar in $(DEMOJARS); do \
		echo "Unjarring $$jar"; \
		(cd $(PTJAR_TMPDIR); "$(JAR)" $(JAR_FLAGS) -xvf ../$$jar); \
	done
	rm -rf $(PTJAR_TMPDIR)/META-INF
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" $(JAR_FLAGS) -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	$(JAR_INDEX)
	rm -rf $(PTJAR_TMPDIR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
