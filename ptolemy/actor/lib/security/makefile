# Makefile for Ptolemy II Security Actors
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
#                                               PT_COPYRIGHT_VERSION_2
#                                               COPYRIGHTENDKEY

ME =            ptolemy/actor/lib/security

DIRS =		    

# Root of the Java directory
ROOT =          ../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)

# Get configuration info
CONFIG =        $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     security
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	CipherActor.java \
	CryptographyActor.java \
	KeyReader.java \
	KeyStoreActor.java \
	KeyToken.java \
	KeyWriter.java \
	Password.java \
	PrivateKeyReader.java \
	PublicKeyReader.java \
	SecretKey.java \
	SecretKeyReader.java \
	SignatureActor.java \
	SignatureSigner.java \
	SignatureVerifier.java \
	SymmetricDecryption.java \
	SymmetricEncryption.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
        doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass $(ROOT)/ptKeystore
install: jclass jars

# The default keystore is $(PTII)/ptKeystore
$(ROOT)/ptKeystore:
	(cd $(ROOT); $(MAKE) ptKeystore)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
