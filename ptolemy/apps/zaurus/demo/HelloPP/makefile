# Makefile for Java Ptolemy Zaurus HelloPP demonstration
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

ME =		ptolemy/apps/zaurus/demo/hellopp

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS =

# Used to build jar files
#PTPACKAGE = 	hello
#PTDIST =	$(PTPACKAGE)$(PTVERSION)
#PTCLASSJAR = 	$(PTPACKAGE).jar


# Keep this list alphabetized.
JSRCS = \
	HelloPP.java


EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \

JCLASS = $(JSRCS:%.java=%.class)

BINNAME =	hellozaurus-evm_1.0_arm.ipk

JFLAGS =	-target 1.1
all: jclass $(BINNAME)
install: jclass subinstall $(PTCLASSJAR)
	cp $(BINNAME).prc $(PALM_ADD_ON_DIR)

$(BINNAME): HelloPP.class
	rm -f *.ipk
	rm -f  *.gz
	mkdir -p home/QtPalmtop/java
	cp HelloPP*.class home/QtPalmtop/java
	gtar -cv --exclude=CVS -f control.tar ./control
	gtar -cv --exclude=CVS -f data.tar ./home
	gzip control.tar
	gzip data.tar
	gtar -cv --exclude=CVS -f hellozaurus.tar ./control.tar.gz ./data.tar.gz
	gzip hellozaurus.tar
	mv hellozaurus.tar.gz $@

# make clean removes $(KRUFT)
KRUFT = control.tar data.tar control.tar.gz data.tar.gz $(BINNAME) \
	home/QtPalmtop/java/*.class

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
