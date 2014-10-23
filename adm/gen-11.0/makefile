# Makefile to create tar files for distribution
# Version: $Id$
# Copyright (c) 1994-2010 The Regents of the University of California.
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
#
# Programmer:  Christopher Hylands

# Variables
#  If you change the version number, then edit 
#  ptII/adm/bin/ptIInightly-mail
#  ptII/adm/bin/ptIInightly
#  buildmaster/web/adm/makefile
MAJOR_VERSION = 	11.0
MINOR_VERSION =		devel

VERSION=	$(MAJOR_VERSION).$(MINOR_VERSION)
# Version string for lame filesystems that don't like more than one .
WINDOWS_VERSION =	11_0_devel

# Even if you are building tar files from ptdesign, this should probably
# still say ptolemy
#PTII_SRC =		pt

# Location of utilities
# Don't use /usr/sww/bin/gtar sun4 binaries if you are building on a sol2
TAR = 		/usr/local/bin/tar
ZIP =		/usr/sww/bin/zip
COMPRESS =	/usr/sww/bin/gzip
GUNZIP =	/usr/sww/bin/gunzip
NON_GNUTAR =	/bin/tar
SUFFIX =

# Script to split tar files into smaller chunks
SPLITTAR =	/users/ptolemy/adm/bin/splittar

# FTP Directory
# Use this for release
PTII_FTP = /export/home/pt0/ptweb/ptolemyII/ptII$(MAJOR_VERSION)

# Name and directory of distribution.  For testing, use ptdesign
# For release, use ptolemy
#USER =		ptolemy
# For use under Windows
# Use tar 1.13.20 or later so that -X works
# Versions later than 1.13 can be found at ftp://alpha.gnu.org/gnu/tar/
# 1.12 can be found at ftp://gatekeeper.dec.com/pub/GNU/tar/
#
# A MSDOS version of 1.12 can be found at ftp://ftp.gnu.org/pub/gnu/tar,
# but that binary does not understand /cygdrive/c notation, and does
# not work well, I get:
#   4 [main] sh 2328 proc_subproc: Couldn't duplicate my handle<0x7E0>
#   for pid 0, Win32 error 6

# 1.14 can be found at ftp://ftp.gnu.org/pub/gnu/tar
# make USER=cxh PTIIHOME=/cygdrive/c/cxh/ptII COMPRESS=gzip TAR=tar clean all src.jar 
# make USER=cxh PTIIHOME=/cygdrive/c/cxh/ptII11.0.devel COMPRESS=gzip TAR=tar clean all src.jar 
# make USER=cxh PTIIHOME=/cygdrive/d/tmp/ptII COMPRESS=gzip TAR=/usr/local/bin/tar
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII3.1alpha PTIIADM=/home/eecs/cxh/ptII/adm TAR=/usr/local/bin/tar
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII PTIIADM=/home/eecs/cxh/ptII/adm TAR=/usr/local/bin/tar clean all src.jar setup
# make USER=cxh PTIIHOME=/export/home1/cxh/src/ptII PTIIADM=/export/home1/cxh/src/ptII/adm TAR=/usr/sfw/bin/gtar JAR=/usr/bin/jar clean all src.jar 
#make USER=cxh PTIIHOME=/tmp/cxh/ptII PTIIADM=/tmp/cxh/ptII/adm TAR=/bin/tar JAR=/usr/sww/bin/jar clean all src.jar 
#make USER=cxh PTIIHOME=/tmp/cxh/ptII11.0.devel PTIIADM=/tmp/cxh/ptII11.0.devel/adm TAR=/bin/tar JAR=/tmp/cxh/ibm-java2-i386-60/bin/jar clean all src.jar fortytwo_test
#make USER=cxh PTIIHOME=/tmp/cxh/ptII PTIIADM=/tmp/cxh/ptII/adm TAR=/bin/tar JAR=/tmp/cxh/jdk1.5.0_09/bin/jar clean all src.jar fortytwo_test
#make USER=cxh PTIIHOME=/tmp/cxh/ptII PTIIADM=/tmp/cxh/ptII/adm TAR=/bin/tar JAR=/usr/sww/bin/jar clean all src.jar viptos.jar
#make USER=cxh PTIIHOME=/tmp/cxh/tmp/ptII PTIIADM=/tmp/cxh/tmp/ptII/adm TAR=/bin/tar JAR=/usr/local/j2sdk1.4.2_01/bin/jar src.jar
#gmake USER=bldmastr PTIIHOME=/export/home1/bldmastr/ptII PTIIADM=/export/home1/bldmastr/ptII/adm TAR=/usr/sfw/bin/gtar JAR=/usr/bin/jar clean all src.jar
#make USER=bldmastr PTIIHOME=/home/bldmastr/ptII PTIIADM=/home/bldmastr/ptII/adm TAR=/usr/local/bin/tar JAR=/usr/bin/jar clean all src.jar setup setup_mac_app_tar
# nohup make USER=cxh PTIIHOME=/home/cxh/tmp/ptII PTIIADM=/home/cxh/tmp/ptII/adm TAR=/usr/local/bin/tar JAR=/etc/alternatives/java_sdk/bin/jar clean all src.jar setup
# make USER=hudson PTIIHOME=/var/lib/hudson/jobs/ptII/workspace PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm JAR=/usr/bin/jar clean all src.jar test_setup

USER =		ptII
# The location of the mastr source tree.
PTIIHOME =	/users/ptII
PTIIADM =	$(PTIIHOME)/adm

# Should be this directory
GENDIR =	$(PTIIADM)/gen-$(MAJOR_VERSION)

# Temporary location of the distribution so that we can
# have the tar files have the version number in them.
# Files in this directory get changed to reflect things that we don't ship.
PTIIDIST = 	$(PTIIADM)/dists/ptII$(VERSION)

# Directories and files in the top level ptII directory that we should
# include in the tar file.
#
# NOTE: If you edit this, edit ptII$(WINDOWS_VERSION)_setup_windows.xml
#
PTII_TOPDIR=	.cshrc \
		.classpath.in \
		.classpath.default \
		.eclipse.epf \
		.project.default \
		README.txt \
		bin \
		build.default.xml \
		build.xml.in \
		com/cureos \
		com/jgoodies \
		com/makefile \
		com/microstar \
		config \
		configure \
		configure.in \
		contrib \
		copyright.htm \
		copyright.txt \
		diva \
		doc \
		jars.xml \
		lib \
		lbnl \
		makefile \
		mk \
		net \
		org \
		plugin.xml \
		pom.xml \
		ptdb \
		ptKeystore.properties \
		pt-modules \
		ptolemy \
		ptserver \
		thales \
		util \
		vergil.jnlp.in \
		vergilBCVTB.jnlp.in \
		vergilDSP.jnlp.in \
		vergilHyVisual.jnlp.in \
		vergilPtiny.jnlp.in \
		vergilPtinyKepler.jnlp.in \
		vergilPtinySandbox.jnlp.in \
		vergilSpace.jnlp.in \
		vergilVisualSense.jnlp.in

# Location of the SVN Repository
SVN_REPOSITORY = file:///home/svn/chess/ptII/trunk

##############################################################################
# Files to be produced

PTII_FULL = 	  $(GENDIR)/ptII$(VERSION).tar$(SUFFIX)
PTII_FULL_ZIP =    $(GENDIR)/ptII$(VERSION).src.zip
# Source only, no .class, .jar or javadoc
PTII_SRC =   $(GENDIR)/ptII$(VERSION).src.tar$(SUFFIX)
PTII_SRC_JAR =   $(GENDIR)/ptII$(VERSION).src.jar

VERGIL = 	  $(GENDIR)/vergil$(VERSION).src.tar$(SUFFIX)

# Three forms we ship in.
PTII_SRCS = 	$(PTII_FULL) $(basename $(PTII_FULL)) $(PTII_FULL_ZIP)

PTII_DEVEL_SRC = $(GENDIR)/ptolemy.devel$(VERSION).src.tar$(SUFFIX)

##############################################################################
# Targets
all: $(PTII_FULL) $(PTII_SRC) #$(PTII_FULL_ZIP) #$(PTII_DEVEL_SRC)

release_newton_all:
	$(MAKE) USER=cxh PTIIHOME=/cygdrive/c/tmp/cxh/ptII COMPRESS=gzip TAR=tar clean all src.jar setup REMOTE4PTIIDIST=c:/tmp/cxh/ptII/adm/gen-9.1/test/ptII$(VERSION)

release_newton_setup:
	$(MAKE) USER=cxh PTIIHOME=/cygdrive/c/tmp/cxh/ptII11.0.devel COMPRESS=gzip TAR=tar setup

release_newton_remote4_test:
	$(MAKE) USER=cxh PTIIHOME=/cygdrive/c/tmp/cxh/ptII11.0.devel COMPRESS=gzip TAR=tar remote4_test REMOTE4PTIIDIST=c:/tmp/cxh/ptII11.0.devel/adm/gen-7.0/test/ptII$(VERSION)

####
release_sisyphus:
	make USER=cxh PTIIHOME=/home/cxh/src/ptII11.0.devel PTIIADM=/home/cxh/src/ptII11.0.devel/adm TAR=/bin/tar JAR=/usr/lib/jvm/java-1.6.0/bin/jar JAVADOC=/usr/lib/jvm/jdk1.8.0_11/bin/javadoc clean all src.jar

release_sisyphus_setup:
	make USER=cxh PTIIHOME=/home/cxh/src/ptII11.0.devel PTIIADM=/home/cxh/src/ptII11.0.devel/adm TAR=/bin/tar JAR=/usr/lib/jvm/java-1.6.0/bin/jar JAVADOC=/usr/lib/jvm/jdk1.8.0_11/bin/javadoc setup

release_sisyphus_remote4_test:
	make USER=cxh PTIIHOME=/home/cxh/src/ptII11.0.devel PTIIADM=/home/cxh/src/ptII11.0.devel/adm TAR=/bin/tar JAR=/usr/lib/jvm/java-1.6.0/bin/jar JAVADOC=/usr/lib/jvm/jdk1.8.0_11/bin/javadoc remote4_Test

release_sisyphus_update_ftp:
	make USER=cxh PTIIHOME=/home/cxh/src/ptII11.0.devel PTIIADM=/home/cxh/src/ptII11.0.devel/adm TAR=/bin/tar JAR=/usr/lib/jvm/java-1.6.0/bin/jar JAVADOC=/usr/lib/jvm/jdk1.8.0_11/bin/javadoc update_ftp

####
release_windows:
	make USER=cxh PTIIHOME=/cygdrive/c/cxh/ptII11.0.devel COMPRESS=gzip TAR=tar JAR=c:/Program\ Files/Java/jdk1.5.0_11/bin/jar clean all src.jar setup

####

####
release_mac:
	make USER=cxh PTIIHOME=/Users/cxh/src/ptII11.0.devel COMPRESS=gzip TAR=tar JAR=/usr/bin/jar clean all src.jar setup

release_mac2:
	make USER=cxh PTIIHOME=/Users/cxh/ptII COMPRESS=gzip TAR=/opt/local/bin/gnutar JAR=/usr/bin/jar clean all src.jar setup

release_mac2_setup:
	make USER=cxh PTIIHOME=/Users/cxh/ptII COMPRESS=gzip TAR=/opt/local/bin/gnutar JAR=/usr/bin/jar setup

####
release_nightly:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr \
		PTII=/home/bldmastr/ptII \
		PTIIHOME=/home/bldmastr/ptII \
		JFLAGS="-deprecation -g -source 1.5" \
		JSDATABASE=/home/bldmastr/jsdatabase \
		JSOUTDIR=/home/www/cvswww/ptolemy/nightly \
		JSCLASSPATH=/usr/local/JavaScope/JavaScope.jar \
		all setup

release_nightly_clean_setup:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr \
		PTII=/home/bldmastr/ptII \
		PTIIHOME=/home/bldmastr/ptII \
		clean all setup setup_mac_app_tar

release_nightly_setup:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr \
		PTII=/home/bldmastr/ptII \
		PTIIHOME=/home/bldmastr/ptII \
		setup setup_mac_app_tar

# Rebuild the distributions
rebuild: clean all installshield_app build_unixPtII

clean:	full_clean_setup
	@echo "#"
	@echo "# Now cleaning old tar files"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	rm -f $(PTII_FULL) $(PTII_FULL_ZIP) $(PTII_DEVEL_SRC) $(PTII_SRC)
	rm -rf $(PTII_SRC_JAR) src_jar
	rm -rf $(PTIIDIST) src installshield/*
	rm -rf $(HYVISUAL_SRC_JAR)
	rm -rf $(VIPTOS_SRC_JAR)
	rm -rf $(PTSETUP)_64.xml $(PTSETUP).jar $(PTSETUP).exe test/installerTest test/ptII$(VERSION)
	rm -rf $(PTSETUP_MAC).xml $(PTSETUP_MAC).jar $(PTSETUP_MAC).app $(PTSETUP_MAC).dmg $(PTSETUP_MAC).app.tar.gz

FINDCLEAN = $(GENDIR)/findclean

# Don't use javadoc from Java 1.6 here, it will hang
JAVADOC=javadoc

# First we create a directory containing the distribution,
# then we modify the distribution slightly,
# then we tar it up.
ptiidist: $(PTIIDIST)
$(PTIIDIST):
	@echo "#"
	@echo "# Now creating PTIIDIST: $@"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	mkdir -p $@
	#@echo "Be sure that ptolemy/media/javasound/javasound.jar exists"
	#(cd $(PTIIHOME)/ptolemy/media/javasound; make jars)
	#@echo "Be sure that ptolemy/actor/lib/javasound/javasound.jar exists"
	#(cd $(PTIIHOME)/ptolemy/actor/lib/javasound; make jars)
	(cd $(PTIIHOME); \
		rm -f build.xml; \
		$(TAR) -c --no-anchored \
			--exclude="CVS" \
			--exclude=",svn" \
			--exclude="*codeDoc*" \
			-X $(GENDIR)/src.ex \
			-X $(GENDIR)/src1.ex \
		$ -f - $(PTII_TOPDIR) )| \
		(cd $@; $(TAR) -xvpf -)
	@echo "Removing codeDoc directories and other documentation"
	#@echo "  tar -X does not seem to work under Cygwin"
	ls -l $(PTIIDIST)/configure $(PTIIDIST)/config
	rm -rf $(PTIIDIST)/doc/codeDoc* \
		$(PTIIDIST)/config/*.jar \
		$(PTIIDIST)/config/*.class
	ls -l $(PTIIDIST)/configure $(PTIIDIST)/config
	-(cd $(PTIIDIST); $(FINDCLEAN))
	(cd $(PTIIDIST); rm -rf `cat $(GENDIR)/src.ex`)
	#
	# Set up vendors directory for making docs and jar_dist
	mkdir -p $(PTIIDIST)/vendors/misc/rxtx/
	cp $(PTIIHOME)/vendors/misc/rxtx/RXTXcomm.jar $(PTIIDIST)/vendors/misc/rxtx/
	mkdir -p $(PTIIDIST)/vendors/misc/joystick/
	cp $(PTIIHOME)/vendors/misc/joystick/Joystick.jar $(PTIIDIST)/vendors/misc/joystick/
	# Tools used to build installers.
	ln -s $(PTIIHOME)/vendors/izpack $(PTIIDIST)/vendors/izpack
	ln -s $(PTIIHOME)/vendors/launch4j $(PTIIDIST)/vendors/launch4j
	#
	# Checking to be sure we can build
	#(cd $(PTIIDIST); PTII=`pwd` ./configure; ant)
	##(cd $(PTIIDIST)/bin; make )
	#@echo "Do make realclean to clean up any trash"
	#cd $(PTIIDIST); make clean
	#cd $(PTIIDIST); make
	#cd $(PTIIDIST); make javadocs
	rm -f $(PTIIDIST)/.glimpse*
	cp $(PTIIHOME)/.glimpse_exclude $(PTIIDIST)
	ls -l $(PTIIDIST)/configure $(PTIIDIST)/config
	# Copy libjvm.dll.a because it was removed by src.ex
	#cp $(PTIIHOME)/ptolemy/codegen/c/lib/win/libjvm.dll.a $(PTIIDIST)/ptolemy/codegen/c/lib/win
	#ls -l  $(PTIIDIST)/ptolemy/codegen/c/lib/win/libjvm.dll.a
	touch $(PTIIDIST)/configure
	# Copy the vergil script for use in the full tar file
	#(cd $(PTIIHOME)/bin; make)
	#rm -f $(PTIIDIST)/bin/vergil
	#cp $(PTIIHOME)/bin/vergil $(PTIIDIST)/bin
	# Bump up the mod time of $PTII/mk/ptII.mk.in so that if
	# the user runs make, then configure will run 
	touch $(PTIIDIST)/mk/ptII.mk.in
	#
	# doc
	#
	# Fix up doc/img/makefile
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd" \
		$(PTIIDIST)/doc/img/makefile \
		> $(PTIIDIST)/doc/img/makefile_tmp
	mv $(PTIIDIST)/doc/img/makefile_tmp \
		$(PTIIDIST)/doc/img/makefile
	# Fix up doc/makefile
	egrep -v "coroutine|giotto.cgc|metroII|openmodelica" \
		< $(PTIIDIST)/doc/makefile \
		> $(PTIIDIST)/doc/makefile_tmp
	mv $(PTIIDIST)/doc/makefile_tmp \
		$(PTIIDIST)/doc/makefile
	#
	# jars.xml
	# 
	# Remove metroII and openmodelica from jars.xml
	egrep -v "^    <include name=.*(metroII|openmodelica|/src)" \
		< $(PTIIDIST)/jars.xml \
		> $(PTIIDIST)/jars.xml_tmp
	mv $(PTIIDIST)/jars.xml_tmp \
		$(PTIIDIST)/jars.xml
	#
	# ptolemy/configs
	#
	# Remove apps/hlacerti from cosimulation.xml
	egrep -v "apps/hlacerti" \
		< $(PTIIDIST)/ptolemy/configs/cosimulation.xml \
		> $(PTIIDIST)/ptolemy/configs/cosimulation.xml_tmp
	mv $(PTIIDIST)/ptolemy/configs/cosimulation.xml_tmp \
		$(PTIIDIST)/ptolemy/configs/cosimulation.xml
	# Remove apps.xml, corba.xml and mail.xml from the configuration
	egrep -v "apps.xml|corba.xml|mail.xml|metroII.xml|openmodelica|scrEffigy|scrTableau|scr.xml" \
		< $(PTIIDIST)/ptolemy/configs/defaultFullConfiguration.xml \
		> $(PTIIDIST)/ptolemy/configs/defaultFullConfiguration.xml_tmp
	sed 's/, &quot;ptolemy.vergil.basic.imprt.fmu.ImportFMUAsJavaFMUAction&quot;//' \
		$(PTIIDIST)/ptolemy/configs/defaultFullConfiguration.xml_tmp \
		> $(PTIIDIST)/ptolemy/configs/defaultFullConfiguration.xml
	# Fix up experimentalDirector.xml
	egrep -v "openmodelica" \
		< $(PTIIDIST)/ptolemy/configs/experimentalDirectors.xml \
		> $(PTIIDIST)/ptolemy/configs/experimentalDirectors.xml_tmp
	mv $(PTIIDIST)/ptolemy/configs/experimentalDirectors.xml_tmp \
		$(PTIIDIST)/ptolemy/configs/experimentalDirectors.xml
	# Remove things that should not be shipped from completeDemos.htm
	awk -f $(GENDIR)/dontship.awk $(PTIIDIST)/ptolemy/configs/doc/completeDemos.htm \
		> $(PTIIDIST)/ptolemy/configs/doc/completeDemos_tmp.htm
	mv $(PTIIDIST)/ptolemy/configs/doc/completeDemos_tmp.htm \
		$(PTIIDIST)/ptolemy/configs/doc/completeDemos.htm
	# Remove metroII and openmodelica from models.txt
	egrep -v "metroII|openmodelica" \
		< $(PTIIDIST)/ptolemy/configs/doc/models.txt \
		> $(PTIIDIST)/ptolemy/configs/doc/models.txt_tmp
	mv $(PTIIDIST)/ptolemy/configs/doc/models.txt_tmp \
		$(PTIIDIST)/ptolemy/configs/doc/models.txt
	#
	# ptolemy/domains
	#
	# Remove coroutine, metroII and openmodelica from domains/makefile
	egrep -v "coroutine|metroII|openmodelica" \
		< $(PTIIDIST)/ptolemy/domains/makefile \
		> $(PTIIDIST)/ptolemy/domains/makefile_tmp
	mv $(PTIIDIST)/ptolemy/domains/makefile_tmp \
		$(PTIIDIST)/ptolemy/domains/makefile
	# Fix up ptolemy/domains/csp/demo/DiningPhilosophers/makefile
	egrep -v checkDeadlock \
		$(PTIIDIST)/ptolemy/domains/csp/demo/DiningPhilosophers/makefile \
		> $(PTIIDIST)/ptolemy/domains/csp/demo/DiningPhilosophers/makefile.tmp
	mv $(PTIIDIST)/ptolemy/domains/csp/demo/DiningPhilosophers/makefile.tmp \
		$(PTIIDIST)/ptolemy/domains/csp/demo/DiningPhilosophers/makefile
	# Fix up dde/kernel/test/makefile
	egrep -v DDEDirector.tcl \
		$(PTIIDIST)/ptolemy/domains/dde/kernel/test/makefile \
		> $(PTIIDIST)/ptolemy/domains/dde/kernel/test/makefile.tmp
	mv $(PTIIDIST)/ptolemy/domains/dde/kernel/test/makefile.tmp \
		$(PTIIDIST)/ptolemy/domains/dde/kernel/test/makefile
	# Fix up giotto/makefile
	egrep -v cgc \
		$(PTIIDIST)/ptolemy/domains/giotto/makefile \
		> $(PTIIDIST)/ptolemy/domains/giotto/makefile.tmp
	mv $(PTIIDIST)/ptolemy/domains/giotto/makefile.tmp \
		$(PTIIDIST)/ptolemy/domains/giotto/makefile
	# Fix up ptolemy/domains/giotto/demo
	egrep -v Legiotto \
		$(PTIIDIST)/ptolemy/domains/giotto/demo/makefile \
		> $(PTIIDIST)/ptolemy/domains/giotto/demo/makefile.tmp
	mv $(PTIIDIST)/ptolemy/domains/giotto/demo/makefile.tmp \
		$(PTIIDIST)/ptolemy/domains/giotto/demo/makefile
	# Exclude quicktime from domains/gr/gr.xml
	egrep -v quicktime.xml \
		$(PTIIDIST)/ptolemy/domains/gr/gr.xml \
		> $(PTIIDIST)/ptolemy/domains/gr/gr.xml_tmp
	mv $(PTIIDIST)/ptolemy/domains/gr/gr.xml_tmp \
		$(PTIIDIST)/ptolemy/domains/gr/gr.xml
	# Fix up tm/makefile
	egrep -v runtime \
		$(PTIIDIST)/ptolemy/domains/tm/makefile \
		> $(PTIIDIST)/ptolemy/domains/tm/makefile.tmp
	mv $(PTIIDIST)/ptolemy/domains/tm/makefile.tmp \
		$(PTIIDIST)/ptolemy/domains/tm/makefile
	# Fix up ptolemy/domains/wireless/demo
	egrep -v Network \
		$(PTIIDIST)/ptolemy/domains/wireless/demo/makefile \
		> $(PTIIDIST)/ptolemy/domains/wireless/demo/makefile.tmp
	mv $(PTIIDIST)/ptolemy/domains/wireless/demo/makefile.tmp \
		$(PTIIDIST)/ptolemy/domains/wireless/demo/makefile
	#
	# ptolemy/vergil
	#
	# Remove ImportFMUAsJavaFMUAction.java from the makefile
	egrep -v "ImportFMUAsJavaFMUAction.java" \
		< $(PTIIDIST)/ptolemy/vergil/basic/imprt/fmu/makefile \
		> $(PTIIDIST)/ptolemy/vergil/basic/imprt/fmu/makefile_tmp
	mv $(PTIIDIST)/ptolemy/vergil/basic/imprt/fmu/makefile_tmp \
		$(PTIIDIST)/ptolemy/vergil/basic/imprt/fmu/makefile
	# Remove the html generated files, the filenames are too long
	(cd $(PTIIDIST); rm -rf `(cd $(PTIIHOME); svn status | egrep '\?' | grep /demo/ | awk '{print $$2}')` )
	# Rebuild jar files after removing unwanted code.
	# Rebuild jar_dist so that the Windows installer gets the new jars)
	rm -rf $(PTIIDIST)/jar_dist
	$(MAKE) $(PTIIDIST)/jar_dist
	@echo "Regenerate the documentation with comm, gr, JAI, JMF etc."
	(cd $(PTIIDIST)/doc; make JAVADOC=$(JAVADOC) all_packages)


dist_jar_dist: $(PTIIDIST)/jar_dist
$(PTIIDIST)/jar_dist:
	if [ ! -d "$@" ] ; then \
	    echo "Running $(MAKE) -k  jar_dist and other commands in $(PTIIDIST)"; \
	    (cd $(PTIIDIST); PTII=`pwd` JAVADOC=$(JAVADOC) ./configure; PTII=`pwd`; ant; \
		(cd $(PTIIDIST)/doc; PTII=`pwd` make JAVADOC=$(JAVADOC) all_packages); \
		ant jars; PTII=`pwd` $(MAKE) -k jar_dist); \
	fi


clean_src:
	rm -f $(PTII_FULL)
src: $(PTII_FULL) #$(PTII_FULL_ZIP)
PTII_FULL: $(PTII_FULL) #$(PTII_FULL_ZIP)
$(PTII_FULL): $(PTIIDIST)
	@echo "#"
	@echo "# Now creating PTII_FULL: $@"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(PTIIDIST); chmod -R a+rX .)
	@echo "For some reason under Cygwin, we need to build the compressed"
	@echo "  tar file or we get errors"
	(cd $(PTIIDIST)/..; \
		rm -f $(PTIIDIST)/build.xml build.xml; \
		$(TAR) -c -v --no-anchored -X $(GENDIR)/src.ex \
		 -f $@ \
		ptII$(VERSION))
	#(cd $(PTIIDIST)/..; \
	#$(TAR) -c -X $(GENDIR)/src.ex \
	# -f $(basename $@) \
	#ptII$(VERSION))
	#@echo "We will provide gzipped and non-gzipped tars"
	#cp $(basename $@) $(basename $@).bak
	#$(COMPRESS) $(basename $@)
	#mv $(basename $@).bak $(basename $@)
	#chmod g+w $@ $(basename $@)
	#-chgrp ptolemy $@ $(basename $@)
	#ls -l $@ $(basename $@)

$(PTII_FULL_SPLIT):: $(PTII_FULL)
	$(SPLITTAR) $^

src.zip: $(PTII_FULL_ZIP)
ptii.src.zip: $(PTII_FULL_ZIP)
$(PTII_FULL_ZIP): $(PTIIDIST)
	@echo "Creating $@"
	(cd $(PTIIDIST)/..; \
	$(ZIP) -r -q $@ \
	ptII$(VERSION) \
	)
	chmod g+w $@
	chgrp ptolemy $@
	ls -l $@

VERSION_FILES = \
	src/ptII$(VERSION)/ptolemy/configs/*/* \
	src/ptII$(VERSION)/ptolemy/kernel/attributes/VersionAttribute.java

updateversions_test:
	./updateversions $(VERSION) $(VERSION)_`date +"%Y%m%d"` $(VERSION_FILES) 

ptii_src: $(PTII_SRC) $(PTII_SRC_JAR)
$(PTII_SRC): $(PTII_FULL)
	@echo "#"
	@echo "# Now creating PTII_SRC: $@"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	rm -rf src/ptII$(VERSION)
	-mkdir src
	(cd src; \
		$(TAR) -xvf $(PTII_FULL); \
		cd ptII$(VERSION); \
		echo "About to configure `pwd`"; \
		PTII=`$(GENDIR)/ptcygpwd`  ./configure; make -k clean; \
		touch mk/ptII.mk.in; \
		echo "(0)About to remove class files in `pwd`"; \
		find . -name "*.class" -ls -exec rm {} \; ;\
		cd ptolemy; \
		echo "(1)About to remove jars in `pwd`"; \
		find . \( -name ptinyos -o -name jini -o -name extractJarFileTest.jar -o -name antlr.jar -o -name "ojdbc5.jar" -o -name "ojdbc6.jar" -o -name "mysql-connector-java-5.1.6-bin.jar" -o -name PDFSample.jar \) -prune -o -name "*.jar" -ls -exec rm {} \; ;\
		cd ..; \
		cd ptolemy/domains/ptinyos; \
		echo "(2)About to remove jars in `pwd`"; \
		find . -name jdom.jar -prune -o -name nesc.jar -prune -o -name "*.jar" -ls -exec rm {} \; ;\
		cd ../../../doc; \
		echo "(3)About to remove jars in `pwd`"; \
		find . -name "*.jar" -ls -exec rm {} \; ;\
		cd ..; \
		echo "(4)About to remove files named core `pwd`"; \
		find . -type f -name core-ls -exec rm {} \; ;\
		rm -rf doc/codeDoc*; \
		rm -rf `cat $(GENDIR)/src.ex`; \
		echo "About to remove unneeded sources from `pwd`"; \
		rm -rf $(PTII_SRC_TO_BE_REMOVED); \
		ls $(PTII_SRC_TO_BE_REMOVED); \
		$(FINDCLEAN); \
		cd ..;)
	# Fix up doc/img/makefile
	#egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd" \
	#	src/ptII$(VERSION)/doc/img/makefile \
	#	> src/ptII$(VERSION)/doc/img/makefile.tmp
	#mv src/ptII$(VERSION)/doc/img/makefile.tmp \
	#	src/ptII$(VERSION)/doc/img/makefile
	# Fix up dde/kernel/test/makefile
	#egrep -v DDEDirector.tcl \
	#	src/ptII$(VERSION)/ptolemy/domains/dde/kernel/test/makefile \
	#	> src/ptII$(VERSION)/ptolemy/domains/dde/kernel/test/makefile.tmp
	#mv src/ptII$(VERSION)/ptolemy/domains/dde/kernel/test/makefile.tmp \
	#	src/ptII$(VERSION)/ptolemy/domains/dde/kernel/test/makefile
	# # Fix up giotto/makefile
	# egrep -v cgc \
	# 	src/ptII$(VERSION)/ptolemy/domains/giotto/makefile \
	# 	> src/ptII$(VERSION)/ptolemy/domains/giotto/makefile.tmp
	# mv src/ptII$(VERSION)/ptolemy/domains/giotto/makefile.tmp \
	# 	src/ptII$(VERSION)/ptolemy/domains/giotto/makefile
	# # Fix up tm/makefile
	# egrep -v runtime \
	# 	src/ptII$(VERSION)/ptolemy/domains/tm/makefile \
	# 	> src/ptII$(VERSION)/ptolemy/domains/tm/makefile.tmp
	# mv src/ptII$(VERSION)/ptolemy/domains/tm/makefile.tmp \
	# 	src/ptII$(VERSION)/ptolemy/domains/tm/makefile
	# Copy libjvm.dll.a because it was removed by src.ex
	#cp $(PTIIHOME)/ptolemy/codegen/c/lib/win/libjvm.dll.a \
	#       src/ptII$(VERSION)/ptolemy/codegen/c/lib/win/
	#ls -l  		src/ptII$(VERSION)/ptolemy/codegen/c/lib/win/
	# Update the versions
	./updateversions $(VERSION) $(VERSION)_`date +"%Y%m%d"` $(VERSION_FILES) 
	(cd src; \
		echo "#  Starting tar of $@"; \
		rm -f ptII$(VERSION)/build.xml; \
		$(TAR) -c -v --no-anchored -X $(GENDIR)/src.ex --exclude bin/vergil -f $(PTII_SRC) ptII$(VERSION); \
		);

###############
# Build a jar file for use inside the installer
# Create a jar file for use in the installer.
# 1. The files that are jar'd do not include the large pdf docs, since
# the installer already includes those
# 2. The top directory of the jar file is not ptII$(VERSION), instead
# it is _inside_ ptII$(VERSION) so that when the installer unjars it
# the files come out in the right location


# Files to be removed from all source distributions
PTII_SRC_TO_BE_REMOVED = \
		META-INF \
		bin/ptcg.bat \
		bin/comm.policy \
		bin/convertToLazy \
		bin/convertToLazy.bat \
		bin/copernicus.bat \
		bin/decompile.bat \
		bin/emachine.bat \
		bin/giottoc.bat \
		bin/histogram.bat \
		bin/jode.bat \
		bin/mescal.bat \
		bin/moml.bat \
		bin/obfuscate.bat \
		bin/ptcg \
		bin/ptexecute.bat \
		bin/ptjacl.bat \
		bin/ptjaclapp.bat \
		bin/ptjg \
		bin/ptjg.bat \
		bin/ptolemy.bat \
		bin/ptinvoke \
		bin/ptplot.bat \
		bin/ptstartjini.bat \
		bin/pxgraph.bat \
		bin/soot.bat \
		bin/startjini.gui.bat \
		bin/tinic.bat \
		bin/tinicomm.bat \
		bin/tiniconverter.bat \
		bin/vergil.bat \
		config/jarTest.jar \
		diva/build.xml \
		diva/canvas/test/alljsimpletests.tcl \
		diva/canvas/tutorial/doc-files \
		diva/graph/test/alljsimpletests.tcl \
		diva/util/test/alljsimpletests.tcl \
		doc/img/banner.psd \
		doc/img/ptIIbanner2.psd \
		doc/img/PtolemyIICD.ec3 \
		doc/img/PtolemyIISplash.psd \
		doc/design/hyvisual.jar \
		doc/design/visualsense.jar \
		jar_dist \
		lib/cachedir \
		lib/ptII.properties \
		lib/ptvr.jar \
		mk/ptII.mk \
		ptolemy/actor/gui/style/test/alljsimpletests.tcl \
		ptolemy/actor/gui/test/alljsimpletests.tcl \
		ptolemy/actor/lib/security/demo/Signature/ptKeystore \
		ptolemy/actor/lib/security/test/foo.keystore \
		ptolemy/actor/lib/test/Const.txt \
		ptolemy/actor/lib/test/ReadFile1Output.txt \
		ptolemy/component \
		ptolemy/domains/coroutine \
		ptolemy/domains/fmi \
		ptolemy/vergil/basic/imprt/fmu/ImportFMUAsJavaFMUAction.java \
		ptolemy/domains/giotto/cgc \
		ptolemy/domains/gr/demo/Demo1 \
		ptolemy/domains/gr/lib/vr \
		ptolemy/domains/hs \
		ptolemy/domains/metroII \
		ptolemy/domains/openmodelica \
		ptolemy/matlab/ptmatlab.exp \
		ptolemy/matlab/ptmatlab.lib \
		ptolemy/matlab/ptmatlab.obj \
		ptolemy/media/test/tmp2_7.au \
		ptolemy/moml/jxta/demo/EPtolemyA/EPtolemy.properties \
		ptolemy/moml/jxta/JXTAClassLoader.class \
		ptolemy/moml/jxta/JXTALibrary.class \
	        ptolemy/plot/ptplotlocales \
		ptolemy/util/test/FileUtilities-1.1.tmp \
		ptolemy/vergil/toolbox/test/alljsimpletests.tcl \
		signed


BASE_SRC_TO_BE_REMOVED = \
		doc/usingVergil \
		lib/chic.jar \
		lib/colt.jar \
		lib/jasminclasses.jar \
		lib/java_cup.jar \
		lib/mapss.jar \
		lib/ptCal.jar \
		lib/saxon.jar \
		lib/sootclasses.jar \
		lib/synthesis.jar \
		ptolemy/actor/lib/conversions/test/auto/Round.xml \
		ptolemy/actor/lib/ecsl \
		ptolemy/actor/lib/io \
		ptolemy/actor/lib/io/comm \
		ptolemy/actor/lib/jai \
		ptolemy/actor/lib/jmf \
		ptolemy/actor/lib/joystick \
		ptolemy/actor/lib/test/OrderedMerge.tcl \
		ptolemy/actor/lib/test/auto/MathFunction.xml \
		ptolemy/actor/lib/test/auto/OrderedMerge.xml \
	        ptolemy/actor/lib/test/auto/Switch.xml \
		ptolemy/actor/lib/x10 \
		ptolemy/build.xml \
		ptolemy/caltrop \
		ptolemy/component \
		ptolemy/configs/dsp \
		ptolemy/configs/full \
		ptolemy/configs/jxta \
		ptolemy/configs/SingleWindow \
		ptolemy/configs/copernicus \
		ptolemy/copernicus \
		ptolemy/domains/ci \
		ptolemy/domains/dde \
		ptolemy/domains/ddf \
		ptolemy/domains/domains.xml \
		ptolemy/domains/dt \
		ptolemy/domains/ddf/test/auto/controlled_kahnnet.xml \
		ptolemy/domains/giotto \
		ptolemy/domains/gr \
		ptolemy/domains/hde \
		ptolemy/domains/hdf \
		ptolemy/domains/petrinet \
		ptolemy/domains/psdf \
		ptolemy/domains/sdf/test/auto/sdf_inside_pn.xml \
		ptolemy/domains/sr \
		ptolemy/domains/test \
		ptolemy/domains/tm \
		ptolemy/demo \
		ptolemy/moml/jxta/demo/EPtolemyA/EPtolemy.properties \
		ptolemy/plot/build.xml \
		ptolemy/plot/plotml/build.xml \
		thales

# Files to be removed from HyVisual source distributions
HYVISUAL_SRC_TO_BE_REMOVED = \
		$(BASE_SRC_TO_BE_REMOVED) \
		doc/design/usingVergil \
		lib/jython.jar \
		ptolemy/actor/lib/colt \
		ptolemy/actor/lib/comm \
		ptolemy/actor/lib/javasound \
		ptolemy/actor/lib/python \
		ptolemy/actor/lib/security \
		ptolemy/actor/lib/xslt \
		ptolemy/actor/ptalon \
		ptolemy/configs/ptiny \
		ptolemy/domains/csp \
		ptolemy/domains/pn \
		ptolemy/domains/ptinyDomains.xml \
		ptolemy/domains/ptinyos \
		ptolemy/domains/wireless \
		ptolemy/configs/visualsense

# Files to be removed from Viptos source distributions
VIPTOS_SRC_TO_BE_REMOVED = \
		$(BASE_SRC_TO_BE_REMOVED) \
		bin/hyvisual \
		doc/codeDocHyVisual \
		doc/codeDocHyVisual.jar \
		doc/codeDocVisualSense \
		doc/codeDocVisualSense.jar \
		ptolemy/actor/TypedCompositeActorWithCoSimulation.java \
		ptolemy/actor/ptalon \
		ptolemy/configs/hyvisual \
		ptolemy/domains/ptinyDomains.xml

# Location of the jar binary.  Cygwin /bin/jar is broken.
JAR=c:/Program\ Files/Java/jdk1.5.0_14/bin/jar

# The Jar file that we create.
src.jar: $(PTII_SRC_JAR)
$(PTII_SRC_JAR): src_jar
	(cd src_jar/ptII$(VERSION); $(JAR) -cf ../../ptII$(VERSION).src.jar .)

# The directory we jar up.
src_jar: #$(PTII_SRC) 
	mkdir src_jar;
	(cd src_jar; $(TAR) -xvf $(PTII_SRC))
	rm -f src_jar/ptII$(VERSION)/doc/design/hyvisual.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/ptIIdesign1-intro.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/ptIIdesign2-software.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/ptIIdesign3-domains.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/visualsense.pdf
	(cd src_jar/ptII$(VERSION); $(FINDCLEAN))
	(cd src_jar/ptII$(VERSION); rm -rf `cat ../../src.ex`)
	echo "About to remove unneeded sources from `pwd`"; \
	(cd src_jar/ptII$(VERSION); rm -rf $(PTII_SRC_TO_BE_REMOVED))

############################################
# HyVisual

HYVISUAL_SRC_JAR = hyvisual$(VERSION).src.jar

# The Jar file that we create.
hyvisual.jar: $(HYVISUAL_SRC_JAR)
$(HYVISUAL_SRC_JAR): src_jar/hyvisual$(VERSION)
	(cd src_jar/hyvisual$(VERSION); $(JAR) -cf ../../hyvisual$(VERSION).src.jar .)

HYVISUAL_SRC_TAR = hyvisual$(VERSION).src.tar.gz
# The Jar file that we create.
hyvisual.tar: $(HYVISUAL_SRC_TAR)
$(HYVISUAL_SRC_TAR): src_jar/hyvisual$(VERSION)
	(cd src_jar; \
		cp $(PTIIHOME)/doc/design/hyvisual.pdf \
			hyvisual$(VERSION)/doc/design/hyvisual.pdf; \
		$(TAR) -czf ../hyvisual$(VERSION).src.tar.gz \
		hyvisual$(VERSION) \
	)

# The directory we jar up.
# We include DE because some of the kernel/util tests use it.  CT demos also.
hyvisual_src_jar: src_jar/hyvisual$(VERSION)
src_jar/hyvisual$(VERSION): ptII$(VERSION).src.jar
	if [ ! -d src_jar/hyvisual$(VERSION) ]; then \
		 mkdir -p src_jar/hyvisual$(VERSION); \
	fi
	(cd src_jar/hyvisual$(VERSION); \
		$(JAR) -xf ../../ptII$(VERSION).src.jar)

	(cd src_jar/hyvisual$(VERSION); \
		echo "About to remove unneeded sources from `pwd`"; \
		rm -rf $(PTII_SRC_TO_BE_REMOVED); \
		rm -rf $(HYVISUAL_SRC_TO_BE_REMOVED) thales; \
	   touch configure; \
	   chmod a+rx configure util/testsuite/chkextra; \
	)
	egrep -v "copernicus|decompile|emachine|giottoc|htmlconverter|jode|mescal|obfuscate|soot|tinic|startjini.gui" src_jar/hyvisual$(VERSION)/bin/makefile \
		> src_jar/hyvisual$(VERSION)/bin/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/bin/makefile.tmp \
		src_jar/hyvisual$(VERSION)/bin/makefile
	# Fix up actor/lib/makefile
	egrep -v "comm.jar|javasound.jar|security.jar|xslt.jar" \
		src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/makefile
	# Fix up domains/makefile
	egrep -v "csp.jar|ddf.jar|pn.jar|domains.xml|ptinyDomains.xml|ci.jar|dde.jar|dt/dt.jar|giotto.jar|petrinet.jar|tm.jar|sr.jar|wireless.jar"	\
		src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile
	# Fix up configs/makefile
	egrep -v "dsp.jar|full.jar|jxta.jar|ptiny.jar|visualsense.jar" \
		src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile
	# Fix up doc/design/makefile
	egrep -v "ptIIdesign1-intro.pdf|ptIIdesign2-software.pdf|ptIIdesign3-domains.pdf|visualsense.pdf|visualsense.jar|usingVergil" \
		src_jar/hyvisual$(VERSION)/doc/design/makefile \
		> src_jar/hyvisual$(VERSION)/doc/design/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/doc/design/makefile.tmp \
		src_jar/hyvisual$(VERSION)/doc/design/makefile
	# Fix up doc/makefile
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd|lib/comm|lib.comm|python|security|ci/kernel|ci/lib|copernicus|csp|dde|ddf|dt.kernel|giotto|gr.kernel|gr.lib|io.comm|javasound|lib.xslt|lib.xslt|pn/kernel|psdf|petrinet|ptolemy.caltrop|ptolemy.domains.ci|ptolemy.domains.pn|sr.kernel|sr/kernel|sr.lib|sr/lib|thales.actor|thales.vergil|tm.kernel|tm/kernel|tm.lib|tm/lib|wireless" \
		src_jar/hyvisual$(VERSION)/doc/makefile \
		> src_jar/hyvisual$(VERSION)/doc/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/doc/makefile.tmp \
		src_jar/hyvisual$(VERSION)/doc/makefile
	# Fix up actor/lib/test/makefile
	egrep -v OrderedMerge.tcl \
		src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/test/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/test/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/test/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/actor/lib/test/makefile
	# Fix up ptolemy/makefile
	sed -e 's/\$$(PTCALTROP_DIR)//' \
	    -e 's/\$$(PTCOPERNICUS_DIR)//' \
		< src_jar/hyvisual$(VERSION)/ptolemy/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/makefile
	# Fix up makefile
	egrep -v "actor/lib/javasound/demo/demo.jar|domains/dt/demo/demo.jar|domains/giotto/demo/demo.jar|domains/pn/demo/demo.jar|domains/tm/demo/demo.jar|thales" \
		src_jar/hyvisual$(VERSION)/makefile \
		> src_jar/hyvisual$(VERSION)/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/makefile.tmp \
		src_jar/hyvisual$(VERSION)/makefile \
	#cd src_jar/hyvisual$(VERSION); $(FINDCLEAN))
	(cd src_jar/hyvisual$(VERSION); rm -rf `cat ../../src.ex`)

###############
# Viptos
VIPTOS_VERSION=1.1.devel
VIPTOS_SRC_JAR = viptos$(VIPTOS_VERSION).src.jar

# The Jar file that we create.
viptos.jar: $(VIPTOS_SRC_JAR)
$(VIPTOS_SRC_JAR): src_jar/viptos$(VIPTOS_VERSION)
	(cd src_jar/viptos$(VIPTOS_VERSION); $(JAR) -cf ../../viptos$(VIPTOS_VERSION).src.jar .)

VIPTOS_SRC_TAR = viptos$(VIPTOS_VERSION).src.tar.gz
# The Jar file that we create.
viptos: $(VIPTOS_SRC_TAR)
viptos.tar: $(VIPTOS_SRC_TAR)
$(VIPTOS_SRC_TAR): src_jar/viptos$(VIPTOS_VERSION)
	(cd src_jar; \
		$(TAR) -cvzf ../viptos$(VIPTOS_VERSION).src.tar.gz \
		viptos$(VIPTOS_VERSION) \
	)
	#cp $(PTIIHOME)/doc/design/viptos.pdf \
	#	viptos$(VIPTOS_VERSION)/doc/design/viptos.pdf; \


# The directory we jar up.
# We include DE because some of the kernel/util tests use it.  CT demos also.
viptos_src_jar: src_jar/viptos$(VIPTOS_VERSION)
src_jar/viptos$(VIPTOS_VERSION): $(PTII_SRC_JAR)
	if [ ! -d src_jar/viptos$(VIPTOS_VERSION) ]; then \
		 mkdir -p src_jar/viptos$(VIPTOS_VERSION); \
	fi
	(cd src_jar/viptos$(VIPTOS_VERSION); \
		$(JAR) -xf ../../ptII$(VERSION).src.jar)
	(cd src_jar/viptos$(VIPTOS_VERSION); \
		echo "About to remove unneeded sources from `pwd`"; \
		rm -rf $(PTII_SRC_TO_BE_REMOVED); \
		rm -rf $(VIPTOS_SRC_TO_BE_REMOVED) thales; \
	   touch configure; \
	   chmod a+rx configure util/testsuite/chkextra; \
	)
	# README_VIPTOS.txt
	echo "Viptos $(VIPTOS_VERSION) is an interface between TinyOS and Ptolemy II" \
		> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "To get started, you must have the AVR tools, TinyOS and Nesc installed" \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "and some environment variables must be set." \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "For details, see ptolemy/domains/ptinyos/doc/installation.htm." \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "The quick installation guide:" \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "1. Install the AVR tools, TinyOS, Nesc" \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "   as per ptolemy/domains/ptinyos/doc/installation.htm." \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "2. Set \$$PTII to this directory.  See doc/install.htm for details." \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "3. Run     ./configure" \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "4. Run     make fast" \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	echo "5. Run     bin/viptos" \
		>> src_jar/viptos$(VIPTOS_VERSION)/README_VIPTOS.txt
	# Fix up bin/makefile
	egrep -v "copernicus|decompile|emachine|hyvisual|giottoc|htmlconverter|jode|mescal|obfuscate|soot|tinic|startjini.gui" src_jar/viptos$(VIPTOS_VERSION)/bin/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/bin/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/bin/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/bin/makefile
	# Fix up actor/makefile
	egrep -v "TypedCompositeActorWithCoSimulation.java" \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/makefile
	# Fix up actor/lib/makefile
	egrep -v "comm.jar|javasound.jar|security.jar|xslt.jar" \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/makefile
	# Fix up domains/makefile
	egrep -v "csp.jar|ddf.jar|pn.jar|domains.xml|ptinyDomains.xml|ci.jar|dde.jar|dt/dt.jar|giotto.jar|hdf/hdf.jar|petrinet.jar|tm.jar|sr.jar|wireless.jar"	\
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/domains/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/ptolemy/domains/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/ptolemy/domains/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/domains/makefile
	# Fix up configs/makefile
	egrep -v "dsp.jar|full.jar|hyvisual.jar|jxta.jar|ptiny.jar|visualsense.jar" \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/configs/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/ptolemy/configs/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/ptolemy/configs/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/configs/makefile \
	# Fix up doc/design/makefile
	egrep -v "hyvisual.jar|hyvisual.pdf|ptIIdesign1-intro.pdf|ptIIdesign2-software.pdf|ptIIdesign3-domains.pdf|visualsense.pdf|visualsense.jar|usingVergil" \
		src_jar/viptos$(VIPTOS_VERSION)/doc/design/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/doc/design/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/doc/design/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/doc/design/makefile
	# Fix up doc/makefile/img
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd"\
		src_jar/viptos$(VIPTOS_VERSION)/doc/img/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/doc/img/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/doc/img/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/doc/img/makefile
	# Fix up doc/makefile
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd|lib/comm|lib.comm|python|security|ci/kernel|ci/lib|copernicus|csp|dde|ddf|dt.kernel|giotto|gr.kernel|gr.lib|io.comm|javasound|lib.xslt|lib.xslt|pn/kernel|psdf|petrinet|ptolemy.caltrop|ptolemy.domains.ci|ptolemy.domains.pn|sr.kernel|sr/kernel|sr.lib|sr/lib|thales.actor|thales.vergil|tm.kernel|tm/kernel|tm.lib|tm/lib|wireless" \
		src_jar/viptos$(VIPTOS_VERSION)/doc/makefile \
		| sed 's/codeDocHyVisual.jar codeDocViptos.jar codeDocVisualSense.jar//' \
	        | sed 's@^all:.*$$@all: suball whatsnew.htm whatsnewVergil.htm codeDoc/tree.html@' \
		| sed 's@^codeDoc/tree.html:@codeDocFull/tree.html:@' \
		> src_jar/viptos$(VIPTOS_VERSION)/doc/makefile.tmp
        # Add a codeDoc/tree.html rule at the bottom
	echo "##############" \
		>> src_jar/viptos$(VIPTOS_VERSION)/doc/makefile.tmp
	echo "codeDoc/tree.html:" \
		>> src_jar/viptos$(VIPTOS_VERSION)/doc/makefile.tmp
	echo "	if [ ! -d codeDoc ]; then mkdir -p codeDoc; fi" \
		>> src_jar/viptos$(VIPTOS_VERSION)/doc/makefile.tmp
	echo "	-(cd ..; CLASSPATH=\"\$$(CLASSPATH)\" \"\$$(JAVADOC)\" \$$(JDOCFLAGS) -d doc/codeDoc \$$(VIPTOSPACKAGES) \$$(VISUALSENSEPACKAGES) \$$(PTINYPACKAGES))" \
		>> src_jar/viptos$(VIPTOS_VERSION)/doc/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/doc/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/doc/makefile
	# Fix up actor/lib/test/makefile
	egrep -v OrderedMerge.tcl \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/test/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/test/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/test/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/actor/lib/test/makefile
	# Fix up ptolemy/makefile
	sed -e 's/\$$(PTCALTROP_DIR)//' \
	    -e 's/\$$(PTCOPERNICUS_DIR)//' \
		< src_jar/viptos$(VIPTOS_VERSION)/ptolemy/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/ptolemy/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/ptolemy/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/ptolemy/makefile
	# Fix up makefile
	egrep -v "actor/lib/javasound/demo/demo.jar|domains/dt/demo/demo.jar|domains/giotto/demo/demo.jar|domains/pn/demo/demo.jar|domains/tm/demo/demo.jar|thales" \
		src_jar/viptos$(VIPTOS_VERSION)/makefile \
		> src_jar/viptos$(VIPTOS_VERSION)/makefile.tmp
	mv src_jar/viptos$(VIPTOS_VERSION)/makefile.tmp \
		src_jar/viptos$(VIPTOS_VERSION)/makefile \
	#cd src_jar/viptos$(VIPTOS_VERSION); $(FINDCLEAN))
	(cd src_jar/viptos$(VIPTOS_VERSION); rm -rf `cat ../../src.ex`)

##########


update_ftp: update_ptii_ftp untar update_ftp_book update_ftp_index
update_ptii_ftp:
	@echo "Copying ptII files to website"
	scp -p $(PTII_FULL) $(PTII_SRC) \
		moog:$(PTII_FTP)
untar:
	ssh moog rm -rf $(PTII_FTP)/ptII$(VERSION)
	ssh moog "cd $(PTII_FTP); tar -xf ptII$(VERSION).tar$(SUFFIX)"
	-echo "Copy gr.jar and codeDoc in from a windows box"
	ssh moog "cd $(PTII_FTP)/ptII$(VERSION); ln -s ../jnlp-$(VERSION)/signed ."

update_ftp_book: PtolemyII_DigitalV1_02.pdf
	scp PtolemyII_DigitalV1_02.pdf moog:$(PTII_FTP)/ptII$(VERSION)/doc/books/systems/

PtolemyII_DigitalV1_02.pdf:
	wget http://ptolemy.eecs.berkeley.edu/books/Systems/PtolemyII_DigitalV1_02.pdf

update_ftp_index:
	scp index2.htm moog:$(PTII_FTP)/ptII$(VERSION)/index.htm

# Run this to copy the .exe and zip file
update_windows: ptII$(WINDOWS_VERSION)_src.zip
	scp -p $(PTSETUP).exe ptII$(WINDOWS_VERSION)_src.zip moog:$(PTII_FTP)

PTSETUP_MAC=ptII$(VERSION).setup.mac
update_mac: $(PTSETUP_MAC).dmg
	scp -p $(PTSETUP_MAC).dmg moog:$(PTII_FTP)

ptII$(WINDOWS_VERSION)_src.zip:
	@echo "create the .zip file by untaring $(PTII_SRC) and using zip"

# Make the distribution, run htmlchek
htmlchek:
	-(cd $(PTIIHOME); svn update)
	$(MAKE) clean
	$(MAKE)
	(cd $(PTIIDIST); PTII=$(PTIIDIST) make htmlchek) > $(PTIIDIST)/htmlchek.out 2>&1
	(cd $(PTIIDIST); ls -d `cat htmlchekout.HREF | grep -v '#' | grep -v http: | grep -v mailto: | grep -v ftp:` | grep 'not found' 2>&1)

nightly:

# Stuff to do every night.
# ptII's crontab runs ~ptII/adm/bin/ptIInightly, which runs 'make nightly'
# when the remote jobs finish, the remotealldone rule is called, which
# runs the ptIInightly-mail script.
oldnightly: ptclean ptbuild \
	clean all \
	glimpse \
	findchanges \
	ChangeLog \
	jnlp \
	remote4nuke \
	remote5nuke \
	remote4 \
	remote5

#	remote2
#	remote6

####################################
# Rules that operate in ~ptII

# clean in ~ptII
ptclean:
	@echo "#"
	@echo "# Now running make clean in ~ptII "
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(PTIIHOME); \
		$(MAKE) clean)
	# Don't remove codeDoc directories in the reviews directory
	cd $(PTIIHOME); \
		find . -xdev -name reviews -prune -name codeDoc -exec rm -rf {} \;

# build in ~ptII
ptbuild:
	@echo "#"
	@echo "# Now Rebuilding in ~ptII without -depend"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	rm -f /users/$(USER)/logs/java`date  '+%m%d'`
	-(cd $(PTIIHOME); rm configure; svn update; PTII=/users/ptII $(MAKE) JFLAGS="-deprecation -g -deprecation -source 1.5" -k sources fast install)  > /users/$(USER)/logs/java`date  '+%m%d'` 2>&1

# test in ~ptII
pttests:
	(cd $(PTIIHOME); $(MAKE) tests)

# Generate the glimpse file in the master tree
glimpse:
	@echo "#"
	@echo "# Now generating glimpse index in ~ptII "
	@echo "#"
	date
	cd $(PTIIHOME); $(MAKE) glimpse

# Generate a page containing the files changed in the last 10 days
FINDCHANGEFILE = changedfiles.html
findchanges:
	rm -f $(FINDCHANGEFILE)
	$(PTIIADM)/bin/findchanges > $(FINDCHANGEFILE)

ChangeLog:
	@echo "#"
	@echo "# Now generating ChangeLog file in $(PTIIHOME)"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(PTIIHOME); \
	    if [ ! -d adm/dists/.svn ]; then \
		echo "Removing adm/dists because adm/dists/.svn not found"; \
	        rm -rf adm/dists; \
	    fi) 
	(cd $(PTIIHOME); \
	    if [ -d adm/dists ]; then \
		echo "Removing adm/dists because adm/dists/.svn not found"; \
	        rmdir adm/dists; \
	    fi) 
	(cd $(PTIIHOME); rm -f ChangeLog; \
		 $(MAKE) -k ChangeLog; \
		rm -f ChangeLog.txt; ln -s ChangeLog ChangeLog.txt)

# Build jnlp installers
jnlp:
	@echo "#"
	@echo "# Now running make jnlp in $(PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(PTIIDIST);	PTII=$(PTIIDIST) \
		$(MAKE) -k jnlp_dist_nightly)

# Run tests outside of Javascope
nonjsall:
	@echo "#"
	@echo "# Now running make non Javascope tests in $(PTIIHOME)"
	@echo "# on  `hostname`"
	@echo "# To add to the list of directories, edit "
	@echo "# ~ptII/adm/gen-latest/makefile"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	@echo "# Run make in the non javascope core directories"
	@echo "# that need to be built before we build the other non javascope"
	@-if [ "x$(NONJSALLCOREDIRS)" != "x" ]; then \
		set $(NONJSALLCOREDIRS); \
		for x do \
		    if [ -w $(PTIIHOME)/$$x ] ; then \
			( cd $(PTIIHOME)/$$x ; \
			echo making all  in $(PTIIHOME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				all \
			);  \
		    fi ; \
		done ; \
	fi
	-if [ "x$(NONJSALLDIRS)" != "x" ]; then \
		set $(NONJSALLDIRS); \
		for x do \
		    if [ -w $(PTIIHOME)/$$x ] ; then \
			( cd $(PTIIHOME)/$$x ; \
			echo making tests  in $(PTIIHOME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
				tests \
			);  \
		    fi ; \
		done ; \
	fi


# Logdir
GENLOGDIR = $(GENDIR)/logs

# Make the log dir if necessary
$(GENLOGDIR):
	if [ -d $@ ]; then \
		echo "Making $@ directory"; \
		mkdir -p $@; \
	fi

####################################
# Build and generate test case coverage on a remote host

# Use a non-gnu make here for testing purposes
OTHERMAKE = /usr/ccs/bin/make

# Remote location of Ptolemy II tree for testing
# Host to run on
REMOTE1HOST = moog
REMOTE1HOME = /vol/moog/moog2/ptII
REMOTE1PTIIDIST = $(REMOTE1HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE1LOG = $(GENLOGDIR)/remote1.out

#JSHOME = $(HOME)
JSHOME = /users/ptII
# JavaScope database, gets removed every night
# Note that this must be a directory called jsdatabase in the user's
# home directory.  Overriding this is tricky, it requires setting
# up a javascope.properties file that can be read at runtime
JSDATABASE=$(JSHOME)/jsdatabase

# Directory to place output in
JSOUTDIR=$(JSHOME)/public_html/nightly/js

# Directory to place output in
JSOUTDIR=/users/ptII/public_html/nightly/js

# Create a separate tree and run the test suite with code coverage
# on REMOTE1HOST
remote1: remote1coverage
remote1coverage: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE1HOST) for code coverage"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	ssh $(REMOTE1HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		remote1nuke remote1ptiidist remote1configure \
		remote1distclean \
		remote1distjsall remote1distjsrestore remote1distjavadocs \
		remote1done >& $(REMOTE1LOG)" &

remote1local: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE1HOST) for code coverage"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	ssh $(REMOTE1HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		USER=$(USER) PTIIHOME=/tmp/cxh/ptII PTIIADM=/tmp/cxh/ptII/adm \
		TAR=/bin/tar
		remote1nuke remote1ptiidist remote1configure \
		remote1distclean \
		remote1distjsall remote1distjsrestore remote1distjavadocs \
		remote1done >& $(REMOTE1LOG)" &

# Commands run by hudson by hand.  See $PTII/adm/test/Release.tcl and Nightly.tcl and NightlyInstall.tcl for what really gets run.
hudson: 
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup test_setup updateDOPCenterImage update_moog coverity

hudson_mac:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	setup_mac_jar

hudson_notest: 
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup update_moog

hudson_ptiny.exe:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	ptiny.exe

hudson_setup:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup update_moog

hudson_setup_only:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup

hudson_setup_test:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup test_setup update_moog
hudson_start: 
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup test_setup_start

hudson_test_setup:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	test_setup

hudson_jar_dist:
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	dist_jar_dist

hudson_ant: 
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	test_setup_ant

hudson_dist: 
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	clean all jnlp src.jar setup updateDOPCenterImage update_moog 

hudson_update_moog: 
	$(MAKE) -k USER=hudson \
	PTIIHOME=/var/lib/hudson/jobs/ptII/workspace \
	PTIIADM=/var/lib/hudson/jobs/ptII/workspace/adm \
	JAR=/usr/bin/jar \
	update_moog 

# caltrop requires -source 1.4, ptalon -source 1.5
# Move remote1doc to after rebuild so that the Actor Index tool is built.
bldmastrcoverage:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr \
		PTII=/home/bldmastr/ptII \
		PTIIHOME=/home/bldmastr/ptII \
		JFLAGS="-deprecation -g -source 1.5" \
		JSDATABASE=/home/bldmastr/jsdatabase \
		JSOUTDIR=/home/www/cvswww/ptolemy/nightly \
		JSCLASSPATH=/usr/local/JavaScope/JavaScope.jar \
		clean \
		all \
		remote1distclean \
		remote1configure \
		remote1distjsall \
		remote1distjsrestore \
		remote1build \
		remote1distnonjsall \
		remote1ChangeLog \
		remote1doc \
		remote1ojdcheck \
		remote1doccheck \
		remote1findbugs \
		remote1jnlp \
		test_setup \
		updateDOPCenterImage

# Just build a distribution
bldmastr_dist:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr \
		PTII=/home/bldmastr/ptII \
		PTIIHOME=/home/bldmastr/ptII \
		JFLAGS="-deprecation -g -source 1.5" \
		JSDATABASE=/home/bldmastr/jsdatabase \
		JSOUTDIR=/home/www/cvswww/ptolemy/nightly \
		JSCLASSPATH=/usr/local/JavaScope/JavaScope.jar \
		clean \
		all \
		remote1distclean \
		remote1configure \
		remote1build \
		remote1ChangeLog \
		remote1jnlp \
		test_setup



#		remote4
#		remote5

# Remove the remote dist
remote1nuke:
	rm -rf $(REMOTE1PTIIDIST) $(REMOTE1DONEFILE)

# untarthe remote dist.  Usually this is run on the remote machine
remote1ptiidist: $(REMOTE1PTIIDIST)
$(REMOTE1PTIIDIST): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1HOME); $(TAR) -zxf $(PTII_FULL))

remote1configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST);	rm -f config.cache; PTII=$(REMOTE1PTIIDIST) ./configure)

# Clean the remote1 tree
# The line '#ptIInightly-mail#: Starting make clean at `date`'
# is special, the ptIInightly-mail script uses it
remote1distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE1PTIIDIST) on `hostname`"
	@echo "#"
	@echo "#ptIInightly-mail#: Starting make clean at `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST); \
		$(MAKE) clean)
	-(cd $(REMOTE1PTIIDIST); \
		find . -name codeDoc -exec rm -rf {} \;)

remote1distjavadocs:
	@echo "#"
	@echo "# Now generating javadocs in $(PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE1PTIIDIST); $(OTHERMAKE) -k javadocs)

# JSALLDIRS contains the directory that has the java files, not the test dir.
# math first, then kernel, then data, then actor, then alphabetical
# If you edit JSALLDIRS, then you should probably add the directory to
# $PTII/doc/makefile in two places so that we automatically generate docs.
# * Tests in actor/lib use DE, so actor/lib should be after de/lib
# * Tests in actor/lib/auto use vergil and moml
# * de/test should be run after sdf
# * dt/kernel should be run after sdf
# * fsm/kernel should be run after moml
# * vergil should be run after vergil/toolbox
# * We include the following directories so that they will appear in the
# * code coverage stats even though they are graphical: gui actor/gui
# * auto tests should be run after actor/gui so that MoMLSimpleApplication
#   is present

# Don't run
#	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib/huffman
# it hangs
#
JSALLDIRS = \
	$(REMOTE1PTIIDIST)/util/testsuite \
	$(REMOTE1PTIIDIST)/ptolemy/util \
	$(REMOTE1PTIIDIST)/ptolemy/math \
	$(REMOTE1PTIIDIST)/ptolemy/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/kernel/util \
	$(REMOTE1PTIIDIST)/ptolemy/kernel/attributes \
	$(REMOTE1PTIIDIST)/ptolemy/kernel/undo \
	$(REMOTE1PTIIDIST)/ptolemy/data \
	$(REMOTE1PTIIDIST)/ptolemy/data/expr \
	$(REMOTE1PTIIDIST)/ptolemy/data/type \
	$(REMOTE1PTIIDIST)/ptolemy/actor \
	$(REMOTE1PTIIDIST)/ptolemy/actor/gui \
	$(REMOTE1PTIIDIST)/ptolemy/data/unit \
	$(REMOTE1PTIIDIST)/ptolemy/moml/unit \
	$(REMOTE1PTIIDIST)/ptolemy/actor/process \
	$(REMOTE1PTIIDIST)/ptolemy/actor/sched \
	$(REMOTE1PTIIDIST)/ptolemy/actor/util \
	$(REMOTE1PTIIDIST)/ptolemy/actor/corba \
	$(REMOTE1PTIIDIST)/ptolemy/actor/corba/util \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ca/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ca/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ci/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/continuous/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/continuous/kernel/solver \
	$(REMOTE1PTIIDIST)/ptolemy/domains/continuous/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/gr/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/gr/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/jogl/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/jogl/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/modal/kernel/fmv \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/kernel/event \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pthales/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pthales/JNI \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pthales/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptides/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptides/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptinyos/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptinyos/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptinyos/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptinyos/gui \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rendezvous/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rendezvous/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib/vq \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tester/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tester/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ddf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ddf/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dt/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/petrinet/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptera/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptera/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sr/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sr/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sr/gui \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tm/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tm/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless/lib \
	$(REMOTE1PTIIDIST)/ptolemy/graph \
	$(REMOTE1PTIIDIST)/ptolemy/graph/analysis \
	$(REMOTE1PTIIDIST)/ptolemy/graph/analysis/analyzer \
	$(REMOTE1PTIIDIST)/ptolemy/graph/analysis/strategy \
	$(REMOTE1PTIIDIST)/ptolemy/graph/mapping \
	$(REMOTE1PTIIDIST)/ptolemy/graph/sched \
	$(REMOTE1PTIIDIST)/ptolemy/media \
	$(REMOTE1PTIIDIST)/ptolemy/media/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/moml \
	$(REMOTE1PTIIDIST)/ptolemy/domains/giotto/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/modal/modal \
	$(REMOTE1PTIIDIST)/ptolemy/domains/modal/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/modal/kernel/ia \
	$(REMOTE1PTIIDIST)/ptolemy/domains/psdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/hdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/moml/filter \
	$(REMOTE1PTIIDIST)/ptolemy/plot \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/gui \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/basic \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/kernel/attributes \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/toolbox \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/icon \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/tree \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/modal/fmv \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/modal/ia \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/modal/modal \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/modal \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/ontologies \
	$(REMOTE1PTIIDIST)/ptolemy/actor/gt \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib \
	$(REMOTE1PTIIDIST)/ptolemy/actor/parameters \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/colt \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/comm \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/conversions \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/conversions/json \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/fmi \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/hoc \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/io \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/gui \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/logic \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/mail \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/net \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/python \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/qm \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/security \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/string \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/vhdl \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/xslt \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/util \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/xmlparser \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/eclipse/ast \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ci \
	$(REMOTE1PTIIDIST)/ptolemy/domains/continuous \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct \
	$(REMOTE1PTIIDIST)/ptolemy/domains/curriculum \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ddf \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de \
	$(REMOTE1PTIIDIST)/ptolemy/domains/fsm \
	$(REMOTE1PTIIDIST)/ptolemy/domains/modal \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pthales \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptides \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptera \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rendezvous \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/optimize \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/optimize/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sequence \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sr \
	$(REMOTE1PTIIDIST)/ptolemy/domains/taskpt/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/taskpt/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/taskpt/lib/gui \
	$(REMOTE1PTIIDIST)/ptolemy/domains/taskpt \
	$(REMOTE1PTIIDIST)/ptolemy/domains/thales \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tm \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop/actors \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop/ddi \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop/util \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies/lattice/adapters/abstractInterpretation \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies/lattice \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies/demo/CarTracking \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies/demo/ConstAbstractInterpretation \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies/demo/ConstNonconst \
	$(REMOTE1PTIIDIST)/ptolemy/data/ontologies/demo/MonotonicityAnalysis \
	$(REMOTE1PTIIDIST)/ptolemy/distributed/actor/lib \
	$(REMOTE1PTIIDIST)/ptolemy/distributed/domains/sdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/actor/ptalon \
	$(REMOTE1PTIIDIST)/ptolemy/gui \
	$(REMOTE1PTIIDIST)/ptolemy/verification \
	$(REMOTE1PTIIDIST)/lbnl \
	$(REMOTE1PTIIDIST)/lbnl/actor/lib \
	$(REMOTE1PTIIDIST)/diva/util/java2d/ \
	$(REMOTE1PTIIDIST)/doc/books/design/modal \
	$(REMOTE1PTIIDIST)/doc/books/design/time \
	$(REMOTE1PTIIDIST)/doc/books/embedded/concurrent \
	$(REMOTE1PTIIDIST)/doc/books/system/continuous \
	$(REMOTE1PTIIDIST)/doc/books/system/dataflow \
	$(REMOTE1PTIIDIST)/doc/books/system/discreteevents \
	$(REMOTE1PTIIDIST)/doc/books/system/expressions \
	$(REMOTE1PTIIDIST)/doc/books/system/intro \
	$(REMOTE1PTIIDIST)/doc/books/system/modal \
	$(REMOTE1PTIIDIST)/doc/books/system/plotter \
	$(REMOTE1PTIIDIST)/doc/books/system/pn \
	$(REMOTE1PTIIDIST)/doc/books/system/synchronous \
	$(REMOTE1PTIIDIST)/doc/books/system/vergil \
	$(REMOTE1PTIIDIST)/doc/papers/y11/modularSemantics \
	$(REMOTE1PTIIDIST)/doc/tutorial/graph \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ci/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless/demo \
	$(REMOTE1PTIIDIST)/ptolemy/domains \
	$(REMOTE1PTIIDIST)/ptolemy/demo \
	$(REMOTE1PTIIDIST)/ptdb/kernel/database \
	$(REMOTE1PTIIDIST)/ptdb/common/dto \
	$(REMOTE1PTIIDIST)/ptdb/common/exception \
	$(REMOTE1PTIIDIST)/ptdb/common/util \
	$(REMOTE1PTIIDIST)/ptdb/gui \
	$(REMOTE1PTIIDIST)/ptdb/kernel/bl/load \
	$(REMOTE1PTIIDIST)/ptdb/kernel/bl/migration \
	$(REMOTE1PTIIDIST)/ptdb/kernel/bl/save \
	$(REMOTE1PTIIDIST)/ptdb/kernel/bl/search \
	$(REMOTE1PTIIDIST)/ptdb/kernel/bl/setup \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/basic/layout/kieler \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/ptera \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/basic/export \
	$(REMOTE1PTIIDIST)/ptolemy/vergil

# Directories we run make in before running make in the NONJSALLDIRS
NONJSALLCOREDIRS = \
	ptolemy/copernicus/kernel \
	ptolemy/copernicus/gui

# Directories to run tests in outside of JavaScope
NONJSALLDIRS = \
	ptolemy/cg/kernel/generic/program/procedural/java \
	ptolemy/cg/kernel/generic \
	ptolemy/cg/kernel/generic/html \
	ptolemy/cg/adapter \
	ptolemy/cg/lib \
	ptolemy/cg/gui \
	ptolemy/cg/adapter/generic/html \
	ptolemy/cg/kernel/generic/program/procedural/c/type \
	ptolemy/cg/kernel/generic/program/procedural/c \
	ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/lib/conversions \
	ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/lib/string \
	ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/lib \
	ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal \
	ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/sdf/lib \
	ptolemy/cg/adapter/generic/program/procedural/adapters/ptolemy/actor/lib \
	ptolemy/cg/adapter/generic/program/procedural/adapters/ptolemy/actor/lib/string \
	ptolemy/cg/adapter/generic/program/procedural/adapters/ptolemy/domains/sdf/lib \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/colt \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/conversions \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/hoc \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/logic \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/string \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal \
	ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/lib \
	ptolemy/cg/kernel/generic/program/procedural/java/modular \
	ptolemy/copernicus/interpreted \
	ptolemy/copernicus/shallow \
	ptolemy/domains/giotto \
	ptolemy/domains/giotto/cgc \
	ptolemy/kernel \
	ptolemy/moml \
	ptolemy/util \
	ptolemy/configs \
	ptserver/actor \
	ptserver/actor/lib/io \
	ptserver/communication \
	ptserver/control \
	ptserver/data \
	ptserver/data/handler \
	ptserver \
	adm

# Run JavaScope Code Coverage tool in the distribution tree
# For command line options, see
#http://www.gigascale.org/pubs/gsrc-downloads/JavaScope/docs/pdf/UGApxTools.pdf
remote1distjsall: remote1distjsall_1 remote1distjsall_2 remote1distjsall_3


remote1distjsall_1:
	@echo "#"
	@echo "# Now running make jsall in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "# Generating test coverage data for: "
	@echo "# $(JSALLDIRS)"
	@echo "# To add to the list of directories, edit "
	@echo "# ~ptII/adm/gen-latest/makefile"
	@echo "#"
	@echo "$@ `df -k $(REMOTE1PTIIDIST)`" 
	@echo "$@ `date`"
	@echo "`uptime`"
	@echo "Setting up javax.comm.properties so that we can build under solaris"
	-(cd $(REMOTE1PTIIDIST)/lib; rm javax.comm.properties; cp /usr/java/commapi/docs/javax.comm.properties .)
	@echo "Removing vendors/sun/commapi so that we can build under solaris"
	@echo "Removing $(JSDATABASE) in prep for $$x "
	-ls -ldg $(JSDATABASE) $(JSDATABASE)/*
	-rm -rf $(JSDATABASE)
	mkdir $(JSDATABASE)

	@-if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jsall in $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
				jsall JSINSTRFLAGS=;\
			);  \
		    fi ; \
		done ; \
	fi

remote1distjsall_2:
	@echo "copernicus/applet requires that the jar files are present"
	@echo "FIXME: assuming Solaris with the : classpathseparator here"
	(cd $(REMOTE1PTIIDIST); \
	    $(MAKE) $(MFLAGS) $(MAKEVARS) \
		AUXCLASSPATH=":$(JSCLASSPATH)" \
		install)
	(cd $(REMOTE1PTIIDIST)/ptolemy/copernicus/applet; \
		$(MAKE) $(MFLAGS) $(MAKEVARS) \
			"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
			AUXCLASSPATH=":$(JSCLASSPATH)" \
			jsall JSINSTRFLAGS=;)

remote1distjsall_3:
	echo $$PATH
	jssummary -HTML -PROGRESS \
		-OUTFILE=$(JSOUTDIR)/coverage.html
	jsreport -HTML -PROGRESS -RECURSIVE \
		-OUTDIR=$(JSOUTDIR)
	# Add links and colorize the coverage.html page
	sh $(PTIIADM)/bin/coveragerating $(PTII) \
		$(JSOUTDIR)/coverage.html
	-chmod -R a+r $(JSOUTDIR)

# report directories that have java files in them, but are not
# in JSALLDIRS
jsalldirs_check:
	@sh $(PTIIHOME)/adm/bin/jsalldirs_check $(REMOTE1PTIIDIST) $(JSALLDIRS)

# Remove the JavaScope instrumentation
# Directories in which we should run jsrestore
# We do this by running jsinstr instead of running make because some
# of the backtrack directories do not have makefiles.
JSCLEANUPDIRS = \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/applet \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy/actor/lib \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy/math \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy/domains/sdf/lib

remote1distjsrestore:
	@echo "#"
	@echo "# Now running make jsrestore in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `df -k $(REMOTE1PTIIDIST)`" 
	@echo "$@ `date`"
	@echo "`uptime`"
	@if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jsrestore in $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jsrestore;\
			);  \
		    fi ; \
		done ; \
	fi
	set $(JSCLEANUPDIRS); \
	for x do \
	    if [ -w $$x ] ; then \
		( cd $$x ; \
		echo running jsrestore in `pwd` ; \
		ls jsoriginal; \
		jsrestore *.java; \
		if [ -d jsoriginal ]; then \
			rm -f jsoriginal/README; \
			rmdir jsoriginal; \
		fi; \
		grep "import COM.sun.suntest.javascope.database.CoverageUnit" *.java ; \
		rm -f *.class; \
		);  \
	    fi ; \
	done ; \
	# Try to get rid of JavaScope files
	(cd $(REMOTE1PTIIDIST)/ptolemy/backtrack; rm -rf automatic util; svn update)

# Build the docs
remote1doc:
	@echo "#"
	@echo "# remote1doc rule"
	@echo "# Now running make install in $(REMOTE1PTIIDIST)/doc"
	@echo "# on `hostname`"
	@echo "# We run make in the doc directory early so that"
	@echo "# the website link checker will find files in doc/codeDoc"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	@echo "Setting up javax.comm.properties so that we can build under solaris"
	-(cd $(REMOTE1PTIIDIST)/lib; rm javax.comm.properties; cp /usr/java/commapi/docs/javax.comm.properties .)
	#@echo "Removing vendors/sun/commapi so that we can build under solaris"
	#-rm $(REMOTE1PTIIDIST)/vendors/sun/commapi/*
	(cd $(REMOTE1PTIIDIST)/doc; PTII=$(REMOTE1PTIIDIST) $(MAKE) install)
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	@echo "# Running make in $(REMOTE1PTIIDIST)/doc/test"
	(cd $(REMOTE1PTIIDIST)/doc/test; PTII=$(REMOTE1PTIIDIST) $(MAKE))

# Remove the JavaScope instrumentation
remote1build:
	@echo "#"
	@echo "# Now running make in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST);	\
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k sources install)

# Run tests outside of Javascope
remote1distnonjsall:
	@echo "#"
	@echo "# Now running make non Javascope tests in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "# To add to the list of directories, edit "
	@echo "# ~ptII/adm/gen-latest/makefile"
	@echo "#"
	@echo "$@ `df -k $(REMOTE1PTIIDIST)`" 
	@echo "$@ `date`"
	@echo "`uptime`"
	@echo "# Run make in the non javascope core directories"
	@echo "# that need to be built before we build the other non javascope"
	@-if [ "x$(NONJSALLCOREDIRS)" != "x" ]; then \
		set $(NONJSALLCOREDIRS); \
		for x do \
		    if [ -w $(PTIIHOME)/$$x ] ; then \
			( cd $(PTIIHOME)/$$x ; \
			echo making all  in $(PTIIHOME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				all \
			);  \
		    fi ; \
		done ; \
	fi
	-if [ "x$(NONJSALLDIRS)" != "x" ]; then \
		set $(NONJSALLDIRS); \
		for x do \
		    if [ -w $(PTIIHOME)/$$x ] ; then \
			( cd $(PTIIHOME)/$$x ; \
			echo making tests in $(PTIIHOME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				tests \
			);  \
		    fi ; \
		done ; \
	fi

remote1ChangeLog:
	@echo "#"
	@echo "# Now generating ChangeLog file in $(REMOTE1PTIIDIST)"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST); \
	    if [ ! -d adm/dists/.svn ]; then \
		echo "Removing adm/dists because adm/dists/.svn not found"; \
	        rm -rf adm/dists; \
	    fi) 
	@echo "Problems generating ChangeLog from ptolemy/backtrack/automatic/ptolemy"
	ls -l $(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy
	#rm -rf $(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy
	#(cd $(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic; svn update)
	#ls -l $(REMOTE1PTIIDIST)/ptolemy/backtrack/automatic/ptolemy
	(cd $(REMOTE1PTIIDIST);	rm -f ChangeLog; \
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k ChangeLog; \
		rm -f ChangeLog.txt; ln -s ChangeLog ChangeLog.txt)

remote1ojdcheck:
	@echo "#"
	@echo "# Now generating ojdcheck files in $(REMOTE1PTIIDIST)/doc"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST)/doc; rm -rf ojdcheck.htm; \
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k ojdcheck.htm)

remote1doccheck:
	@echo "#"
	@echo "# Now generating doccheck files in $(REMOTE1PTIIDIST)/doc"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST)/doc; rm -rf doccheck; \
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k doccheck)

remote1findbugs:
	@echo "#"
	@echo "# Now generating findbugs files in $(REMOTE1PTIIDIST)/doc"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST)/doc; rm -rf findbugs.*; \
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k PTII=$(REMOTE1PTIIDIST) findbugs)

# Build jnlp installers
remote1jnlp:
	@echo "#"
	@echo "# Now running make jnlp in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST);	PTII=$(REMOTE1PTIIDIST) \
		$(MAKE) -k jnlp_dist_nightly)



#####################################################################
# Build on a remote host under JDK1.4

# Remote location of Ptolemy II tree for testing
# Host to run on
REMOTE2HOST = carson

# Directory to place test distribution in
REMOTE2HOME = /vol/$(REMOTE2HOST)/$(REMOTE2HOST)1/ptII

# Actual ptII directory
REMOTE2PTIIDIST = $(REMOTE2HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE2LOG = $(GENLOGDIR)/remote2.out

remote2: remote2jdk1.4
remote2jdk1.4:  $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE2HOST) with JDK1.4 "
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	ssh $(REMOTE2HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		OTHERMAKE=${OTHERMAKE} \
		PTIIHOME=${PTIIHOME} \
		PTIIADM=${PTIIADM} \
		REMOTE2HOST=${REMOTE2HOST} \
		REMOTE2HOME=${REMOTE2HOME} \
		TAR=${TAR} \
		remote2nuke remote2ptiidist remote2distclean \
		remote2distjdk1.4 remote2distjdk1.4test remote2done \
		>& $(REMOTE2LOG)" &

remote2fortytwo:
	$(MAKE) USER=cxh OTHERMAKE=make PTIIHOME=/tmp/cxh/ptII PTIIADM=/tmp/cxh/ptII/adm TAR=/bin/tar REMOTE2HOST=fortytwo REMOTE2HOME=/tmp/cxh/r2 remote2

# Remove the remote dist
# $(REMOTE2DONEFILE) is used to signal when everything is done
remote2nuke:
	rm -rf $(REMOTE2PTIIDIST) $(REMOTE2DONEFILE)

#Untar the remote dist.  Usually this is run on the remote machine
remote2ptiidist: $(REMOTE2PTIIDIST)
$(REMOTE2PTIIDIST): $(PTII_SRC)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE2HOME); $(TAR) -zxf $(PTII_SRC))


# clean the remote tree
remote2distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE2PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE2PTIIDIST); \
		$(OTHERMAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)


# Compile with JDK1.4
remote2distjdk1.4:
	@echo "#"
	@echo "# Now building with JDK1.4"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE2PTIIDIST); rm -f config.status config.cache; \
		PTII=$(REMOTE2PTIIDIST) \
		PATH=/usr/local/j2se/bin:$$PATH ./configure; \
	PTII=$(REMOTE2PTIIDIST) $(MAKE) -k fast)
	@echo "$@ `date`"
	@echo "`uptime`"

# Test with JDK1.4
REMOTE2PTJACL_JAR	= $(REMOTE2PTIIDIST)/ptjacl.jar
$(REMOTE2PTJACL_JAR):
	@echo "#"
	@echo "# Creating $@"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE2PTIIDIST); cvs -d $(CVS_REPOSITORY) checkout tcl; \
		cd tcl;	\
		PTII=$(REMOTE2PTIIDIST) \
		PATH=/usr/local/j2se/bin:$$PATH \
		$(MAKE) -k sources all install)
	ls -l $@
	@echo "$@ `date`"
	@echo "`uptime`"

remote2distjdk1.4test: #$(REMOTE2PTJACL_JAR)
	@echo "#"
	@echo "# Now testing with on `hostname`"
	@echo "# with `java -version`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE2PTIIDIST); \
		PTII=$(REMOTE2PTIIDIST) \
		PATH=/usr/local/j2se/bin:$$PATH \
		$(MAKE) -k \
			"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
			tests)
	@echo "$@ `date`"
	@echo "`uptime`"


#####################################################################
# Build on a remote host by doing a svn checkout

# Host to run on
REMOTE3HOST = moog

# Directory to build in
REMOTE3HOME = /vol/$(REMOTE3HOST)/$(REMOTE3HOST)1/ptII

# Actual ptII tree.  Note that there is no version number because
# we are checking it out from svn
REMOTE3PTIIDIST = $(REMOTE3HOME)/ptII

# Log file to save the results in
REMOTE3LOG = $(GENLOGDIR)/remote3.out

remote3: remote3svn
remote3svn:  $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE3HOST) by doing a svn checkout "
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	ssh -x -n $(REMOTE3HOST) "cd $(GENDIR); make remote3nuke >& $(REMOTE3LOG)"
	# ssh-ing and then using svn to ssh results in
	# (cd /vol/gauss/gauss2/ptII; svn -d :ext:gigasource.eecs.berkeley.edu:/home/svn checkout ptII; svn update -P -d)
	# You have no controlling tty and no DISPLAY.  Cannot read passphrase.
	# so we do the svn co locally
	cd $(GENDIR); make remote3ptiidist >> $(REMOTE3LOG) 2>&1
	ssh -x -n $(REMOTE3HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		remote3distclean \
		remote3distbuild remote3distbuild remote3done \
		>>& $(REMOTE3LOG)" &

# Remove the remote dist
# $(REMOTE3DONEFILE) is used to signal when everything is done
remote3nuke:
	rm -rf $(REMOTE3PTIIDIST) $(REMOTE3DONEFILE)

#Untar the remote dist.  Usually this is run on the remote machine
remote3ptiidist: $(REMOTE3PTIIDIST)
$(REMOTE3PTIIDIST):
	@echo "#"
	@echo "# Now doing a svn checkout on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE3HOME); svn checkout $(SVN_REPOSITORY) )


# clean the remote tree
remote3distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE3PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE3PTIIDIST); \
		$(OTHERMAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)


# Compile
remote3distbuild:
	@echo "#"
	@echo "# Now building in $(REMOTE3PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE3PTIIDIST); rm -f config.status config.cache; \
		PTII=$(REMOTE3PTIIDIST) ./configure; $(MAKE) -k)
	@echo "$@ `date`"
	@echo "`uptime`"

# Build Jacl
REMOTE3PTJACL_JAR	= $(REMOTE3PTIIDIST)/ptjacl.jar
$(REMOTE3PTJACL_JAR):
	@echo "#"
	@echo "# Creating $@"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE3PTIIDIST); cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs checkout tcl; \
		cd tcl; $(MAKE) install)
	ls -l $@
	@echo "$@ `date`"
	@echo "`uptime`"

# Test
remote3disttest: $(REMOTE3PTJACL_JAR)
	@echo "#"
	@echo "# Now testing in $(REMOTE3PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE3PTIIDIST); \
		$(OTHERMAKE) \
			"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
		tests)
	@echo "$@ `date`"
	@echo "`uptime`"


#####################################################################
# Since we are running on multiple machines, when we are done
# each remote build creates a file.  When all the files are present
# we concatenate the results to the log file and then call ptIInightly-mail

# Log file to concatenate to
PTIINIGHTLY_TXT = $(PTIIADM)/test/ptIInightly.txt

# Semaphore file for the remote1 build
REMOTE1DONEFILE = $(GENLOGDIR)/remote1done
remote1done: $(REMOTE1DONEFILE) remotealldone
$(REMOTE1DONEFILE):
	date > $@

# Semaphore file for the remote2 build
REMOTE2DONEFILE = $(GENLOGDIR)/remote2done
remote2done: $(REMOTE2DONEFILE) remotealldone
$(REMOTE2DONEFILE):
	date > $@

# Semaphore file for the remote3 build
REMOTE3DONEFILE = $(GENLOGDIR)/remote3done
remote3done: $(REMOTE3DONEFILE) remotealldone
$(REMOTE3DONEFILE):
	date > $@

# Semaphore file for the remote4 build
REMOTE4DONEFILE = $(GENLOGDIR)/remote4done
remote4done: $(REMOTE4DONEFILE) remotealldone
$(REMOTE4DONEFILE):
	date > $@

# Semaphore file for the remote5 build
REMOTE5DONEFILE = $(GENLOGDIR)/remote5done
remote5done: $(REMOTE5DONEFILE) remotealldone
$(REMOTE5DONEFILE):
	date > $@

# Semaphore file for the remote6 build
REMOTE6DONEFILE = $(GENLOGDIR)/remote6done
remote6done: $(REMOTE6DONEFILE) remotealldone
$(REMOTE6DONEFILE):
	date > $@


# If the remote done files are present, send email.
# We used to put remote2.log into ptIInightly_txt.
remotealldone:
	if [ -f $(REMOTE4DONEFILE) \
		-a -f $(REMOTE5DONEFILE) \
			]; then \
		date; \
		echo "Remote processes finished"; \
		/bin/sh $(PTIIADM)/bin/ptIInightly-mail cxh ; \
		/bin/sh $(PTIIADM)/bin/ptIIcodegen-mail $(EMAILTO) ; \
	else \
		date; \
		echo "Still waiting"; \
		ls -l $(REMOTE4DONEFILE) $(REMOTE5DONEFILE); \
	fi

##########################################################################
# Build from a tar file - similar to remote1, but w/o test case coverage
# except for the deep codegen

# Host to run on
REMOTE4HOST = moog

# Remote location of Ptolemy II tree for testing
#REMOTE4HOME = /vol/$(REMOTE4HOST)/$(REMOTE4HOST)2/ptII
#REMOTE4HOME = /export/home1/bldmastr/pttmp/remote4
REMOTE4HOME = /export/home2/ptII/remote4
REMOTE4PTIIDIST = $(REMOTE4HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE4LOG = $(GENLOGDIR)/remote4.out

remote4_test: $(PTII_SRC) 
	if [ ! -d test ]; then mkdir test; fi
	$(MAKE) REMOTE4HOME=$(GENDIR)/test \
		PTII_FULL=$(PTII_SRC) \
		remote4nuke remote4ptiidist remote4configure \
		remote4distclean remote4build \
		remote4done

# Create a separate tree and build on REMOTE4HOST
remote4: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE4HOST) from dist tar file"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
#	-ssh $(REMOTE4HOST) "cd $(GENDIR); cp $(REMOTE4LOG) $(REMOTE4LOG).old"
#	ssh $(REMOTE4HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
#		remote4nuke remote4ptiidist remote4configure \
#		remote4distclean remote4build \
#		remote4javatest \
#		remote4done \
#		 >& $(REMOTE4LOG)" &
	if [ ! -d $(GENLOGDIR) ]; then mkdir $(GENLOGDIR); fi
	-(cd $(GENDIR); cp $(REMOTE4LOG) $(REMOTE4LOG).old)
	cd $(GENDIR); $(MAKE) -k EMAILTO=$(EMAILTO) \
		remote4nuke remote4ptiidist remote4configure \
		remote4distclean remote4build \
		remote4javatest \
		remote4done \
		 > $(REMOTE4LOG) 2>&1


# Remove the remote dist
remote4nuke:
	rm -rf $(REMOTE4PTIIDIST) $(REMOTE4DONEFILE)

# untarthe remote dist.  Usually this is run on the remote machine
remote4ptiidist: $(REMOTE4PTIIDIST)
$(REMOTE4PTIIDIST): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE4HOME); $(TAR) -xf $(PTII_FULL))
	@echo "Creating  vendors/jode"
	(cd $(PTIIHOME); $(TAR) -cf - vendors/jode) | \
		(cd $(REMOTE4PTIIDIST); $(TAR) -xf -)

remote4configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE4PTIIDIST);	rm -f config.cache; PTII=$(REMOTE4PTIIDIST) ./configure)

# Clean the remote4 tree
remote4distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE4PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE4PTIIDIST); \
		$(MAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)

remote4distjavadocs:
	@echo "#"
	@echo "# Now generating javadocs in $(PTIIDIST) on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-(cd $(REMOTE4PTIIDIST); $(OTHERMAKE) -k javadocs)

remote4build:
	@echo "#"
	@echo "# Now running make fast all install in $(REMOTE4PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE4PTIIDIST);	PTII=$(REMOTE4PTIIDIST) $(MAKE) fast)
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE4PTIIDIST);	PTII=$(REMOTE4PTIIDIST) $(MAKE) install)

# Run the deep tests, this is likely to take 14 hours.
remote4javatest:
	@echo "#"
	@echo "# Now running make in $(REMOTE4PTIIDIST)/ptolemy/copernicus/java/test"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE4PTIIDIST)/ptolemy/copernicus/java/test; \
		PTII=$(REMOTE4PTIIDIST) $(MAKE) \
		"JTCLSHFLAGS=-Dptolemy.ptII.timeOutSeconds=79200 -Xmx384M" \
		test_long)

##########################################################################
# Build from a src tar file - similar to remote1, but w/o test case coverage
# except for the shallow codegen

# Host to run on
REMOTE5HOST = moog

# Directory to build in
#REMOTE5HOME = /vol/$(REMOTE5HOST)/$(REMOTE5HOST)1/ptIIsrc
#REMOTE5HOME = /export/home1/bldmastr/pttmp/remote5
REMOTE5HOME = /export/home2/ptII/remote5

REMOTE5PTIIDIST = $(REMOTE5HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE5LOG = $(GENLOGDIR)/remote5.out

# Create a separate tree and build on REMOTE5HOST
remote5: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE5HOST) from dist tar file"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
#	-ssh $(REMOTE5HOST) "cd $(GENDIR); cp $(REMOTE5LOG) $(REMOTE5LOG).old"
#	ssh $(REMOTE5HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
#		remote5nuke remote5ptiidist remote5configure \
#		remote5build \
#		remote5shallowtest \
#		remote5done \
#		 >& $(REMOTE5LOG)" &
	if [ ! -d $(GENLOGDIR) ]; then mkdir $(GENLOGDIR); fi
	-(cd $(GENDIR); cp $(REMOTE5LOG) $(REMOTE5LOG).old)
	cd $(GENDIR); $(MAKE) -k EMAILTO=$(EMAILTO) \
		remote5nuke remote5ptiidist remote5configure \
		remote5build \
		remote5shallowtest \
		remote5done \
		 > $(REMOTE5LOG) 2>&1


# Remove the remote dist
remote5nuke:
	rm -rf $(REMOTE5PTIIDIST) $(REMOTE5DONEFILE)

# untarthe remote dist.  Usually this is run on the remote machine
remote5ptiidist: $(REMOTE5PTIIDIST)
$(REMOTE5PTIIDIST): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE5HOME); $(TAR) -xf $(PTII_SRC))

remote5configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE5PTIIDIST);	rm -f config.cache; PTII=$(REMOTE5PTIIDIST) ./configure)


remote5build:
	@echo "#"
	@echo "# Now running make fast all install in $(REMOTE5PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE5PTIIDIST);	PTII=$(REMOTE5PTIIDIST) $(MAKE) fast install)


# Run the shallow tests, this is likely to take 3 hours.
remote5shallowtest:
	@echo "#"
	@echo "# Now running make in $(REMOTE5PTIIDIST)/ptolemy/copernicus/shallow/test"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE5PTIIDIST)/ptolemy/copernicus/shallow/test; \
		PTII=$(REMOTE5PTIIDIST) \
		$(MAKE) \
			"JTCLSHFLAGS=-Dptolemy.ptII.timeOutSeconds=50400 -Xmx256M"\
			test_long)



##########################################################################
# Build from a src tar file - similar to remote1, we only generate
# test case coverage from the /test/auto/*.xml tests

# Host to run on
REMOTE6HOST = moog

# Directory to build in
REMOTE6HOME = /vol/$(REMOTE6HOST)/$(REMOTE6HOST)2/ptII

REMOTE6PTIIDIST = $(REMOTE6HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE6LOG = $(GENLOGDIR)/remote6.out

# Create a separate tree and build on REMOTE6HOST
remote6: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE6HOST) from src tar file"
	@echo "#  Similar to remote1, we only generate test case coverage"
	@echo "#  from the /test/auto/*.xml tests"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	ssh $(REMOTE6HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		remote6nuke remote6ptiidist remote6configure \
		remote6build \
		remote6buildjsall \
		remote6autojsall \
		remote6done \
		 >& $(REMOTE6LOG)" &


# Remove the remote dist
remote6nuke:
	rm -rf $(REMOTE6PTIIDIST) $(REMOTE6DONEFILE)

# untarthe remote dist.  Usually this is run on the remote machine
remote6ptiidist: $(REMOTE6PTIIDIST)

# $(REMOTE6PTIIDIST) is the same as $(REMOTE1PTIIDIST), so we comment
# out this rule.
#$(REMOTE6PTIIDIST): $(PTII_FULL)
#	@echo "#"
#	@echo "# Now untaring on `hostname`"
#	@echo "#"
#	@echo "$@ `date`"
#	@echo "`uptime`"
#	(cd $(REMOTE6HOME); $(TAR) -zxf $(PTII_SRC))

remote6configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE6PTIIDIST);	rm -f config.cache; PTII=$(REMOTE6PTIIDIST) ./configure)


remote6build:
	@echo "#"
	@echo "# Now running make fast all install in $(REMOTE6PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE6PTIIDIST);	PTII=$(REMOTE6PTIIDIST) $(MAKE) sources fast all install)

# Run JavaScope Code Coverage tool in the distribution tree
# For command line options, see
#http://www.gigascale.org/pubs/gsrc-downloads/JavaScope/docs/pdf/UGApxTools.pdf

remote6buildjsall:
	@echo "#"
	@echo "# Now running make jsoriginal clean jsbuild in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "# Generating test coverage data for: auto directories"
	@echo "# $(JSALLDIRS)"
	@echo "# To add to the list of directories, edit "
	@echo "# ~ptII/adm/gen-latest/makefile"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	@echo "Removing $(JSDATABASE) in prep for $$x "
	-ls -ldg $(JSDATABASE) $(JSDATABASE)/*
	-rm -rf $(JSDATABASE)
	mkdir $(JSDATABASE)

	@-if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jsall in $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
				jsoriginal clean jsbuild JSINSTRFLAGS=;\
			);  \
		    fi ; \
		done ; \
	fi

remote6autojsall:
	-if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x/test/auto ] ; then \
			( cd $$x/test ; \
			echo making jstest_jauto in $$x/test ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				"JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true -Dptolemy.ptII.isRunningNightlyBuild=true -Xmx1500M" \
				jstest_jauto; \
			);  \
		    fi ; \
		done ; \
	fi
	jssummary -HTML -PROGRESS \
		-OUTFILE=$(JSOUTDIR)/coverage.html
	jsreport -HTML -PROGRESS -RECURSIVE \
		-OUTDIR=$(JSOUTDIR)
	# Add links and colorize the coverage.html page
	sh $(PTIIADM)/bin/coveragerating $(PTII) \
		$(JSOUTDIR)/coverage.html
	-chmod -R a+r $(JSOUTDIR)


###########################################################################
# Rules for cleaning up the distribution
SPELLEXCLUDEDIRS = 'filter|collections|microstar'
spell_all:
	cd $(PTIIDIST); $(PTIIHOME)/util/testsuite/ptspell `find . -xdev -name '*.java' -print | egrep -v $(SPELLEXCLUDEDIRS)`

rmtrailingspace_all:
	@-if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo running rmtrailingspace in $$x ; \
			rmtrailingspace *.java; \
			);  \
		    fi ; \
		done ; \
	fi

indent_all:
	@-if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo running jindent in $$x ; \
			jindent *.java; \
			);  \
		    fi ; \
		done ; \
	fi

########################################################################

# IzPack is the installer we use
# Download from http://www.izforge.com/izpack/downloads

#IZPACKDIR=c:/Program Files/IzPack
IZPACKDIR=$(PTII)/vendors/izpack
IZPACK_COMPILE=$(IZPACKDIR)/bin/compile
IZPACK_TAR=IzPack.src.3.10.2.tar.gz

# Under Solaris: gmake make_izpack TAR=gtar
make_izpack: $(IZPACK_COMPILE)
$(IZPACK_COMPILE): $(PTII)/vendors/izpack
	 cd $(PTII)/vendors/izpack/src; ant dist

# Download IzPack
$(PTII)/vendors/izpack:
	mkdir $(PTII)/vendors/izpack
	cd $(PTII)/vendors/izpack; \
		wget http://download.berlios.de/izpack/$(IZPACK_TAR); \
		$(TAR) -zxf $(IZPACK_TAR)

# It is necessary to this before create the Windows and Mac setup jar files.
cleanSrc:
	@echo "#"
	@echo "# cleanSrc: removing files created by configure"
	@echo "# `date`"
	if [ -f src/ptII$(VERSION)/mk/ptII.mk ]; then \
		(cd src/ptII$(VERSION)/bin; mv Vergil.app Ptiny.app ..; $(MAKE) clean; mv ../Vergil.app ../Ptiny.app .); \
	fi
	rm -f src/ptII$(VERSION)/mk/ptII.mk
	rm -f src/ptII$(VERSION)/.classpath
	rm -f src/ptII$(VERSION)/lib/ptII.properties

BOOK_PDF=PtolemyII_DigitalV1_02.pdf
PTSETUP=ptII$(WINDOWS_VERSION)_setup_windows
# 32 bit IzPack installer
setup_jar: $(PTSETUP).jar
$(PTSETUP).jar: $(PTSETUP).xml $(PTIIDIST)/jar_dist $(PTIIDIST)/ptiny.exe cleanSrc $(PTIIDIST)/bin/vergil $(PTIIDIST)/ptolemy/matlab/ptmatlab.dll $(PTIIDIST)/doc/books/systems/$(BOOK_PDF)
	@echo "#"
	@echo "# Building $(PTSETUP).jar"
	@echo "# `date`"
	-ls -l $(PTIIDIST)/bin/vergil
	sh "$(IZPACK_COMPILE)" $<
	if [ ! -f $@ ];  then \
		error "$@ does not exist?"; \
	else \
		ls -l $@; \
	fi

# Build a 64 bit installer.  See http://jira.codehaus.org/browse/IZPACK-506
$(PTSETUP)_64.xml: $(PTSETUP).xml
	sed 's/ShellLink.dll/ShellLink_x64.dll/' $^ > $@

setup64_jar: $(PTSETUP)_64.jar
$(PTSETUP)_64.jar: $(PTSETUP)_64.xml $(PTIIDIST)/ptiny.exe $(PTIIDIST)/bin/vergil cleanSrc
	@echo "#"
	@echo "# Building $(PTSETUP)_64.jar"
	@echo "# `date`"
	ls -l $(PTIIDIST)/bin/vergil
	sh "$(IZPACK_COMPILE)" $<
	if [ ! -f $@ ];  then \
		error "$@ does not exist?"; \
	else \
		ls -l $@; \
	fi

$(PTIIDIST)/bin/vergil:
	@echo "#"
	@echo "# Creating $@"
	@echo "# `date`"
	cp $(PTII)/bin/vergil $@

#	c:/cxh/src/izpack/izpack3.9.0/bin/compile $<


CLASSPATHSEPARATOR=;
IZPACK_CLASSDIR=${IZPACKDIR}/lib/compiler.jar${CLASSPATHSEPARATOR}${IZPACKDIR}/lib/installer.jar${CLASSPATHSEPARATOR}
#IZPACK_CLASSDIR=c:/cxh/src/izpack/trunk/src/lib
ExtractJarFile.class: ExtractJarFile.java
	javac -classpath "$(IZPACK_CLASSDIR)" $^

Echo.class: Echo.java
	javac -classpath "$(IZPACK_CLASSDIR)" $^

ExtractJarFile.jar: ExtractJarFile.class Echo.class
	jar -cf $@ CYGPATH^

#PTCYGPATH=cygpath -a --windows
PTCYGPATH=./ptcygpath

# Recompile izpack
izpack:	
	cd ~/src/izpack/trunk/src/; ant build.standalone-compiler; cp C:/cxh/src/izpack/trunk/lib/standalone-compiler.jar c:/cxh/src/izpack/izpack3.9.0/lib/standalone-compiler.jar 
	c:/cxh/src/izpack/izpack3.9.0/bin/compile test.xml
	"$(L4JC)" `$(PTCYGPATH) test_l4j.xml`

# Location of Launch4J, see http://launch4j.sourceforge.net/
#L4J_DIR=c:/Program Files/Launch4j

L4J_DIR=$(PTII)/vendors/launch4j

L4J_TAR=launch4j-3.0.0-pre2-solaris-sparc.tgz
$(PTII)/vendors/launch4j:
	cd $(PTII)/vendors; \
		wget http://easynews.dl.sourceforge.net/sourceforge/launch4j/$(L4J_TAR); \
		$(TAR) -zxf $(L4J_TAR)


# Launch4J console application that reads in .xml files and creates .exe files.
#L4JC=$(L4J_DIR)/launch4jc.exe
L4JC=$(L4J_DIR)/launch4j

PTSETUP_DEPENDENCIES = \
	$(PTIIDIST)/ptiny.exe \
	$(PTIIDIST)/ptbook.exe \
	$(PTIIDIST)/jar_dist \
	$(PTIIDIST)/ptolemy/matlab/ptmatlab.dll


# Launch4J
$(PTSETUP).exe: $(PTSETUP_DEPENDENCIES) \
			jre \
			$(PTIIDIST)/lib/matlabWindows.jar \
			$(PTSETUP)_l4j.xml \
			$(PTSETUP).jar #ExtractJarFile.class

	@echo "#"
	@echo "# Building $(PTSETUP).exe"
	@echo "# `date`"
	"$(L4JC)" `$(PTCYGPATH) $(PTSETUP)_l4j.xml`
	if [ ! -f $@ ];  then \
		error "$@ does not exist?"; \
	else \
		ls -l $@; \
	fi

$(PTSETUP)_64_l4j.xml: $(PTSETUP)_l4j.xml
	sed 's/setup_windows/setup_windows_64/' $^ > $@

setup_64: $(PTSETUP)_64.exe
$(PTSETUP)_64.exe: $(PTSETUP_DEPENDENCIES) \
			jre \
			$(PTIIDIST)/lib/matlabWindows.jar \
			$(PTSETUP)_64_l4j.xml \
			$(PTSETUP)_64.jar #ExtractJarFile.class

	@echo "#"
	@echo "# Building $(PTSETUP).exe"
	@echo "# `date`"
	"$(L4JC)" `$(PTCYGPATH) $(PTSETUP)_64_l4j.xml`
	if [ ! -f $@ ];  then \
		error "$@ does not exist?"; \
	else \
		ls -l $@; \
	fi

ptiny.exe: $(PTIIDIST)/ptiny.exe
$(PTIIDIST)/ptiny.exe:
	 $(MAKE) -C $(PTIIDIST) PTII=$(PTIIDIST) exes

$(PTIIDIST)/ptbook.exe:
	 $(MAKE) -C $(PTIIDIST) PTII=$(PTIIDIST) exes

book_pdf: $(PTIIDIST)/doc/books/systems/$(BOOK_PDF)

BOOK_URL= http://ptolemy.eecs.berkeley.edu/books/Systems/$(BOOK_PDF)
$(PTIIDIST)/doc/books/systems/$(BOOK_PDF):
	if [ ! -f "$@" ] ; then \
		echo "Downloading $(BOOK_URL)"; \
		wget $(BOOK_URL); \
		mv $(BOOK_PDF) $(PTIIDIST)/doc/books/systems; \
		ls -l "$@"; \
	fi

$(PTIIDIST)/ptolemy/matlab/ptmatlab.dll: $(PTIIDIST)/lib/matlabWindows.jar
	(cd $(PTIIDIST)/ptolemy/matlab; jar -xf $(PTIIDIST)/lib/matlabWindows.jar)


jre:
	if [ ! -d jre ] ; then \
		mkdir jre; \
		echo "JRE Directory" > jre/README.txt; \
	fi

setup: $(PTSETUP).exe $(PTSETUP)_64.exe $(PTSETUP_MAC).app.tar.gz

clean_setup: 
	rm -f $(PTSETUP).jar $(PTSETUP).exe

full_clean_setup: clean_setup
	rm -rf $(PTSETUP_DEPENDENCIES)

test_setup:  test_setup_start test_setup_make_fast_install test_setup_test test_setup_ant

test_setup_start: test_setup_announce setup_jar setup setup_mac_app_tar test_setup_installerTest 

test_setup_test: test_setup_XvfbStart test_setup_make_tests test_setup_end


test_setup_announce:
	@echo "#"
	@echo "# Now testing the setup file" 
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"

test_setup_installerTest: _test_setup_installerTest_clean test/installerTest

_test_setup_installerTest_clean:
	rm -rf test/installerTest

test/installerTest:
	if [ ! -f test/installerTest ]; then \
		echo "Invoking java -jar $(PTSETUP).jar installerTest.xml to create test/installerTest"; \
		java -jar $(PTSETUP).jar installerTest.xml; \
	fi

test_setup_XvfbStart:
	-pkill Xvfb
	-pkill -9 Xvfb
	# Ignore errors 
	-ls -l /tmp/.X8-lock
	-rm -rf /tmp/.X8-lock
	Xvfb :8 -screen 0 1024x768x24 &

test_setup_make_fast_install:
	@echo "#"
	@echo "# Now configuring and running make fast install" 
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd test/installerTest; chmod a+x ./configure; export DISPLAY=localhost:8.0; echo "DISPLAY=$$DISPLAY"; PTII=`pwd` ./configure --enable-verbose; cat mk/ptII.mk; PTII=`pwd` make -k PTII=`pwd` sources fast install)

test_setup_make_tests:
	@echo "#"
	@echo "# Now configuring and running make tests" 
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd test/installerTest; chmod a+x ./configure; export DISPLAY=localhost:8.0; echo "DISPLAY=$$DISPLAY"; PTII=`pwd` ./configure --enable-verbose; cat mk/ptII.mk; PTII=`pwd` make -k PTII=`pwd` tests)


test_setup_ant: test/installerTest
	(cd test/installerTest; chmod a+x ./configure; export DISPLAY=localhost:8.0; echo "DISPLAY=$$DISPLAY"; PTII=`pwd` ./configure; PTII=`pwd` ant clean; PTII=`pwd` ant; PTII=`pwd` ant jars; PTII=`pwd` ant test.single -Dtest.name=ptolemy.kernel.test.junit.JUnitTclTest; PTII=`pwd` ant clean; cp build.default.xml build.xml; PTII=`pwd` ant) 

test_setup_end: test_setup_XvfbStop

test_setup_XvfbStop:
	@echo "#"
	@echo "# Now stopping Xvfb"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	-pkill Xvfb
	-pkill -9 Xvfb
	rm -f /tmp/.X8-lock

updateDOPCenterImage:
	$(GENDIR)/updateDOPCenterImageX11

# Echo the jars needed for the full package in izpack
full_jars:
	@(cd $(PTII); make -s echo_jars JARS=HYBRID_SYSTEMS_ONLY0_JNLP_JARS)
	@(cd $(PTII); make -s echo_jars JARS=FULL_ONLY_JNLP_JARS)
	@(cd $(PTII); make -s echo_jars JARS=VIPTOS_ONLY_JNLP_JARS)
	@(cd $(PTII); make -s echo_jars JARS=VISUAL_SENSE_ONLY_JNLP_JARS)

########################################################################
# Mac installer
ptsetup_mac: $(PTSETUP_MAC).xml
$(PTSETUP_MAC).xml: $(PTSETUP).xml macOSInclude.awk #makefile
	awk -f macOSInclude.awk $(PTSETUP).xml | \
	    sed -e 's@<!-- Vergil.app -->@<file src="../dists/ptII11.0.devel/bin/Vergil.app" targetdir="$$INSTALL_PATH/bin"/> <executable targetfile="$$INSTALL_PATH/bin/Vergil.app/Contents/MacOS/JavaApplicationStub"/>@' \
	    -e 's@<!-- Ptiny.app -->@<file src="../dists/ptII11.0.devel/bin/Ptiny.app" targetdir="$$INSTALL_PATH/bin"/>  <executable targetfile="$$INSTALL_PATH/bin/Ptiny.app/Contents/MacOS/JavaApplicationStub"/>@' \
	    -e 's@ptmatlab.dll@libptmatlab.jnilib@' \
	    -e 's@os="windows"@os="mac"@' \
	    > $@

PTINY_APP = $(PTIIDIST)/bin/Ptiny.app
Ptiny.app: $(PTINY_APP)
$(PTINY_APP): 
	if [ ! -f $@ ]; then \
		(cd $(PTIIDIST); PTII=$(PTIIDIST) ./configure; cd bin;  make Ptiny.app Vergil.app); \
        fi

setup_mac_jar: $(PTSETUP_MAC).jar
$(PTSETUP_MAC).jar: $(PTSETUP_MAC).xml $(PTIIDIST)/jar_dist $(PTINY_APP) $(PTIIDIST)/bin/vergil cleanSrc
	@echo "#"
	@echo "# Building $@"
	@echo "# `date`"
	ls -ldg $(PTINY_APP)
	sh "$(IZPACK_COMPILE)" $<
	if [ ! -f $@ ];  then \
		error "$@ does not exist?"; \
	else \
		ls -l $@; \
	fi




IZPACK2APP=$(IZPACKDIR)/utils/wrappers/izpack2app/izpack2app.py
setup_mac_app: $(PTSETUP_MAC).app
$(PTSETUP_MAC).app: $(PTSETUP_MAC).jar
	python $(IZPACK2APP) $^ $@ 
	find $@ -type f -exec touch {} \; 
	#cp $(PTII)/bin/macContents/Contents/Resources/ptolemy.icns $(PTSETUP_MAC).app/Contents/Resources/izpack.icns

setup_mac_app_tar: $(PTSETUP_MAC).app.tar.gz
$(PTSETUP_MAC).app.tar.gz: $(PTSETUP_MAC).app
	tar -zcf $@ $<

######################################################################
# Web Start Applets
#
JNLPDIST=test/ptII$(VERSION)
jnlpdist: $(JNLPDIST)/signed
$(JNLPDIST): $(GENLOG_DIR)
	$(MAKE)	release_sisyphus_remote4_test


# Directory that should be in vendors so that we build the jar files
$(JNLPDIST)/vendors/misc/rxtx/RXTXcomm.jar: $(JNLPDIST)
	# Build joystick, rxtx
	if [ ! -f $@ ]; then \
		echo "Reconfiguring with all optional packages"; \
		(cd $(JNLPDIST); rm -rf vendors; ln -s $(PTII)/vendors .; PTII=`pwd`;./configure;$(MAKE) PTII=`pwd` install); \
	fi

$(JNLPDIST)/signed: $(JNLPDIST)/vendors/misc/rxtx/RXTXcomm.jar
	if [ ! -d $@ ]; then \
		echo "Creating $@"; \
		cp ptKeystore.properties $(JNLPDIST); \
		(cd $(JNLPDIST); $(MAKE) PTII=`pwd` KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass $(PASSWORD)" KEYPASSWORD="-keypass $(PASSWORD)" jnlp_dist); \
	fi


$(JNLPDIST)/ptolemy/copernicus/applet/makeJNLPs: $(JNLPDIST)/signed
	(cd $(JNLPDIST); export PTII=`pwd`; cd ptolemy/copernicus/applet; ./updateDemos > makeJNLPs)

# One of the jnlp files to be created.
TARGET_JNLP = $(JNLPDIST)/ptolemy/verification/demo/SimpleTrafficLight/SimpleTrafficLight.jnlp
$(TARGET_JNLP): $(JNLPDIST)/ptolemy/copernicus/applet/makeJNLPs
	(cd $(JNLPDIST); export PTII=`pwd`; cd ptolemy/copernicus/applet; sh makeJNLPs)

JNLPS_TAR = jnlps.tar
$(JNLPS_TAR): $(TARGET_JNLP)
	(cd $(JNLPDIST)/doc; mv deployJava.js deployJavaApplet.js; \
	   cp deployJavaJNLP.js deployJava.js)
	(cd $(JNLPDIST); tar -cf $(PTIIDIST)/$(JNLPS_TAR) `find . -name "*.jnlp"` `find . -name "signed_*.jar"` signed doc/deployJava.js doc/deployJavaApplet.js)

updateJNLPs: $(JNLPS_TAR)
	scp $(JNLPS_TAR) moog:$(PTII_FTP)
	ssh moog "cd $(PTII_FTP)/ptII$(VERSION); /usr/local/bin/tar -xf ../$(JNLPS_TAR)"

###################################################################
# Update moog
BLDMASTR_HOME = /home/bldmastr
HUDSON_PTII = /var/lib/hudson/jobs/ptII/workspace
# Location of the nightly directory on moog
MOOG_HUDSON_HOME3 = /home2/hudson
MOOG_HOME_PTII = /home/hudson/ptII
PTEXTERNAL_NIGHTLY=/home/www/cvswww/chess.eecs.berkeley.edu/ptexternal/nightly

# The major version number, ptII/adm/gen-$(PTII_MAJOR_VERSION) should exist
PTII_MAJOR_VERSION=$(MAJOR_VERSION)

PTII_WINDOWS_VERSION=$(WINDOWS_VERSION)

# The complete version number, used to name tar files
PTII_VERSION=$(PTII_MAJOR_VERSION).devel

PTEXTERNAL_NIGHTLY=/home/www/cvswww/chess.eecs.berkeley.edu/ptexternal/nightly
update_moog:
	# Create ChangeLog.txt. We do each year separately.
	rm -rf $(HUDSON_PTII)/adm/dists
	-cd $(BLDMASTR_HOME)/adm/ChangeLogs; ./ptChangeLogs 
	mkdir $(HUDSON_PTII)/adm/dists
	scp -Bprq $(BLDMASTR_HOME)/adm/ChangeLogs/ChangeLog moog:$(PTEXTERNAL_NIGHTLY)/ChangeLog.txt
	#
	# The doc directories are on a separate partition
	ssh moog rm -rf $(MOOG_HOME_PTII)	
	ssh moog ln -s $(MOOG_HUDSON_HOME3)/ptII $(MOOG_HOME_PTII)
	#
	# Link to codeDoc*
	ssh moog rm -rf $(PTEXTERNAL_NIGHTLY)/doc/codeDoc 
	ssh moog rm -rf $(PTEXTERNAL_NIGHTLY)/doc/codeDocHyVisual 
	ssh moog rm -rf $(PTEXTERNAL_NIGHTLY)/doc/codeDocViptos
	ssh moog rm -rf $(PTEXTERNAL_NIGHTLY)/doc/codeDocVisualSense
	ssh moog ln -s $(MOOG_HOME_PTII)/doc/codeDoc $(PTEXTERNAL_NIGHTLY)/doc
	ssh moog ln -s $(MOOG_HOME_PTII)/doc/codeDocHyVisual $(PTEXTERNAL_NIGHTLY)/doc
	ssh moog ln -s $(MOOG_HOME_PTII)/doc/codeDocVisualSense $(PTEXTERNAL_NIGHTLY)/doc
	ssh moog ln -s $(MOOG_HOME_PTII)/doc/codeDocViptos $(PTEXTERNAL_NIGHTLY)/doc
	#
	# Copy over doc/coding/eclipse.htm and doc/eclipse/
	ssh moog mkdir -p $(PTEXTERNAL_NIGHTLY)/doc/coding
	scp -Bprq $(HUDSON_PTII)/doc/coding/addinganactor.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/addinganactor.htm
	scp -Bprq $(HUDSON_PTII)/doc/coding/ant.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/ant.htm
	scp -Bprq $(HUDSON_PTII)/doc/coding/performance.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/performance.htm
	scp -Bprq $(HUDSON_PTII)/doc/coding/svn-config-auto-props.txt moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/svn-config-auto-props.txt
	scp -Bprq $(HUDSON_PTII)/doc/coding/style.css moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/style.css
	scp -Bprq $(HUDSON_PTII)/doc/coding/style.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/style.htm
	scp -Bprq $(HUDSON_PTII)/doc/coding/style.pdf moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/style.pdf
	scp -Bprq $(HUDSON_PTII)/doc/coding/style_index.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/style_index.htm
	scp -Bprq $(HUDSON_PTII)/doc/coding/eclipse.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/eclipse.htm
	scp -Bprq $(HUDSON_PTII)/doc/coding/eclipseSimple.htm moog:$(PTEXTERNAL_NIGHTLY)/doc/coding/eclipse.htm
	(cd $(HUDSON_PTII)/doc/; tar -cf - eclipse footer.htm ) | ssh moog "cd $(PTEXTERNAL_NIGHTLY)/doc; tar -xf -"
	#
	# Copy images in eclipse.htm
	-ssh moog mkdir -p $(PTEXTERNAL_NIGHTLY)/doc/img
	scp -Bprq $(HUDSON_PTII)/doc/img/{eclipseWorkspaceLauncher.png,eclipsePreferencesGeneralWorkspace.png,eclipseNewProject.png,eclipseNewJavaProject.png,eclipseNewJavaProject2.png,eclipsePreferencesGeneralWorkspaceWindows.png,eclipseWorkspaceLauncherWindows.png} moog:$(PTEXTERNAL_NIGHTLY)/doc/img
	# Update the doc/eclipse directory to work with the CHESS site
	ssh moog "cd $(PTEXTERNAL_NIGHTLY)/doc/eclipse; ./fixEclipseInstructions"
	# Copy the ptII tree over to moog.
	# We need to at least copy over the build log
	##@echo "Removing $(MOOG_HUDSON_HOME3)/ptII on moog";
	##ssh moog rm -rf $(MOOG_HUDSON_HOME3)/ptII;
	@echo "Cleaning up in $(BLDMASTER_HOME)/vendors/misc/dbxml-2.5.16";
	rm -rf $(HUDSON_PTII)/vendors/misc/dbxml-2.5.16/build*
	rm -rf $(HUDSON_PTII)/vendors/misc/dbxml-2.5.16/db-*
	rm -rf $(HUDSON_PTII)/vendors/misc/dbxml-2.5.16/xerces-c-src
	rm -rf $(HUDSON_PTII)/vendors/misc/dbxml-2.5.16/xqilla
	@echo "Cleaning up in $(BLDMASTER_HOME)/ptdb/config/database/testdata";
	rm -rf $(BLDMASTER_PTII)/ptdb/config/database/testdata/log.*
	rm -rf $(BLDMASTER_PTII)/ptdb/config/database/testdata/__db.*
	@echo "Cleaning up in $(BLDMASTER_HOME)/adm";
	rm -rf $(HUDSON_PTII)/adm/dists/*
	rm -rf $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/$(PTSETUP_MAC)
	rm -rf $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/src
	rm -rf $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/src_jar
	rm -rf $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/test
	if [ -f $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).tar ]; then \
		rm -f $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).tar.gz; \
		echo "gzipping ptII$(PTII_VERSION).tar"; \
		gzip $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).tar; \
	fi
	if [ -f $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).src.tar ]; then \
		rm -f $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).src.tar.gz; \
		echo "gzipping ptII$(PTII_VERSION).src.tar"; \
		gzip $(HUDSON_PTII)/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).src.tar; \
	fi
	# 
	@echo "Cleaning up in $(HUDSON_HOME)";
	rm -rf $(HUDSON_PTII)/ptolemy/vergil/basic/layout/kieler/test/layoutPerformance.xml
	rm -rf $(HUDSON_PTII)/ptolemy/vergil/basic/layout/kieler/test/layoutPerformance2.xml
	@echo "`date`: Updating $(MOOG_HUDSON_HOME3)/ptII on moog";
	ssh moog rm -rf $(MOOG_HUDSON_HOME3)/tmp;
	ssh moog mkdir -p $(MOOG_HUDSON_HOME3)/tmp;
	echo "`date`: This will about 3 minutes . . ."
	# 1. Copy over the contents of \$PTII
	#-scp -Bprq $(HUDSON_PTII) moog:$(MOOG_HUDSON_HOME3)/tmp;
	-(cd $(HUDSON_PTII)/..; tar --exclude-from=$(GENDIR)/moog.ex -cf - workspace) | ssh moog "cd $(MOOG_HUDSON_HOME3)/tmp; tar -xf -"
	# 2. Move the previous ptII to ptII.old so that the links always work
	-ssh moog mv $(MOOG_HUDSON_HOME3)/ptII $(MOOG_HUDSON_HOME3)/ptII.old;
	ssh moog mv $(MOOG_HUDSON_HOME3)/tmp/workspace $(MOOG_HUDSON_HOME3)/ptII;
	@echo "`date`: Removing $(MOOG_HUDSON_HOME3)/ptII.old on moog";
	ssh moog rm -rf $(MOOG_HUDSON_HOME3)/ptII.old;
	@echo "`date`: Done removing $(MOOG_HUDSON_HOME3)/ptII.old on moog";
	#
	@echo "Create links to tar files"
	@echo "Make directories"
	-ssh moog mkdir $(MOOG_HUDSON_HOME3)/builds
	-ssh moog mkdir $(PTEXTERNAL_NIGHTLY)/builds
	#
	@echo "Remove old tar files and logs"
	ssh moog "find $(MOOG_HUDSON_HOME3)/builds -mtime +10 -ls -exec rm {} \;"
	ssh moog rm -f \
	   $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.tar.gz \
	   $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.src.tar.gz \
	   $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows-`date +%Y-%m-%d`.exe \
	   $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows_64-`date +%Y-%m-%d`.exe \
	   $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION).setup.mac-`date +%Y-%m-%d`.app.tar.gz \
	   $(MOOG_HUDSON_HOME3)/builds/ptIInightly_mail-`date +%Y-%m-%d`.txt \
	   $(MOOG_HUDSON_HOME3)/builds/ptIInightly-`date +%Y-%m-%d`.htm \
	   $(MOOG_HUDSON_HOME3)/builds/ptIInightly-`date +%Y-%m-%d`.txt \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.src.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows-`date +%Y-%m-%d`.exe \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows_64-`date +%Y-%m-%d`.exe \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_VERSION).setup.mac-`date +%Y-%m-%d`.app.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptIInightly_mail-`date +%Y-%m-%d`.txt \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptIInightly-`date +%Y-%m-%d`.htm \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptIInightly-`date +%Y-%m-%d`.txt \
	   $(MOOG_HUDSON_HOME3)/builds/ptII-latest.tar.gz \
	   $(MOOG_HUDSON_HOME3)/builds/ptII-latest.src.tar.gz \
	   $(MOOG_HUDSON_HOME3)/builds/ptII-latest_setup_windows.exe \
	   $(MOOG_HUDSON_HOME3)/builds/ptII-latest_setup_windows_64.exe \
	   $(MOOG_HUDSON_HOME3)/builds/ptII-latest.setup.mac.app.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII-latest.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII-latest.src.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII-latest_setup_windows.exe \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII-latest_setup_windows_64.exe \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII-latest.setup.mac.app.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptIInightly_mail-latest.txt \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptIInightly-latest.htm \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptIInightly-latest.txt
	#
	@echo "Remove contents of build directory"
	ssh moog rm -rf $(PTEXTERNAL_NIGHTLY)/builds
	ssh moog mkdir $(PTEXTERNAL_NIGHTLY)/builds
	@echo "Create links to tar files and logs"
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).tar.gz \
		$(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.tar.gz
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).src.tar.gz \
		$(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.src.tar.gz
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_WINDOWS_VERSION)_setup_windows.exe \
		$(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows-`date +%Y-%m-%d`.exe
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_WINDOWS_VERSION)_setup_windows_64.exe \
		$(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows_64-`date +%Y-%m-%d`.exe
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/gen-$(PTII_MAJOR_VERSION)/ptII$(PTII_VERSION).setup.mac.app.tar.gz \
		$(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION).setup.mac-`date +%Y-%m-%d`.app.tar.gz
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/logs/ptIInightly_mail.txt \
		$(MOOG_HUDSON_HOME3)/builds/ptIInightly_mail-`date +%Y-%m-%d`.txt
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/logs/ptIInightly.htm \
		$(MOOG_HUDSON_HOME3)/builds/ptIInightly-`date +%Y-%m-%d`.htm
	-ssh moog ln $(MOOG_HUDSON_HOME3)/ptII/adm/logs/ptIInightly.txt \
		$(MOOG_HUDSON_HOME3)/builds/ptIInightly-`date +%Y-%m-%d`.txt
	#
	@echo "Create links for tars and logs"
	-ssh moog ln -s $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.tar.gz \
		$(PTEXTERNAL_NIGHTLY)/builds
	-ssh moog ln -s $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.src.tar.gz \
		$(PTEXTERNAL_NIGHTLY)/builds
	-ssh moog ln -s $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows-`date +%Y-%m-%d`.exe \
		$(PTEXTERNAL_NIGHTLY)/builds
	-ssh moog ln -s $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows_64-`date +%Y-%m-%d`.exe \
		$(PTEXTERNAL_NIGHTLY)/builds
	-ssh moog ln -s $(MOOG_HUDSON_HOME3)/builds/ptII$(PTII_VERSION).setup.mac-`date +%Y-%m-%d`.app.tar.gz \
		$(PTEXTERNAL_NIGHTLY)/builds
	# 
	@echo "Create links for latest tars and logs"
	-ssh moog ln -s $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.tar.gz \
		$(PTEXTERNAL_NIGHTLY)/builds/ptII-latest.tar.gz
	-ssh moog ln -s $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_VERSION)-`date +%Y-%m-%d`.src.tar.gz \
		$(PTEXTERNAL_NIGHTLY)/builds/ptII-latest.src.tar.gz
	-ssh moog ln -s $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows-`date +%Y-%m-%d`.exe \
		$(PTEXTERNAL_NIGHTLY)/builds/ptII-latest_setup_windows.exe
	-ssh moog ln -s $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_WINDOWS_VERSION)_setup_windows_64-`date +%Y-%m-%d`.exe \
		$(PTEXTERNAL_NIGHTLY)/builds/ptII-latest_setup_windows_64.exe
	-ssh moog ln -s $(PTEXTERNAL_NIGHTLY)/builds/ptII$(PTII_VERSION).setup.mac-`date +%Y-%m-%d`.app.tar.gz \
	   $(PTEXTERNAL_NIGHTLY)/builds/ptII-latest.setup.mac.app.tar.gz

# Invoke the coverity scan.
coverity:
	(cd $(PTII); adm/bin/coverity)


# Build from the website
PTBUILDRELEASE=tmp/build/ptII$(VERSION)
build_release: $(PTBUILDRELEASE)

ptbuildrelease: $(PTBUILDRELEASE) $(PTBUILDRELEASE)/ptolemy/ptsupport.jar $(PTBUILDRELEASE)/signed $(PTBUILDRELEASE)/test.out clean_ptrelease_doit $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit clean_ptrelease_doit.out $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out clean_ptrelease_scpDemos.out $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out  $(PTBUILDRELEASE)/ptolemy/plot/dists.out

ptbuildrelease_jnlp: $(PTBUILDRELEASE) $(PTBUILDRELEASE)/ptolemy/ptsupport.jar $(PTBUILDRELEASE)/signed clean_ptrelease_doit $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit clean_ptrelease_doit.out

ptbuildrelease2: $(PTBUILDRELEASE)/test.out $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out  $(PTBUILDRELEASE)/ptolemy/plot/dists.out

ptbuildrelease3: $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out  $(PTBUILDRELEASE)/ptolemy/plot/dists.out

ptbuildrelease4: $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out $(PTBUILDRELEASE)/ptolemy/plot/dists.out

ptbuildrelease5: $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out $(PTBUILDRELEASE)/ptolemy/plot/dists.out

$(PTBUILDRELEASE):
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE) directory so that we can update the website"
	@echo "#"
	@echo "$@ `date`"
	if [ ! -d tmp/build ]; then mkdir -p tmp/build; fi
	#(cd tmp/build; wget http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII11.0/ptII$(VERSION).src.tar; tar -xf ptII$(VERSION).src.tar);
	(cd tmp/build; tar -xf ../../ptII$(VERSION).src.tar);


$(PTBUILDRELEASE)/ptolemy/ptsupport.jar:
	@echo "#"
	@echo "# Building in the $(PTBUILDRELEASE) directory so that we can update the website"
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; export JAVADOC=/usr/lib/jvm/jdk1.8.0_11/bin/javadoc; ./configure; ant; (cd bin; make); (cd doc; make codeDoc/tree.html); ant jars)

$(PTBUILDRELEASE)/signed:
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE)/signed directory so that we can update the website"
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; make jnlp_dist USER=`whoami` STOREPASSWORD="-storepass `cat ~/.certpw`" KEYPASSWORD="-keypass `cat ~/.certpw`")

$(PTBUILDRELEASE)/test.out:
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE)/test.out by running the longest tests, which export the models.  This will take about 100 minutes."
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; export DISPLAY=localhost:2.0; echo "DISPLAY=$$DISPLAY"; ant test.longest >& test.out)

$(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit:
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit by running a script."
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; cd ptolemy/copernicus/applet; ./updateDemos >& doit)

clean_ptrelease_doit:
	rm -f $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit

$(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out:
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out by running the doit script."
	@echo "# This will run export the models for WebStart."
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; export DISPLAY=localhost:2.0; cp $(HOME)/ptKeystore.properties . ; (cd bin; make); cd ptolemy/copernicus/applet; sh ./doit >& doit.out)

clean_ptrelease_doit.out:
	rm -f $(PTBUILDRELEASE)/ptolemy/copernicus/applet/doit.out

$(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out:
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out by running the scpDemos script."
	@echo "# This will copy the JNLP and WebStart demos to the website"
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; cd ptolemy/copernicus/applet; ./scpDemos >& scpDemos.out)

clean_ptrelease_scpDemos.out:
	rm -f $(PTBUILDRELEASE)/ptolemy/copernicus/applet/scpDemos.out

dists.out: $(PTBUILDRELEASE)/ptolemy/plot/dists.out
$(PTBUILDRELEASE)/ptolemy/plot/dists.out:
	@echo "#"
	@echo "# Creating the $(PTBUILDRELEASE)/ptolemy/plot/adm direcotry by running 'make dists' in plot/."
	@echo "# This will update the Ptplot pages on the website."
	@echo "#"
	@echo "$@ `date`"
	(cd tmp/build/ptII$(VERSION); export PTII=`pwd`; cd ptolemy/plot; rm -rf adm tmp; make dists >& dists.out && make update_ftp)
clean_ptrelease_dists.out:
	rm -f $(PTBUILDRELEASE)/ptolemy/plot/dists.out

