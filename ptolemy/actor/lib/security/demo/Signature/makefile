# Makefile for Ptolemy II Security Signature demo
#
# @Authors: Christopher Hylands Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2004-2009 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/security/demo/Signature

# Root of the Java directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	Signature
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	ptKeystore \
	Signature.xml

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \
	Signature.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

# 'make fast' will create $(EXTRA_TARGETS)
EXTRA_TARGETS = ptKeystore

# Make clean removes $(KRUFT)
KRUFT = ptKeystore

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass ptKeystore
	@echo "To run all the demos, run 'make demo'"

install: all $(PTCLASSJAR)


# Makefile variables used to set up keys for jar signing.
# To use Web Start, we have to sign the jars.
KEYDNAME = "CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, S=Your State, C=US "
KEYSTORE = ptKeystore
KEYALIAS = claudius
# The password should not be stored in a makefile, for production
# purposes, run something like:
#
# make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD= jnlp_all
#
STOREPASSWORD = -storepass this.is.the.storePassword,change.it
KEYPASSWORD = -keypass this.is.the.keyPassword,change.it

# $(KEYTOOL) is set in configure to the keytool binary.  Note that the gcj keytool
# binary is not likely to work, use the Sun keytool binary instead.

$(KEYSTORE): 
	"$(KEYTOOL)" -genkey \
		-dname $(KEYDNAME) \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD)
	"$(KEYTOOL)" -selfcert \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD)
	"$(KEYTOOL)" -list \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD)

demo: demo_ptolemy demo_vergil

#demo_applet:
#	CLASSPATH="$(CLASSPATH)" appletviewer Signature.htm

#demo_applet_vergil:
#	CLASSPATH="$(CLASSPATH)" appletviewer SignatureVergil.htm

demo_ptolemy:
	"$(PTII)/bin/ptolemy" Signature.xml

demo_vergil:
	"$(PTII)/bin/vergil" Signature.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
