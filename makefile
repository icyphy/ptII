# Ptolemy II makefile
#
# @Version: $Id$
#
# Copyright (c) 1995-2001 The Regents of the University of California.
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
DIRS = util com ptolemy bin doc

# Root of Ptolemy II directory
ROOT =		.

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptII
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =


# Jar files that contain demos.  Thi
PTDEMOJARS = \
		ptolemy/actor/lib/javasound/demo/demo.jar \
		ptolemy/data/type/demo/demo.jar \
		ptolemy/domains/ct/demo/demo.jar \
		ptolemy/domains/de/demo/demo.jar \
		ptolemy/domains/dt/demo/demo.jar \
		ptolemy/domains/giotto/demo/demo.jar \
		ptolemy/domains/fsm/demo/demo.jar \
		ptolemy/domains/pn/demo/demo.jar \
		ptolemy/domains/rtos/demo/demo.jar \
		ptolemy/domains/sdf/demo/demo.jar \
		ptolemy/moml/demo/demo.jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		doc/docConfig.jar \
		lib/diva.jar \
		$(PTAUXALLJARS) \
		ptolemy/domains/experimentalDomains.jar \
		$(PTDEMOJARS) \
		ptolemy/ptolemy.jar \
		ptolemy/vergil/vergil.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Makefile variables used to set up keys for jar signing.
# To use Web Start, we have to sign the jars.
KEYDNAME = "CN=Claudius Ptolemaus, OU=Ptolemy Project, O=UC Berkeley, L=Berkeley, S=California, C=US"
KEYSTORE = ptKeystore
KEYALIAS = claudius
# The password should not be stored in a makefile, for production
# purposes, run:
#  make STOREPASSWORD= KEYPASSWORD= jnlp_sign
STOREPASSWORD = -storepass this.is.not.secure,it.is.for.testing.only
KEYPASSWORD = -keypass this.is.not.secure,it.is.for.testing.only
KEYTOOL = $(PTJAVA_DIR)/bin/keytool
$(KEYSTORE): 
	$(KEYTOOL) -genkey \
		-dname $(KEYDNAME) \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD)
	$(KEYTOOL) -selfcert \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD)
	$(KEYTOOL) -list \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD)

# vergil.jnlp is for Web Ramp.  For jar signing to work with Web Ramp,
# the .jnlp file itself must be included in the signed jar file
# and not be changed (See Section 5.4 of the JNLP specification).
jnlp_sign: vergil.jnlp $(PTCLASSALLJAR) $(KEYSTORE)
	@echo "Updating JNLP-INF/APPLICATION.JNLP with vergil.jnlp"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp vergil.jnlp JNLP-INF/APPLICATION.JNLP
	$(JAR) -uf $(PTCLASSALLJAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	@echo "Signing the jar file"
	$(PTJAVA_DIR)/bin/jarsigner \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(PTCLASSALLJAR) $(KEYALIAS)

EXTRA_SRCS = \
	README.txt \
	copyright.txt \
	configure.in \
	configure \
	vergil.jnlp.in

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	bin \
	config \
	lib \
	mk \
	tutorial

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
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
	vergil.jnlp

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
GLIMPSEINDEX =	/usr/sww/bin/glimpseindex
glimpse: .glimpse_exclude
	@echo "Saving .glimpse_exclude, removing the .glimpse* files"
	rm -f glimpse_exclude
	cp .glimpse_exclude glimpse_exclude
	rm -f .glimpse*
	cp  glimpse_exclude .glimpse_exclude
	$(GLIMPSEINDEX) -H `pwd` `pwd`
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

# Java Network Launch Protocol aka Web Start
vergil.jnlp: vergil.jnlp.in
	@echo "Don't forget that if you change vergil.jnlp.in, you need"
	@echo " to run 'make jnlp_sign' which will update vergil.jnlp"
	@echo " in ptII.jar."
	sed 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' $< > $@

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
