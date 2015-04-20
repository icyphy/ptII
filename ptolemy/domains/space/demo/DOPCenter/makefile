# Makefile for Ptolemy II DOPCenter demo
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2008-2014 The Regents of the University of California.
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

ME =		ptolemy/domains/space/demo/DOPCenter

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	DOPCenter
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	$(JSRCS) \
	DOPCSV.xml \
	DOPCenter.gif \
	DOPCenter.xml \
	expand-group.vbs \
	Placard.xml \
	updateDOPCenterImage

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) \
	 \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	Placard.pdf \
	DOP.csv \
	DOPCenter.png

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass
	@echo "To run all the demos, run 'make demo'"

install: all $(PTCLASSJAR)

demo:  demo_ptolemy demo_vergil

copernicus:
	"$(PTII)/bin/copernicus" -codeGenerator applet -targetPath ptolemy/domains/space/demo/DOPCenter DOPCenter.xml

# Generate applet code in a remote directory.  This command will
# copy jar files to the remote directory 
# Don't use c: here, Solaris make fails.
PTII_USER_DIRECTORY=/cygdrive/c/tmp/ptIIapplet
$(PTII_USER_DIRECTORY):
	if [ ! -d "$(PTII_USER_DIRECTORY)" ]; then \
		mkdir -p "$(PTII_USER_DIRECTORY)/DOPCenter"; \
	fi

remoteApplet: $(PTII_USER_DIRECTORY)
	 "$(PTII)/bin/copernicus" -codeGenerator applet \
		-ptIIUserDirectory '"$(PTII_USER_DIRECTORY)"' \
		-targetPath DOPCenter DOPCenter.xml


SIGNED_DIRECTORY=$(PTIII_USER_DIRECTORY)/signed
$(SIGNED_DIRECTORY):
	if [ ! -d "$(SIGNED_DIRECTORY)" ]; then \
		echo "Creating signed jars in $(SIGNED_DIRECTORY)"; \
		(cd $(ROOT); $(MAKE) jnlp_sign); \
	fi


$(PTII_USER_DIRECTORY)/signed/ptolemy/vergil/vergilApplet.jar: $(SIGNED_DIRECTORY) $(PTII_USER_DIRECTORY)/ptolemy/vergil/vergilApplet.jar
	mkdir -p "$(PTII)/signed/ptolemy/actor/lib/database"
	cp "$(PTII)/ptolemy/vergil/vergilApplet.jar" \
		"$(PTII)/signed/ptolemy/vergil/vergilApplet.jar"
	(cd "$(PTII)"; $(MAKE) sign_jar JARFILE=signed/ptolemy/vergil/vergilApplet.jar)

$(PTII_USER_DIRECTORY)/signed/ptolemy/actor/lib/database/ojdbc6.jar: $(SIGNED_DIRECTORY) $(PTII_USER_DIRECTORY)/ptolemy/actor/lib/database/ojdbc6.jar
	mkdir -p "$(PTII)/signed/ptolemy/actor/lib/database"
	cp "$(PTII)/ptolemy/actor/lib/database/ojdbc6.jar" \
		"$(PTII)/signed/ptolemy/actor/lib/database/ojdbc6.jar"
	(cd "$(PTII)"; $(MAKE) sign_jar JARFILE=signed/ptolemy/actor/lib/database/ojdbc6.jar)

$(PTII_USER_DIRECTORY)/signed/ptolemy/actor/lib/database/database.jar: $(SIGNED_DIRECTORY) $(PTII_USER_DIRECTORY)/ptolemy/actor/lib/database/database.jar"
	mkdir -p "$(PTII)/signed/ptolemy/actor/lib/database"
	cp "$(PTII)/ptolemy/actor/lib/database/database.jar" \
		"$(PTII)/signed/ptolemy/actor/lib/database/database.jar"
	(cd "$(PTII)"; $(MAKE) sign_jar JARFILE=signed/ptolemy/actor/lib/database/database.jar)

$(PTII_USER_DIRECTORY)/signed/ptolemy/domains/space/space.jar: $(SIGNED_DIRECTORY) $(PTII_USER_DIRECTORY)/ptolemy/domains/space/space.jar 
	mkdir -p "$(PTII)/signed/ptolemy/actor/lib/database"
	cp "$(PTII)/ptolemy/domains/space/space.jar" \
		"$(PTII)/signed/ptolemy/domains/space/space.jar"
	(cd "$(PTII)"; $(MAKE) sign_jar JARFILE=signed/ptolemy/domains/space/space.jar)

OTHER_SIGNED_JARS = \
	$(PTII_USER_DIRECTORY)/signed/ptolemy/vergil/vergilApplet.jar \
	$(PTII_USER_DIRECTORY)/ptolemy/actor/lib/database/ojdbc6.jar \
	$(PTII_USER_DIRECTORY)/ptolemy/actor/lib/database/database.jar \
	$(PTII_USER_DIRECTORY)/ptolemy/domains/space/space.jar

remoteSignedApplet: $(PTII_USER_DIRECTORY) $(OTHER_SIGNED_JARS) 
	cp DOPCenter.gif  "$(PTII_USER_DIRECTORY)/DOPCenter"
	 "$(PTII)/bin/copernicus" \
		-verbose \
		-ptIIJarsPath '"$(SIGNED_DIRECTORY)"' \
		-codeGenerator applet \
		-ptIIUserDirectory '"$(PTII_USER_DIRECTORY)"' \
		-targetPath '"DOPCenter"' \
		DOPCenter.xml


demo_ptolemy:
	"$(PTII)/bin/ptolemy" DOPCenter.xml

demo_vergil:
	"$(PTII)/bin/vergil" DOPCenter.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
