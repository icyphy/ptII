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

# Web Start: Full Runtime version of Vergil - No sources or build env.
vergil.jnlp: vergil.jnlp.in
	@echo "Don't forget that if you change vergil.jnlp.in, you need"
	@echo " to run 'make jnlp_sign' which will update vergil.jnlp"
	@echo " in ptII.jar."
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@

# Web Start: DSP version of Vergil - No sources or build env.
vergilDSP.jnlp: vergilDSP.jnlp.in
	@echo "Don't forget that if you change vergil.jnlp.in, you need"
	@echo " to run 'make jnlp_sign' which will update vergil.jnlp"
	@echo " in ptII.jar."
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@

# Web Start: Ptiny version of Vergil - No sources or build env
vergilPtiny.jnlp: vergilPtiny.jnlp.in
	@echo "Don't forget that if you change vergil.jnlp.in, you need"
	@echo " to run 'make jnlp_sign' which will update vergil.jnlp"
	@echo " in ptII.jar."
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@

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

CORE_JNLP_JARS = \
	ptolemy/vergil/vergil.jar \
	doc/docConfig.jar \
	lib/diva.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/ptolemy.jar

PTINY_JNLP_JARS = \
	$(CORE_JNLP_JARS) \
	ptolemy/actor/lib/javasound/demo/demo.jar \
	ptolemy/data/type/demo/demo.jar \
	ptolemy/domains/ct/demo/demo.jar \
	ptolemy/domains/de/demo/demo.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/moml/demo/demo.jar

VERGIL_JNLP =		vergil.jnlp 
VERGIL_MAIN_JAR = 	ptolemy/vergil/vergil.jar

VERGIL_JNLP =		vergilPtiny.jnlp 
VERGIL_MAIN_JAR = 	ptolemy/ptolemy.jar
JNLP_JARS =		$(PTINY_JNLP_JARS)

# vergil*.jnlp is for Web Start.  For jar signing to work with Web Start,
# the .jnlp file itself must be included in the signed jar file
# and not be changed (See Section 5.4 of the JNLP specification).
jnlp_sign: $(VERGIL_JNLP) update_jar $(KEYSTORE)
	set $(JNLP_JARS); \
	for x do \
		echo "signing '$$x' jar file"; \
		"$(PTJAVA_DIR)/bin/jarsigner" \
			-keystore $(KEYSTORE) \
			$(STOREPASSWORD) \
			$$x $(KEYALIAS); \
	done;

update_jar: $(VERGIL_JNLP)
	@echo "Updating JNLP-INF/APPLICATION.JNLP with vergil.jnlp"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $(VERGIL_JNLP) JNLP-INF/APPLICATION.JNLP
	@echo "$(VERGIL_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(VERGIL_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(VERGIL_MAIN_JAR) $(KEYALIAS)

