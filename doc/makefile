# Makefile for the Ptolemy II documentation directory
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2003 The Regents of the University of California.
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

ME =		doc

# Sub directories to run make in.
DIRS =		 coding design img tour tutorial ui  #internals #usersGuides

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	doc
PTVERSION =	3.0
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Files in this directory (but not the subdirectories) that should go
# into docConfig.jar because files in ptolemy/config refer to them.
#
# We use a separate variable here so that we can refer to these files
# in EXTRA_SRCS with out have to duplicate the names.  
#
UI_FILES_TO_BE_JARED = \
	acks.htm \
	authors.htm \
	codegen.htm \
	default.css \
	expressions.htm \
	webStartHelp.htm \
	whatsnew.htm \
	whatsnewVergil.htm

EXTRA_SRCS = \
	$(UI_FILES_TO_BE_JARED) \
	banner.htm \
	body.htm \
	buttons.htm \
	contents.css \
	contentsheader.htm \
	docbody.htm \
	docbuttons.htm \
	docs.htm \
	doctoc.htm \
	domains.htm \
	footer.htm \
	help.htm \
	index.htm \
	install.htm \
	installJNLP.htm \
	javahtml.awk \
	main.htm \
	mainheader.htm \
	oldFeatures.htm \
	ptII$(PTVERSION).release.htm \
	ptIIfaq.htm \
	removeasd \
	toc.css \
	toc.htm \
	troubleshooting.htm \
	types.htm \
	whatsnew.awk

# Derived files that are preprocessed for use within vergil.
VERGIL_DOCS = \
	mainVergil.htm \
	ptII$(PTVERSION).releaseVergil.htm \
	troubleshootingVergil.htm

# Include these jars in PTAUXALLJAR
PTAUXALLJARS = \
		coding/codingConfig.jar \
		img/img.jar

# After Ptolemy II 2.0, usingVergil is no longer a part of docConfig.jar
#		design/usingVergil/usingVergil.jar 

# All the doc/ files that are referred to by files in ptolemy/config
PTAUXALLJAR =	docConfig.jar

# Files in this directory and any subdirectories that should go into
# docConfig.jar because files in ptolemy/config refer to them.
# FIXME: doc/design/usingVergil/index.htm is a derived file
# that refers to other files. 
OTHER_FILES_TO_BE_JARED = \
	$(VERGIL_DOCS) \
	$(UI_FILES_TO_BE_JARED) \

	#doc/design/usingVergil/index.htm 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	\
	$(DIRS) \
	$(VERGIL_DOCS) \
	img \
	tutorial

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	\
	codeDoc \
	codeDoc.jar \
	codeDocPtiny \
	uml

# Java packages for copernicus 

# Don't include copernicus.jhdl because it requires byucc.jhdl from JHDL.jar
#JHDLPACKAGE = 		ptolemy.copernicus.jhdl

# Javadoc can't deal with packages that end with 'java', see
# http://developer.java.sun.com/developer/bugParade/bugs/4507380.html
# So, we expand the ptolemy.copernicus.java package
COPERNICUSPACKAGES = \
			ptolemy.copernicus.applet \
			ptolemy.copernicus.kernel \
			ptolemy.copernicus.c \
			ptolemy/copernicus/java/*.java \
			$(JHDLPACKAGE) \
			ptolemy.copernicus.shallow

# Packages used by HyVisual
HYVISUALPACKAGES = \
			ptolemy.actor.lib.javasound \
			ptolemy.actor.lib \
			ptolemy.actor.lib.comm \
			ptolemy.actor.lib.conversions \
			ptolemy.actor.lib.gui \
			ptolemy.actor.lib.io \
			ptolemy.actor.lib.logic \
			ptolemy.actor.lib.python \
			ptolemy.actor.lib.string \
			ptolemy.actor.parameters \
			ptolemy.domains.ct.kernel \
			ptolemy.domains.ct.kernel.solver \
			ptolemy.domains.ct.kernel.util \
			ptolemy.domains.ct.lib \
			ptolemy.domains.fsm.kernel \
			ptolemy.domains.sdf.kernel \
			ptolemy.domains.sdf.lib \
			ptolemy.domains.sdf.lib.security \
			ptolemy.domains.sdf.lib.vq \
			ptolemy.media.javasound \
			ptolemy.vergil.fsm.modal


# Currently, there are no .java files in fsm.lib
#			ptolemy.domains.fsm.lib

# Packages used by Ptiny.  
PTINYPACKAGES = \
			$(HYVISUALPACKAGES) \
			ptolemy.domains.de.kernel \
			ptolemy.domains.de.lib \

# Packages for the SUPERB classes
#SUPERBPACKAGES = \
#			ptolemy.apps.superb.actor.lib

# actor.lib.comm uses the Java comm api for communicating with
# serial ports.  javacomm is not always available, so actor.lib.comm
# is not part of the default build	
#COMMPACKAGES =		ptolemy.actor.lib.io.comm

# GR domain uses Java 3D, which is not part of the default
# JDK1.3 download, so we can't include the GR domain classes in
# the default build.
#GRPACKAGES =		ptolemy.domains.gr.kernel \
#			ptolemy.domains.gr.lib

# ptoelmy.actor.lib.joystick.Joystick uses the Joystick API
#JOYSTICKPACKAGES =	ptolemy.actor.lib.joystick

ALLPACKAGES =		$(COMMPACKAGES) \
			$(COPERNICUSPACKAGES) \
			$(JOYSTICKPACKAGES) \
			$(PTINYPACKAGES) \
			$(GRPACKAGES) \
			$(SUPERBPACKAGES) \
			jni \
			ptolemy.plot \
			ptolemy.plot.plotml \
			ptolemy.plot.compat \
			ptolemy.kernel \
			ptolemy.kernel.attributes \
			ptolemy.kernel.util \
			ptolemy.actor \
			ptolemy.actor.gui \
			ptolemy.actor.gui.style \
			ptolemy.actor.hoc \
			ptolemy.actor.process \
			ptolemy.actor.lib.hoc \
			ptolemy.actor.lib.net \
			ptolemy.actor.sched \
			ptolemy.actor.util \
			ptolemy.domains.ci.kernel \
			ptolemy.domains.ci.lib \
			ptolemy.domains.csp.kernel \
			ptolemy.domains.csp.lib \
			ptolemy.domains.dde.kernel \
			ptolemy.domains.dde.lib \
			ptolemy.domains.giotto.kernel \
			ptolemy.domains.pn.kernel \
			ptolemy.domains.petrinet.kernel \
			ptolemy.domains.sr.kernel \
			ptolemy.domains.sr.lib \
			ptolemy.domains.tm.kernel \
			ptolemy.domains.tm.lib \
			ptolemy.data \
			ptolemy.data.expr \
			ptolemy.data.type \
			ptolemy.data.unit \
			ptolemy.graph \
			ptolemy.graph.analysis \
			ptolemy.gui \
			ptolemy.math \
			ptolemy.matlab \
			ptolemy.media \
			ptolemy.moml \
			ptolemy.moml.filter \
			ptolemy.vergil \
			ptolemy.vergil.actor \
			ptolemy.vergil.basic \
			ptolemy.vergil.debugger \
			ptolemy.vergil.icon \
			ptolemy.vergil.fsm \
			ptolemy.vergil.fsm.ia \
			ptolemy.vergil.kernel \
			ptolemy.vergil.toolbox \
			ptolemy.vergil.tree


all: suball whatsnew.htm whatsnewVergil.htm \
	codeDoc/tree.html codeDocHyVisual/doc/codeDoc/tree.html

install: all jars codeDoc.jar codeDocHyVisual.jar

codeDoc.jar:
	"$(JAR)" -cvf $@ -C $(ROOT) doc/codeDoc


# Vergil gets special versions of the html files because the JDK1.3 Java
# HTML viewer is not very functional.
vergil_docs: $(VERGIL_DOCS)
$(VERGIL_DOCS): javahtml.awk

# Version of main.htm without links with targets
# Convert <a href="foobar" target="body">bif</a> -> bif
# and the convert Ptolemy Project web page to
#     <a href="http://ptolemy.eecs.berkeley.edu/#in_browser">Ptolemy Project web page</a>
mainVergil.htm: main.htm
	sed -e 's@<a href="[^"]*" target="[^"]*">\([^<]*\)</a>@\1@' \
		-e 's@Ptolemy Project web page@<a href="http://ptolemy.eecs.berkeley.edu/#in_browser">Ptolemy Project web page</a>@' \
	        -e 's@^<link .*>@@' \
		main.htm > $@

# Generate a file that consists of only the text within
# <!--vergilDoc--> ... <!--/vergilDoc--> blocks
ptII$(PTVERSION).releaseVergil.htm: ptII$(PTVERSION).release.htm
	awk -f javahtml.awk ptII$(PTVERSION).release.htm > $@  
ptII$(PTVERSION).releaseHyVisual.htm: ptII$(PTVERSION).release.htm
	awk -f hyVisualHtml.awk ptII$(PTVERSION).release.htm > $@  
troubleshootingVergil.htm: troubleshooting.htm
	awk -f javahtml.awk troubleshooting.htm > $@  

# Generate a file that consists of only the text within
# <!--whatsnew--> ... <!--/whatsnew--> blocks
whatsnew.htm: ptII$(PTVERSION).release.htm
	awk -f whatsnew.awk ptII$(PTVERSION).release.htm > $@  

# Convert demo/*/*.htm to demo/*/*.xml
# Pull out the links to the domains/*/doc/body.htm
# For example:
#  <a href="../ptolemy/domains/tm/doc/body.htm" target="body">Timed Multitasking Domain</a>
# becomes:
#  <a href="../ptolemy/domains/tm/doc/main.htm">Timed Multitasking Domain</a>
#
whatsnewVergil.htm: whatsnew.htm
	sed -e 's@\(demo/[^/]*/[^/]*\).htm@\1.xml@g' \
	    -e 's@<a href="\([^"]*/domains/[^/]*/doc/\)body.htm" target="[^"]*">\([^<]*\)</a>@<a href="\1main.htm">\2</a>@' \
            -e 's@<body .*>@<body>@' \
            -e 's@^<link .*>@@' \
		< whatsnew.htm > whatsnewVergil.htm

# Files that are derived from other files, such as ptII$(PTVERSION).release.htm
DERIVED_FILES = \
	mainVergil.htm \
	ptII$(PTVERSION).releaseVergil.htm \
	ptII$(PTVERSION).releaseHyVisual.htm \
	troubleshootingVergil.htm \
	whatsnew.htm \
	whatsnewVergil.htm 

update: $(DERIVED_FILES)
	cvs commit -m "Master files changed" \
		mainVergil.htm ptII$(PTVERSION).releaseVergil.htm \
		troubleshootingVergil.htm \
		whatsnew.htm whatsnewVergil.htm 
	rm -f docConfig.jar
	$(MAKE) docConfig.jar

update_clean: 
	rm -f $(DERIVED_FILES)

docs: codeDoc/tree.html
javadocs: codeDoc/tree.html
codeDoc/tree.html:	../ptolemy/plot/*.java \
			../ptolemy/plot/compat/*.java \
			../ptolemy/plot/plotml/*.java \
			../ptolemy/kernel/*.java \
			../ptolemy/kernel/attributes/*.java \
			../ptolemy/kernel/util/*.java \
			../ptolemy/actor/*.java \
			../ptolemy/actor/gui/*.java \
			../ptolemy/actor/gui/style/*.java \
			../ptolemy/actor/hoc/*.java \
			../ptolemy/actor/lib/*.java \
			../ptolemy/actor/lib/comm/*.java \
			../ptolemy/actor/lib/conversions/*.java \
			../ptolemy/actor/lib/hoc/*.java \
			../ptolemy/actor/lib/io/*.java \
			../ptolemy/actor/lib/net/*.java \
			../ptolemy/actor/lib/gui/*.java \
			../ptolemy/actor/lib/logic/*.java \
			../ptolemy/actor/lib/python/*.java \
			../ptolemy/actor/lib/string/*.java \
			../ptolemy/actor/parameters/*.java \
			../ptolemy/actor/process/*.java \
			../ptolemy/actor/sched/*.java \
			../ptolemy/actor/util/*.java \
			../ptolemy/domains/ci/kernel/*.java \
			../ptolemy/domains/ci/lib/*.java \
			../ptolemy/domains/csp/kernel/*.java \
			../ptolemy/domains/csp/lib/*.java \
			../ptolemy/domains/ct/kernel/*.java \
			../ptolemy/domains/ct/kernel/solver/*.java \
			../ptolemy/domains/ct/kernel/util/*.java \
			../ptolemy/domains/ct/lib/*.java \
			../ptolemy/domains/dde/kernel/*.java \
			../ptolemy/domains/dde/lib/*.java \
			../ptolemy/domains/de/kernel/*.java \
			../ptolemy/domains/de/lib/*.java \
			../ptolemy/domains/fsm/kernel/*.java \
			../ptolemy/domains/giotto/kernel/*.java \
			../ptolemy/domains/pn/kernel/*.java \
			../ptolemy/domains/petrinet/kernel/*.java \
			../ptolemy/domains/sdf/kernel/*.java \
			../ptolemy/domains/sdf/lib/*.java \
			../ptolemy/domains/sdf/lib/security/*.java \
			../ptolemy/domains/sdf/lib/vq/*.java \
			../ptolemy/domains/sr/kernel/*.java \
			../ptolemy/domains/sr/lib/*.java \
			../ptolemy/data/*.java \
			../ptolemy/data/expr/*.java \
			../ptolemy/data/type/*.java \
			../ptolemy/data/unit/*.java \
			../ptolemy/graph/*.java \
			../ptolemy/graph/analysis/*.java \
			../ptolemy/gui/*.java \
			../ptolemy/math/*.java \
			../ptolemy/matlab/*.java \
			../ptolemy/media/*.java \
			../ptolemy/moml/*.java \
			../ptolemy/moml/filter/*.java \
			../ptolemy/vergil/*.java \
			../ptolemy/vergil/actor/*.java \
			../ptolemy/vergil/basic/*.java \
			../ptolemy/vergil/debugger/*.java \
			../ptolemy/vergil/icon/*.java \
			../ptolemy/vergil/fsm/*.java \
			../ptolemy/vergil/fsm/ia/*.java \
			../ptolemy/vergil/fsm/modal/*.java \
			../ptolemy/vergil/kernel/*.java \
			../ptolemy/vergil/toolbox/*.java \
			../ptolemy/vergil/tree/*.java
	if [ ! -d codeDoc ]; then mkdir -p codeDoc; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx132m $(JDOCBREAKITERATOR)"'
	-(cd ..; CLASSPATH=".$(CLASSPATHSEPARATOR)$(DIVA_JAR)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc $(ALLPACKAGES) )
	@echo "Removing the asd lines so that they are not displayed by"
	@echo " view documentation"
	-chmod a+x removeasd
	./removeasd codeDoc


codeDocPtiny/tree.html:
	if [ ! -d codeDocPtiny ]; then mkdir -p codeDocPtiny; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx132m"'
	-(cd ..; CLASSPATH=".$(CLASSPATHSEPARATOR)$(DIVA_JAR)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocPtiny $(PTINYPACKAGES) )
	@echo "Removing the asd lines so that they are not displayed by"
	@echo " view documentation"
	-chmod a+x removeasd
	./removeasd codeDocPtiny


# Note that we change into the codeDocHyVisual directory

# so that the jar file is created with a subset of the full set of docs
# so that we can use this smaller jar file instead of the full codeDoc.jar
codeDocHyVisual.jar: codeDocHyVisual/doc/codeDoc/tree.html
	"$(JAR)" -cvf $@ -C $(ROOT)/doc/codeDocHyVisual doc/codeDoc

# We do something crafty here and create a tree of html files
# that we can substitute in for the usual doc/codeDoc tree in the
# HyVisual webstart
codeDocHyVisual/doc/codeDoc/tree.html:
	if [ ! -d codeDocHyVisual ]; then mkdir -p codeDocHyVisual; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx132m"'
	-(cd ..; CLASSPATH=".$(CLASSPATHSEPARATOR)$(DIVA_JAR)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocHyVisual/doc/codeDoc $(HYVISUALPACKAGES) )
	@echo "Removing the asd lines so that they are not displayed by"
	@echo " view documentation"
	-chmod a+x removeasd
	./removeasd codeDocHyVisual

# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptno-compile.mk
