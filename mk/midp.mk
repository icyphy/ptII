# Makefile for applications for the Palm Pilot using the MIDP
#
# @Authors: Christopher Hylands
#
# @Version: : $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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

# Please don't use GNU make extensions in this file, such as 'ifdef' or '%'.
# If you really must use an GNU make extension, please label it.
#
# Usually, this makefile is included right at the bottom of a makefile,
# just before ptcommon.mk is included.
#
# See $PTII/doc/kvm.htm for more documentation
# See $PTII/ptolemy/kvm/demo/ramp/makefile for an example makefile that
# includes this makefile.
#
#
# Standard Ptolemy II external makefile variables that this file uses:
#
# CLASSPATHSEPARATOR   Either : or ; for Unix or Windows.  Usually set in
#		$PTII/mk/ptII.mk by configure
# JAVA		The location of the Java Interpreter.  Set in ptII.mk
# JAVAC		The location of the Java Compiler.  Set in ptII.mk
# MIDP_CLASSES	CLASSPATH specification to find the MIDP.  Set in ptII.mk
# MIDP_DIR	Directory where the MIDP is located.  Set in ptII.mk
# ME		Directory where the makefile that includes midp.mk is located.
# ROOT		Location of the PTII directory to the makefile that
#		includes midp.mk.

# MIDP specific makefiles variables that need to be set before
# including kvm.mk:
#
# SOURCE_SYSTEM_CLASS  Classpath to the source system we are generating
#		code for, for example ptolemy.kvm.demo.ramp.RampSystem
# ITERATIONS	Number of iterations, for example 50
# TARGETPACKAGE	Output package name for generated code, which also
# 		determines the directory relative to PTII where the
#		code will appear.  If TARGETPACKAGE is set to cg.ramp, then
#		the code will appear in $PTII/cg/ramp.
# TARGETPACKAGE_DIR	Location of the TARGETPACKAGE directory.
#		If TARGETPACKAGE is cg.ramp, then TARGETPACKAGE_DIR would be cg/ramp
# TARGETPACKAGE_ROOT   The relative path from TARGETPACKAGE to $PTII.
#		If TARGETPACKAGE is cg.ramp, then TARGETPACKAGE_ROOT would be ../..
# TARGETPACKAGE_MAIN_CLASS The class that contains the main() method
#		For example CG_Main

# Uncomment these to turn on verbosity, or run
# make JAVAC_VERBOSE= -verbose JAVA_VERBOSE= -verbose:class MAKEPALMAPP_VERBOSE=" -v -v" codegen
#JAVAC_VERBOSE =	-verbose
#JAVA_VERBOSE =	-verbose:class
#MAKEPALMAPP_VERBOSE = -v -v

MIDP_DIR = 	$(ROOT)/vendors/sun/WTK104
MIDP_CLASSES = 	$(MIDP_DIR)/lib/midpapi.zip

CONVERTER_DIR =		$(ROOT)/vendors/sun/midp4palm1.0/Converter
CONVERTER_CLASSES =	$(CONVERTER_DIR)/Converter.jar 

KRUFT = $(TARGETPACKAGE_MAIN_CLASS).prc \
	$(TARGETPACKAGE_MAIN_CLASS).jar \
	$(TARGETPACKAGE_MAIN_CLASS).jad \
	STDERR.txt \
	STDOUT.txt
# Run the demo via the usual method without any codegen.
demo_interpreted: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS)

codegen: generate_sdf_code compile_codegen preverify build_prc run_codegen kvm

# Read in SOURCE_SYSTEM_CLASS and generate .java files in $PTII/$(TARGETPACKAGE)
generate_sdf_code: $(JCLASS) $(ROOT)/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).java
$(ROOT)/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).java:
	@echo "###################################"
	@echo "# Generating code for $(SOURCE_SYSTEM_CLASS) in $PTII/$(TARGETPACKAGE)"
	@echo "###################################"
	CLASSPATH="$(ROOT)" \
	$(JAVA) $(JAVA_VERBOSE) ptolemy.domains.sdf.codegen.SDFCodeGenerator \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS) \
		-outdir $(ROOT) -outpkg $(TARGETPACKAGE)

# Compile the codegen kvm code in $(PTII)/$(TARGETPACKAGE)
# Note that we compile without debug as the default
compile_codegen: $(ROOT)/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class
$(ROOT)/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class: \
			$(ROOT)/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).java
	@echo "###################################"
	@echo "# Compiling codegen kvm *.java files in $(PTII)/$(TARGETPACKAGE)"
	@echo "###################################"
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
	$(JAVAC) -g:none -O $(JAVAC_VERBOSE) \
		-bootclasspath $(MIDP_CLASSES)  \
		-classpath $(TARGETPACKAGE_ROOT) \
		$(TARGETPACKAGE_MAIN_CLASS).java)

# Compile the non-codegen kvm code in $(PTII)/$(TARGETPACKAGE)
# Note that we compile without debug as the default
compile_midp:
	@echo "###################################"
	@echo "# Compiling non-codegen kvm *.java files in $(PTII)/$(TARGETPACKAGE)"
	@echo "###################################"
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
	"$(JAVAC)" -g:none -O $(JAVAC_VERBOSE) \
		-bootclasspath $(MIDP_CLASSES)  \
		-classpath $(TARGETPACKAGE_ROOT) \
		$(TARGETPACKAGE_MAIN_CLASS).java)


# Run the kvm preverify tool in $(PTII)/$(TARGETPACKAGE)
# and generate .class files in $(PTII)/$(TARGETPACKAGE)/output/$(TARGETPACKAGE_DIR)
preverify: output/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class
output/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class: $(JCLASS)
	@echo "###################################"
	@echo "# preverifying in $(PTII)/$(TARGETPACKAGE_DIR), creating new .class files"
	@echo "###################################"
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
	for class in *.class; do \
		echo "`pwd`"; \
		echo $$class ; \
		"../$(MIDP_DIR)/bin/preverify" \
			-classpath \
			"../$(MIDP_CLASSES)$(CLASSPATHSEPARATOR)$(TARGETPACKAGE_ROOT)" \
			$(TARGETPACKAGE).`basename $$class .class`; \
	done)


# Run the kvm preverify tool in $(PTII)/$(TARGETPACKAGE)
# and generate .class files in $(PTII)/$(TARGETPACKAGE)/output/$(TARGETPACKAGE_DIR)
preverifyModel: $(MODEL)/output/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class
$(MODEL)/output/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class: $(JCLASS)
	@echo "###################################"
	@echo "# preverifying in $(PTII)/$(TARGETPACKAGE_DIR), creating new .class files"
	@echo "###################################"
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
	for class in *.class; do \
		echo $$class ; \
		../$(MIDP_DIR)/bin/preverify \
			-classpath \
			"../$(MIDP_CLASSES)$(CLASSPATHSEPARATOR)../$(TARGETPACKAGE_ROOT)" \
			$(TARGETPACKAGE).`basename $$class .class`; \
	done)


emulatorRun:
	@echo ""
	@echo "If you have missing classes, then run"
	@echo "make preverifyPTII PREVERIFY_CLASS=classname"
	@echo "For example:"
	@echo "make preverifyPTII PREVERIFY_CLASS=ptolemy.data.unit.UnitUtilities"
	@echo "and then rerun make $@"
	@echo ""
	@echo "When you have all the classes, run"
	@echo "make demo3"
	$(MIDP_DIR)/bin/emulator.exe \
		-classpath "$(MODEL)/output$(CLASSPATHSEPARATOR)$(PTII)" \
		-Xverbose:class \
		$(TARGETPACKAGE).Main


# Run preverify on a .class file in the PTII tree and copy it
# to $(MODEL)/output.
# Usually, this step is run by hand to preverify classes that the emulator
# needs from $PTII to $(MODEL)/output
# To run this rule, do:
#   make preverifyPTII PREVERIFY_CLASS=ptolemy.data.unit.UnitUtilities"
# We include apps/midp so that we can override classes.
preverifyPTII:
	$(MIDP_DIR)/bin/preverify \
		-classpath \
		"$(MIDP_CLASSES)$(CLASSPATHSEPARATOR)$(ROOT)/ptolemy/apps/midp$(CLASSPATHSEPARATOR)$(PTII)" \
		-d $(MODEL)/output $(PREVERIFY_CLASS)



TREESHAKE_PREFIX = $(ROOT)/$(TARGETPATH)/treeshake
TREESHAKE=$(ROOT)/util/testsuite/treeshake
# Location of class files that shadow and replace class files in $PTII
PTII_MIDP =  		$(ROOT)/ptolemy/apps/midp
treeShakeDemo: $(MODEL)/treeshake.jar
$(MODEL)/treeshake.jar:
	@echo "Create the minimal jar file and run it" 
	@echo "We include .class files from $PTII_MIDP"
	"$(TREESHAKE)" "$(JAR)" $(TREESHAKE_PREFIX).jar \
		"$(JAVA)" -Xfuture -classpath "$(PTII_MIDP)$(CLASSPATHSEPARATOR)$(CLASSPATH)$(CLASSPATHSEPARATOR)$(MIDP_CLASSES)" \
		$(TARGETPACKAGE).Main 
	ls -l $(TREESHAKE_PREFIX).jar

runTreeShake: $(MODEL)/treeshake.jar
	java -jar $(MODEL)/treeshake.jar

# Preverify a jar file
RELATIVE_PREVERIFY_JAR = ../$(MODEL)/treeshake.jar
preverifyTreeShake: $(MODEL)/treeshake.jar
	@echo "Run preverify on the treeshaken jar file and generate"
	@echo "a new set of .class files in the ./output/ directory"
	rm -rf ptjar_tmpdir output
	mkdir ptjar_tmpdir
	(cd ptjar_tmpdir; jar -xf $(RELATIVE_PREVERIFY_JAR); \
		classes=`find . -name "*.class" -print | sed -e 's@^./@@g' -e 's/.class//g' -e 's@/@.@g'`; \
		for class in $$classes; do \
			echo "$$class" ; \
			../$(MIDP_DIR)/bin/preverify \
				-classpath \
				"../$(MIDP_CLASSES)$(CLASSPATHSEPARATOR).$(CLASSPATHSEPARATOR)../$(TARGETPACKAGE_ROOT)" \
				-d ../output \
				$$classes; \
		done; \
	)
	rm -rf ptjar_tmpdir


# Create a Palm binary from the class files in
# $(PTII)/$(TARGETPACKAGE)/output/$(TARGETPACKAGE_DIR)
#   Note that to build a Palm binary, you should first run the preverifier
#   and then use the .class files from the output directory that are
#   created by the preverifier.  If you use the class files that were
#   created by javac directly, then you may get verifier errors.

$(TARGETPACKAGE_MAIN_CLASS).jad:
	echo "MIDlet-Name: $(TARGETPACKAGE_MAIN_CLASS)" > $@
	echo "MIDlet-Version: 1.0" >> $@
	echo "MIDlet-Vendor: `whoami`" >> $@
	echo "MIDlet-Description: Test midlet" >> $@
	echo "MicroEdition-Profile: MIDP-1.0" >> $@
	echo "MicroEdition-Configuration: CLDC-1.0" >> $@
	echo "MIDlet-1: $(TARGETPACKAGE).$(TARGETPACKAGE_MAIN_CLASS), $(TARGETPACKAGE_MAIN_CLASS).png, $(TARGETPACKAGE).$(TARGETPACKAGE_MAIN_CLASS)" >> $@
	echo "MIDlet-Jar-URL: $(TARGETPACKAGE_MAIN_CLASS).jar" >> $@

$(TARGETPACKAGE_MAIN_CLASS).jar: $(TARGETPACKAGE_MAIN_CLASS).jad \
		output/$(TARGETPACKAGE_DIR)/$(TARGETPACKAGE_MAIN_CLASS).class
	(cd output; "$(JAR)" -cfm ../$(TARGETPACKAGE_MAIN_CLASS).jar ../$(TARGETPACKAGE_MAIN_CLASS).jad .)

build_prc: $(TARGETPACKAGE_MAIN_CLASS).prc
$(TARGETPACKAGE_MAIN_CLASS).prc: $(TARGETPACKAGE_MAIN_CLASS).jar
	@echo "###################################"
	@echo "# Creating Palm executable from classes in"
	@echo "# $(TARGETPACKAGE_MAIN_CLASS).jar"
	@echo "###################################"
	"$(JAVA)" -cp $(CONVERTER_CLASSES) \
		com.sun.midp.palm.database.MakeMIDPApp \
		-verbose \
		-creator PTOL \
		$(TARGETPACKAGE_MAIN_CLASS).jar


# Run the java profiler (javap) on all the classes
javap:
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
	javap -classpath $PTII `ls -1 *.class | awk '{s=substr($0,1,length($0)-6); print "$(TARGETPACKAGE)."s}' `)

# Remove all "import ptolemy.*" lines
fix:
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
	for files in *.java; do \
		sed 's@\(import ptolemy.*;\)@//\1@' $$files > tmp; \
		diff $$files  tmp; \
		cp tmp $$files; \
	done)

run_codegen:
	(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
		$(JAVA) -classpath $(TARGETPACKAGE_ROOT) \
			$(TARGETPACKAGE).$(TARGETPACKAGE_MAIN_CLASS))

# FIXME: what about Solaris?
MIDP_BINARY = $(MIDP_DIR)/kvm/VmWin/build/kvm.exe
# Run a Java simulator of the Palm
# kvm.exe needs to be built by hand, see the kvm instructions.
kvm:
	if [ ! -f "$(MIDP_BINARY)" ]; then \
		echo "$(MIDP_BINARY) is not found."; \
		echo "This binary simulates the Palm/MIDP environment"; \
		echo "It is not required but it is useful for debugging"; \
		echo "To build it, see the kvm instructions."; \
	else \
		(cd $(ROOT)/$(TARGETPACKAGE_DIR); \
			$(MIDP_BINARY) -classpath \
			"$(MIDP_CLASSES)$(CLASSPATHSEPARATOR)output" \
			$(TARGETPACKAGE).$(TARGETPACKAGE_MAIN_CLASS)); \
	fi

clean_codegen: clean
	rm -rf $(ROOT)/$(TARGETPACKAGE_DIR)
