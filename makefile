# Ptolemy II makefile
#
# @Version: $Id$
#
# Copyright (c) 1995-2014 The Regents of the University of California.
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
# Go into com before compiling ptolemy so we get com/microstar, used by MoMLParser.
# PTLBNL_DIR is set to lbnl by configure in $PTII/mk/ptII.mk
#   if libexpat was found.
# PTMESCAL_DIR is set to mescal by configure in $PTII/mk/ptII.mk
#   if $PTII/mescal was found
PTTHALES_DIR = thales
DIRS = util com diva net org ptolemy \
	$(PTDB_DIR) $(PTLBNL_DIR) $(PTMESCAL_DIR) $(PTTHALES_DIR) \
	ptserver contrib bin doc

# Root of Ptolemy II directory
ROOT =		.

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptII
# If you change the version number, be sure to edit 
# adm/test/Nightly*.tcl, doc/*,
# ptolemy/configs/*, ptolemy/configs/doc and
# ptolemy/kernel/attributes/VersionAttribute.java
# Also, create a new build director:
#  cd $PTII/adm
#  svn cp gen-N.M gen-N.O
# and update the versions in gen-N.O/makefile and the .xml files
PTVERSION =	10.0.devel
PTCLASSJAR =


# Jar files that contain demos and docs
PTDEMODOCJARS = \
		doc/docConfig.jar \
		ptolemy/actor/lib/javasound/demo/demo.jar \
		ptolemy/data/type/demo/demo.jar \
		ptolemy/domains/de/demo/demo.jar \
		ptolemy/domains/dt/demo/demo.jar \
		ptolemy/domains/giotto/demo/demo.jar \
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
		ptolemy/vergil/vergil.jar \
		ptolemy/vergil/gt/gt.jar

#PTCLASSALLJARS = \
#		$(PTDEMODOCJARS) \
#		ptolemy/ptolemy.jar \
#		ptolemy/vergil/vergil.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

EXTRA_SRCS = \
	.classpath.in \
	.eclipse.epf \
	README.txt \
	build.default.xml \
	build.xml \
	build.xml.in \
	copyright.htm \
	copyright.txt \
	configure.in \
	configure \
	jars.xml \
	plugin.xml \
	pom.xml \
	pt-modules \
	vergil.jnlp.in \
	vergilBCVTB.jnlp.in \
	vergilDSP.jnlp.in \
	vergilHyVisual.jnlp.in \
	vergilPtiny.jnlp.in \
	vergilPtinyKepler.jnlp.in \
	vergilPtinySandbox.jnlp.in \
	vergilSpace.jnlp.in \
	vergilVisualSense.jnlp.in

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	bin \
	config \
	lbnl \
	lib \
	mk \
	ptKeystore.properties

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
	config/*.class \
	vergil.jnlp \
	vergilDSP.jnlp \
	vergilHyVisual.jnlp \
	vergilPtiny.jnlp \
	vergilPtinySandbox.jnlp \
	vergilVisualSense.jnlp \
	ptKeystore \
	$(L4J_EXES) \
	$(L4J_CONFIGS)

# Files to be removed by 'make distclean'
DISTCLEAN_STUFF = \
	mk/ptII.mk config.log config.status config.cache

# The first rule is make fast so that if a user types 'make' they will 
# get make fast
default: antAllMessage fast 

antAllMessage:
	@echo "----------------"
	@echo "Warning: Please consider running 'ant' instead of make, it is faster.  See $$PTII/doc/coding/ant.htm.  Then run (cd $$PTII/bin; make)."
	@echo "----------------"

# Make copyright.txt readonly so that when we open up the text editor
# we open up a readonly texteditor
all: mk/ptII.mk antAllMessage suball
	chmod a-w copyright.txt


install: antInstallMessage subinstall $(PTCLASSALLJAR)

antInstallMessage:
	@echo "----------------"
	@echo "Warning: Please consider running 'ant jars' instead of make install, it is faster.  See $$PTII/doc/coding/ant.htm"
	@echo "----------------"
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
	@echo "This may occur if you do a svn update, and the mod time"
	@echo "of configure.in is newer than that of configure"
	@echo "even though the configure script in the repository"
	@echo "was modified after configure.in was modified."
	@echo "Note that if you don't have GNU autoconf installed,"
	@echo "you can try running 'touch configure' to work around"
	@echo "this problem."
	autoconf

# Generate a ChangeLog file from the SVN logs
# This rurequires that the SVN directory be present and takes
# quite awhile to update
ChangeLog:
	@if [ -d .svn ]; then \
		echo "Running ./util/testsuite/gnuify-changelog.pl"; \
		echo " This could take several minutes"; \
		echo " Consider running 'make ChangeLogThisYear.txt' instead"; \
		echo " See also http://chess.eecs.berkeley.edu/ptexternal/nightly/ChangeLog.txt"; \
		svn log | ./util/testsuite/gnuify-changelog.pl > ChangeLog; \
	else \
		echo ".svn directory not present, so we can't update $@"; \
	fi

# Produce a better ChangeLog.  Running it on the entire repository
# is too slow.  The nightly build has a copy, see 
# http://chess.eecs.berkeley.edu/ptexternal/nightly/ChangeLog.txt

# svn2cl, by Arthur de Jong, from http://ch.tudelft.nl/~arthur/svn2cl/
SVN2CLDIR=util/testsuite/svn2cl-0.10
ChangeLogThisYear.txt:
	$(SVN2CLDIR)/svn2cl.sh --include-rev -r "{`date +%Y`-12-31}:{`date +%Y`-01-01}" --stdout > ChangeLogThisYear.txt

update:
	-svn update
	$(MAKE) -k clean fast

mvnClean:
	(cd $(PTII)/ptolemy/actor/ptalon; rm -f `make -s echo_OPTIONAL_JSRCS`)
	(cd $(PTII)/ptolemy/data/expr; rm -f `make -s echo_OPTIONAL_JSRCS`)
	(cd $(PTII)/ptolemy/moml/unit; rm -f `make -s echo_OPTIONAL_JSRCS`)
	-(cd $(PTII)/ptolemy/copernicus/kernel/fragment; rm -f `make -s echo_OPTIONAL_JSRCS`)

cleanDerivedJavaFiles:
	(cd $(PTII)/ptolemy/actor/ptalon; rm -f `make -s echo_OPTIONAL_JSRCS`)
	(cd $(PTII)/ptolemy/data/expr; rm -f `make -s echo_OPTIONAL_JSRCS`)
	(cd $(PTII)/ptolemy/moml/unit; rm -f `make -s echo_OPTIONAL_JSRCS`)
	-(cd $(PTII)/ptolemy/copernicus/kernel/fragment; rm -f `make -s echo_OPTIONAL_JSRCS`)
	rm -rf vendors ptolemy/apps
	rm -rf ptolemy/backtrack/util/java/util/*.java

CLEAN_SHIPPING_FILES = \
		.classpath \
		*.jnlp \
		autom4te.cache \
		bin/ptinvoke \
		bin/comm.policy \
		build.xml \
		com/microstar/xml/SAXDriver.* \
		config.log \
		diva/build.xml \
		diva/canvas/tutorial/doc-files \
		doc/img/PtolemyIICD.ec3 \
		doc/codeDoc*.jar \
		doc/ptII.fb \
		doc/ptII.fbp \
		jnlp_manifest.txt \
		jnlp_sandbox_manifest.txt \
		lib/ptII.properties \
		lbnl/demo/CRoom/cclient.dSYM \
		lbnl/lib/util/libbcvtb.dylib \
		lbnl/lib/util/libbcvtb.dylib.dSYM \
		lbnl/lib/util/libbcvtb.jnilib \
		lbnl/lib/util/libbcvtb.jnilib.dSYM \
		lbnl/lib/util/libbcvtb.so \
		lib/cachedir \
		mk/ptII.mk \
		ptKeystore \
		ptolemy/apps \
		ptolemy/actor/lib/ptp \
		ptolemy/actor/gui/test/CustomQueryExample.tar.gz \
		ptolemy/actor/lib/gui/KeystrokeSensor* \
		ptolemy/actor/lib/python/demo/HelloWorld \
		ptolemy/actor/lib/python/demo/NotifyFailedTest \
		ptolemy/actor/lib/security/test/foo.keystore \
		ptolemy/copernicus/*/test/codeGenerator.tmp \
		ptolemy/copernicus/jhdl \
		ptolemy/copernicus/kernel/fragment \
		ptolemy/copernicus/kernel/test/substitute.out \
		ptolemy/copernicus/*/cg \
		ptolemy/configs/doc/whatsNew8.0.htm \
		ptolemy/domains/csp/demo/DiningPhilosophers/checkDeadlock \
		ptolemy/domains/ct/lib/IPCInterface.class \
		ptolemy/domains/ct/lib/IPCInterface.java \
		ptolemy/domains/dde/kernel/test/test.tcl \
		ptolemy/domains/fairdf \
		ptolemy/domains/fmi \
		ptolemy/domains/fp \
		ptolemy/domains/pdf \
		ptolemy/domains/gr/lib/Loader3D.* \
		ptolemy/domains/gr/lib/experimental \
		ptolemy/domains/tm/lib/PeriodicTrigger* \
		ptolemy/domains/wireless/demo/Network \
		ptolemy/domains/wireless/lib/network \
		ptolemy/domains/wireless/lib/tinyOS \
		ptolemy/matlab/libptmatlab.dylib \
		ptolemy/matlab/libptmatlab.so \
		ptolemy/matlab/matlabLinux.jar \
		ptolemy/matlab/ptmatlab.dll \
		ptolemy/matlab/ptmatlab.exp \
		ptolemy/matlab/ptmatlab.h \
		ptolemy/matlab/ptmatlab.lib \
		ptolemy/matlab/ptmatlab.obj \
		ptolemy/moml/filter/test/testModels.txt \
		ptolemy/moml/filter/test/testNamedObjs.txt \
		ptolemy/ptp \
		ptolemy/util/test/junit/javachidr32 \
	        ptolemy/vergil/basic/layout/kieler/test/layoutPerformance.xml \
	        ptolemy/vergil/basic/layout/kieler/test/layoutPerformance2.xml \
		signed \
		tmp
 
clean_shipping:
	rm -rf $(CLEAN_SHIPPING_FILES)

svn_delete_clean_shipping:
	for files in $(CLEAN_SHIPPING_FILES); do \
		if [ -e $$files ]; then \
			svn delete -f $$files; \
	        fi \
	done 


# Include rules to build Web Start JNLP files
include $(ROOT)/mk/jnlp.mk

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
