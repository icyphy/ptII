# Makefile for scripts that invoke Ptolemy II Java classes
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

ME =		bin

DIRS =

# Root of the Java directory
ROOT =		..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Bourne Shell scripts for Unix and Cygwin
# These files are generated automatically as links to 
# ptinvoke
# Note that this does not contain ptinvoke and makebat,
# which are generated independently by this makefile.
SHELL_FILES = \
	copernicus \
	copernicusjhdl \
	decompile \
	emachine \
	giottoc \
	histogram \
	htmlconverter \
	jode \
	mescal \
	moml \
	obfuscate \
	ptexecute \
	ptjacl \
	ptjaclapp \
	ptplot \
	ptstartjini \
	ptolemy \
	pxgraph \
	soot \
	tinicomm \
	tinic \
	tiniconverter \
	startjini.gui \
	vergil

# DOS batch files that might or might not be automatically generated
# by the makebat script.
BAT_FILES = $(foreach file, $(SHELL_FILES), $(file).bat)

EXTRA_SRCS = \
	policy.all \
	ptinvoke.in \
	sandbox.policy \
	startjini \
	mkjnlp

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(SHELL_FILES) \
	$(BAT_FILES) \
	ptinvoke \
	makebat \
	ptmatlab.dll

all: ptinvoke ptscripts ptbats

ptscripts:
	for file in $(SHELL_FILES); do \
		echo "$$file"; \
		rm -f "$$file"; \
		ln -s ptinvoke "$$file"; \
		chmod a+x "$$file"; \
	done

ptbats:
	rm -f makebat
	ln -s ptinvoke makebat
	for file in $(SHELL_FILES); do \
		rm -f "$$file.bat"; \
		makebat "$$file"; \
	done


install: all

fast: install
demo: all

ptinvoke: ptinvoke.in ../config.status
	(cd ..; ./config.status)
	-chmod a+x ptinvoke
# zip might not get the permissions of configure right, so we run /bin/sh
../config.status: ../configure
	(cd ..; /bin/sh ./configure)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
