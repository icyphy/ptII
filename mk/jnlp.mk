# Ptolemy II to build Web Start JNLP files
#
# @Author: Christopher Hylands
# @Version: $Id$
#
# Copyright (c) 2001 The Regents of the University of California.
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

# Java Network Launch Protocol aka Web Start
#
# This makefile should be included from the bottom of $PTII/makefile
# It is a separate file so as to not clutter up $PTII/makefile

# Large jar file containing all the codedoc documentation.
# Comment this out for testing
#DOC_CODEDOC_JAR = \
#	doc/codeDoc.jar
DOC_CODEDOC_JAR =

# Jar files that will appear in all Ptolemy II JNLP files.
CORE_JNLP_JARS = \
	doc/docConfig.jar \
	lib/diva.jar \
	ptolemy/domains/domains.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
	ptolemy/actor/lib/javasound/javasound.jar \
	ptolemy/media/javasound/javasound.jar \
	$(DOC_CODEDOC_JAR)

#######
# DSP - The smallest runtime
#
# Jar files that will appear in a DSP only JNLP Ptolemy II Runtime.
DSP_ONLY_JNLP_JARS =

DSP_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/DSPApplication.jar

DSP_JNLP_JARS =	\
	$(DSP_MAIN_JAR) \
	$(CORE_JNLP_JARS)


#######
# Ptiny
#
# Jar files that will appear in a smaller (Ptiny) JNLP Ptolemy II Runtime.
PTINY_ONLY_JNLP_JARS = \
	ptolemy/actor/lib/javasound/demo/demo.jar \
	ptolemy/data/type/demo/demo.jar \
	ptolemy/domains/ct/demo/demo.jar \
	ptolemy/domains/de/demo/demo.jar \
	ptolemy/domains/fsm/demo/demo.jar \
	ptolemy/moml/demo/demo.jar

PTINY_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinyApplication.jar

PTINY_JNLP_JARS = \
	$(PTINY_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)

#######
# Full
#
COPERNICUS_JARS = \
	lib/jasminclasses.jar \
	lib/sootclasses.jar \
	ptolemy/copernicus/copernicus.jar

# Jar files that will appear in a full JNLP Ptolemy II Runtime
FULL_ONLY_JNLP_JARS = \
	$(COPERNICUS_JARS) \
	ptolemy/domains/experimentalDomains.jar \
	ptolemy/codegen/codegen.jar \
	ptolemy/domains/dt/demo/demo.jar \
	ptolemy/domains/giotto/demo/demo.jar \
	ptolemy/domains/gr/demo/demo.jar \
	ptolemy/domains/pn/demo/demo.jar \
	ptolemy/domains/rtos/demo/demo.jar \
	ptolemy/domains/sr/demo/demo.jar

FULL_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/FullApplication.jar

FULL_JNLP_JARS = \
	$(FULL_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(FULL_ONLY_JNLP_JARS)

#########

# All the JNLP Jar files, hopefully without duplicates so that 
# we don't sign jars twice.
ALL_JNLP_JARS = \
	$(CORE_JNLP_JARS) \
	$(FULL_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS)

# Script to update a *.jnlp file with the proper jar files
MKJNLP =		$(PTII)/bin/mkjnlp


# JNLP files that do the actual installation
JNLPS =	vergilDSP.jnlp vergilPtiny.jnlp  vergil.jnlp 

jnlp_all: $(JNLPS) jnlp_sign
jnlps: $(JNLPS)
jnlp_clean: 
	rm -f $(JNLPS)

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
	"$(KEYTOOL)" -genkey \
		-dname $(KEYDNAME) \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD)
	"$(KEYTOOL)" -selfcert \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD)
	"$(KEYTOOL)" -list \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD)

# vergil*.jnlp is for Web Start.  For jar signing to work with Web Start,
# Web Start: DSP version of Vergil - No sources or build env.
vergilDSP.jnlp: vergilDSP.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x $(MKJNLP)
	"$(MKJNLP)" $@ \
		$(DSP_MAIN_JAR) \
		$(DSP_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(DSP_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(DSP_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(DSP_MAIN_JAR) $(KEYALIAS)

# Web Start: Ptiny version of Vergil - No sources or build env.
vergilPtiny.jnlp: vergilPtiny.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x $(MKJNLP)
	"$(MKJNLP)" $@ \
		$(PTINY_MAIN_JAR) \
		$(PTINY_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(PTINY_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(PTINY_MAIN_JAR) $(KEYALIAS)


# Web Start: Full Runtime version of Vergil - No sources or build env.
vergil.jnlp: vergil.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x $(MKJNLP)
	"$(MKJNLP)" $@ \
		$(FULL_MAIN_JAR) \
		$(FULL_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(FULL_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(FULL_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(FULL_MAIN_JAR) $(KEYALIAS)

# the .jnlp file itself must be included in the signed jar file
# and not be changed (See Section 5.4 of the JNLP specification).
jnlp_sign: $(JNLPS) $(KEYSTORE)
	set $(ALL_JNLP_JARS); \
	for x do \
		echo "# Signing '$$x'."; \
		"$(PTJAVA_DIR)/bin/jarsigner" \
			-keystore $(KEYSTORE) \
			$(STOREPASSWORD) \
			$$x $(KEYALIAS); \
	done;


sign_jar: 
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(JARFILE) $(KEYALIAS)


# Verify the jar files.  This is useful for debugging if you are
# getting errors about unsigned applications
 
jnlp_verify:
	set $(ALL_JNLP_JARS); \
	for x do \
		echo "$$x"; \
		"$(PTJAVA_DIR)/bin/jarsigner" -verify $$x; \
	done;
