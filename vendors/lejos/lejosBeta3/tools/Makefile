# Makefile for the lejos package.
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2001 The Regents of the University of California.
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

ME =		vendors/lejos/lejosBeta3/tools

# Order matters here,
DIRS = 		

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = 

PTCLASSALLJAR =

JSRCS = 

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass programs
install: jclass jars


depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
include $(ROOT)/mk/lejos.mk


####################################
# DO WE NEED THIS?
CCOPT=-I../vmsrc -O2 -Wall -Wno-unused
#FIRMDL_OBJ=tvm_firmdl3.o util.o

LEJOSRCXDEFS=-DWRITE_ORDER=\"BE\" -DLOADER_TOOL=\"lejosrun\" 

LEJOSHOSTDEFS=-DWRITE_ORDER=\"LE\" -DLOADER_TOOL=\"emu-lejosrun\"

PROGRAMS=${UPLOADER} ${RCXLEJOS} ${HOSTLEJOS}

programs: ${PROGRAMS}

%.o: %.c
	${CC} ${CCOPT} -c $*.c

# NOTE: THIS RULE ISN'T USED.
#${FIRMDL}: ${FIRMDL_OBJ}
#	${CC} ${FIRMDL_OBJ} -o ${FIRMDL} ${LIBS}
${FIRMDL}:
	cd firmdl; make

${UPLOADER}: tvm.o
	${CC} tvm.o -o ${UPLOADER}

${RCXLEJOS}: javaexec.c util.o
	${CC} ${LEJOSRCXDEFS} -c javaexec.c
	${CC} javaexec.o util.o -o ${RCXLEJOS}

${HOSTLEJOS}: javaexec.c util.o
	${CC} ${LEJOSHOSTDEFS} -c javaexec.c
	${CC} javaexec.o util.o -o ${HOSTLEJOS}



