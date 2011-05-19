# Makefile for applications for the Dallas Semiconductor Tini board
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
# See $PTII/doc/tini.htm for more documentation
# See $PTII/ptolemy/tini/demo/ramp/makefile for an example makefile that
# includes this makefile.
#
#
# Standard Ptolemy II external makefile variables that this file uses:
#
# CLASSPATHSEPARATOR   Either : or ; for Unix or Windows.  Usually set in
#		$PTII/mk/ptII.mk by configure
# JAVA		The location of the Java Interpreter.  Set in ptII.mk
# JAVAC		The location of the Java Compiler.  Set in ptII.mk
# TINI_CLASSES	CLASSPATH specification to find the TINI.  Set in ptII.mk
# TINI_DIR	Directory where the TINI is located.  Set in ptII.mk
# ME		Directory where the makefile that includes tini.mk is located.
# ROOT		Location of the PTII directory to the makefile that
#		includes tini.mk.

# TINI specific makefiles variables that need to be set before
# including tini.mk:
#
# SOURCE_SYSTEM_CLASS  Classpath to the source system we are generating
#		code for, for example ptolemy.tini.demo.ramp.RampSystem
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

# Run the demo via the usual method without any codegen.
demo_interpreted: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS)

codegen: generate_sdf_code compile_codegen build_prc run_codegen tini

# FIXME: JAVASRC_SKELETON_DIR needs to go away.
# It is the location of the java sources and the .skel files
# If you don't have these, copy them from /users/ptII/vendors/sun/src/
# See $PTII/ptolemy/java/lang/makefile
JAVASRC_SKELETON_DIR=$(PTII)/vendors/sun/src

# Read in SOURCE_SYSTEM_CLASS and generate .java files in $PTII/$(OUTPKG)
generate_sdf_code: $(JCLASS) $(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java
$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java:
	@echo "###################################"
	@echo "# Generating code for $(SOURCE_SYSTEM_CLASS) in $PTII/$(OUTPKG)"
	@echo "###################################"
	@if [ ! -d "$(JAVASRC_SKELETON_DIR)" ]; then \
		echo "Warning $(JAVASRC_SKELETON_DIR) does not exist"; \
		echo "Copy the zip file from /users/ptII/vendors/sun/src/"; \
		echo "See $PTII/ptolemy/java/lang/makefile for details"; \
	fi
	CLASSPATH="$(ROOT)$(CLASSPATHSEPARATOR)$(JAVASRC_SKELETON_DIR)" \
	$(JAVA) $(JAVA_VERBOSE) ptolemy.domains.sdf.codegen.SDFCodeGenerator \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS) \
		-outdir $(ROOT) -outpkg $(OUTPKG)

# Compile the codegen tini code in $(PTII)/$(OUTPKG)
# Note that we compile without debug as the default
compile_codegen: $(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class
$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class: \
			$(ROOT)/$(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).java
	@echo "###################################"
	@echo "# Compiling codegen tini *.java files in $(PTII)/$(OUTPKG)"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	CLASSPATH="$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(TINI_CLASSES)" \
	$(JAVAC) -g:none -O $(JAVAC_VERBOSE) \
		$(OUTPKG_MAIN_CLASS).java)

# Compile the non-codegen tini code in $(PTII)/$(OUTPKG)
# Note that we compile without debug as the default
compile_tini:
	@echo "###################################"
	@echo "# Compiling non-codegen tini *.java files in $(PTII)/$(OUTPKG)"
	@echo "###################################"
	(cd $(ROOT)/$(OUTPKG_DIR); \
	CLASSPATH="$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(TINI_CLASSES)" \
	$(JAVAC) -bootclasspath $(TINI_CLASSES) -g:none -O $(JAVAC_VERBOSE) \
		$(OUTPKG_MAIN_CLASS).java)



# Create a Tini binary from the class files in
# $(PTII)/$(OUTPKG)
build_prc:
	@echo "###################################"
	@echo "# Creating Tini executable from classes in"
	@echo "# $(PTII)/$(OUTPKG_DIR)"
	@echo "###################################"
	(cd $(ROOT); \
	CLASSPATH=".$(CLASSPATHSEPARATOR)$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(TINI_CLASSES)" \
		$(JAVA) TINIConvertor \
		-f $(OUTPKG_DIR)/$(OUTPKG_MAIN_CLASS).class \
		-o $(OUTPKG_MAIN_CLASS).tini \
		-d $(TINI_DIR)/bin/tini.db \
		)

run_codegen:
	(cd $(ROOT)/$(OUTPKG_DIR); \
	CLASSPATH="$(OUTPKG_ROOT)$(CLASSPATHSEPARATOR)$(TINI_CLASSES)" \
		$(JAVA) tini.applet.Applet $(OUTPKG).$(OUTPKG_MAIN_CLASS))

clean_codegen: clean
	rm -rf $(ROOT)/$(OUTPKG_DIR)
