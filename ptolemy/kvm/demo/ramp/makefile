# Makefile for Ptolemy II Simple Ramp demo
#
# @Authors: Christopher Hylands
#
# @Version: : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp $
#
# @Copyright (c) 2000 The Regents of the University of California.
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

ME =		ptolemy/kvm/demo/ramp

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ramp
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	RampSystem.java

EXTRA_SRCS = \
	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
#OPTIONAL_FILES = 
#	

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)
	@echo "To run all demo, run 'make demo'"
install: all

demo: $(PTCLASSJAR)
	CLASSPATH=$(CLASSPATH) \
	$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
	 -class ptolemy.kvm.demo.ramp.RampSystem -iterations 10

depend:
	@echo "no dependencies in this directory"

# FIXME: we can't ship these
JAVASRC_STUBS=$(PTII)/vendors/sun/src

codegen: generate_codegen compile_codegen preverify ramp.prc run_codegen 
generate_codegen:
	@if [ ! -d "$(JAVASRC_STUBS)" ]; then \
		echo "Warning $(JAVASRC_STUBS) does not exist"; \
	fi
	CLASSPATH=$(CLASSPATH)$(CLASSPATHSEPARATOR)$(JAVASRC_STUBS) \
	$(JAVA) -verbose:class  ptolemy.domains.sdf.codegen.SDFCodeGenerator \
	-class  ptolemy.kvm.demo.ramp.RampSystem \
	-iterations 500 \
	-outdir $(ROOT) -outpkg cg.ramp
run_codegen: 	
	(cd $(ROOT)/cg/ramp; \
		$(JAVA) -classpath ../.. cg.ramp.CG_Main)

J2ME_CLDC = $(PTII)/vendors/sun/j2me_cldc
J2ME_CLDC = /j2me_cldc



compile_codegen: $(ROOT)/cg/ramp/CG_Main.class
$(ROOT)/cg/ramp/CG_Main.class: $(ROOT)/cg/ramp/CG_Main.java
	(cd $(ROOT)/cg/ramp; \
	javac -g:none -verbose \
		-bootclasspath $(J2ME_CLDC)/bin/api/classes  \
		-classpath $(ROOT) \
		CG_Main.java)

preverify:
	(cd $(ROOT)/cg/ramp; \
	for class in *.class; do \
	$(J2ME_CLDC)/bin/preverify \
		-classpath "$(J2ME_CLDC)/bin/api/classes$(CLASSPATHSEPARATOR)$(ROOT)" \
		$(ME)/`basename $$class .class`; \
	done)


ramp.prc: $(J2ME_CLDC)/tools/palm/src/palm/database/MakePalmApp.class
	(cd $(ROOT)/cg/ramp; \
	java -classpath $(J2ME_CLDC)/tools/palm/src \
	 palm.database.MakePalmApp \
	-v -v \
	-version "1.0" \
	-bootclasspath $(J2ME_CLDC)/bin/api/classes  \
	-classpath $(PTII) \
	cg.ramp.CG_Main)

$(J2ME_CLDC)/tools/palm/src/palm/database/MakePalmApp.class: \
		$(J2ME_CLDC)/tools/palm/src/palm/database/MakePalmApp.java
	(cd $(J2ME_CLDC)/tools/palm/src/palm/database; javac *.java)

run:
	(cd $(ROOT)/cg/ramp; \
	java -verbose:class \
		-Xint \
		-Xbootclasspath/p:$(J2ME_CLDC)/bin/api/classes \
		-classpath ../.. \
		cg.ramp.CG_Main)

javap_all:
	(cd $(ROOT)/cg/ramp; \
	javap -classpath $PTII `ls -1 *.class | awk '{s=substr($0,1,length($0)-6); print "cg.ramp."s}' `)

fix:
	(cd $(ROOT)/cg/ramp; \
	for files in *.java; do \
		sed 's@\(import ptolemy.*;\)@//\1@' $$files > tmp; \
		diff $$files  tmp; \
		cp tmp $$files; \
	done)

kvm:
	$(J2ME_CLDC)/kvm/VmWin/build/kvm.exe -classpath "$(J2ME_CLDC)/api/classes\;./output" cg.ramp.CG_Main

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
