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
	ptinvoke \
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

# DOS batch files
BAT_FILES = \
	histogram.bat \
	moml.bat \
	ptexecute.bat \
	ptplot.bat \
	ptolemy.bat \
	pxgraph.bat \
	vergil.bat

EXTRA_SRCS = \
	policy.all \
	ptinvoke.in \
	sandbox.policy \
	startjini \
	mkjnlp \
	$(BAT_FILES)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(SHELL_FILES) \
	ptmatlab.dll

all: ptinvoke
	-chmod a+x ptinvoke
	rm -f copernicus
	ln -s ptinvoke copernicus
	rm -f copernicusjhdl
	ln -s ptinvoke copernicusjhdl
	rm -f decompile
	ln -s ptinvoke decompile
	rm -f emachine
	ln -s ptinvoke emachine
	rm -f giottoc
	ln -s ptinvoke giottoc
	rm -f histogram
	ln -s ptinvoke histogram
	rm -f htmlconverter
	ln -s ptinvoke htmlconverter
	rm -f jode
	ln -s ptinvoke jode
	rm -f mescal
	ln -s ptinvoke mescal
	rm -f moml
	ln -s ptinvoke moml
	rm -f obfuscate
	ln -s ptinvoke obfuscate
	rm -f ptexecute
	ln -s ptinvoke ptexecute
	rm -f ptjacl
	ln -s ptinvoke ptjacl
	rm -f ptjaclapp
	ln -s ptinvoke ptjaclapp
	rm -f ptstartjini
	ln -s ptinvoke ptstartjini
	rm -f ptolemy
	ln -s ptinvoke ptolemy
	rm -f ptplot
	ln -s ptinvoke ptplot
	rm -f pxgraph
	ln -s ptinvoke pxgraph
	rm -f soot
	ln -s ptinvoke soot
	rm -f tinicomm
	ln -s ptinvoke tinicomm
	rm -f tinic
	ln -s ptinvoke tinic
	rm -f tiniconverter
	ln -s ptinvoke tiniconverter
	rm -f vergil
	ln -s ptinvoke vergil


install: all
	-chmod a+x $(SHELL_FILES) $(BAT_FILES)

fast: install
demo: all

ptinvoke: ptinvoke.in ../config.status
	(cd ..; ./config.status)
# zip might not get the permissions of configure right, so we run /bin/sh
../config.status: ../configure
	(cd ..; /bin/sh ./configure)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
