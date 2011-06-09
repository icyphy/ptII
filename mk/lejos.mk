# Makefile for applications for the Lego Mindstorms LEJOS VM.
#
# @Authors: Steve Neuendorffer
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
# See $PTII/ptolemy/vendors/lejos/lejosBeta3/examples/hworld/makefile
# for an example makefile that includes this makefile.
#
#
# Standard Ptolemy II external makefile variables that this file uses:
#
# CLASSPATHSEPARATOR   Either : or ; for Unix or Windows.  Usually set in
#		$PTII/mk/ptII.mk by configure
# JAVA		The location of the Java Interpreter.  Set in ptII.mk
# JAVAC		The location of the Java Compiler.  Set in ptII.mk
# LEJOS_DIR	Directory where the WABA is located.  Set in ptII.mk
# ME		Directory where the makefile that includes waba.mk is located.
# ROOT		Location of the PTII directory to the makefile that
#		includes waba.mk.

# LEJOS specific makefiles variables that need to be set before
# including lejos.mk:

# Uncomment these to turn on verbosity, or run
# make JAVAC_VERBOSE= -verbose JAVA_VERBOSE= -verbose:class MAKEPALMAPP_VERBOSE=" -v -v" codegen
#JAVAC_VERBOSE =	-verbose
#JAVA_VERBOSE =	-verbose:class
#MAKEPALMAPP_VERBOSE = -v -v

FIRMDL=$(LEJOS_DIR)/bin/lejosfirmdl
HOSTLEJOS=$(LEJOS_DIR)/bin/emu-lejos
RCXLEJOS=$(LEJOS_DIR)/bin/lejos
UPLOADER=$(LEJOS_DIR)/bin/lejosrun

# NOTE: Lejos demos don't use the standard JDK...  we override bootclasspath.
CLASSPATH = $(LEJOS_DIR)/lib/classes.jar$(CLASSPATHSEPARATOR).$(MYCLASSPATH)
JFLAGS = -bootclasspath "$(BOOTCLASSPATH)"

STRIPCLASS = $(JCLASS:%.class=%,)

# Run the demo on the rcx
demo_rcx: $(JCLASS)
	CLASSPATH="$(CLASSPATH)" \
		$(RCXLEJOS) $(STRIPCLASS)

# Run the demo in emulation mode.
demo_emulate: $(JSRCS)
	CLASSPATH="$(CLASSPATH)" \
		$(HOSTLEJOS) $(STRIPCLASS)

# upload the lejos firmware to the rcx.
firmware:
	$(LEJOSFIRMDL)
