# Ptolemy code generation for JHDL SimpleAdd demo
#
# @Authors: (makefile only) Christopher Hylands- based on a file by Thomas M. Parks
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

ME =		ptolemy/copernicus/jhdl/demo/SimpleAdd

DIRS =

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(JHDL_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	SimpleAdd
PTDIST =	$(PTPACKAGE)$(PTVERSION)
#PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	SimpleAdd.java \
	SimpleAddSystem.java \
	SimpleSub.java \
	SimpleMult.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all $(PTCLASSJAR)

TARGETPACKAGE = ptolemy.copernicus.jhdl.demo.SimpleAdd.cg
TARGETPATH = ptolemy/copernicus/jhdl/demo/SimpleAdd/cg
SOURCEPACKAGE = ptolemy.copernicus.jhdl.demo.SimpleAdd
MODEL = SimpleAddSystem
TARGETCLASS = $(TARGETPACKAGE).$(MODEL)
SOURCECLASS = $(SOURCEPACKAGE).$(MODEL)

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS = 

#		-d "$(ROOT)" \

demo: jclass
	@echo "First we generate SimpleAdd.class"
	$(JAVA) -Xmx128M -Xms128M \
		 -classpath "$(CLASSPATH)" \
		ptolemy.copernicus.jhdl.Main \
		$(SOURCECLASS) \
		-p wjtp.at deep,targetPackage:$(TARGETPACKAGE) \
		-p wjtp.mt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.clt targetPackage:$(TARGETPACKAGE)$(ITERATIONS_PARAMETER) \
		-p wjtp.fot targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffpt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.idt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.iat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ipt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.itt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ttn disabled,targetPackage:$(TARGETPACKAGE) \
		-p wjtp.snapshot1 outDir:$(ROOT)/$(TARGETPATH)/jimple1 \
		-p wjtp.snapshot2 outDir:$(ROOT)/$(TARGETPATH)/jimple2 \
		-p wjtp.snapshot3 outDir:$(ROOT)/$(TARGETPATH)/jimple3 \
		-p wjtp.snapshot4 outDir:$(ROOT)/$(TARGETPATH)/jimple4 \
		-p wjtp.iee enabled \
		-p wjtp.circuit targetPackage:$(TARGETPACKAGE) \
		$(SOOT_USER_ARGS)
	@echo "decompile the simpleAdd actor"
	"$(PTII)/bin/jode" $(TARGETPACKAGE).simpleAdd
#	java -classpath $(ROOT) $(TARGETCLASS)

interpret: jclass
	@echo "Run as a Ptolemy system"
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTII)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(TARGETCLASS) \
		-iterations 1

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



