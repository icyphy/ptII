# Makefile for scripts that invoke Ptolemy II Java classes
#
# @Author: Christopher Hylands 
#
# @Version: $Id$
#
# @Copyright (c) 1998-2014 The Regents of the University of California.
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

# Be careful about setting the CLASSPATH here, since if CLASSPATH=$(ROOT),
# then when we run configure we might miss the location of JMF
#CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Bourne Shell scripts for Unix and Cygwin
# These files are generated automatically as links to 
# ptinvoke
# Note that this does not contain ptinvoke, makeapp and makebat,
# which are generated independently by this makefile.
SHELL_FILES = \
	.momllib-util \
	.nc2moml-util \
	.ncapp2moml-util \
	convertToLazy \
	copernicus \
	decompile \
	emachine \
	giottoc \
	histogram \
	homer \
	jode \
	mescal \
	moml \
	obfuscate \
	ptcg \
	ptcodegen \
	ptexecute \
	ptjacl \
	ptjaclapp \
	ptplot \
	ptstartjini \
	ptolemy \
	ptweb \
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

EXTRA_EXECUTABLE_SRCS = \
	hyvisual \
	startjini \
	mkjnlp \
	mkl4j \
	mkosgi \
	ptjunit \
	updateFmu \
	viptos

EXTRA_SRCS = \
	$(EXTRA_EXECUTABLE_SRCS) \
	policy.all \
	ptcygpath \
	ptinvoke.in \
	sandbox.policy \
	comm.policy.in \


# Mac OS X applications, created by running ./makeapp
# We don't include Ptiny.app because we end up copying the jre into the *.app directory, which is very large.
MAC_APPS = \
	Vergil.app


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = \
	IncrementalFMU_wrap.dll \
	libfmipp.dll

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(SHELL_FILES) \
	$(BAT_FILES) \
	$(MAC_APPS) \
	comm.policy \
	macContents \
	makeapp \
	makebat \
	ptinvoke \
	ptmatlab.dll


# Files to be removed by make clean
KRUFT = $(SHELL_FILES) $(BAT_FILES) $(MAC_APPS) makeapp makebat 

all: $(SHELL_FILES) $(BAT_FILES) $(MAC_APPS)
	chmod a+x $(EXTRA_EXECUTABLE_SRCS) 

# We use a dependency here so that each time we run make we
# do not recreate the files unless it is necessary.
$(SHELL_FILES): ptinvoke
	for file in $(SHELL_FILES); do \
		echo "$$file"; \
		rm -f "$$file"; \
		ln -s ptinvoke "$$file"; \
		chmod a+x "$$file"; \
	done

makeapp: ptinvoke
	rm -f makeapp
	ln -s ptinvoke makeapp
	chmod a+x ptinvoke makeapp

macapps: $(MAC_APPS)

# We touch the .app directory so that running make again does not rerun makeapp
Ptiny.app: makeapp
	./makeapp $@ PTINY -ptiny
	touch $@

Vergil.app: makeapp
	./makeapp $@ FULL -full
	touch $@

makebat: ptinvoke
	rm -f makebat
	ln -s ptinvoke makebat

$(BAT_FILES): makebat
	rm -f "$@"
	./makebat -q "`basename $@ .bat`"


ptinvoke: ptinvoke.in ../config.status
	(cd ..; ./config.status)
	-chmod a+x ptinvoke


# zip might not get the permissions of configure right, so we run /bin/sh.
# Pass --exec-prefix and --prefix to configure so as to avoid:
# Avoid "configure: error: expected an absolute directory name for --exec_prefix: NONE"
# The problem here is that in /bin/sh, the pwd command returns with
# a trailing "\r".  The workaround is to run configure with bash.
../config.status: ../configure
	-(cd ..; \
		/bin/bash ./configure \
			--exec-prefix="${PTII}" --prefix="${PTII}" \
	        || \
		/bin/sh ./configure \
			--exec-prefix="${PTII}" --prefix="${PTII}")

install: all
fast: all
demo: all

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
