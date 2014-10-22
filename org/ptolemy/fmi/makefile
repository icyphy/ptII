# Makefile for Java FMU Interface
#
# @Authors: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2012-2014 The Regents of the University of California.
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

ME =		org/ptolemy/fmi

DIRS = 		driver type 

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	fmi
PTVERSION =	1.0.2
PTDIST =	jfmi$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		driver/driver.jar \
		type/type.jar

PTCLASSALLJAR = 	fmi.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	FMI20ModelExchangeCapabilities.java \
	FMI20ModelInstance.java \
	FMI20CoSimulationCapabilities.java \
	FMICallbackFunctions.java \
	FMICapabilities.java \
	FMICoSimulationCapabilities.java \
	FMIEventInfo.java \
	FMI20EventInfo.java \
	FMILibrary.java \
	FMIModelDescription.java \
	FMIScalarVariable.java \
	FMUBuilder.java \
	FMUFile.java \
	FMULibrary.java \
	FMULog.java \
	FMULogUtilities.java \
	NativeSizeT.java

OTHER_FILES_TO_BE_JARED = build.xml \
	ChangeLog

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Non-source files such as README files and subdirectories
MISC_FILES = driver fmu type

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	manifest.tmp \
	$(PTCLASSALLJAR) \
	$(PTAUXJAR) \
	'FMICallbackFunctions$$ByValue.class' \
	'FMI20EventInfo$$ByReference.class' \
	'FMI20EventInfo$$ByValue.class' \
	'FMIEventInfo$$ByReference.class' \
	'FMIEventInfo$$ByValue.class' \
	'FMILibrary$$FMICallbackAllocateMemory.class' \
	'FMILibrary$$FMICallbackFreeMemory.class' \
	'FMILibrary$$FMICallbackLogger.class' \
	'FMILibrary$$FMIStatus.class' \
	'FMILibrary$$FMIStatusKind.class' \
	'FMILibrary$$FMIStepFinished.class' \
	'FMI20ModelInstance$$ByReference.class' \
	'FMI20ModelInstance$$ByValue.class' \
	'FMIScalarVariable$$Alias.class' \
	'FMIScalarVariable$$Causality.class' \
	'FMIScalarVariable$$Variability.class' \
	'FMUBuilder$$_StreamReaderThread.class' \
	'FMULibrary$$FMUAllocateMemory.class' \
	'FMULibrary$$FMUFreeMemory.class' \
	'FMULibrary$$FMULogger.class' \
	'FMULibrary$$FMUStepFinished.class'

KRUFT =

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass

install: all jars 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk


FIND=/usr/bin/find
PTTMPME= $(PTTMPDIR)/$(PTDIST)/$(ME)
# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "$(PTDIST) - A Java Wrapper for the Functional Mock-up Interface" > \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "A Functional Mock-up Unit (FMU) is a file that contains functionality"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "that may be invoked either via co-simulation or model exchange." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Typically, a FMU file has a .fmu extension. A FMU file is a zip file" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "that contains a file named modelDescription.xml and one or more" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "platform-dependent shared libraries." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "JFMI is a BSD-licensed interface between Java" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "and FMI." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "See org/ptolemy/fmi/driver/FMUCoSimulation.java and" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "org/ptolemy/fmi/driver/FMUModelExchange.java" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "for simple examples that read in .fmu files and invoke methods in" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "the the platform-dependent shared libraries." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Sample .fmu files may be found in fmu/cs and fmu/me." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "for the following architectures: darwin64, linux32, linux64, win32 and win64." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "These files were generated using a port of the FMUSDK which may be found at" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "http://github.com/cxbrooks/fmusdk." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "FMI documentation may be found at http://www.modelisar.com/fmi.html"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Build instructions:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "To build, download and install Apache ant from https://ant.apache.org/" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "cd to org/ptolemy/fmi and run ant: " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "  cd org/ptolemy/fmi" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "  ant" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "To run an example under Mac OS or Linux, try:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "  cd org/ptolemy/fmi" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "  java -classpath ../../../lib/jna.jar:../../.. \\"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "     org.ptolemy.fmi.driver.FMUCoSimulation \\"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "     fmu/cs/bouncingBall.fmu 1.0 0.1 true c results.csv " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Then look at the contents of the results.csv file" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Sponsors:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "JFMI was developed under a Department of Energy Contract" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "for the Building Controls Virtual Test Bed (BCVTB) see http://simulationresearch.lbl.gov/bcvtb" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "This work was supported in part by the TerraSwarm Research Center," >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "one of six centers supported by the STARnet phase of the " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Focus Center Research Program (FCRP) a Semiconductor Research Corporation program " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "sponsored by MARCO and DARPA." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Additional support was provided by Ptolemy II, which is supported " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "by the following organizations:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "The Center for Hybrid and Embedded Software Systems (CHESS)" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "at UC Berkeley, which receives support from the" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "National Science Foundation (NSF awards #0720882 (CSR-EHS: PRET)," >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "#0931843 (CPS: Large: ActionWebs), and #1035672 (CPS: Medium: Ptides))," >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "the Naval Research Laboratory (NRL #N0013-12-1-G015)," >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "and the following companies: Bosch, National Instruments, and Toyota." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Previous versions of JFMI were supported by " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "the U. S. Army Research Laboratory (ARL #W911NF-11-2-0038),"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "the Air Force Research Lab (AFRL), the Multiscale Systems Center (MuSyC),"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "one of six research centers funded under the Focus Center Research Program,"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "a Semiconductor Research Corporation program, and Thales."  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo ""  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Authors: "  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "David Broman (UC Berkeley)"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Christopher Brooks (UC Berkeley)"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Edward A. Lee (UC Berkeley)"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Thierry Stephane Nouidui (Lawrence Berkeley National Laboratory)"   >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Stavros Tripakis (UC Berkeley)"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Michael Wetter (Lawrence Berkeley National Laboratory)"  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo ""  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Support: "  >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "This package is delivered without support.  However, the BCVTB mailing list" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "at https://groups.google.com/group/bcvtb maybe be used for questions." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	# Update copyright
	sed  -e 's/1995-/2012-/' -e 's/Ptolemy II system/JFMI distribution/' \
		$(PTTMPDIR)/$(PTDIST)/copyright.txt | \
		grep -v "Ptolemy II includes the work of others, to see those copyrights" | \
		grep -v "the copyright link on the splash page or see copyright.htm." > \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "JFMI includes the work of others:" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "* The Java Native Access (JNA) package, which is released under LGPL, see" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "lib/jna-license.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "* FMUSDK, which is released with a BSD license, see" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "org/ptolemy/fmi/driver/fmusdk-license.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "* JUnit, which is released with a Common Public License, see" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "lib/junit-license.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	echo "At runtime, only JNA is required.  FMUSDK and JUnit are used for testing only." >> \
		$(PTTMPDIR)/$(PTDIST)/license.txt
	rm -f $(PTTMPDIR)/$(PTDIST)/copyright.txt $(PTTMPDIR)/$(PTDIST)/._copyright.txt
	#
	@echo "Copy over jna stuff"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/lib
	cp "$(PTII)/lib/jna.jar" "$(PTII)/lib/jna-license.htm" \
		$(PTTMPDIR)/$(PTDIST)/lib
	cp "$(PTII)/lib/junit-4.8.2.jar" "$(PTII)/lib/junit-license.htm" \
		$(PTTMPDIR)/$(PTDIST)/lib
	@echo "Remove files we don't want to ship"
	rm -rf $(PTTMPDIR)/$(PTDIST)/autom4te.cache \
		$(PTTMPDIR)/$(PTDIST)/config \
		$(PTTMPDIR)/$(PTDIST)/configure* \
		$(PTTMPDIR)/$(PTDIST)/mk \
		$(PTTMPDIR)/$(PTDIST)/.classpath.in
	find $(PTTMPDIR)/$(PTDIST) -name "makefile" -exec rm {} \; 
	find $(PTTMPDIR)/$(PTDIST) -name "*.class" -exec rm {} \; 
	find $(PTTMPDIR)/$(PTDIST) -name "#*" -exec rm {} \; 
	find $(PTTMPDIR)/$(PTDIST) -name ".#*" -exec rm {} \; 
	find $(PTTMPDIR)/$(PTDIST) -name "*~" -exec rm {} \; 
	#
	# Setting up doc/codeDoc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/codeDoc
	cd $(PTTMPDIR)/$(PTDIST); \
	CLASSPATH=".$(CLASSPATHSEPARATOR)$(PTII)/lib/jna.jar" "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc \
		org.ptolemy.fmi \
		org.ptolemy.fmi.driver \
		org.ptolemy.fmi.type
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPME)/doc/download
	mkdir -p $(PTTMPME)/doc/download
	echo "<html>" > $(PTTMPME)/doc/download/index.htm
	echo "<head>" >> $(PTTMPME)/doc/download/index.htm
	echo "<title>Download JFMI</title>" >> $(PTTMPME)/doc/download/index.htm
	echo '<link href="../default.css" rel="stylesheet" type="text/css">' >> $(PTTMPME)/doc/download/index.htm
	echo '<body bgcolor="#ffffff">'  >> $(PTTMPME)/doc/download/index.htm
	echo "<H1>Download JFMI</H1>" >> $(PTTMPME)/doc/download/index.htm
	echo "The JFMI distribution itself does not include all the available download formats." >> $(PTTMPME)/doc/download/index.htm
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/java/jfmi" target="_top"><code>http://ptolemy.eecs.berkeley.edu/jfmi/ptplot</code></a>' >> $(PTTMPME)/doc/download/index.htm
	echo "</body>" >> $(PTTMPME)/doc/download/index.htm
	echo "</html>" >> $(PTTMPME)/doc/download/index.htm
	chmod -R a+r $(PTTMPDIR)/$(PTDIST)
	$(FIND) $(PTTMPDIR)/$(PTDIST) -name "*.class" -exec rm {} \;

# Update the ftp site
# Update a location with the files necessary to download
DIST_BASE = /export/home/pt0/ptweb/java
DIST_DIR = $(DIST_BASE)/$(PTDIST)


update_ftp:
	ssh moog rm -rf $(DIST_DIR)
	cat $(PTDIST).tar.gz | ssh moog "cd $(DIST_BASE); gtar -zxf -"
	ssh moog "cd $(DIST_BASE)/$(PTDIST)_tmp/; tar -cf - . | (cd $(DIST_DIR)/org/ptolemy/fmi/doc/download; tar -xpf -)"
	#scp doc/download/index.htm moog:$(DIST_DIR)/org/ptolemy/fmi/doc/download
	scp $(PTDIST).tar.gz moog:$(DIST_DIR)/org/ptolemy/fmi
	scp $(PTDIST).zip moog:$(DIST_DIR)/org/ptolemy/fmi


# Experimental support for compiling with gcj.
GCC_DIR=/usr/local/gcc-4.8.0
GCJ=$(GCC_DIR)/bin/gcj
# To build jna using gcj, download the sources and edit WString.java and NativeString.java and comment out the CharSequence in the implements line and comment out the subSequence() method
jna.o: jna.jar
	$(GCJ) -g -fjni -c jna.jar -o jna.o
gcj: jna.o
	$(GCJ) -g -C *.java */*.java --classpath=$(PTII):jna.jar
	$(GCJ) -g -fjni *.class */*.class --classpath=$(PTII):jna.jar jna.o --main=org.ptolemy.fmi.driver.FMUCoSimulation  -lefence
	export LD_LIBRARY_PATH=${GCC_DIR}/lib64
	./a.out fmu/cs/values.fmu 1.0 0.1 true c foo.csv
