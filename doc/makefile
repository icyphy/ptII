# Makefile for the Ptolemy II documentation directory
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2008 The Regents of the University of California.
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
# PTDOCLETS_DIR is set to doclets if tools.jar can be found
DIRS =		 coding design img tour tutorial ui test $(PTDOCLETS_DIR)

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Note that CALTROP_JARS should be before SOOT_JARS because
# CALTROP_JARS contains java_cup.jar and jasminclasses.jar contains
# a version of java cup
CALTROP_JARS = $(CLASSPATHSEPARATOR)$(CALTROP_DIR)/ptCal.jar$(CLASSPATHSEPARATOR)$(SAXON_JAR)$(CLASSPATHSEPARATOR)$(CALTROP_DIR)/java_cup.jar

# Classpath for the PtinyOS utilities
PTINYOS_DIR =   $(PTII)/ptolemy/domains/ptinyos
PTINYOS_CLASSPATH = $(PTINYOS_DIR)/lib/jdom.jar$(CLASSPATHSEPARATOR)$(PTINYOS_DIR)/lib/nesc.jar



CLASSPATH = .$(CLASSPATHSEPARATOR)$(ANTLR_DIR)/antlr.jar$(CLASSPATHSEPARATOR)$(PTBACKTRACK_ECLIPSE_JARS)$(CLASSPATHSEPARATOR)$(PTBACKTRACK_ECLIPSE_DOC_JARS)$(CLASSPATHSEPARATOR)$(PTBSH_JAR)$(CLASSPATHSEPARATOR)$(PTCOLT_JARS)$(CLASSPATHSEPARATOR)$(COMMAPI_DIR)/comm.jar$(CLASSPATHSEPARATOR)$(DIVA_JAR)$(CALTROP_JARS)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(JAI_JARS)$(CLASSPATHSEPARATOR)$(JINI_JARS)$(CLASSPATHSEPARATOR)$(JMF_JARS)$(CLASSPATHSEPARATOR)$(JSCLASSPATH)$(CLASSPATHSEPARATOR)$(JOYSTICK_JAR)$(CLASSPATHSEPARATOR)$(JYTHON_DIR)/jython.jar$(CLASSPATHSEPARATOR)$(TOOLS_JAR)$(CLASSPATHSEPARATOR)$(QTJAVA_ZIP)$(CLASSPATHSEPARATOR)$(PSDF_DIR)/mapss.jar$(CLASSPATHSEPARATOR)$(PTINYOS_CLASSPATH)$(CLASSPATHSEPARATOR)$(X10_JAR)


# Used to build jar files
PTPACKAGE = 	doc
PTVERSION =	7.1
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
	copernicus.htm \
	default.css \
	expressions.htm \
	expressionsa2.htm \
	expressionsa3.htm \
	expressionsa4.htm \
	expressionsa5.htm \
	expressionsa6.htm \
	expressionsa7.htm \
	expressionsa8.htm \
	expressionsa9.htm \
	expressionsa10.htm \
	expressionsa11.htm \
	expressionsa12.htm \
	openInstanceHelp.htm \
	sandbox.htm \
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
	domainCompatibility.htm \
	domains.htm \
	findbugs-exclude.xml \
	footer.htm \
	help.htm \
	hyVisualHtml.awk \
	index.htm \
	install.htm \
	installJNLP.htm \
	javahtml.awk \
	limitations.htm \
	main.htm \
	mainheader.htm \
	oldFeatures.htm \
	ptII$(PTVERSION).release.htm \
	ptII$(PTVERSION).releaseHyVisual.htm \
	ptIIfaq.htm \
	ptinyKepler.awk \
	removeasd \
	toc.css \
	toc.htm \
	troubleshooting.htm \
	types.htm \
	whatsnew.awk

# Derived files that are preprocessed for use within vergil.
VERGIL_DOCS = \
	mainVergil.htm \
	mainVergilPtiny.htm \
	mainVergilPtinyKepler.htm \
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
MISC_FILES = \
	$(DIRS) \
	$(VERGIL_DOCS) \
	img \
	tutorial

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	codeDoc \
	codeDoc.jar \
	codeDocPtiny \
	uml

# Packages under the com package
COMPACKAGES = \
	com.JLex \
	com.microstar.xml

# Java packages for copernicus 

# Don't include copernicus.jhdl because it requires byucc.jhdl from JHDL.jar
#JHDLPACKAGE = 		ptolemy.copernicus.jhdl

# Javadoc can't deal with packages that end with 'java', see
# http://developer.java.sun.com/developer/bugParade/bugs/4507380.html
# So, we expand the ptolemy.copernicus.java package
COPERNICUSPACKAGES = \
	ptolemy.copernicus.applet \
	ptolemy.copernicus.c \
	ptolemy.copernicus.gui \
	ptolemy.copernicus.kernel \
	ptolemy/copernicus/java/*.java \
	$(JHDLPACKAGE) \
	ptolemy.copernicus.shallow

# Packages used by Diva
DIVAPACKAGES = \
	diva.canvas \
	diva.canvas.connector \
	diva.canvas.demo \
	diva.canvas.event \
	diva.canvas.interactor \
	diva.canvas.test \
	diva.canvas.toolbox \
	diva.canvas.tutorial \
	diva.graph \
	diva.graph.basic \
	diva.graph.layout \
	diva.graph.modular \
	diva.graph.test \
	diva.graph.toolbox \
	diva.graph.tutorial \
	diva.gui \
	diva.gui.toolbox \
	diva.gui.tutorial \
	diva.resource \
	diva.util \
	diva.util.java2d \
	diva.util.jester \
	diva.util.test \
	diva.util.xml

# Experimental packages that are not shipped
EXPERIMENTALPACKAGES = 

#	ptolemy.domains.rtp.kernel \
#	ptolemy.domains.rtp.lib


GRAPHTRANSFORMATIONPACKAGES = \
	ptolemy.actor.gt \
	ptolemy.actor.gt.data \
	ptolemy.actor.gt.ingredients.criteria \
	ptolemy.actor.gt.ingredients.operations \
	ptolemy.actor.gt.util \
	ptolemy.vergil.gt

# Packages used by HyVisual
HYVISUALPACKAGES = \
	$(PTCOLT_PACKAGES) \
	$(PTJYTHON_PACKAGES) \
	ptolemy.actor.lib.javasound \
	ptolemy.actor.lib \
	ptolemy.actor.lib.comm \
	ptolemy.actor.lib.conversions \
	ptolemy.actor.lib.gui \
	ptolemy.actor.lib.image \
	ptolemy.actor.lib.io \
	ptolemy.actor.lib.logic \
	ptolemy.actor.lib.security \
	ptolemy.actor.lib.string \
	ptolemy.actor.lib.xslt \
	ptolemy.actor.parameters \
	ptolemy.domains.continuous.kernel \
	ptolemy.domains.continuous.kernel.solver \
	ptolemy.domains.continuous.lib \
	ptolemy.domains.ct.kernel \
	ptolemy.domains.ct.kernel.solver \
	ptolemy.domains.ct.lib \
	ptolemy.domains.de.kernel \
	ptolemy.domains.de.lib \
	ptolemy.domains.fsm.kernel \
	ptolemy.domains.fsm.kernel.ia \
	ptolemy.domains.fsm.modal \
	ptolemy.domains.sdf.kernel \
	ptolemy.domains.sdf.lib \
	ptolemy.domains.sdf.lib.vq \
	ptolemy.hsif \
	ptolemy.matlab \
	ptolemy.media.javasound \
	ptolemy.util \
	ptolemy.vergil.fsm.modal

# All the HyVisual generated documentation
HYVISUAL_DOCS = \
	codeDocHyVisual/doc/codeDoc/tree.html \
	codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml

# Currently, there are no .java files in fsm.lib
#			ptolemy.domains.fsm.lib

# PTIDES packages
PTIDES_PACKAGES = \
	ptolemy.domains.ptides.kernel \
	ptolemy.domains.ptides.lib \
	ptolemy.domains.ptides.platform

# Packages used by Ptiny.  
PTINYPACKAGES = \
	$(HYVISUALPACKAGES) \
	ptolemy.domains.csp.kernel \
	ptolemy.domains.csp.lib \
	ptolemy.domains.pn.kernel \
	ptolemy.domains.pn.lib

# Packages for the SUPERB classes
#SUPERBPACKAGES = \
#			ptolemy.apps.superb.actor.lib

# Packages used in code from Thales.
THALESPACKAGES = \
	thales.actor.gui \
	thales.vergil \
	thales.vergil.navigable

# Time Triggered (TT) domain packages
TT_PACKAGES = \
	ptolemy.domains.tt.kernel \
	ptolemy.domains.tt.tdl.kernel

# All the Viptos generated documentation
VIPTOS_DOCS = \
	codeDocViptos/doc/codeDoc/tree.html \
	codeDocViptos/doc/codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDocViptos/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml

# Packages used by Viptsos
VIPTOSPACKAGES = \
	ptolemy.domains.ptinyos.gui \
	ptolemy.domains.ptinyos.kernel \
	ptolemy.domains.ptinyos.lib \
	ptolemy.domains.ptinyos.util.nc2moml \
	ptolemy.domains.ptinyos.util.ncapp2moml \

# Packages used by VisualSense
VISUALSENSEPACKAGES = \
	ptolemy.domains.wireless.kernel \
	ptolemy.domains.wireless.lib

# All the VisualSense generated documentation
VISUALSENSE_DOCS = \
	codeDocVisualSense/doc/codeDoc/tree.html \
	codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml

#	ptolemy.domains.wireless.lib.network \
#	ptolemy.domains.wireless.lib.network.mac


# PTBACKTRACK_ECLPISE_PACKAGES is set by configure
PTBACKTRACK_PACKAGES = \
	ptolemy.backtrack \
	ptolemy.backtrack.automatic.ptolemy.actor.lib \
	ptolemy.backtrack.automatic.ptolemy.domains.sdf.lib \
	ptolemy.backtrack.manual.ptolemy.domains.ct.kernel \
	ptolemy.backtrack.manual.ptolemy.actor.lib \
	ptolemy.backtrack.ui \
	ptolemy.backtrack.util \
	ptolemy.backtrack.xmlparser \
	$(PTBACKTRACK_ECLIPSE_PACKAGES) 

# Exclude ptolemy.backtrack.automatic.*, to fix these we would need
# to modify the Eclipse transformer so that we can get comments from
# the java source that is parsed.
#			ptolemy.backtrack.automatic.ptolemy.actor.lib \
#			ptolemy.backtrack.automatic.ptolemy.domains.sdf.lib \

# All the Java 1.4 packages
ALL1_4PACKAGES = \
	$(COMPACKAGES) \
	$(COPERNICUSPACKAGES) \
	$(DIVAPACKAGES) \
	$(EXPERIMENTALPACKAGES) \
	$(GRAPHTRANSFORMATIONPACKAGES) \
	$(PTBACKTRACK_PACKAGES) \
	$(PTBSH_PACKAGES) \
	$(PTCOMM_PACKAGES) \
	$(PTDISTRIBUTED_PACKAGES) \
	$(PTGR_PACKAGES) \
	$(PTIDES_PACKAGES) \
	$(PTINYPACKAGES) \
	$(PTJAI_PACKAGES) \
	$(PTJMF_PACKAGES) \
	$(PTJOYSTICK_PACKAGES) \
	$(PTQUICKTIME_PACKAGES) \
	$(PTX10_PACKAGES) \
	$(SUPERBPACKAGES) \
	$(THALESPACKAGES) \
	$(TT_PACKAGES) \
	$(VIPTOSPACKAGES) \
	$(VISUALSENSEPACKAGES) \
	doc.doclets \
	jni \
	jni.gui \
	ptolemy.plot \
	ptolemy.plot.plotml \
	ptolemy.plot.compat \
	ptolemy.kernel \
	ptolemy.kernel.attributes \
	ptolemy.kernel.undo \
	ptolemy.kernel.util \
	ptolemy.actor \
	ptolemy.actor.gui \
	ptolemy.actor.gui.style \
	ptolemy.actor.gui.unit \
	ptolemy.actor.process \
	ptolemy.actor.lib.hoc \
	ptolemy.actor.lib.jni \
	ptolemy.actor.lib.net \
	ptolemy.actor.lib.vhdl \
	ptolemy.actor.sched \
	ptolemy.actor.util \
	ptolemy.codegen.c.actor \
	ptolemy.codegen.c.actor.lib \
	ptolemy.codegen.c.actor.lib.comm \
	ptolemy.codegen.c.actor.lib.conversions \
	ptolemy.codegen.c.actor.lib.gui \
	ptolemy.codegen.c.actor.lib.hoc \
	ptolemy.codegen.c.actor.lib.io \
	ptolemy.codegen.c.actor.lib.javasound \
	ptolemy.codegen.c.actor.lib.jni \
	ptolemy.codegen.c.actor.lib.logic \
	ptolemy.codegen.c.actor.lib.string \
	ptolemy.codegen.c.actor.sched \
	ptolemy.codegen.c.domains.fsm.kernel \
	ptolemy.codegen.c.domains.fsm.modal \
	ptolemy.codegen.c.domains.hdf.kernel \
	ptolemy.codegen.c.domains.sdf.kernel \
	ptolemy.codegen.c.domains.sdf.lib \
	ptolemy.codegen.c.kernel \
	ptolemy.codegen.gui \
	ptolemy.codegen.kernel \
	ptolemy.codegen.vhdl.actor.lib.vhdl \
	ptolemy.codegen.vhdl.kernel \
	ptolemy.domains.ci.kernel \
	ptolemy.domains.ci.lib \
	ptolemy.domains.curriculum \
	ptolemy.domains.dde.kernel \
	ptolemy.domains.dde.lib \
	ptolemy.domains.ddf.kernel \
	ptolemy.domains.ddf.lib \
	ptolemy.domains.dt.kernel \
	ptolemy.domains.fsm.kernel.fmv \
	ptolemy.domains.giotto.kernel \
	ptolemy.domains.hdf.kernel \
	ptolemy.domains.petrinet.kernel \
	ptolemy.domains.psdf.kernel \
	ptolemy.domains.rendezvous.kernel \
	ptolemy.domains.rendezvous.lib \
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
	ptolemy.media \
	ptolemy.moml \
	ptolemy.moml.filter \
	ptolemy.vergil \
	ptolemy.vergil.actor \
	ptolemy.vergil.actor.lib \
	ptolemy.vergil.basic \
	ptolemy.vergil.debugger \
	ptolemy.vergil.icon \
	ptolemy.vergil.fsm \
	ptolemy.vergil.fsm.fmv \
	ptolemy.vergil.fsm.ia \
	ptolemy.vergil.kernel \
	ptolemy.vergil.kernel.attributes \
	ptolemy.vergil.toolbox \
	ptolemy.vergil.tree \
	util.testsuite

# This variable can be used by the user to add custom packages.
USER_PACKAGES =

# All the packages
ALLPACKAGES = \
	$(ALL1_4PACKAGES) \
	$(PTPTALON_PACKAGES) \
	$(USER_PACKAGES) \
	ptolemy.caltrop \
	ptolemy.caltrop.actors \
	ptolemy.caltrop.ddi \
	ptolemy.caltrop.util


# All the Vergil "full"  generated documentation
FULL_DOCS = \
	codeDoc/tree.html \
	codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDoc/ptolemy/actor/lib/RampIdx.xml


# 'make clean' removes files listed in $(KRUFT)
KRUFT = \
	codeDoc.jar \
	codeDocHyVisual.jar \
	codeDocVisualSense.jar \
	codeDocViptos.jar \
	docConfig.jar \
	findbugs.htm \
	findbugs.xml \
	ptII.fb


all: suball whatsnew.htm whatsnewVergil.htm
	@echo "   To build the javadoc and actor indexes for the full version,"
	@echo "   run \"make docs\""
	@echo "   To build the javadoc and actor indexes for all versions,"
	@echo "   run \"make install\""

# Building the jar files also builds javadoc, PtDoclet and actor indices
install: all jars codeDoc.jar \
		codeDocHyVisual.jar codeDocViptos.jar codeDocVisualSense.jar

# Build with all common packages
all_packages:
	$(MAKE) \
	  EXPERIMENTALPACKAGES= \
	  PTCOMM_PACKAGES=ptolemy.actor.lib.io.comm \
	  "PTGR_PACKAGES=ptolemy.domains.gr.kernel ptolemy.domains.gr.lib" \
	  PTJAI_PACKAGES=ptolemy.actor.lib.jai \
	  PTJMF_PACKAGES=ptolemy.actor.lib.jmf \
	  PTJOYSTICK_PACKAGES=ptolemy.actor.lib.joystick \
	  PTQUICKTIME_PACKAGES=ptolemy.domains.gr.lib.quicktime \
	  PTX10_PACKAGES=ptolemy.actor.lib.x10 codeDoc/tree.html

#############################################################################
# Make derived versions of .htm files for Vergil

# Vergil gets special versions of the html files because the JDK1.3 Java
# HTML viewer is not very functional.
vergil_docs: $(VERGIL_DOCS)
$(VERGIL_DOCS): javahtml.awk

# Version of main.htm without links with targets
# Convert <a href="foobar" target="body">bif</a> -> bif
# and the convert Ptolemy Project web page to
#     <a href="http://ptolemy.eecs.berkeley.edu/#in_browser">Ptolemy Project web page</a>
mainVergil.htm: main.htm
	sed -e 's@" target="_top">@#in_browser">@' \
	    -e 's@body.htm" target="body">@main.htm">@' \
	    -e 's@(<a href="../ptolemy/domains/\([^/]*\)/doc/toc.htm" target="toc">@(<a href="../ptolemy/configs/doc/completeDemos.htm#\1">@' \
		main.htm > $@

mainVergilPtiny.htm: mainVergil.htm
	awk -f $(ROOT)/doc/javahtml.awk mainVergil.htm $@ | \
	sed -e 's@completeDemos.htm@completeDemosPtiny.htm@' > $@  

mainVergilPtinyKepler.htm: mainVergilPtiny.htm
	awk -f $(ROOT)/doc/ptinyKepler.awk mainVergilPtiny.htm | grep -v rendezvous | grep -v "with synchronous message" | \
	sed -e 's@completeDemosPtiny.htm@completeDemosPtinyKepler.htm@' > $@  

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
		< whatsnew.htm > whatsnewVergil.htm

# Files that are derived from other files, such as ptII$(PTVERSION).release.htm
DERIVED_FILES = \
	mainVergil.htm \
	mainVergilPtiny.htm \
	mainVergilPtinyKepler.htm \
	ptII$(PTVERSION).releaseVergil.htm \
	ptII$(PTVERSION).releaseHyVisual.htm \
	troubleshootingVergil.htm \
	whatsnew.htm \
	whatsnewVergil.htm 

update: $(DERIVED_FILES)
	cvs commit -m "Master files changed" \
		mainVergil.htm \
		mainVergilPtiny.htm \
		ptII$(PTVERSION).releaseVergil.htm \
		troubleshootingVergil.htm \
		whatsnew.htm whatsnewVergil.htm 
	rm -f docConfig.jar
	$(MAKE) docConfig.jar

update_clean: 
	rm -f $(DERIVED_FILES)

# Run javadoc, the doclet and create the Actor/Demo index 
docs: $(FULL_DOCS)

codeDocPtiny/tree.html:
	if [ ! -d codeDocPtiny ]; then mkdir -p codeDocPtiny; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx512m"'
	-(cd ..; CLASSPATH=".$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocPtiny $(PTINYPACKAGES) )


#############################################################################
# Full Default Vergil

javadocs: full_docs
full_docs: $(FULL_DOCS)

codeDoc.jar: $(FULL_DOCS)
	"$(JAR)" $(JARFLAGS) -cvf $@ -C $(ROOT) doc/codeDoc

codeDoc/tree.html:	../ptolemy/plot/*.java \
			../ptolemy/plot/compat/*.java \
			../ptolemy/plot/plotml/*.java \
			../ptolemy/kernel/*.java \
			../ptolemy/kernel/attributes/*.java \
			../ptolemy/kernel/undo/*.java \
			../ptolemy/kernel/util/*.java \
			../ptolemy/actor/*.java \
			../ptolemy/actor/gui/*.java \
			../ptolemy/actor/gui/style/*.java \
			../ptolemy/actor/gui/unit/*.java \
			../ptolemy/actor/lib/*.java \
			../ptolemy/actor/lib/comm/*.java \
			../ptolemy/actor/lib/conversions/*.java \
			../ptolemy/actor/lib/hoc/*.java \
			../ptolemy/actor/lib/io/*.java \
			../ptolemy/actor/lib/jni/*.java \
			../ptolemy/actor/lib/net/*.java \
			../ptolemy/actor/lib/gui/*.java \
			../ptolemy/actor/lib/logic/*.java \
			../ptolemy/actor/lib/security/*.java \
			../ptolemy/actor/lib/string/*.java \
			../ptolemy/actor/lib/vhdl/*.java \
			../ptolemy/actor/lib/xslt/*.java \
			../ptolemy/actor/parameters/*.java \
			../ptolemy/actor/process/*.java \
			../ptolemy/actor/sched/*.java \
			../ptolemy/actor/util/*.java \
			../ptolemy/codegen/c/actor/*.java \
			../ptolemy/codegen/c/actor/lib/*.java \
			../ptolemy/codegen/c/actor/lib/comm/*.java \
			../ptolemy/codegen/c/actor/lib/conversions/*.java \
			../ptolemy/codegen/c/actor/lib/gui/*.java \
			../ptolemy/codegen/c/actor/lib/hoc/*.java \
			../ptolemy/codegen/c/actor/lib/io/*.java \
			../ptolemy/codegen/c/actor/lib/javasound/*.java \
			../ptolemy/codegen/c/actor/lib/jni/*.java \
			../ptolemy/codegen/c/actor/lib/logic/*.java \
			../ptolemy/codegen/c/actor/lib/string/*.java \
			../ptolemy/codegen/c/actor/sched/*.java \
			../ptolemy/codegen/c/domains/fsm/kernel/*.java \
			../ptolemy/codegen/c/domains/fsm/modal/*.java \
			../ptolemy/codegen/c/domains/hdf/kernel/*.java \
			../ptolemy/codegen/c/domains/sdf/kernel/*.java \
			../ptolemy/codegen/c/domains/sdf/lib/*.java \
			../ptolemy/codegen/c/kernel/*.java \
			../ptolemy/codegen/gui/*.java \
			../ptolemy/codegen/kernel/*.java \
			../ptolemy/codegen/vhdl/actor/lib/vhdl/*.java \
			../ptolemy/codegen/vhdl/kernel/*.java \
			../ptolemy/domains/curriculum/*.java \
			../ptolemy/domains/ci/kernel/*.java \
			../ptolemy/domains/ci/lib/*.java \
			../ptolemy/domains/csp/kernel/*.java \
			../ptolemy/domains/csp/lib/*.java \
			../ptolemy/domains/ct/kernel/*.java \
			../ptolemy/domains/ct/kernel/solver/*.java \
			../ptolemy/domains/ct/lib/*.java \
			../ptolemy/domains/dde/kernel/*.java \
			../ptolemy/domains/dde/lib/*.java \
			../ptolemy/domains/ddf/kernel/*.java \
			../ptolemy/domains/ddf/lib/*.java \
			../ptolemy/domains/de/kernel/*.java \
			../ptolemy/domains/de/lib/*.java \
			../ptolemy/domains/fsm/kernel/*.java \
			../ptolemy/domains/giotto/kernel/*.java \
			../ptolemy/domains/pn/kernel/*.java \
			../ptolemy/domains/psdf/kernel/*.java \
			../ptolemy/domains/petrinet/kernel/*.java \
			../ptolemy/domains/rendezvous/kernel/*.java \
			../ptolemy/domains/rendezvous/lib/*.java \
			../ptolemy/domains/ptides/kernel/*.java \
			../ptolemy/domains/ptides/lib/*.java \
			../ptolemy/domains/ptides/platform/*.java \
			../ptolemy/domains/sdf/kernel/*.java \
			../ptolemy/domains/sdf/lib/*.java \
			../ptolemy/domains/sdf/lib/vq/*.java \
			../ptolemy/domains/sr/kernel/*.java \
			../ptolemy/domains/sr/lib/*.java \
			../ptolemy/domains/tm/kernel/*.java \
			../ptolemy/domains/tm/lib/*.java \
			../ptolemy/domains/wireless/kernel/*.java \
			../ptolemy/domains/wireless/lib/*.java \
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
	@echo 'make JDOCFLAGS="-author -version -J-Xmx512m $(JDOCBREAKITERATOR)"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc $(ALLPACKAGES) )


# PtDoc, the XML format
PTDOCPACKAGES = $(ALL1_4PACKAGES)
# Comment out the above line and uncomment this line for testing.
#PTDOCPACKAGES = ptolemy.actor.lib

# The ptdoc doclet (does not work yet) 
PTDOCLET = -doclet doc.doclets.PtDoclet
# Path relative to $PTII
PTDOCFLAGS = -d doc/codeDoc
ptdoc: codeDoc/ptolemy/actor/lib/Ramp.xml	
codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx512m \
			$(PTDOCFLAGS) \
			$(PTDOCLET) \
			$(PTDOCPACKAGES) )

# All the namedObjs for which we have documentation.
codeDoc/allNamedObjs.txt: codeDoc/ptolemy/actor/lib/Ramp.xml

# Create the actor index.  See $PTII/ptolemy/vergil/actor/docViewerHelp.htm
indexActors: codeDoc/ptolemy/actor/lib/RampIdx.xml
codeDoc/ptolemy/actor/lib/RampIdx.xml: codeDoc/allNamedObjs.txt \
			../ptolemy/configs/doc/models.txt
	@echo "# Create the actor index."  
	@echo "# For details, See \$PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx256m \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
		ptolemy.moml.filter.ActorIndex \
		codeDoc/allNamedObjs.txt \
		../ptolemy/configs/doc/models.txt codeDoc

../ptolemy/configs/doc/models.txt: \
			$(ROOT)/ptolemy/actor/gui/HTMLAbout.class \
			../ptolemy/configs/doc/completeDemos.htm
	(cd ../ptolemy/configs/doc; make mModels.txt)



#############################################################################
# HyVisual

hyvisual_docs: $(HYVISUAL_DOCS)

# Note that we change into the codeDocHyVisual directory
# so that the jar file is created with a subset of the full set of docs
# so that we can use this smaller jar file instead of the full codeDoc.jar
codeDocHyVisual.jar: $(HYVISUAL_DOCS)
	"$(JAR)" -cvf $@ -C $(ROOT)/doc/codeDocHyVisual doc/codeDoc

# We do something crafty here and create a tree of html files
# that we can substitute in for the usual doc/codeDoc tree in the
# HyVisual webstart
codeDocHyVisual/doc/codeDoc/tree.html:
	if [ ! -d codeDocHyVisual ]; then mkdir -p codeDocHyVisual; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx512m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocHyVisual/doc/codeDoc $(HYVISUALPACKAGES) )

# Rules to build for HyVisual
codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx512m \
			-d doc/codeDocHyVisual/doc/codeDoc \
			$(PTDOCLET) \
			$(HYVISUALPACKAGES) )

# All the namedObjs for which we have documentation.
codeDocHyVisual/doc/codeDoc/allNamedObjs.txt: \
		codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/Ramp.xml

# Build the index of actors 
codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml: codeDocHyVisual/doc/codeDoc/allNamedObjs.txt ../ptolemy/configs/hyvisual/hyvisualModels.txt
	@echo "# Create the actor index."  
	@echo "# For details, See \$PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx256m \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
		ptolemy.moml.filter.ActorIndex \
		codeDocHyVisual/doc/codeDoc/allNamedObjs.txt \
		"../ptolemy/configs/hyvisual/hyvisualModels.txt" \
		codeDocHyVisual/doc/codeDoc

../ptolemy/configs/hyvisual/hyvisualModels.txt: \
		$(ROOT)/ptolemy/actor/gui/HTMLAbout.class \
		../ptolemy/configs/hyvisual/intro.htm
	(cd ../ptolemy/configs/hyvisual; make hyvisualModels.txt)

#############################################################################
# Viptos

viptos_docs: $(VIPTOS_DOCS)

# Note that we change into the codeDocViptos directory
# so that the jar file is created with a subset of the full set of docs
# so that we can use this smaller jar file instead of the full codeDoc.jar
codeDocViptos.jar: $(VIPTOS_DOCS)
	"$(JAR)" -cvf $@ -C $(ROOT)/doc/codeDocViptos doc/codeDoc

# We do something crafty here and create a tree of html files
# that we can substitute in for the usual doc/codeDoc tree in the
# Viptos webstart
codeDocViptos/doc/codeDoc/tree.html:
	if [ ! -d codeDocViptos ]; then mkdir -p codeDocViptos; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx512m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocViptos/doc/codeDoc $(VIPTOSPACKAGES) $(VISUALSENSEPACKAGES) $(PTINYPACKAGES))

# Run PtDoclet
codeDocViptos/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx512m \
			-d doc/codeDocViptos/doc/codeDoc \
			$(PTDOCLET) \
	$(VIPTOSPACKAGES) $(VISUALSENSEPACKAGES) $(PTINYPACKAGES))

# All the namedObjs for which we have documentation.
codeDocViptos/doc/codeDoc/allNamedObjs.txt: codeDocViptos/doc/codeDoc/ptolemy/actor/lib/Ramp.xml

# Build the index of actors 
codeDocViptos/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml: \
		codeDocViptos/doc/codeDoc/allNamedObjs.txt \
		../ptolemy/configs/viptos/viptosModels.txt
	@echo "# Create the actor index."  
	@echo "# For details, See $PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx256m \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
		ptolemy.moml.filter.ActorIndex \
		codeDocViptos/doc/codeDoc/allNamedObjs.txt \
		../ptolemy/configs/viptos/viptosModels.txt \
		codeDocViptos/doc/codeDoc

$(ROOT)/ptolemy/configs/viptos/viptosModels.txt: \
			$(ROOT)/ptolemy/actor/gui/HTMLAbout.class \
			../ptolemy/configs/viptos/intro.htm
	(cd ../ptolemy/configs/viptos; make viptosModels.txt)

#############################################################################
# VisualSense

visualsense_docs: $(VISUALSENSE_DOCS)

# Note that we change into the codeDocVisualSense directory
# so that the jar file is created with a subset of the full set of docs
# so that we can use this smaller jar file instead of the full codeDoc.jar
codeDocVisualSense.jar: $(VISUALSENSE_DOCS)
	"$(JAR)" -cvf $@ -C $(ROOT)/doc/codeDocVisualSense doc/codeDoc

# We do something crafty here and create a tree of html files
# that we can substitute in for the usual doc/codeDoc tree in the
# VisualSense webstart
codeDocVisualSense/doc/codeDoc/tree.html:
	if [ ! -d codeDocVisualSense ]; then mkdir -p codeDocVisualSense; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx512m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocVisualSense/doc/codeDoc $(VISUALSENSEPACKAGES) $(PTINYPACKAGES))

# Run PtDoclet
codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx512m \
			-d doc/codeDocVisualSense/doc/codeDoc \
			$(PTDOCLET) \
		$(VISUALSENSEPACKAGES) $(PTINYPACKAGES))

# All the namedObjs for which we have documentation.
codeDocVisualSense/doc/codeDoc/allNamedObjs.txt: codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/Ramp.xml

# Build the index of actors 
codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml: \
		codeDocVisualSense/doc/codeDoc/allNamedObjs.txt \
		../ptolemy/configs/visualsense/visualsenseModels.txt
	@echo "# Create the actor index."  
	@echo "# For details, See \$PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx256m \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
		ptolemy.moml.filter.ActorIndex \
		codeDocVisualSense/doc/codeDoc/allNamedObjs.txt \
		../ptolemy/configs/visualsense/visualsenseModels.txt \
		codeDocVisualSense/doc/codeDoc

../ptolemy/configs/visualsense/visualsenseModels.txt: \
		$(ROOT)/ptolemy/actor/gui/HTMLAbout.class \
		../ptolemy/configs/visualsense/intro.htm
	(cd ../ptolemy/configs/visualsense; make visualsenseModels.txt)

#############################################################################
# Doccheck

# Doccheck is a doclet that checks for bugs
# Location of the doccheck jar file, available from
# http://java.sun.com/developer/earlyAccess/doccheck/
# Note that this is relative to $(ROOT) because we cd
# up one level
DOCCHECKJAR =	vendors/sun/doccheck1.2b2/doccheck.jar
JDOCCHECKDOCLET = -doclet com.sun.tools.doclets.doccheck.DocCheck  \
	-docletpath $(DOCCHECKJAR)

doccheck: doccheck/index.html
doccheck/index.html:
	if [ ! -d doccheck ]; then mkdir -p doccheck; fi
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" \
			$(JDOCCHECKFLAGS) \
			$(JDOCCHECKDOCLET) \
			-d doc/doccheck $(ALLPACKAGES) )

#############
# FindBugs : http://findbugs.sourceforge.net
#

# Location of the FindBugs sources
FINDBUGS=../vendors/findbugs-1.3.3-rc1

# findbugs.xml is special to findbugs, if we use findbugs.xml, we get:
# Warning: could not load plugin file:/c:/cxh/ptII/vendors/findbugs-1.3.1/plugin/coreplugin.jar: java.security.PrivilegedActionException: edu.umd.cs.findbugs.PluginException: Couldn't parse "findbugs.xml"

findbugs: findbugs.htm findbugsOut.xml

ptII.fb:
	echo "[Jar files]" > ptII.fb
	echo "[Source dirs]" >> ptII.fb
	echo "$(PTII)" >> ptII.fb
	echo "[Aux classpath entries]" >> ptII.fb
	for file in `ls $(PTII)/lib/*.jar`; do \
	echo "$$file" >> ptII.fb; \
	done
	if [ -d "$(JXTA_DIR)" ]; then \
		for file in `ls $(JXTA_DIR)/*.jar`; do \
		echo "$$file" >> ptII.fb; \
		done; \
	fi
	if [ -d "$(ANTLR_DIR)" ]; then \
		for file in `ls $(ANTLR_DIR)/*.jar`; do \
		echo "$$file" >> ptII.fb; \
		done; \
	fi
	echo $(TOOLS_JAR) >> ptII.fb 

FINDBUGS_ARGS = -maxHeap 900 -experimental -effort:max -textui -exclude findbugs-exclude.xml -project ptII.fb

findbugs.htm:  ptII.fb findbugs-exclude.xml makefile
	@if [ -f "$(FINDBUGS)/bin/findbugs" ]; then \
	    echo "Invoking findbugs program to create $@, please wait."; \
	    "$(FINDBUGS)/bin/findbugs" \
		$(FINDBUGS_ARGS) \
		-html:fancy.xsl "$(PTII)" > $@; \
	else \
	    echo "BTW - FindBugs program not found, skipping."; \
	    echo "BTW - FindBugs program not found, skipping." > $@; \
	fi


findbugsOut.xml:  ptII.fb findbugs-exclude.xml makefile
	rm -f $@;
	@if [ -f "$(FINDBUGS)/bin/findbugs" ]; then \
	    echo "Invoking findbugs program to create $@, please wait."; \
	    echo "$(FINDBUGS)/bin/findbugs $(FINDBUGS_ARGS) -xml -output $@ $(PTII)"; \
	    "$(FINDBUGS)/bin/findbugs" \
		$(FINDBUGS_ARGS) \
		-xml -output $@ "$(PTII)" ; \
	else \
	    echo "BTW - FindBugs program not found, skipping."; \
	fi

#############################################################################
# We don't include common.mk since we don't use the compiler here
include $(ROOT)/mk/ptcommon.mk
