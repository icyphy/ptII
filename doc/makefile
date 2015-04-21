# Makefile for the Ptolemy II documentation directory
#
# Version: $Id$
# Makefile Author: Christopher Hylands
#
# Copyright (c) 1997-2015 The Regents of the University of California.
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
DIRS =		 books coding design img papers tour tutorial ui test $(PTDOCLETS_DIR)

# Root of Ptolemy II directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# COMPLETE_CLASSPATH is defined in $PTII/mk/ptII.mk.in
# We use "." instead of $PTII here because the javadoc command runs from ..
# Also, if we use $PTII, we get "javadoc: warning - Multiple sources of package comments found for package . . ."
CLASSPATH =	.$(CLASSPATHSEPARATOR)$(COMPLETE_CLASSPATH)

# Used to build jar files
PTPACKAGE = 	doc
PTVERSION =	11.0
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
	copernicus.htm \
	default.css \
	demoLink.js \
	deployJava.js \
	deployJavaJNLP.js \
	expressions.htm \
	expressions.pdf \
	openInstanceHelp.htm \
	release.htm \
	sandbox.htm \
	webStartHelp.htm \
	webStartHelp_index.htm \
	whatsnew.htm \
	whatsnewVergil.htm

EXTRA_SRCS = \
	$(UI_FILES_TO_BE_JARED) \
	banner.htm \
	body.htm \
	buttons.htm \
	checkstyle-ptII.xml \
	contents.css \
	contentsheader.htm \
	docbody.htm \
	docbuttons.htm \
	docs.htm \
	doctoc.htm \
	domainCompatibility.htm \
	domains.htm \
	findbugs-exclude.xml \
	findbugs-include.xml \
	footer.htm \
	help.htm \
	hyVisualHtml.awk \
	index.htm \
	install.htm \
	install_index.htm \
	installJNLP.htm \
	javahtml.awk \
	limitations.htm \
	main.htm \
	mainheader.htm \
	mkDomains \
	oldFeatures.htm \
	ptII$(PTVERSION).release.htm \
	ptII$(PTVERSION).releaseHyVisual.htm \
	ptIIfaq.htm \
	ptinyBcvtb.awk \
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
	$(UI_FILES_TO_BE_JARED)

	#doc/design/usingVergil/index.htm 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	$(DIRS) \
	eclipse \
	hudsonConfig.xml \
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
	com.microstar.xml

# JUnit tests
PTJUNITTESTS = \
	ptolemy.actor.gui.test.junit \
	ptolemy.vergil.basic.layout.kieler.test.junit \
	ptolemy.vergil.basic.test.junit

COREPACKAGES = \
	ptolemy.kernel \
	ptolemy.kernel.attributes \
	ptolemy.kernel.undo \
	ptolemy.kernel.util \
	ptolemy.actor \
	ptolemy.actor.continuous \
	ptolemy.actor.gui.style \
	ptolemy.actor.injection \
	ptolemy.actor.lib.hoc \
	ptolemy.actor.sched \
	ptolemy.actor.util \
	ptolemy.data \
	ptolemy.data.expr \
	ptolemy.data.type \
	ptolemy.data.unit \
	ptolemy.graph \
	ptolemy.graph.analysis \
	ptolemy.graph.analysis.analyzer \
	ptolemy.graph.analysis.strategy \
	ptolemy.graph.mapping \
	ptolemy.graph.sched \
	ptolemy.gui \
	ptolemy.math \
	ptolemy.media \
	ptolemy.moml \
	ptolemy.moml.filter \
	ptolemy.moml.unit \
	ptolemy.plot \
	ptolemy.plot.plotml \
	ptolemy.plot.compat \
	ptolemy.vergil \
	ptolemy.vergil.actor \
	ptolemy.vergil.actor.lib \
	ptolemy.vergil.basic \
	ptolemy.vergil.basic.export \
	ptolemy.vergil.basic.export.html \
	ptolemy.vergil.basic.export.web \
	ptolemy.vergil.basic.layout \
	ptolemy.vergil.basic.layout.kieler \
	ptolemy.vergil.debugger \
	ptolemy.vergil.icon \
	ptolemy.vergil.modal \
	ptolemy.vergil.modal.fmv \
	ptolemy.vergil.modal.ia \
	ptolemy.vergil.kernel \
	ptolemy.vergil.kernel.attributes \
	ptolemy.vergil.toolbox \
	ptolemy.vergil.tree \
	ptolemy.vergil.unit \

# CG Codegen packages
# cd $PTII/ptolemy/cg; find . -name "*.java" | xargs -n 1 dirname | sort | uniq | awk '{dir=substr($0, 2, length($0)-1) ; print "ptolemy/cg" dir}' | sed 's@/@.@g' | grep -v test
CGPACKAGES = \
	ptolemy.cg.adapter.generic.adapters.ptolemy.actor \
	ptolemy.cg.adapter.generic.html.adapters.ptolemy.actor \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.conversions \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.fmi \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.gui \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.hoc \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.logic \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.string \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.de.lib \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.giotto.kernel \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal.kernel \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.lib \
	ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.vergil.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib.conversions \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib.gui \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib.logic \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib.string \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.cg.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.modal \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.arduino.adapters.ptolemy.actor.lib.gui \
	ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.lib.luminary \
	ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.sdf.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.mbed.adapters.ptolemy.actor.lib.gui \
	ptolemy.cg.adapter.generic.program.procedural.c.mbed.adapters.ptolemy.actor.lib.io \
	ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.de.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.domains.de.lib \
	ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.domains.ptides.kernel \
	ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.domains.ptides.lib \
	ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor \
	ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor.lib.fmi \
	ptolemy.cg.adapter.generic.program.procedural.fmimahybrid.adapters.ptolemy.actor \
	ptolemy.cg.adapter.generic.program.procedural.fmimahybrid.adapters.ptolemy.actor.lib.fmi \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.colt \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.conversions \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.gui \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.jopio \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.logic \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.string \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.cg.lib \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.modal.kernel \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.modal.modal \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.kernel \
	ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.lib \
	ptolemy.cg.adapter.generic.program.procedural.java.modular.adapters.ptolemy.cg.lib \
	ptolemy.cg.adapter.generic.program.procedural.java.modular.adapters.ptolemy.domains.sdf.kernel \
	ptolemy.cg.gui \
	ptolemy.cg.kernel.generic \
	ptolemy.cg.kernel.generic.html \
	ptolemy.cg.kernel.generic.program \
	ptolemy.cg.kernel.generic.program.procedural \
	ptolemy.cg.kernel.generic.program.procedural.c \
	ptolemy.cg.kernel.generic.program.procedural.c.ptidyos \
	ptolemy.cg.kernel.generic.program.procedural.fmima \
	ptolemy.cg.kernel.generic.program.procedural.java \
	ptolemy.cg.kernel.generic.program.procedural.java.modular \
	ptolemy.cg.kernel.generic.program.procedural.java.type \
	ptolemy.cg.kernel.generic.syntactic \
	ptolemy.cg.kernel.generic.test.actor \
	ptolemy.cg.lib \
	ptolemy.cg.lib.syntactic

# Java packages for copernicus
COPERNICUSPACKAGES = \
	ptolemy.copernicus.applet \
	ptolemy.copernicus.gui \
	ptolemy.copernicus.kernel \
	ptolemy.copernicus.shallow \

# Packages used by data/ontologies
# Use this command to update:
# cd $PTII/ptolemy/data/ontologies; find . -name "*.java" | xargs -n 1 dirname | sort | uniq | awk '{dir=substr($0, 2, length($0)-1) ; print "ptolemy/data/ontologies" dir}' | sed 's@/@.@g'
DATAONTOLOGIESPACKAGES = \
	ptolemy.data.ontologies \
	ptolemy.data.ontologies.gui \
	ptolemy.data.ontologies.lattice \
	ptolemy.data.ontologies.lattice.adapters.abstractInterpretation.data.expr \
	ptolemy.data.ontologies.lattice.adapters.constPropagation \
	ptolemy.data.ontologies.lattice.adapters.constPropagationAbsInt \
	ptolemy.data.ontologies.lattice.adapters.constPropagationAbsInt.data.expr \
	ptolemy.data.ontologies.lattice.adapters.constAbsIntObservable.data.expr \
	ptolemy.data.ontologies.lattice.adapters.constAbstractInterpretation.data.expr \
	ptolemy.data.ontologies.lattice.adapters.constPropagation.data.expr \
	ptolemy.data.ontologies.lattice.adapters.defaultAdapters.domains.ontologies.lib \
	ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib \
	ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib.gui \
	ptolemy.data.ontologies.lattice.adapters.defaultAdapters.data.expr \
	ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis \
	ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.actor.lib \
	ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.data.expr \
	ptolemy.data.ontologies.lattice.unit \
	ptolemy.domains.ontologies.lib

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

GRAPHTRANSFORMATIONPACKAGES = \
	ptolemy.actor.gt \
	ptolemy.actor.gt.controller \
	ptolemy.actor.gt.data \
	ptolemy.actor.gt.gui \
	ptolemy.actor.gt.ingredients.criteria \
	ptolemy.actor.gt.ingredients.operations \
	ptolemy.actor.gt.util \
	ptolemy.vergil.gt

# Packages used by BCVTB: continuous, sdf and modal
# PTLBNL_PACKAGES set by configure in ptII/mk/ptII.mk if libexpat is found.
# PTMATLAB_PACKAGES is set by configure in $PTII/mk/ptII.mk
BCVTBPACKAGES = \
	$(PTFMI_PACKAGES) \
	$(PTJYTHON_PACKAGES) \
	$(PTLBNL_PACKAGES) \
	ptolemy.actor.lib.javasound \
	ptolemy.actor.lib \
	ptolemy.actor.lib.comm \
	ptolemy.actor.lib.conversions \
	ptolemy.actor.lib.gui \
	ptolemy.actor.gui \
	ptolemy.actor.gui.exec \
	ptolemy.actor.gui.jnlp \
	ptolemy.actor.gui.properties \
	ptolemy.actor.gui.ptjacl \
	ptolemy.actor.lib.image \
	ptolemy.actor.lib.io \
	ptolemy.actor.lib.jopio \
	ptolemy.actor.lib.logic \
	ptolemy.actor.lib.security \
	ptolemy.actor.lib.string \
	ptolemy.actor.lib.xslt \
	ptolemy.actor.parameters \
	ptolemy.domains.continuous.kernel \
	ptolemy.domains.continuous.kernel.solver \
	ptolemy.domains.continuous.lib \
	ptolemy.domains.modal.kernel \
	ptolemy.domains.modal.kernel.ia \
	ptolemy.domains.modal.modal \
	ptolemy.domains.sdf.kernel \
	ptolemy.domains.sdf.lib \
	ptolemy.domains.sdf.lib.vq \
	$(PTMATLAB_PACKAGES) \
	ptolemy.media.javasound \
	ptolemy.util \
	ptolemy.vergil.modal.modal

# All the BCVTB generated documentation
BCVTB_DOCS = \
	codeDocBcvtb/doc/codeDoc/tree.html \
	codeDocBcvtb/doc/codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDocBcvtb/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml


# Packages used by HyVisual
HYVISUALPACKAGES = \
	$(BCVTBPACKAGES) \
	$(PTCOLT_PACKAGES) \
	ptolemy.domains.de.kernel \
	ptolemy.domains.de.lib \
	ptolemy.domains.sdf.optimize \
	ptolemy.domains.sdf.optimize.lib

# All the HyVisual generated documentation
HYVISUAL_DOCS = \
	codeDocHyVisual/doc/codeDoc/tree.html \
	codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml

PTMACHINE_LEARNING_PACKAGES = \
	com.cureos.numerics \
	org.ptolemy.faultModels \
	org.ptolemy.machineImprovisation \
	org.ptolemy.machineLearning \
	org.ptolemy.machineLearning.hmm \
	org.ptolemy.machineLearning.hmmAOM \
	org.ptolemy.machineLearning.hsmm \
	org.ptolemy.machineLearning.particleFilter \
	org.ptolemy.optimization

# PTIDES packages
PTIDES_PACKAGES = \
	ptolemy.domains.ptides.kernel \
	ptolemy.domains.ptides.lib \
	ptolemy.domains.ptides.lib.luminary \
	ptolemy.domains.ptides.lib.qm

# Packages used by Ptiny.  
PTINYPACKAGES = \
	$(HYVISUALPACKAGES) \
	ptolemy.domains.csp.kernel \
	ptolemy.domains.csp.lib \
	ptolemy.domains.pn.kernel \
	ptolemy.domains.pn.kernel.event \
	ptolemy.domains.pn.lib

# Packages for the SUPERB classes
#SUPERBPACKAGES = \
#			ptolemy.apps.superb.actor.lib

# Packages used by TerraSwarm.  Note that configure sets
# PTJAVASCRIPT_PACKAGES to include org.terraswarm.kernel, so that
# should not be included here.
TERRASWARMPACKAGES =

# Packages used in code from Thales.
THALESPACKAGES = \
	thales.actor.gui \
	thales.vergil \
	thales.vergil.navigable

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

# This variable can be used by the user to add custom packages.
USER_PACKAGES =

# All the packages
ALLPACKAGES = \
	$(CGPACKAGES) \
	$(COREPACKAGES) \
	$(COMPACKAGES) \
	$(COPERNICUSPACKAGES) \
	$(DATAONTOLOGIESPACKAGES) \
	$(DIVAPACKAGES) \
	$(EXPERIMENTALPACKAGES) \
	$(GRAPHTRANSFORMATIONPACKAGES) \
	$(PTBACKTRACK_PACKAGES) \
	$(PTBSH_PACKAGES) \
	$(PTDATABASE_PACKAGES) \
	$(PTDB_PACKAGES) \
	$(PTDISTRIBUTED_PACKAGES) \
	$(PTEXCEL_PACKAGES) \
	$(PTFMIPP_PACKAGES) \
	$(PTG4LTL_PACKAGES) \
	$(PTGDP_PACKAGES) \
	$(PTGR_PACKAGES) \
	$(PTHLACERTI_PACKAGES) \
	$(PTHOMER_PACKAGES) \
	$(PTANGO_PACKAGES) \
	$(PTIDES_PACKAGES) \
	$(PTITEXTPDF_PACKAGES) \
	$(PTINYPACKAGES) \
	$(PTJAI_PACKAGES) \
	$(PTJAVACV_PACKAGES) \
	$(PTJAVAMAIL_PACKAGES) \
	$(PTJAVASCRIPT_PACKAGES) \
	$(PTJMF_PACKAGES) \
	$(PTJOYSTICK_PACKAGES) \
	$(PTJSON_PACKAGES) \
	$(PTJUNIT_PACKAGES) \
	$(PTJUNITTESTS) \
	$(PTMACHINE_LEARNING_PACKAGES) \
	$(PTMETROII_PACKAGES) \
	$(PTOPENMODELICA_PACKAGES) \
	$(PTOPENCV_PACKAGES) \
	$(PTOSCP5_PACKAGES) \
	$(PTPAHO_MQTT_PACKAGES) \
	$(PTQUICKTIME_PACKAGES) \
	$(PTRXTX_PACKAGES) \
	$(PTSERVER_PACKAGES) \
	$(PTSPACEDOMAIN_PACKAGES) \
	$(PTVERTX_PACKAGES) \
	$(SUPERBPACKAGES) \
	$(TERRASWARMPACKAGES) \
	$(THALESPACKAGES) \
	$(VIPTOSPACKAGES) \
	$(VISUALSENSEPACKAGES) \
	$(PTPTALON_PACKAGES) \
	$(USER_PACKAGES) \
	doc.doclets \
	org.ptolemy.qss \
	org.ptolemy.qss.solver \
	org.ptolemy.qss.util \
	org.ptolemy.ssm \
	ptolemy.actor.process \
	ptolemy.actor.lib.aspect \
	ptolemy.actor.lib.jjs \
	ptolemy.actor.lib.net \
	ptolemy.actor.lib.video \
	ptolemy.actor.lib.vhdl \
	ptolemy.caltrop \
	ptolemy.caltrop.actors \
	ptolemy.caltrop.ddi \
	ptolemy.caltrop.util \
	ptolemy.domains.algebraic.kernel \
	ptolemy.domains.ca.kernel \
	ptolemy.domains.ca.lib \
	ptolemy.domains.ca.lib.gui \
	ptolemy.domains.ci.kernel \
	ptolemy.domains.ci.lib \
	ptolemy.domains.coroutine.kernel \
	ptolemy.domains.coroutine.lib \
	ptolemy.domains.curriculum \
	ptolemy.domains.dde.kernel \
	ptolemy.domains.dde.lib \
	ptolemy.domains.ddf.kernel \
	ptolemy.domains.ddf.lib \
	ptolemy.domains.de.lib.aspect \
	ptolemy.domains.dt.kernel \
	ptolemy.domains.giotto.cgc \
	ptolemy.domains.giotto.kernel \
	ptolemy.domains.hdf.kernel \
	ptolemy.domains.modal.kernel.fmv \
	ptolemy.domains.petrinet.kernel \
	ptolemy.domains.petrinet.lib \
	ptolemy.domains.petrinet.lib.gui \
	ptolemy.domains.psdf.kernel \
	ptolemy.domains.ptera.kernel \
	ptolemy.domains.ptera.lib \
	ptolemy.domains.pthales.JNI \
	ptolemy.domains.pthales.kernel \
	ptolemy.domains.pthales.lib \
	ptolemy.domains.qss.kernel \
	ptolemy.domains.qss.lib \
	ptolemy.domains.rendezvous.kernel \
	ptolemy.domains.rendezvous.lib \
	ptolemy.domains.scr \
	ptolemy.domains.sequence.kernel \
	ptolemy.domains.sequence.gui \
	ptolemy.domains.sequence.lib \
	ptolemy.domains.sr.kernel \
	ptolemy.domains.sr.lib \
	ptolemy.domains.sr.lib.gui \
	ptolemy.domains.sysml.kernel \
	ptolemy.domains.taskpt.kernel \
	ptolemy.domains.taskpt.lib \
	ptolemy.domains.taskpt.lib.gui \
	ptolemy.domains.tdl.kernel \
	ptolemy.domains.tester.kernel \
	ptolemy.domains.tester.lib \
	ptolemy.domains.tm.kernel \
	ptolemy.domains.tm.lib \
	$(PTJSOUP_PACKAGES) \
	ptolemy.vergil.ontologies \
	ptolemy.vergil.pdfrenderer \
	ptolemy.vergil.ptera \
	ptolemy.vergil.scr \
	ptolemy.vergil.tdl \
	ptolemy.verification.gui \
	ptolemy.verification.kernel \
	ptolemy.verification.kernel.maude \
	ptolemy.verification.lib \
	util.testsuite

# All the Vergil "full"  generated documentation
FULL_DOCS = \
	codeDoc/tree.html \
	codeDoc/ptolemy/actor/lib/Ramp.xml \
	codeDoc/ptolemy/actor/lib/RampIdx.xml


# 'make clean' removes files listed in $(KRUFT)
KRUFT = \
	codeDoc \
	codeDocBcvtb \
	codeDocHyVisual \
	codeDocVisualSense \
	codeDocViptos \
	codeDoc.jar \
	codeDocBcvtb.jar \
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
install: all jars codeDoc.jar codeDocBcvtb.jar \
		codeDocHyVisual.jar codeDocViptos.jar codeDocVisualSense.jar

# Build with all common packages
all_packages:
	$(MAKE) \
	  EXPERIMENTALPACKAGES= \
	  PTRXTX_PACKAGES=ptolemy.actor.lib.io.comm \
	  "PTGR_PACKAGES=ptolemy.domains.gr.kernel ptolemy.domains.gr.lib" \
	  PTJAI_PACKAGES=ptolemy.actor.lib.jai \
	  PTJMF_PACKAGES=ptolemy.actor.lib.jmf \
	  PTJOYSTICK_PACKAGES=ptolemy.actor.lib.joystick \
	  PTQUICKTIME_PACKAGES=ptolemy.domains.gr.lib.quicktime \
	  codeDoc/tree.html

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
	sed -e 's@http://\([^"]*\)" target="_top">@http://\1#in_browser">@' \
	    -e 's@#in_browser#in_browser@#in_browser@' \
	    -e 's@(<a href="../ptolemy/domains/\([^/]*\)/doc/toc.htm" target="toc">@(<a href="../ptolemy/configs/doc/completeDemos.htm#\1">@' \
	    -e 's@<a href="../ptolemy/domains/\([^/]*\)/doc/body.htm" target="body">@<a href="../ptolemy/domains/\1/doc/main.htm">@' \
	    -e 's@<a href="../ptolemy/domains/\([^/]*\)/doc/index.htm" target="_top">@<a href="../ptolemy/domains/\1/doc/main.htm">@' \
		main.htm > $@

mainVergilPtiny.htm: mainVergil.htm
	awk -f $(ROOT)/doc/javahtml.awk mainVergil.htm | \
	grep -v ptolemy/domains/gr/doc/ | \
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
	svn commit -m "Master files changed" \
		mainVergil.htm \
		mainVergilPtiny.htm \
		mainVergilPtinyKepler.htm \
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
	@echo 'make JDOCFLAGS="-author -version -J-Xmx1500m"'
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
			../ptolemy/actor/lib/*.java \
			../ptolemy/actor/lib/comm/*.java \
			../ptolemy/actor/lib/conversions/*.java \
			../ptolemy/actor/lib/hoc/*.java \
			../ptolemy/actor/lib/io/*.java \
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
			../ptolemy/domains/curriculum/*.java \
			../ptolemy/domains/ci/kernel/*.java \
			../ptolemy/domains/ci/lib/*.java \
			../ptolemy/domains/csp/kernel/*.java \
			../ptolemy/domains/csp/lib/*.java \
			../ptolemy/domains/dde/kernel/*.java \
			../ptolemy/domains/dde/lib/*.java \
			../ptolemy/domains/ddf/kernel/*.java \
			../ptolemy/domains/ddf/lib/*.java \
			../ptolemy/domains/de/kernel/*.java \
			../ptolemy/domains/de/lib/*.java \
			../ptolemy/domains/de/lib/aspect/*.java \
			../ptolemy/domains/giotto/kernel/*.java \
			../ptolemy/domains/pn/kernel/*.java \
			../ptolemy/domains/psdf/kernel/*.java \
			../ptolemy/domains/petrinet/kernel/*.java \
			../ptolemy/domains/rendezvous/kernel/*.java \
			../ptolemy/domains/rendezvous/lib/*.java \
			../ptolemy/domains/ptides/kernel/*.java \
			../ptolemy/domains/ptides/lib/*.java \
			../ptolemy/domains/sdf/kernel/*.java \
			../ptolemy/domains/sdf/lib/*.java \
			../ptolemy/domains/sdf/lib/vq/*.java \
			../ptolemy/domains/space/*.java \
			../ptolemy/domains/sr/kernel/*.java \
			../ptolemy/domains/sr/lib/*.java \
			../ptolemy/domains/sr/lib/gui/*.java \
			../ptolemy/domains/sysml/kernel/*.java \
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
			../ptolemy/media/*.java \
			../ptolemy/moml/*.java \
			../ptolemy/moml/filter/*.java \
			../ptolemy/moml/unit/*.java \
			../ptolemy/vergil/*.java \
			../ptolemy/vergil/actor/*.java \
			../ptolemy/vergil/basic/*.java \
			../ptolemy/vergil/debugger/*.java \
			../ptolemy/vergil/icon/*.java \
			../ptolemy/vergil/kernel/*.java \
			../ptolemy/vergil/toolbox/*.java \
			../ptolemy/vergil/tree/*.java \
			../ptolemy/vergil/unit/*.java
	if [ ! -d codeDoc ]; then mkdir -p codeDoc; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx1500m $(JDOCBREAKITERATOR)"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc $(ALLPACKAGES) )

# Rule to check that ALLPACKAGES names directories that exist
chkallpackages:
	(cd ..; ls -dl `echo "$(ALLPACKAGES)" | sed 's/\*.java//g' | sed 's@\.@/@g'`) > /dev/null

# Command to check to see if package.html exists
# Try 
#  make chkpackagehtml > /tmp/s
#  cd $PTII
#  $PTII/adm/bin/mkpackagehtml `cat /tmp/s`
#  svn add `egrep '(README.txt|package.html)' /tmp/s | awk '{print $2}'`
#  svn commit -m "Added package.html and README.txt" `egrep '(README.txt|package.html)' /tmp/s | awk '{print $2}'`
chkpackagehtml:
	@(cd ..; $(PTII)/adm/bin/chkpackagehtml `echo "$(ALLPACKAGES)" | sed 's/\*.java//g'`)

echo:
	@echo "$(ALLPACKAGES)" | awk '{for(i=1;i<=NF;i++) {print $$i}}' | sort

# PtDoc, the XML format
PTDOCPACKAGES = $(ALLPACKAGES)
# Comment out the above line and uncomment this line for testing.
#PTDOCPACKAGES = ptolemy.actor.lib

# The ptdoc doclet (does not work yet) 
PTDOCLET = -doclet doc.doclets.PtDoclet
# Path relative to $PTII
PTDOCFLAGS = -d doc/codeDoc
ptdoc: codeDoc/ptolemy/actor/lib/Ramp.xml	
codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx1500m \
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
	"$(JAVA)" -Xmx1500m \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
		ptolemy.moml.filter.ActorIndex \
		codeDoc/allNamedObjs.txt \
		../ptolemy/configs/doc/models.txt codeDoc

../ptolemy/configs/doc/models.txt: \
			$(ROOT)/ptolemy/actor/gui/HTMLAbout.class \
			../ptolemy/configs/doc/completeDemos.htm
	(cd ../ptolemy/configs/doc; make models.txt)



#############################################################################
# Bcvtb

bcvtb_docs: $(BCVTB_DOCS)

# Note that we change into the codeDocBcvtb directory
# so that the jar file is created with a subset of the full set of docs
# so that we can use this smaller jar file instead of the full codeDoc.jar
codeDocBcvtb.jar: $(BCVTB_DOCS)
	"$(JAR)" -cvf $@ -C $(ROOT)/doc/codeDocBcvtb doc/codeDoc

# We do something crafty here and create a tree of html files
# that we can substitute in for the usual doc/codeDoc tree in the
# Bcvtb webstart
codeDocBcvtb/doc/codeDoc/tree.html:
	if [ ! -d codeDocBcvtb ]; then mkdir -p codeDocBcvtb; fi
	@echo "If this fails because of memory problems, try"
	@echo 'make JDOCFLAGS="-author -version -J-Xmx1500m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocBcvtb/doc/codeDoc $(BCVTBPACKAGES) $(COREPACKAGES) )

# Rules to build for Bcvtb
codeDocBcvtb/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx1500m \
			-d doc/codeDocBcvtb/doc/codeDoc \
			$(PTDOCLET) \
			$(BCVTBPACKAGES) )

# All the namedObjs for which we have documentation.
codeDocBcvtb/doc/codeDoc/allNamedObjs.txt: \
		codeDocBcvtb/doc/codeDoc/ptolemy/actor/lib/Ramp.xml

# Build the index of actors 
codeDocBcvtb/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml: codeDocBcvtb/doc/codeDoc/allNamedObjs.txt ../ptolemy/configs/bcvtb/bcvtbModels.txt
	@echo "# Create the actor index."  
	@echo "# For details, See \$PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx1500m \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
		ptolemy.moml.filter.ActorIndex \
		codeDocBcvtb/doc/codeDoc/allNamedObjs.txt \
		"../ptolemy/configs/bcvtb/bcvtbModels.txt" \
		codeDocBcvtb/doc/codeDoc

../ptolemy/configs/bcvtb/bcvtbModels.txt: \
		$(ROOT)/ptolemy/actor/gui/HTMLAbout.class \
		../ptolemy/configs/bcvtb/intro.htm
	(cd ../ptolemy/configs/bcvtb; make bcvtbModels.txt)

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
	@echo 'make JDOCFLAGS="-author -version -J-Xmx1500m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocHyVisual/doc/codeDoc $(HYVISUALPACKAGES) $(COREPACKAGES) )

# Rules to build for HyVisual
codeDocHyVisual/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx1500m \
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
	"$(JAVA)" -Xmx1500m \
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
	@echo 'make JDOCFLAGS="-author -version -J-Xmx1500m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocViptos/doc/codeDoc $(VIPTOSPACKAGES) $(VISUALSENSEPACKAGES) $(PTINYPACKAGES))

# Run PtDoclet
codeDocViptos/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx1500m \
			-d doc/codeDocViptos/doc/codeDoc \
			$(PTDOCLET) \
	$(VIPTOSPACKAGES) $(VISUALSENSEPACKAGES) $(PTINYPACKAGES) $(COREPACKAGES))

# All the namedObjs for which we have documentation.
codeDocViptos/doc/codeDoc/allNamedObjs.txt: codeDocViptos/doc/codeDoc/ptolemy/actor/lib/Ramp.xml

# Build the index of actors 
codeDocViptos/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml: \
		codeDocViptos/doc/codeDoc/allNamedObjs.txt \
		../ptolemy/configs/viptos/viptosModels.txt
	@echo "# Create the actor index."  
	@echo "# For details, See $PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx1500m \
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
	@echo 'make JDOCFLAGS="-author -version -J-Xmx1500m"'
	-(cd ..; CLASSPATH="$(CLASSPATH)" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDocVisualSense/doc/codeDoc $(VISUALSENSEPACKAGES) $(PTINYPACKAGES) $(COREPACKAGES))

# Run PtDoclet
codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/Ramp.xml:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" -J-Xmx1500m \
			-d doc/codeDocVisualSense/doc/codeDoc \
			$(PTDOCLET) \
		$(VISUALSENSEPACKAGES) $(PTINYPACKAGES) $(COREPACKAGES))

# All the namedObjs for which we have documentation.
codeDocVisualSense/doc/codeDoc/allNamedObjs.txt: codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/Ramp.xml

# Build the index of actors 
codeDocVisualSense/doc/codeDoc/ptolemy/actor/lib/RampIdx.xml: \
		codeDocVisualSense/doc/codeDoc/allNamedObjs.txt \
		../ptolemy/configs/visualsense/visualsenseModels.txt
	@echo "# Create the actor index."  
	@echo "# For details, See \$PTII/ptolemy/vergil/basic/docViewerHelp.htm"
	"$(JAVA)" -Xmx1500m \
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
			-d doc/doccheck `echo $(ALLPACKAGES)| awk '{for(i=1;i<NF;i++) print $$i}' | sort | uniq` )
######
# OJDCHECK
# An updated version of DocCheck.  See
# http://github.com/egonw/ojdcheck
# We have a local copy at
# svn co svn+ssh://source.eecs.berkeley.edu/chess/ojdcheck 
# The primary difference is that ojdcheck handles enums

OJDCHECKJAR =	lib/ojdcheck.jar
OJDCHECKDOCLET = -doclet com.github.ojdcheck.OpenJavaDocCheck -docletpath $(OJDCHECKJAR)

ojdcheck: ojdcheck.htm
ojdcheck.htm:
	-(cd ..; CLASSPATH="$(CLASSPATH)" \
		"$(JAVADOC)" \
			$(JDOCCHECKFLAGS) \
			$(OJDCHECKDOCLET) \
			-xhtml \
			-file doc/ojdcheck.htm `echo $(ALLPACKAGES)| awk '{for(i=1;i<NF;i++) print $$i}' | sort` )

#############
# FindBugs : http://findbugs.sourceforge.net
#

# Location of the FindBugs sources
#FINDBUGS=../vendors/findbugs-1.3.8
FINDBUGS=../vendors/findbugs-3.0.1

findbugs: findbugs.htm findbugsOut.xml

ptII.fb:
	echo "[Jar files]" > ptII.fb
	echo "[Source dirs]" >> ptII.fb
	echo "$(PTII)" >> ptII.fb
	echo "[Aux classpath entries]" >> ptII.fb
	echo "$(CLASSPATH)" | awk -v CLASSPATHSEPARATOR=$(CLASSPATHSEPARATOR) '{nf=split($$0, f, CLASSPATHSEPARATOR); for(i=1;i<=nf;i++) {if (length(f[i]) > 0 && f[i] != ".") {printf("%s\n", f[i])}}}' >> ptII.fb
	if [ -d "$(JXTA_DIR)" ]; then \
		for file in `ls $(JXTA_DIR)/*.jar`; do \
		echo "$$file" >> ptII.fb; \
		done; \
	fi

ptII.fbp:
	echo "<Project projectName=\"ptII\">" > ptII.fbp
	echo "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTPOWERMOCK_JARS)" | awk -v CLASSPATHSEPARATOR=$(CLASSPATHSEPARATOR) '{nf=split($$0, f, CLASSPATHSEPARATOR); for(i=1;i<=nf;i++) {if (length(f[i]) > 0 && f[i] != ".") {printf(" <AuxClasspathEntry>%s</AuxClasspathEntry>\n", f[i])}}}' >> ptII.fbp
	if [ -d "$(JXTA_DIR)" ]; then \
		for file in `ls $(JXTA_DIR)/*.jar`; do \
		echo " <AuxClasspathEntry>$$file</AuxClasspathEntry>" >> ptII.fbp; \
		done; \
	fi
	if [ -d "$(PTJAVA_HOME)/lib/ext" ]; then \
		for file in `find $(PTJAVA_HOME)/lib/ext -name "*.jar"`; do \
		echo " <AuxClasspathEntry>$$file</AuxClasspathEntry>" >> ptII.fbp; \
		done; \
	fi
	if [ -f "$(CHIC_DIR)/chic.jar" ]; then \
		echo " <AuxClasspathEntry>$(CHIC_DIR)/chic.jar</AuxClasspathEntry>" >> ptII.fbp; \
	fi
	echo " <SrcDir>$(PTII)</SrcDir>" >> ptII.fbp
	echo "</Project>" >> ptII.fbp


# Use this with FindBugs-1.8.3
#FINDBUGS_ARGS = -maxHeap 2000 -experimental -effort:max -textui -include findbugs-include.xml -exclude findbugs-exclude.xml -project ptII.fb

# Use this with FindBugs-2.0.3
# -effort:max was causing us to run out of memory, but -nested:false fixes the problem
# -nested:false avoids taking far too much time, see http://sourceforge.net/p/findbugs/bugs/1236/
FINDBUGS_ARGS = -maxHeap 2000  -nested:false -effort:max -textui -include findbugs-include.xml -exclude findbugs-exclude.xml -project ptII.fbp

findbugs.htm:  ptII.fb ptII.fbp findbugs-exclude.xml makefile
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
