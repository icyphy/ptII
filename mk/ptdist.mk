# Makefile rules for building distributions 
#
# Author: Christopher Hylands

# Version Identification:
# $Id$
#
# Copyright (c) 1990-2000 The Regents of the University of California.
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

# Please don't use GNU make extensions in this file, such as 'ifdef' or '%'.
# If you really must use an GNU make extension, please label it.


# This file is only used for building tar file and zip source distributions.
# Currently, ptplot uses this makefile
#
# Here, we use the term 'calling makefile' for the top level makefile
# of the package that we are shipping.  For example, if we are shipping
# ptolemy/plot, then ptolemy/plot/makefile is the calling makefile that
# includes ptdist.mk and sets the proper variables.
#
# The calling makefile should have the following features:
#
#  1. It should include ptdist.mk at the top of the file
#  include $(ROOT)/mk/ptdist.mk
#
#  2. It should set the following makefile variables
#  PTPACKAGE = 	ptolemy.plot
#  PTVERSION =	2.0
#  PTDIST =	$(PTPACKAGE)$(PTVERSION)
#  PTCLASSJAR = 	$(PTPACKAGE).jar
# 
#  3. It should have a fixtmpdist rule that makes any modifications
#  that are necessary to adm/tmp/$(PTDIST).  For example:
#
#  fixtmpdist:
#	cp README.ptplot adm/tmp/$(PTDIST)
#
#
# To create a distribution, this makefile follows the steps below:
# 1. Copy the files to a adm/tmp subdirectory
# 2. Run the fixdist rule in the top level makefile
# 3. Tar and zip up the directory

###############################################################
# Makefile variables

# The distributions to build
PTDISTS =	$(PTDIST).tar.gz $(PTDIST).zip 

# Temporary directory 
# If you change PTTMPDIR, you may need to change RELATIVE_ME
PTTMPDIR =	adm/tmp

# The relative pathname from the PTTMPDIR to the ME
RELATIVE_ME = ../..

# Temporary distribution, PTDIST is set in the calling makefile 
PTTMPDIST =	$(PTMPDIR)/$(PTDIST)

# Files to ship in the top level directory
TOPFILES = config/confTest.java config/install-sh \
	configure configure.in copyright.txt\
	mk/ptII.mk.in mk/ptcommon.mk mk/ptdir.mk mk/ptno-compile.mk \
	mk/ptdist.mk

# List of files for tar to exclude.
PTDIST_EX_BASE = $(PTDIST).ex
PTDIST_EX =	$(PTTMPDIR)/$(PTDIST_EX_BASE)

# GNU tar
GNUTAR =	gtar 

# Minimal path for testing.  The path should not include GNU make.
TESTPATH = 	/opt/jdk1.1.6/bin:/bin:/usr/ccs/bin:.

# InstallShield Java executable
# See http://www.installshield.com/java
# Runs on ISJAVA_SRC
ISJAVA = /users/ptII/vendors/installshield/isjava25/bin/isjava


###############################################################
# Makefile rules

# The dists rule builds both a tar file and zip file of the sources
# This is the rule to call to build the distributions
# The fixtmpdist rule should be defined in the calling makefile
dists: sources install distsfiles
# We split up the dists rule to aid in debugging
distsfiles: $(PTTMPDIST) fixtmpdist $(PTDISTS) isjavadists

# This name is a little too close to distclean
distsclean:
	rm -f $(PTDISTS) $(PTDIST_EX)
	rm -rf $(PTTMPDIR)/$(PTDIST)

# Create the temporary distribution which we will modify to create
# the final distribution
$(PTTMPDIST): pttmpdist
pttmpdist: $(PTTMPDIR) $(PTDIST_EX)
	-mkdir -p $(PTTMPDIR)/$(PTDIST)
	(cd $(ROOT); \
	 	$(GNUTAR) -cf - -X $(ME)/$(PTDIST_EX) \
			$(ME) $(TOPFILES)) | \
	(cd $(PTTMPDIR)/$(PTDIST); $(GNUTAR) -xf -)

$(PTTMPDIR):
	@if [ ! -d $@ ]; then echo "Creating $@"; mkdir -p $@; fi

# Create the list of files for tar to exclude
# If ptdist.mk changes, then update the list of files we are excluding
$(PTDIST_EX): $(ROOT)/mk/ptdist.mk
	@if [ "$(ME)x" = "x" ]; then \
		echo "ME is not set in the makefile, so we";\
		echo "won't create a tar exclude file"; \
	else \
		echo "dummy" | \
		awk '{printf("adm\nSCCS\nRCS\nCVS\n*.tar.gz\n*[0-9].zip\n")}' \
			> $@; \
	fi

# Tar file distribution
$(PTDIST).tar.gz:  $(PTDIST_EX)
	if [ "$(ME)x" = "x" ]; then \
		echo "ME is not set in the makefile, so we"; \
		echo "won't create a tar exclude file"; \
	else \
		echo "Building $@"; \
		(cd $(PTTMPDIR); \
		 $(GNUTAR) -zcf $(RELATIVE_ME)/$@ \
			-X $(PTDIST_EX_BASE) $(PTDIST) ); \
	fi

# Zip distribution
$(PTDIST).zip:
	@if [ "$(ME)x" = "x" ]; then \
		echo "ME is not set in the makefile, so we"; \
		echo "won't create a tar exclude file"; \
	else \
		echo "Building $@"; \
		(cd $(PTTMPDIR); zip -rq $(RELATIVE_ME)/$@ $(PTDIST) -x \*/adm/\* -x \*/SCCS/\* -x \*/$(PTDIST).tar.gz -x \*/$(PTDIST).zip); \
	fi

# Use InstallShield's Java installer
isjavadists:
	$(ISJAVA) $(ISJAVA_SRC)

# Build sources in a form suitable for releasing
buildjdist:
	$(MAKE) sources
	$(MAKE) distclean
	$(MAKE) distclean
	$(MAKE) JFLAGS=-O jclass
	$(MAKE) install
	$(MAKE) dists

# Test the distribution
diststest: 
	rm -rf $(PTTMPDIR)/$(PTDIST)
	gzcat < $(PTDIST).tar.gz > $(PTTMPDIR)/$(PTDIST).tar 
	cd $(PTTMPDIR);	/bin/tar -xvf $(PTDIST).tar
	(cd $(PTTMPDIR)/$(PTDIST); \
		PATH=$(TESTPATH) configure; \
		PATH=$(TESTPATH) make clean install; \
	)
# Create a distribution and install it.
# This rule is particular to our local installation
JDESTDIR = /vol/ptolemy/pt0/ptweb/java
installjdist:
	$(MAKE) buildjdist
	$(MAKE) updatewebsite

updatewebsite: $(PTDISTS)
	@echo "Updating website"
	(cd $(JDESTDIR); rm -rf $(PTDIST); mkdir -p $(PTDIST)/$(ME))
	cp $(PTDISTS) $(JDESTDIR)/$(PTDIST)/$(ME)
	(cd $(JDESTDIR); $(GNUTAR) \
		-zxf $(PTDIST)/$(ME)/$(PTDIST).tar.gz;\
	 chmod g+ws $(PTDIST))
	(cd $(JDESTDIR)/$(PTDIST)/$(ME); chmod g+w $(PTDISTS))
