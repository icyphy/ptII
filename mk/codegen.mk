# Makefile for codegen applications
#
# @Authors: Christopher Hylands
#
# @Version: : $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of California.
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

# This makefile contains the rules that run and build codegen applicaions
# usually, a codegen system creates a makefile that defines 
# the makefile variables below and then includes this makefile
# that defines the rules that actually build and run the system

# Standard Ptolemy II external makefile variables that this file uses:
# ROOT		Location of the PTII directory, for example $(PTII)
# CLASSPATHSEPARATOR   Either : or ; for Unix or Windows.  Usually set in
#		$PTII/mk/ptII.mk by configure
# JAVA		The location of the Java Interpreter.  Set in ptII.mk
# JAVAC		The location of the Java Compiler.  Set in ptII.mk

# Codegen  specific makefiles variables that need to be set before
# including this makefile:

# CG_ROOT	The root of the directory tree that contains
#	        the .java files that are generated.  Usually set to $(ROOT).
# SOURCE_SYSTEM_CLASS  The classname of the system to generate code for
#		This class extends TypedCompositeActor.
# ITERATIONS	Number of iterations, for example 10.
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
#

CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

all: interpret generate compile run

# Run the source system without using code generation.
interpret: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTII)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS)

# Generate code for the system.
generate:
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTII)" \
		"$(JAVA)" ptolemy.domains.sdf.codegen.SDFCodeGenerator \
		-class $(SOURCE_SYSTEM_CLASS) \
		-iterations $(ITERATIONS) \
		-shallowLoading \
		-outdir $(CG_ROOT) \
		-outpkg $(OUTPKG)

# Compile the code generation system.
compile:
	(cd $(OUTPKG_DIR); \
		CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTII)$(CLASSPATHSEPARATOR)$(OUTPKG_ROOT)" \
		"$(JAVAC)" $(JFLAGS) $(OUTPKG_MAIN_CLASS).java)

# Run the code generation system.
run:
	(cd $(OUTPKG_DIR); \
		CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTII)$(CLASSPATHSEPARATOR)$(OUTPKG_ROOT)" \
		"$(JAVA)" $(OUTPKG).$(OUTPKG_MAIN_CLASS))

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
