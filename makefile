# Ptolemy II makefile
#
# @Version: $Id$
#
# Copyright (c) 1995-2003 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the above
# copyright notice and the following two paragraphs appear in all copies
# of this software.
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
#						PT_COPYRIGHT_VERSION_2
#						COPYRIGHTENDKEY
##########################################################################

# Current directory relative to $PTII
ME =		.

# Order matters here.
# Go into util first so we get the latest version of the testsuite
# Go into com before compiling ptolemy so we get JLex
# PTJNI_DIR is set to jni by configure in $PTII/mk/ptII.mk
#   if gcc or cc was found.
# PTMESCAL_DIR is set to mescal by configure in $PTII/mk/ptII.mk
#   if $PTII/mescal was found
PTTHALES_DIR = thales

DIRS = util com diva ptolemy $(PTJNI_DIR) $(PTMESCAL_DIR) $(PTTHALES_DIR) \
	bin doc

# Root of Ptolemy II directory
ROOT =		.

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptII
# If you change the version number, be sure to edit doc/*,
# ptolemy/configs/*, ptolemy/configs/doc and
# ptolemy/kernel/attributes/VersionAttribute.java
PTVERSION =	3.1-devel
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =


# Jar files that contain demos and docs
PTDEMODOCJARS = \
		doc/docConfig.jar \
		ptolemy/actor/lib/javasound/demo/demo.jar \
		ptolemy/data/type/demo/demo.jar \
		ptolemy/domains/ct/demo/demo.jar \
		ptolemy/domains/de/demo/demo.jar \
		ptolemy/domains/dt/demo/demo.jar \
		ptolemy/domains/giotto/demo/demo.jar \
		ptolemy/domains/fsm/demo/demo.jar \
		ptolemy/domains/pn/demo/demo.jar \
		ptolemy/domains/sdf/demo/demo.jar \
		ptolemy/domains/tm/demo/demo.jar \
		ptolemy/moml/demo/demo.jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		doc/codeDoc.jar \
		lib/diva.jar \
		$(PTAUXALLJARS) \
		ptolemy/domains/experimentalDomains.jar \
		$(PTDEMODOCJARS) \
		ptolemy/ptolemy.jar \
		ptolemy/vergil/vergil.jar

PTCLASSALLJARS = \
		$(PTDEMODOCJARS) \
		ptolemy/ptolemy.jar \
		ptolemy/vergil/vergil.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS = \
	.classpath.in \
	.eclipse.epf \
	README.txt \
	copyright.txt \
	configure.in \
	configure \
	vergil.jnlp.in \
	vergilDSP.jnlp.in \
	vergilHyVisual.jnlp.in \
	vergilPtiny.jnlp.in \
	vergilPtinySandbox.jnlp.in

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	bin \
	config \
	lib \
	mk

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	.classpath \
	adm \
	config.log \
	config.status \
	config.cache \
	confTest.class \
	logs \
	public_html \
	tcl \
	vendors

# Files to be removed by 'make clean'
KRUFT = \
	vergil.jnlp \
	vergilDSP.jnlp \
	vergilHyVisual.jnlp \
	vergilPtiny.jnlp \
	vergilPtinySandbox.jnlp \
	ptKeystore

# Files to be removed by 'make distclean'
DISTCLEAN_STUFF = \
	mk/ptII.mk config.log config.status config.cache

# Make copyright.txt readonly so that when we open up the text editor
# we open up a readonly texteditor
all: mk/ptII.mk suball
	chmod a-w copyright.txt

install: subinstall $(PTCLASSALLJAR)

# Glimpse is a tool that prepares an index of a directory tree.
# glimpse is not included with Ptolemy II, see http://glimpse.cs.arizona.edu
GLIMPSEINDEX =	/usr/local/bin/glimpseindex
glimpse: .glimpse_exclude
	@echo "Saving .glimpse_exclude, removing the .glimpse* files"
	rm -f glimpse_exclude
	cp .glimpse_exclude glimpse_exclude
	rm -f .glimpse*
	cp  glimpse_exclude .glimpse_exclude
	$(GLIMPSEINDEX) -n -H `pwd` `pwd`
	chmod a+r .glimpse_*
	rm -f glimpse_exclude

# Generate ptII.mk by running configure
mk/ptII.mk: configure mk/ptII.mk.in
	./configure

configure: configure.in
	@echo "configure.in is newer than configure, so we run"
	@echo "autoconf to update the configure file"
	@echo "This may occur if you do a cvs update, and the mod time"
	@echo "of configure.in is newer than that of configure"
	@echo "even though the configure script in the repository"
	@echo "was modified after configure.in was modified."
	@echo "Note that if you don't have GNU autoconf installed,"
	@echo "you can try running 'touch configure' to work around"
	@echo "this problem."
	autoconf

# Arguments for cvs2cl.pl, which is used to generate a ChangeLog
# from the CVS logs.  
# -W 3600 means unify entries that are within 3600 seconds or 1 hr.
CVS2CL_ARGS = -W 3600

# Generate a ChangeLog file from the CVS logs
# This rurequires that the CVS directory be present and takes
# quite awhile to update
ChangeLog:
	@if [ -d CVS ]; then \
		echo "Running ./util/testsuite/cvs2cl.pl"; \
		echo " This could take several minutes"; \
		./util/testsuite/cvs2cl.pl -W 3600; \
	else \
		echo "CVS directory not present, so we can't update $@"; \
	fi

update:
	-cvs update -P -d 
	$(MAKE) -k clean fast

# Include rules to build Web Start JNLP files
include $(ROOT)/mk/jnlp.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
