# Makefile for ECSL actor classes
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2004 The Regents of the University of California.
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

ME =		vendors/ecsl_dp/ptolemy

DIRS = 		# test

# Root of the Java directory
ROOT =		../../..

# Do not add diva.jar to the CLASSPATH in actor/lib/makefile
# instead, move any actors that require diva.jar into actor/lib/gui -cxh
CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ecslPtolemy
PTCLASSJAR =    ecslptolemy.jar

# Keep this list alphabetized.
JSRCS = \
	ECSLAbs.java \
	ECSLClock.java \
	ECSLConstant.java \
	ECSLDemux.java \
	ECSLDiscreteFilter.java \
	ECSLDiscretePulseGenerator.java \
	ECSLDisplay.java \
	ECSLFrom.java \
	ECSLGain.java \
	ECSLGoto.java \
	ECSLGround.java \
	ECSLIntegrator.java \
	ECSLLogic.java \
	ECSLMerge.java \
	ECSLMinMax.java \
	ECSLMultiPortSwitch.java \
	ECSLMux.java \
	ECSLProduct.java \
	ECSLRelationalOperator.java \
	ECSLSFunction.java \
	ECSLSaturate.java \
	ECSLSignum.java \
	ECSLSin.java \
	ECSLStep.java \
	ECSLSum.java \
	ECSLSwitch.java \
	ECSLTerminator.java \
	ECSLToWorkspace.java \
	ECSLUnitDelay.java \
	ECSLZeroOrderHold.java

OTHER_FILES_TO_BE_JARED = \

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

cruiseControlECSL.moml: cruiseControlECSL.xml ecsl2moml
	./ecsl2moml $<

cruiseControlECSL.xml: cruiseControl.xml
	../XML2ECSL/bin/Xml2Ecsl_d $< cruiseControlECSL.xml

cruiseControl.xml: cruiseControl.mdl
	../MDL2XML/bin/MDL2XML $<


demo: all cruiseControlECSL.moml
	$(PTII)/bin/vergil cruiseControlECSL.moml

# Electronic Throttle Control (etc) demo
demo_etc: all etc_controlECSL.moml
	$(PTII)/bin/vergil etc_controlECSL.moml

etc_controlECSL.moml: etc_controlECSL.xml ecsl2moml
	./ecsl2moml $<

etc_controlECSL.xml: ../XML2ECSL/bin/etc_control.xml
	../XML2ECSL/bin/Xml2Ecsl_d $< $@


TARGETPATH = $(ME)/cg
cg/cruiseControlECSL:
	$(PTII)/bin/copernicus -codeGenerator shallow \
		-outputDirectory $(PTII)/$(TARGETPATH) \
		-targetPath $(TARGETPATH) \
		cruisecontrolECSL.moml

run_cg:
	$(JAVA) \
	-classpath "$(PTII)/$(TARGETPATH)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
        "-Dptolemy.ptII.dir=c:/cxh/ptII" \
        "ptolemy.actor.gui.CompositeActorApplication" \
        "-class" \
        "cruiseControlECSL.CGcruiseControlECSL"

# The place where treeshake scripts are.
TREESHAKE=$(ROOT)/util/testsuite/treeshake

cruiseControl.jar:
	"$(TREESHAKE)" "$(JAR)" cruiseControl.jar \
		-main ptolemy.actor.gui.CompositeActorApplication \
		"$(JAVA)" \
			-classpath "$(PTII)/$(TARGETPATH)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \


			ptolemy.actor.gui.PtExecuteApplication \
		        "-class" \
		        "cruiseControlECSL.CGcruiseControlECSL"


cruiseControl.jar:
	"$(TREESHAKE)" "$(JAR)" cruiseControl.jar \
		-main ptolemy.actor.gui.MoMLSimpleApplication \
		"$(JAVA)" \
			-classpath "$(PTII)/$(TARGETPATH)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
			ptolemy.actor.gui.MoMLSimpleApplication \
			cruiseControlECSL.moml


run_treeshake: cruiseControl.jar
	$(JAVA) \
			-classpath "$(PTII)/$(TARGETPATH)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
			ptolemy.actor.gui.PtExecuteApplication \
		-class cruiseControlECSL.CGcruiseControlECSL

# Rules to run gcj, the GNU Java -> Native compiler                            
# This code is very experimental, and not likely to work                       
GCJ_DIR =       /usr/local

# The GNU C/Java compiler                                                      
GCJ =           gcj
GCJ_LIBDIR =    $(GCJ_DIR)/lib

GCJ_JAR = /usr/local/share/java/libgcj-3.3.2.jar

# GCJ Options, see http://gcc.gnu.org/onlinedocs/                              
# If there is no -g or -O option, then the default is -g1                      
#GCJ_FLAGS =    -static                                                        
#GCJ_FLAGS =    -pg -g0 -O3 -fno-bounds-check --classpath=$(GCJ_JAR)           
GCJ_FLAGS =     -g -O3 -fno-bounds-check --classpath=$(GCJ_JAR)
GCJ_FLAGS =     -g

gcj:
	"$(GCJ)" -v $(GCJ_FLAGS) \
		--main=ptolemy.actor.gui.MoMLSimpleApplication \
		-o cruiseControl_gcj -L$(PTII)/ptolemy/moml -LMoML cruiseControl.jar

run:
	$(JAVA) -verbose \
		 -classpath $(PTII) ptolemy.actor.gui.MoMLSimpleApplication \
		cruiseControlECSL.moml
# Ptolemy II dirs
dirs:
	jar -tvf cruiseControl.jar | awk '{ print $$NF}' | awk -F / '{printf("%s", $$1); for(i=2;i<NF;i++){printf("/%s", $$i)} printf("\n")}' | sort | uniq

JDIRS = \
	com/microstar/xml \
	ptolemy/actor \
	ptolemy/actor/gui \
	ptolemy/actor/gui/style \
	ptolemy/actor/lib \
	ptolemy/actor/sched \
	ptolemy/actor/util \
	ptolemy/data \
	ptolemy/data/expr \
	ptolemy/data/type \
	ptolemy/data/unit \
	ptolemy/domains/ct/kernel \
	ptolemy/domains/ct/kernel/solver \
	ptolemy/domains/ct/kernel/util \
	ptolemy/domains/ct/lib \
	ptolemy/graph \
	ptolemy/graph/analysis \
	ptolemy/graph/analysis/analyzer \
	ptolemy/graph/analysis/strategy \
	ptolemy/kernel \
	ptolemy/kernel/attributes \
	ptolemy/kernel/undo \
	ptolemy/kernel/util \
	ptolemy/math \
	ptolemy/moml \
	ptolemy/moml/filter \
	ptolemy/util \
	vendors/ecsl_dp/ptolemy

gcj_dirs:
		set $(JDIRS); \
		for x do \
		    if [ -w $(PTII)/$$x ] ; then \
			( cd $(PTII)/$$x ; \
			echo running gcj in $(PTII)/$$x ; \
			$(GCJ) -c -I$(PTII) $(GCJ_FLAGS) *.java ;\
			) \
		    fi ; \
		done ; \


# Find the names of missing files
missing:
	grep "undefined reference" vendors/ecsl_dp/ptolemy/gcj2.out | grep ::class | awk '{print substr($$5, 2, length($$5)-2)}' | sed -e 's@::class\$$@.class@' -e 's@::@/@g' | sort | uniq 

update_jar:
	cp c.jar cruiseControl.jar
	$(JAR) -uf cruiseControl.jar \
		org/w3c/dom/Document.class \
		ptolemy/actor/gui/PtolemyQuery.class \
		ptolemy/gui/Top.class \
		ptolemy/data/expr/XMLParser.class
	(cd $(PTII); $(JAR) -uf vendors/ecsl_dp/ptolemy/cruiseControl.jar `cat u3`)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk








