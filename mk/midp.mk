# Makefile for applications for the Palm Pilot using the MIDP
#
# @Authors: Christopher Hylands
#
# @Version: : $Id$
#
# @Copyright (c) 2000-2002 The Regents of the University of California.
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
# OUTPKG	Output package name for generated code, which also
# 		determines the directory relative to PTII where the
#		code will appear.  If OUTPKG is set to cg.ramp, then
#		the code will appear in $PTII/cg/ramp.
# OUTPKG_DIR	Location of the OUTPKG directory.
#		If OUTPKG is cg.ramp, then OUTPKG_DIR would be cg/ramp
# OUTPKG_ROOT   The relative path from OUTPKG to $PTII.
#		If OUTPKG is cg.ramp, then OUTPKG_ROOT would be ../..
# OUTPKG_MAIN_CLASS The class that contains the main() method
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

# Run the demo via the usual method without any codegen.
demo_interpreted: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS)

codegen: generate_sdf_code compile_codegen preverify build_prc run_codegen kvm

# Read in SOURCE_SYSTEM_CLASS and generate .java files in $PTII/$(OUTPKG)
generate_sdf_code: $(JCLASS) $(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java
$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java:
	@echo "###################################"
	@echo "# Generating code for $(SOURCE_SYSTEM_CLASS) in $PTII/$(OUTPKG)"
	@echo "###################################"
	CLASSPATH="$(ROOT)" \
	$(JAVA) $(JAVA_VERBOSE) ptolemy.domains.sdf.codegen.SDFCodeGenerator \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS) \
		-outdir $(ROOT) -outpkg $(OUTPKG)

# Compile the codegen kvm code in $(PTII)/$(OUTPKG)
# Note that we compile without debug as the default
compile_codegen: $(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class
$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class: \
			$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java
	@echo "###################################"
	@echo "# Compiling codegen kvm *.java files in $(PTII)/$(OUTPKG)"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	$(JAVAC) -g:none -O $(JAVAC_VERBOSE) \
		-bootclasspath $(MIDP_CLASSES)  \
		-classpath $(OUTPKG_ROOT) \
		$(OUTPKG_MAIN_CLASS).java)

# Compile the non-codegen kvm code in $(PTII)/$(OUTPKG)
# Note that we compile without debug as the default
compile_midp:
	@echo "###################################"
	@echo "# Compiling non-codegen kvm *.java files in $(PTII)/$(OUTPKG)"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	"$(JAVAC)" -g:none -O $(JAVAC_VERBOSE) \
		-bootclasspath $(MIDP_CLASSES)  \
		-classpath $(OUTPKG_ROOT) \
		$(OUTPKG_MAIN_CLASS).java)


# Run the kvm preverify tool in $(PTII)/$(OUTPKG)
# and generate .class files in $(PTII)/$(OUTPKG)/output/$(OUTPKG_DIR)
preverify: output/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class
output/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class: jclass
	@echo "###################################"
	@echo "# preverifying in $(PTII)/$(OUTPKG_DIR), creating new .class files"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	for class in *.class; do \
		echo $$class ; \
		$(MIDP_DIR)/bin/preverify \
			-classpath \
			"$(MIDP_CLASSES)$(CLASSPATHSEPARATOR)$(OUTPKG_ROOT)" \
			$(OUTPKG).`basename $$class .class`; \
	done)


# Create a Palm binary from the class files in
# $(PTII)/$(OUTPKG)/output/$(OUTPKG_DIR)
#   Note that to build a Palm binary, you should first run the preverifier
#   and then use the .class files from the output directory that are
#   created by the preverifier.  If you use the class files that were
#   created by javac directly, then you may get verifier errors.

$(OUTPKG_MAIN_CLASS).jad:
	echo "MIDlet-Name: $(OUTPKG_MAIN_CLASS)" > $@
	echo "MIDlet-Version: 1.0" >> $@
	echo "MIDlet-Vendor: `whoami`" >> $@
	echo "MIDlet-Description: Test midlet" >> $@
	echo "MicroEdition-Profile: MIDP-1.0" >> $@
	echo "MicroEdition-Configuration: CLDC-1.0" >> $@
	echo "MIDlet-1: $(OUTPKG).$(OUTPKG_MAIN_CLASS), $(OUTPKG_MAIN_CLASS).png, $(OUTPKG).$(OUTPKG_MAIN_CLASS)" >> $@
	echo "MIDlet-Jar-URL: $(OUTPKG_MAIN_CLASS).jar" >> $@

$(OUTPKG_MAIN_CLASS).jar: $(OUTPKG_MAIN_CLASS).jad \
		output/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class
	(cd output; "$(JAR)" -cfm ../$(OUTPKG_MAIN_CLASS).jar ../$(OUTPKG_MAIN_CLASS).jad .)

build_prc: $(OUTPKG_MAIN_CLASS).prc
$(OUTPKG_MAIN_CLASS).prc: $(OUTPKG_MAIN_CLASS).jar
	@echo "###################################"
	@echo "# Creating Palm executable from classes in"
	@echo "# $(OUTPKG_MAIN_CLASS).jar"
	@echo "###################################"
	"$(JAVA)" -cp $(CONVERTER_CLASSES) \
		com.sun.midp.palm.database.MakeMIDPApp \
		-verbose \
		-creator PTOL \
		$(OUTPKG_MAIN_CLASS).jar


# Run the java profiler (javap) on all the classes
javap:
	(cd $(ROOT)/$(OUTPKG_DIR); \
	javap -classpath $PTII `ls -1 *.class | awk '{s=substr($0,1,length($0)-6); print "$(OUTPKG)."s}' `)

# Remove all "import ptolemy.*" lines
fix:
	(cd $(ROOT)/$(OUTPKG_DIR); \
	for files in *.java; do \
		sed 's@\(import ptolemy.*;\)@//\1@' $$files > tmp; \
		diff $$files  tmp; \
		cp tmp $$files; \
	done)

run_codegen:
	(cd $(ROOT)/$(OUTPKG_DIR); \
		$(JAVA) -classpath $(OUTPKG_ROOT) \
			$(OUTPKG).$(OUTPKG_MAIN_CLASS))

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
		(cd $(ROOT)/$(OUTPKG_DIR); \
			$(MIDP_BINARY) -classpath \
			"$(MIDP_CLASSES)$(CLASSPATHSEPARATOR)output" \
			$(OUTPKG).$(OUTPKG_MAIN_CLASS)); \
	fi

clean_codegen: clean
	rm -rf $(ROOT)/$(OUTPKG_DIR)
