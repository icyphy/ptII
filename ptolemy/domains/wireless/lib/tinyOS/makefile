# Makefile for Ptolemy II TinyOS using gcc
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		ptolemy/domains/wireless/lib/tinyOS

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = tinyOS	
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
        VirtualTinyOS.java

OTHER_FILES_TO_BE_JARED = \


EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# Under Windows, this would be $(PTPACKAGE).dll
# Under Solaris, this would be lib$(PTPACKAGE).so
SHARED_LIBRARY = $(PTJNI_SHAREDLIBRARY_PREFIX)TOS.$(PTJNI_SHAREDLIBRARY_SUFFIX)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(SHARED_LIBRARY)
	@echo "To run all the demos, run 'make demo'"

install: all jars

#libs:
#	$(MAKE) SHAREDBASE=Blink shared

#VirtualTinyOS.class: VirtualTinyOS.java
#	javac -classpath $(ROOT) VirtualTinyOS.java

#cann't do it autumatiocally in the makefile since we need to manually
#modify the .h file before we could compile the other .c files.
#VirtualTinyOS.h: VirtualTinyOS.class
#	javah -classpath $(ROOT) -o VirtualTinyOS.h ptolemy.domains.wireless.lib.tinyOS.VirtualTinyOS

# PTCC is set in $PTII/mk/ptII.mk
# See also ptolemy/matlab/makefile
		
$(SHARED_LIBRARY): VirtualTinyOS.h VirtualTinyOS.c 
	$(PTCC) -mno-cygwin -Ic:/j2sdk1.4.2/include -Ic:/j2sdk1.4.2/include/win32 -Wl,--add-stdcall-alias -shared $(PTJNI_SHAREDLIBRARY_CFLAG) \
		-o $@ \
		Blink.c Timer.c Ledc.c BlinkMain.c VirtualTinyOS.c

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
