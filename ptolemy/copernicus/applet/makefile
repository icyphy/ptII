# Makefile for Ptolemy II applet generation
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2014 The Regents of the University of California.
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

ME =		ptolemy/copernicus/applet

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	applet
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	AppletWriter.java \
	JarSigner.java \
	JNLPApplication.java \
	Main.java

OTHER_FILES_TO_BE_JARED = \
	default.css \
	makefile.in \
	model.htm.in \
	model.jnlp.in \
	modelJnlp.htm.in \
	modelJnlp-ssi.htm.in \
	modelVergil.htm.in \
	modelVergil-ssi.htm.in \
	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	appletDemos \
	scpDemos \
	updateDemos

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	cg \
	doc \
	'JarSigner$$SignatureFile$$Block.class' \
	'JarSigner$$SignatureFile.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all $(PTPACKAGE).jar JNLPApplication.jar
	@echo "To run a demo that uses self contained jars: 'make test1'"

demo: compileDemo #runDemo

MENUAPPLICATION_DIR = $(ROOT)/ptolemy/actor/gui/jnlp/
MENUAPPLICATION_CLASS = $(MENUAPPLICATION_DIR)/MenuApplication.class
$(MENUAPPLICATION_CLASS): $(MENUAPPLICATION_DIR)/MenuApplication.java
	(cd $(MENUAPPLICATION_DIR); $(MAKE)) 

JNLPApplication.jar: JNLPApplication.class $(MENUAPPLICATION_CLASS)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy Application specific classes from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp JNLPApplication.class \
		$(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	mkdir -p $(PTJAR_TMPDIR)/ptolemy/actor/gui/jnlp
	-cp $(MENUAPPLICATION_CLASS) \
		$(PTJAR_TMPDIR)/ptolemy/actor/gui/jnlp
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

# OrthogonalCom
MODEL =			OrthogonalCom
MODELPATH =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# 
#MODEL =			modulation
#MODELPATH = 		../../../ptolemy/moml/demo/modulation.xml

MODEL =			Butterfly
MODELPATH = 		$(ROOT)/ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#   make MODEL=IIR MODELPATH=../../../ptolemy/actor/lib/test/auto/IIR.xml compileDemo runDemo 
# To run on ArrayAppend:
#   make MODEL=ArrayAppend MODELPATH=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml compileDemo runDemo 

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.applet.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/applet/cg/$(MODEL)

# Package that the decompiler creates classes in
DECOMPILERTARGETPACKAGE =	$(TARGETPACKAGE)
# Directory that the decompiler creates classes in
DECOMPILERTARGETPATH =		$(ROOT)/$(TARGETPATH)

JVM_SIZE = 		-Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT = 	100000

# The tests and the UI use the compileDemo rule, so if you
# change this rule, be sure to run make tests
compileDemo: $(JCLASS)
	@echo "generate an applet for '$(MODELPATH)'"
	$(PTII)/bin/copernicus \
		-verbose \
		-codeGenerator   "applet" \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		-targetPackage   "$(TARGETPACKAGE)" \
		-targetPath      "$(TARGETPATH)" \
		$(MODELPATH)

# change this rule, be sure to run make tests
jnlpDemo: $(JCLASS)
	@echo "generate an applet for '$(MODELPATH)'"
	$(PTII)/bin/copernicus \
		-verbose \
		-codeGenerator   "applet" \
		-run 		 "false" \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		-targetPackage   "$(TARGETPACKAGE)" \
		-targetPath      "$(TARGETPATH)" \
		$(MODELPATH)


jnlpIIRSimple:
	$(MAKE) MODEL=IIR MODELPATH=../../../ptolemy/actor/lib/test/auto/IIR.xml jnlpSimpleDemo
	javaws -wait cg/IIR/IIR.jnlp

# 
jnlpSimpleDemo:
	@echo "generate a jnlp for '$(MODELPATH)'"
	$(PTII)/bin/copernicus \
		-verbose \
		-codeGenerator   "applet" \
		-run 		 "false" \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		$(MODELPATH)


# Used to update demos with jnlp files, see ./updateDemos
jnlpUpdateDemo:
	@echo "generate a jnlp for '$(MODELPATH)'"
	JAVAFLAGS="-Dptolemy.ptII.isRunningNightlyBuild=yes -Dptolemy.ptII.ptKeystore=/home/cxh/ptKeystore.properties" $(PTII)/bin/copernicus \
		-verbose \
		-codeGenerator   "applet" \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		-run 		 "false" \
		-targetPath	 "$(TARGETPATH)" \
		-ptIILocalURL	 "https://ptolemy.berkeley.edu/ptolemyII/ptII11.0/ptII11.0.devel/" \
		$(MODELPATH)
	#		-ptIILocalURL	 "file:///home/cxh/src/ptII11.0.devel/" \

	#javaws -wait $(PTII)/$(TARGETPATH)/$(MODEL).jnlp
	#$(ROOT)/bin/vergil $(PTII)/$(TARGETPATH)/$(MODEL).htm#in_browser



# The default is to run the model before exporting so that we capture the plot. 
# However, the PrintingPress model has no plots.
EXPORT_HTML_RUN=-run

# The directory that contains the html version of the model,
# generated by ptolemy.vergil.basic.export.ExportModel -run htm
#HTML_MODEL =           $(JNLP_MODEL_DIRECTORY)/$(JNLP_MODEL)
HTML_MODEL_JNLP =           $(PTII)/$(TARGEPATH)/$(MODEL)
html_model_jnlp: $(HTML_MODEL_JNLP)
$(HTML_MODEL_JNLP):
	#
	# mk/jnlp.mk: HTML_MODEL rule. Exporting $(JNLP_MODEL).xml to html.
	#
	# Note that it is better to run (cd $PTII; ant test.longest)
	# and avoid attempting to run demos that do not work, see
	# ptolemy/vergil/basic/export/test/junit/ExportModelJUnitTest.java
	#
	#(cd $(PTII)/$(TARGETPATH); $(PTII)/bin/ptinvoke -Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true ptolemy.vergil.basic.export.ExportModel $(EXPORT_HTML_RUN) -force -whiteBackground -openComposites htm $(MODEL).xml; find $(MODEL) -name "*.htm*" -exec chmod a+x {} \;)



# Run the OrthoComm demo
ORTHOCOMMTARGETDIRECTORY = $(TARGETPATH)/ToplevelModel
runOrthoCommDemo:
	@echo "Run the OrthoComm model"
	cd $(ROOT)/$(ORTHOCOMMTARGETDIRECTORY); appletviewer ToplevelModel.htm
	cd $(ROOT)/$(ORTHOCOMMTARGETDIRECTORY); appletviewer ToplevelModelVergil.htm

# Time the OrthoComm demo
TIMEORTHOCOMM_ITERATIONS = 50000
timeOrthoCommDemo:
	@echo "No need to time anything in the applet directory"

APPLETTARGETDIRECTORY = $(TARGETPATH)
runDemo:
	@echo "Run the model"
	cd $(ROOT)/$(APPLETTARGETDIRECTORY); appletviewer $(MODEL).htm
	cd $(ROOT)/$(APPLETTARGETDIRECTORY); appletviewer $(MODEL)Vergil.htm

# Test writing in a separate directory
TESTDIR=/tmp/test
test1: $(JCLASS)
	if [ ! -d "$(TESTDIR)" ]; then \
		mkdir -p $(TESTDIR); \
	fi 
	@echo "generate an applet for '$(MODELPATH)' using jar files"
	$(PTII)/bin/copernicus \
		-verbose \
		-codeGenerator   applet \
		-ptIIUserDirectory   '"$(TESTDIR)"' \
		-targetPackage   '"$(MODEL)"' \
		$(MODELPATH)


# Test using signed jars
TESTSIGNED_DIR=/tmp/testSigned

SIGNED_DIRECTORY=$(PTII)/signed

testSigned: $(JCLASS) $(ROOT)/signed
	if [ ! -d "$(TESTSIGNED_DIR)" ]; then \
		mkdir -p $(TESTSIGNED_DIR); \
	fi 
	if [ ! -d $(SIGNED_DIRECTORY) ]; then \
		@echo "Creating signed jars in $(SIGNED_DIRECTORY)"; \
		(cd $(ROOT); $(MAKE) jnlp_sign); \
	fi
	@echo "generate an applet for '$(MODELPATH)' using jar files"
	$(PTII)/bin/copernicus \
		-verbose \
		-ptIIJarsPath '"$(SIGNED_DIRECTORY)"' \
		-codeGenerator   applet \
		-ptIIUserDirectory   '"$(TESTSIGNED_DIR)"' \
		-targetPackage   '"$(MODEL)"' \
		$(MODELPATH)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

