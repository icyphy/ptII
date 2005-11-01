# Makefile to create tar files for distribution
# Version: $Id$
# Copyright (c) 1994-2005 The Regents of the University of California.
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
MAJOR_VERSION = 	5.1
MINOR_VERSION =		alpha

VERSION=	$(MAJOR_VERSION)-$(MINOR_VERSION)
# Version string for lame filesystems that don't like more than one .
WINDOWS_VERSION =	5_2_alpha

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
PTII_FTP = /vol/ptolemy/pt0/ptweb/ptolemyII/ptII$(MAJOR_VERSION)

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
# make USER=cxh PTIIHOME=/cygdrive/c/cxh/ptII-4.0 COMPRESS=gzip TAR=/usr/local/bin/tar clean all src.jar 
# make USER=cxh PTIIHOME=/cygdrive/d/tmp/ptII COMPRESS=gzip TAR=/usr/local/bin/tar
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII3.1alpha PTIIADM=/home/eecs/cxh/ptII/adm TAR=/usr/local/bin/tar
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII PTIIADM=/home/eecs/cxh/ptII/adm TAR=/usr/local/bin/tar
# make USER=cxh PTIIHOME=/export/home1/cxh/src/ptII PTIIADM=/export/home1/cxh/src/ptII/adm TAR=/usr/sfw/bin/gtar JAR=/usr/bin/jar clean all src.jar 
#make USER=cxh PTIIHOME=/tmp/cxh/ptII PTIIADM=/tmp/cxh/ptII/adm TAR=/bin/tar JAR=/usr/local/j2sdk1.4.2_01/bin/jar clean all src.jar 

#make USER=cxh PTIIHOME=/tmp/cxh/tmp/ptII PTIIADM=/tmp/cxh/tmp/ptII/adm TAR=/bin/tar JAR=/usr/local/j2sdk1.4.2_01/bin/jar src.jar

USER =		ptII
PTIIHOME =	/users/ptII
PTIIADM =	$(PTIIHOME)/adm

# Should be this directory
GENDIR =	$(PTIIADM)/gen-$(MAJOR_VERSION)

# Temporary location of the distribution so that we can
# have the tar files have the version number in them
PTIIDIST = 	$(PTIIADM)/dists/ptII$(VERSION)

# Directories and files in the top level ptII directory that we should
# include in the tar file.
PTII_TOPDIR=	.cshrc \
		.classpath.in \
		.classpath.default \
		.eclipse.epf \
		README.txt \
		bin \
		build.xml \
		com/JLex \
		com/makefile \
		com/microstar \
		config \
		configure \
		configure.in \
		copyright.txt \
		diva \
		doc \
		lib \
		makefile \
		mk \
		ptolemy \
		thales \
		util \
		vergil.jnlp.in \
		vergilDSP.jnlp.in \
		vergilHyVisual.jnlp.in \
		vergilPtiny.jnlp.in \
		vergilPtinySandbox.jnlp.in \
		vergilVisualSense.jnlp.in

# Location of the CVS Repository
CVS_REPOSITORY = :ext:gigasource.eecs.berkeley.edu:/home/cvs

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

# Rebuild the distributions
rebuild: clean all installshield_app build_unixPtII

clean:
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

FINDCLEAN = $(GENDIR)/findclean

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
	mkdir $@
	#@echo "Be sure that ptolemy/media/javasound/javasound.jar exists"
	#(cd $(PTIIHOME)/ptolemy/media/javasound; make jars)
	#@echo "Be sure that ptolemy/actor/lib/javasound/javasound.jar exists"
	#(cd $(PTIIHOME)/ptolemy/actor/lib/javasound; make jars)
	(cd $(PTIIHOME); \
		$(TAR) -c --no-anchored \
			--exclude="CVS" \
			--exclude="*codeDoc*" \
			-X $(GENDIR)/src.ex \
			-X $(GENDIR)/src1.ex \
		$ -f - $(PTII_TOPDIR) )| \
		(cd $@; $(TAR) -xpf -)
	@echo "Removing codeDoc directories and other documentation"
	@echo "  tar -X does not seem to work under Cygwin"
	rm -rf $(PTIIDIST)/doc/codeDoc* $(PTIIDIST)/config/*.jar \
		$(PTIIDIST)/config/*.class
	-(cd $(PTIIDIST); $(FINDCLEAN))
	(cd $(PTIIDIST); rm -rf `cat $(GENDIR)/src.ex`)
	@echo "Regenerate the documentation with comm, gr, JAI, JMF etc."
	#(cd $(PTIIDIST)/doc; make JAVADOC=javadoc all_packages)
	#@echo "Do make realclean to clean up any trash"
	#cd $(PTIIDIST); make clean
	#cd $(PTIIDIST); make
	#cd $(PTIIDIST); make javadocs
	rm -f $(PTIIDIST)/.glimpse*
	cp $(PTIIHOME)/.glimpse_exclude $(PTIIDIST)
	touch $(PTIIDIST)/configure
	# Bump up the mod time of $PTII/mk/ptII.mk.in so that if
	# the user runs make, then configure will run 
	touch $(PTIIDIST)/mk/ptII.mk.in
	# Remove apps.xml and corba.xml from the configuration
	egrep -v "apps.xml|corba.xml" \
		< $(PTIIHOME)/ptolemy/configs/defaultFullConfiguration.xml \
		> /tmp/defaultFullConfiguration_tmp.xml
	mv /tmp/defaultFullConfiguration_tmp.xml \
		$(PTIIDIST)/ptolemy/configs/defaultFullConfiguration.xml

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
		$(TAR) -c --no-anchored -X $(GENDIR)/src.ex \
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
		$(TAR) -xf $(PTII_FULL); \
		cd ptII$(VERSION); \
		echo "About to configure `pwd`"; \
		PTII=`pwd` ./configure; make -k clean; \
		touch mk/ptII.mk.in; \
		find . -name "*.class" -ls -exec rm {} \; ;\
		cd ptolemy; find . -name ptinyos -prune -o -name "*.jar" -ls -exec rm {} \; ;\
		cd ..; \
		cd ptolemy/domains/ptinyos; find . -name jdom.jar -prune -o -name nesc-dump.jar -prune -o -name "*.jar" -ls -exec rm {} \; ;\
		cd ../../..; \
		find doc -name "*.jar" -ls -exec rm {} \; ;\
		rm -rf doc/codeDoc*; \
		rm -rf `cat $(GENDIR)/src.ex`; \
		echo "About to remove unneeded sources"; \
		rm -rf $(PTII_SRC_TO_BE_REMOVED); \
		ls $(PTII_SRC_TO_BE_REMOVED); \
		$(FINDCLEAN); \
		cd ..; \
		echo "#  Starting tar of $@"; \
		$(TAR) -c --no-anchored -X $(GENDIR)/src.ex -f $(PTII_SRC) ptII$(VERSION); \
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
		jni \
		bin/comm.policy \
		bin/copernicus.bat \
		bin/copernicusjhdl.bat \
		bin/decompile.bat \
		bin/emachine.bat \
		bin/giottoc.bat \
		bin/histogram.bat \
		bin/hyvisual \
		bin/jode.bat \
		bin/mescal.bat \
		bin/moml.bat \
		bin/obfuscate.bat \
		bin/ptexecute.bat \
		bin/ptjacl.bat \
		bin/ptjaclapp.bat \
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
		build.xml \
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
		doc/ptII5.0.releaseVergil.htm \
		lib/cachedir \
		lib/ptII.properties \
		lib/ptvr.jar \
		lib/saxon8.jar \
		jni \
		mk/ptII.mk \
		mk/codegen.mk \
		mk/jnlp.mk \
		mk/kvm.mk \
		mk/lejos.mk \
		mk/midp.mk \
		mk/tini.mk \
		mk/waba.mk \
		ptolemy/actor/gui/style/test/alljsimpletests.tcl \
		ptolemy/actor/gui/test/alljsimpletests.tcl \
		ptolemy/actor/lib/security/demo/Signature/ptKeystore \
		ptolemy/actor/lib/security/test/foo.keystore \
		ptolemy/actor/lib/test/Const.txt \
		ptolemy/actor/lib/test/ReadFile1Output.txt \
		ptolemy/actor/lib/x10/ApplianceController.class \
		ptolemy/actor/lib/x10/CommandListener.class \
		ptolemy/actor/lib/x10/LampController.class \
		ptolemy/actor/lib/x10/LevelListener.class \
		ptolemy/actor/lib/x10/Listener.class \
		ptolemy/actor/lib/x10/Receiver$$1.class \
		ptolemy/actor/lib/x10/Receiver.class \
		ptolemy/actor/lib/x10/Receiver$$CommandListener.class \
		ptolemy/actor/lib/x10/Sender.class \
		ptolemy/actor/lib/x10/X10Interface.class \
		ptolemy/actor/lib/x10/demo/MotionDrivenDemos/shutdown.bat \
		ptolemy/backtrack \
		ptolemy/codegen \
		ptolemy/component \
		ptolemy/domains/giotto/cgc \
		ptolemy/domains/gr/demo/Demo1 \
		ptolemy/domains/gr/lib/vr \
		ptolemy/domains/hs \
		ptolemy/distributed \
		ptolemy/hsif \
		ptolemy/matlab/ptmatlab.exp \
		ptolemy/matlab/ptmatlab.lib \
		ptolemy/matlab/ptmatlab.obj \
		ptolemy/media/test/tmp2_7.au \
		ptolemy/moml/jxta/demo/EPtolemyA/EPtolemy.properties \
		ptolemy/moml/jxta/JXTAClassLoader.class \
		ptolemy/moml/jxta/JXTALibrary.class \
	        ptolemy/plot/ptplotlocales \
		ptolemy/util/test/FileUtilities-1.1.tmp \
		ptolemy/vergil/toolbox/test/alljsimpletests.tcl

BASE_SRC_TO_BE_REMOVED = \
		doc/usingVergil \
		lib/hsif \
		lib/jni.jar \
		lib/chic.jar \
		lib/colt.jar \
		lib/jasminclasses.jar \
		lib/java_cup.jar \
		lib/mapss.jar \
		lib/ptCal.jar \
		lib/saxon.jar \
		lib/sootclasses.jar \
		lib/synthesis.jar \
		ptolemy/actor/gui/python \
		ptolemy/actor/lib/conversions/test/auto/Round.xml \
		ptolemy/actor/lib/ecsl \
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
		ptolemy/configs/jni \
		ptolemy/configs/jxta \
		ptolemy/configs/SingleWindow \
		ptolemy/configs/ptiny \
		ptolemy/configs/copernicus \
		ptolemy/copernicus \
		ptolemy/domains/ci \
		ptolemy/domains/ct/lib/IPCInterface.java \
		ptolemy/domains/ct/lib/IPCInterface.class \
		ptolemy/domains/dde \
		ptolemy/domains/ddf \
		ptolemy/domains/domains.xml \
		ptolemy/domains/dt \
		ptolemy/domains/ddf/test/auto/controlled_kahnnet.xml \
		ptolemy/domains/fsm/kernel/test/auto/booleanSelect.xml \
		ptolemy/domains/fsm/kernel/test/auto/feedBackLoop.xml \
		ptolemy/domains/fsm/test/auto/booleanSelect.xml \
		ptolemy/domains/fsm/test/auto/feedBackLoop.xml \
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
		thales \
		vergil.jnlp.in \
		vergilDSP.jnlp.in \
		vergilHyVisual.jnlp.in \
		vergilPtiny.jnlp.in \
		vergilPtinySandbox.jnlp.in \
		vergilVisualSense.jnlp.in

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
		ptolemy/configs/ptiny \
		ptolemy/domains/csp \
		ptolemy/domains/pn \
		ptolemy/domains/ptinyDomains.xml \
		ptolemy/domains/ptinyos \
		ptolemy/domains/wireless \
		ptolemy/configs/visualsense

# Files to be removed from HyVisual source distributions
VIPTOS_SRC_TO_BE_REMOVED = \
		$(BASE_SRC_TO_BE_REMOVED) \
		doc/codeDocHyVisual \
		doc/codeDocHyVisual.jar \
		doc/codeDocVisualSense \
		doc/codeDocVisualSense.jar \
		ptolemy/configs/hyvisual

# Location of the jar binary.  Cygwin /bin/jar is broken.
JAR=c:/j2sdk1.4.2_05/bin/jar

# The Jar file that we create.
src.jar: $(PTII_SRC_JAR)
$(PTII_SRC_JAR): src_jar
	(cd src_jar/ptII$(VERSION); $(JAR) -cf ../../ptII$(VERSION).src.jar .)

# The directory we jar up.
src_jar: #$(PTII_SRC) 
	mkdir src_jar;
	(cd src_jar; $(TAR) -xf $(PTII_SRC))
	rm -f src_jar/ptII$(VERSION)/doc/design/hyvisual.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/ptIIdesign1-intro.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/ptIIdesign2-software.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/ptIIdesign3-domains.pdf
	rm -f src_jar/ptII$(VERSION)/doc/design/visualsense.pdf
	(cd src_jar/ptII$(VERSION); $(FINDCLEAN))
	(cd src_jar/ptII$(VERSION); rm -rf `cat ../../src.ex`)
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
	egrep -v "csp.jar|ddf.jar|hdf.jar|pn.jar|domains.xml|ptinyDomains.xml|ci.jar|dde.jar|dt/dt.jar|giotto.jar|petrinet.jar|tm.jar|sr.jar|wireless.jar"	\
		src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/domains/makefile
	# Fix up configs/makefile
	egrep -v "dsp.jar|full.jar|jni.jar|jxta.jar|ptiny.jar|visualsense.jar" \
		src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile \
		> src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile.tmp \
		src_jar/hyvisual$(VERSION)/ptolemy/configs/makefile \
	# Fix up doc/design/makefile
	egrep -v "ptIIdesign1-intro.pdf|ptIIdesign2-software.pdf|ptIIdesign3-domains.pdf|visualsense.pdf|visualsense.jar|usingVergil" \
		src_jar/hyvisual$(VERSION)/doc/design/makefile \
		> src_jar/hyvisual$(VERSION)/doc/design/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/doc/design/makefile.tmp \
		src_jar/hyvisual$(VERSION)/doc/design/makefile
	# Fix up doc/makefile
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd|lib/comm|lib.comm|python|security|ci/kernel|ci/lib|copernicus|csp|dde|ddf|dt.kernel|giotto|gr.kernel|gr.lib|io.comm|javasound|jni |lib.xslt|lib.xslt|pn/kernel|psdf|petrinet|ptolemy.caltrop|ptolemy.domains.ci|ptolemy.domains.pn|sr.kernel|sr/kernel|sr.lib|sr/lib|thales.actor|thales.vergil|tm.kernel|tm/kernel|tm.lib|tm/lib|wireless" \
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
		src_jar/hyvisual$(VERSION)/makefile | \
		sed 's/\$$(PTJNI_DIR)//' \
		> src_jar/hyvisual$(VERSION)/makefile.tmp
	mv src_jar/hyvisual$(VERSION)/makefile.tmp \
		src_jar/hyvisual$(VERSION)/makefile \
	#cd src_jar/hyvisual$(VERSION); $(FINDCLEAN))
	(cd src_jar/hyvisual$(VERSION); rm -rf `cat ../../src.ex`)

###############
# Viptos

VIPTOS_SRC_JAR = viptos$(VERSION).src.jar

# The Jar file that we create.
viptos.jar: $(VIPTOS_SRC_JAR)
$(VIPTOS_SRC_JAR): src_jar/viptos$(VERSION)
	(cd src_jar/viptos$(VERSION); $(JAR) -cf ../../viptos$(VERSION).src.jar .)

VIPTOS_SRC_TAR = viptos$(VERSION).src.tar.gz
# The Jar file that we create.
viptos: $(VIPTOS_SRC_TAR)
viptos.tar: $(VIPTOS_SRC_TAR)
$(VIPTOS_SRC_TAR): src_jar/viptos$(VERSION)
	(cd src_jar; \
		$(TAR) -czf ../viptos$(VERSION).src.tar.gz \
		viptos$(VERSION) \
	)
		#cp $(PTIIHOME)/doc/design/viptos.pdf \
		#	viptos$(VERSION)/doc/design/viptos.pdf; \


# The directory we jar up.
# We include DE because some of the kernel/util tests use it.  CT demos also.
viptos_src_jar: src_jar/viptos$(VERSION)
src_jar/viptos$(VERSION): $(PTII_SRC_JAR)
	if [ ! -d src_jar/viptos$(VERSION) ]; then \
		 mkdir -p src_jar/viptos$(VERSION); \
	fi
	(cd src_jar/viptos$(VERSION); \
		$(JAR) -xf ../../ptII$(VERSION).src.jar)

	(cd src_jar/viptos$(VERSION); \
		rm -rf $(PTII_SRC_TO_BE_REMOVED); \
		rm -rf $(VIPTOS_SRC_TO_BE_REMOVED) thales; \
		rm -rf ptolemy/domains/ptinyos/demo/*/output; \
	   touch configure; \
	   chmod a+rx configure util/testsuite/chkextra; \
	   (cd ptolemy/domains/ptinyos; \
		chmod a+rx util/unzip-opts.sh util/nc2moml/nc2moml;
		chmod a+rx util/ncapp2moml/ncapp2moml util/zip-opts.sh);
	)
	# README_VIPTOS.txt
	echo "Viptos is an interface between TinyOS and Ptolemy II" \
		> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "To get started, you must have the AVR tools, TinyOS and Nesc installed" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "and some environment variables must be set." \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "For details, see ptolemy/domains/util/nc2moml/index.htm." \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "":\
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "The quick installation guide:" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "1. Install the AVR tools, TinyOS, Nesc" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "   as per ptolemy/ptinyosdomains/util/nc2moml/index.htm." \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "2. Be sure the following environment variables are set:" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "   TOSDIR, PTINYOS_MOMLROOT, NC2MOMLLIB_NESC and TOSROOT" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "3. Set PTII to this directory.  See doc/install.htm for details." \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "4. Run     ./configure" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "5. Run     make fast" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	echo "6. Run     bin/viptos" \
		>> src_jar/viptos$(VERSION)/README_VIPTOS.txt
	# Fix up bin/makefile
	egrep -v "copernicus|decompile|emachine|hyvisual|giottoc|htmlconverter|jode|mescal|obfuscate|soot|tinic|startjini.gui" src_jar/viptos$(VERSION)/bin/makefile \
		> src_jar/viptos$(VERSION)/bin/makefile.tmp
	mv src_jar/viptos$(VERSION)/bin/makefile.tmp \
		src_jar/viptos$(VERSION)/bin/makefile
	# Fix up domains/makefile
	egrep -v "ddf.jar|domains.xml|ci.jar|dde.jar|dt/dt.jar|hdf/hdf.jar|giotto.jar|petrinet.jar|tm.jar|sr.jar"	\
		src_jar/viptos$(VERSION)/ptolemy/domains/makefile \
		> src_jar/viptos$(VERSION)/ptolemy/domains/makefile.tmp
	mv src_jar/viptos$(VERSION)/ptolemy/domains/makefile.tmp \
		src_jar/viptos$(VERSION)/ptolemy/domains/makefile
	# Fix up configs/makefile
	egrep -v "dsp.jar|full.jar|hyvisual.jar|jni.jar|jxta.jar|ptiny.jar|visualsense.jar" \
		src_jar/viptos$(VERSION)/ptolemy/configs/makefile \
		> src_jar/viptos$(VERSION)/ptolemy/configs/makefile.tmp
	mv src_jar/viptos$(VERSION)/ptolemy/configs/makefile.tmp \
		src_jar/viptos$(VERSION)/ptolemy/configs/makefile \
	# Fix up doc/design/makefile
	egrep -v "hyvisual.pdf|hyvisual.jar|ptIIdesign1-intro.pdf|ptIIdesign2-software.pdf|ptIIdesign3-domains.pdf|visualsense.pdf|visualsense.jar|usingVergil" \
		src_jar/viptos$(VERSION)/doc/design/makefile \
		> src_jar/viptos$(VERSION)/doc/design/makefile.tmp
	mv src_jar/viptos$(VERSION)/doc/design/makefile.tmp \
		src_jar/viptos$(VERSION)/doc/design/makefile
	# Fix up doc/makefile/img
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd"\
		src_jar/viptos$(VERSION)/doc/img/makefile \
		> src_jar/viptos$(VERSION)/doc/img/makefile.tmp
	mv src_jar/viptos$(VERSION)/doc/img/makefile.tmp \
		src_jar/viptos$(VERSION)/doc/img/makefile
	# Fix up doc/makefile
	egrep -v "PtolemyIISplash.psd|banner.psd|ptIIbanner2.psd|ci/kernel|ci/lib|copernicus|dde|ddf|dt.kernel|giotto|gr.kernel|gr.lib|jni |psdf|petrinet|ptolemy.caltrop|ptolemy.domains.ci|sr.kernel|sr/kernel|sr.lib|sr/lib|thales.actor|thales.vergil|tm.kernel|tm/kernel|tm.lib|tm/lib" \
		src_jar/viptos$(VERSION)/doc/makefile \
		| sed 's/codeDocHyVisual.jar codeDocVisualSense.jar//' \
		> src_jar/viptos$(VERSION)/doc/makefile.tmp
	mv src_jar/viptos$(VERSION)/doc/makefile.tmp \
		src_jar/viptos$(VERSION)/doc/makefile
	# Fix up actor/lib/test/makefile
	egrep -v OrderedMerge.tcl \
		src_jar/viptos$(VERSION)/ptolemy/actor/lib/test/makefile \
		> src_jar/viptos$(VERSION)/ptolemy/actor/lib/test/makefile.tmp
	mv src_jar/viptos$(VERSION)/ptolemy/actor/lib/test/makefile.tmp \
		src_jar/viptos$(VERSION)/ptolemy/actor/lib/test/makefile
	# Fix up ptolemy/makefile
	sed -e 's/\$$(PTCALTROP_DIR)//' \
	    -e 's/\$$(PTCOPERNICUS_DIR)//' \
		< src_jar/viptos$(VERSION)/ptolemy/makefile \
		> src_jar/viptos$(VERSION)/ptolemy/makefile.tmp
	mv src_jar/viptos$(VERSION)/ptolemy/makefile.tmp \
		src_jar/viptos$(VERSION)/ptolemy/makefile
	# Fix up makefile
	egrep -v "actor/lib/javasound/demo/demo.jar|domains/dt/demo/demo.jar|domains/giotto/demo/demo.jar|domains/pn/demo/demo.jar|domains/tm/demo/demo.jar|thales|jnlp.mk" \
		src_jar/viptos$(VERSION)/makefile | \
		sed 's/\$$(PTJNI_DIR)//' \
		> src_jar/viptos$(VERSION)/makefile.tmp
	mv src_jar/viptos$(VERSION)/makefile.tmp \
		src_jar/viptos$(VERSION)/makefile \
	#cd src_jar/viptos$(VERSION); $(FINDCLEAN))
	(cd src_jar/viptos$(VERSION); rm -rf `cat ../../src.ex`)

##########


update_ftp: update_ptii_ftp untar
update_ptii_ftp:
	@echo "Copying ptII files to website"
	scp -p $(PTII_FULL) $(PTII_SRC) \
		messier:$(PTII_FTP)
untar:
	ssh messier rm -rf $(PTII_FTP)/ptII$(VERSION)
	ssh messier "cd $(PTII_FTP); /usr/local/bin/tar -xf ptII$(VERSION).tar$(SUFFIX)"
	-echo "Copy gr.jar and codeDoc in from a windows box"

# Make the distribution, run htmlchek
htmlchek:
	-(cd $(PTIIHOME); cvs update -d -P)
	$(MAKE) clean
	$(MAKE)
	(cd $(PTIIDIST); PTII=$(PTIIDIST) make htmlchek) > $(PTIIDIST)/htmlchek.out 2>&1
	(cd $(PTIIDIST); ls -d `cat htmlchekout.HREF | grep -v '#' | grep -v http: | grep -v mailto: | grep -v ftp:` | grep 'not found' 2>&1)

# Stuff to do every night.
# ptII's crontab runs ~ptII/adm/bin/ptIInightly, which runs 'make nightly'
# when the remote jobs finish, the remotealldone rule is called, which
# runs the ptIInightly-mail script.
nightly: ptclean ptbuild \
	clean all \
	glimpse \
	findchanges \
	ChangeLog \
	jnlp \
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
	-(cd $(PTIIHOME); cvs update -d -P; PTII=/users/ptII $(MAKE) JFLAGS="-deprecation -g -deprecation -source 1.4" -k sources fast install)  > /users/$(USER)/logs/java`date  '+%m%d'` 2>&1

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
	(cd $(PTIIHOME); rm -f ChangeLog; \
		 $(MAKE) -k ChangeLog; \
		rm -f ChangeLog.txt; ln -s ChangeLog ChangeLog.txt)

# Build jnlp installers
jnlp:
	@echo "#"
	@echo "# Now running make jnlp in $(PTIIHOME)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(PTIIHOME);	PTII=$(PTIIHOME) \
		$(MAKE) -k jnlp_clean jnlp_all PTII_LOCALURL=http://ptolemy.eecs.berkeley.edu/~ptII/ptIItree)

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
				"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
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
REMOTE1HOST = andrews
REMOTE1HOME = /vol/andrews/andrews2/ptII
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


# caltrop requires -source 1.4
bldmastrcoverage:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr \
		PTII=/home/bldmastr/ptII \
		PTIIHOME=/home/bldmastr/ptII \
		VERSION= \
		JFLAGS="-deprecation -g -source 1.4" \
		JSDATABASE=/home/bldmastr/jsdatabase \
		JSOUTDIR=/home/www/cvswww/ptolemy/nightly \
		JSCLASSPATH=/usr/local/JavaScope/JavaScope.zip \
		remote1distclean \
		remote1configure \
		remote1doc \
		remote1distjsall \
		remote1distjsrestore \
		remote1build \
		remote1distnonjsall \
		remote1ChangeLog \
		remote1doccheck \
		remote1jnlp \
		remote4 \
		remote5

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
		$(MAKE) clean; \
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
	$(REMOTE1PTIIDIST)/ptolemy/actor/process \
	$(REMOTE1PTIIDIST)/ptolemy/actor/sched \
	$(REMOTE1PTIIDIST)/ptolemy/actor/util \
	$(REMOTE1PTIIDIST)/ptolemy/actor/corba \
	$(REMOTE1PTIIDIST)/ptolemy/actor/corba/util \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ci/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/kernel/solver \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/kernel/util \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/gr/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/gr/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/kernel/event \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptinyos/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ptinyos/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rtp/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rtp/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib/vq \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ddf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ddf/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dt/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/giotto/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/petrinet/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sr/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sr/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tm/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tm/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless/lib \
	$(REMOTE1PTIIDIST)/ptolemy/graph \
	$(REMOTE1PTIIDIST)/ptolemy/graph/analysis \
	$(REMOTE1PTIIDIST)/ptolemy/media \
	$(REMOTE1PTIIDIST)/ptolemy/media/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/moml \
	$(REMOTE1PTIIDIST)/ptolemy/domains/fsm/modal \
	$(REMOTE1PTIIDIST)/ptolemy/domains/fsm/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/fsm/kernel/ia \
	$(REMOTE1PTIIDIST)/ptolemy/domains/psdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/moml/filter \
	$(REMOTE1PTIIDIST)/com/JLex \
	$(REMOTE1PTIIDIST)/ptolemy/plot \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/jhdl \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/c \
	$(REMOTE1PTIIDIST)/ptolemy/copernicus/gui \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/toolbox \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/icon \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/tree \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/fsm/modal \
	$(REMOTE1PTIIDIST)/ptolemy/domains/hdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib \
	$(REMOTE1PTIIDIST)/ptolemy/actor/parameters \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/colt \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/comm \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/conversions \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/hoc \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/gui \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/logic \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/python \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/security \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/string \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/xslt \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/util \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/xmlparser \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack/ast \
	$(REMOTE1PTIIDIST)/ptolemy/backtrack \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/c/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/c/domains/sdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/c/domains/sdf/lib \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/c/actor/lib/conversions \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/c/actor/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ddf \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de \
	$(REMOTE1PTIIDIST)/ptolemy/domains/giotto \
	$(REMOTE1PTIIDIST)/ptolemy/domains/giotto/cgc \
	$(REMOTE1PTIIDIST)/ptolemy/domains/fsm \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf \
	$(REMOTE1PTIIDIST)/ptolemy/domains/tm \
	$(REMOTE1PTIIDIST)/ptolemy/domains/wireless \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop/actors \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop/ddi \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop \
	$(REMOTE1PTIIDIST)/ptolemy/caltrop/util \
	$(REMOTE1PTIIDIST)/ptolemy/gui \
	$(REMOTE1PTIIDIST)/ptolemy/hsif \
	$(REMOTE1PTIIDIST)/jni \
	$(REMOTE1PTIIDIST)/ptolemy/vergil \
	$(REMOTE1PTIIDIST)/ptolemy/configs 


# Directories we run make in before running make in the NONJSALLDIRS
NONJSALLCOREDIRS = \
	ptolemy/copernicus/kernel \
	ptolemy/copernicus/java \

# Directories to run tests in outside of JavaScope
NONJSALLDIRS = \
	ptolemy/copernicus/actor \
	ptolemy/copernicus/interpreted \
	ptolemy/copernicus/java \
	ptolemy/copernicus/shallow

#	ptolemy/copernicus/c
#	


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
				"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
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
			"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
			AUXCLASSPATH=":$(JSCLASSPATH)" \
			jsall JSINSTRFLAGS=;)

remote1distjsall_3:
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
remote1distjsrestore:
	@echo "#"
	@echo "# Now running make jsrestore in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
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
	( cd $(REMOTE1PTIIDIST)/ptolemy/copernicus/applet; \
		echo making jsrestore in ptolemy/copernicus/applet ; \
		$(MAKE) $(MFLAGS) $(MAKEVARS) jsrestore)

# Remove the JavaScope instrumentation
remote1doc:
	@echo "#"
	@echo "# Now running make in $(REMOTE1PTIIDIST)/doc"
	@echo "# on `hostname`"
	@echo "# We run make in the doc directory early so that"
	@echo "# the website link checker will find files in doc/codeDoc"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST)/doc; PTII=$(REMOTE1PTIIDIST) $(MAKE))

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
	(cd $(REMOTE1PTIIDIST);	rm -f ChangeLog; \
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k ChangeLog; \
		rm -f ChangeLog.txt; ln -s ChangeLog ChangeLog.txt)

remote1doccheck:
	@echo "#"
	@echo "# Now generating doccheck files in $(REMOTE1PTIIDIST)/doc"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST)/doc; rm -rf doccheck; \
		PTII=$(REMOTE1PTIIDIST) $(MAKE) -k doccheck)

# Build jnlp installers
remote1jnlp:
	@echo "#"
	@echo "# Now running make jnlp in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE1PTIIDIST);	PTII=$(REMOTE1PTIIDIST) \
		$(MAKE) -k jnlp_clean jnlp_all PTII_LOCALURL=http://www.gigascale.org/ptolemy/src/ptII)



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
			"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
			tests)
	@echo "$@ `date`"
	@echo "`uptime`"


#####################################################################
# Build on a remote host by doing a cvs checkout

# Host to run on
REMOTE3HOST = andrews

# Directory to build in
REMOTE3HOME = /vol/$(REMOTE3HOST)/$(REMOTE3HOST)1/ptII

# Actual ptII tree.  Note that there is no version number because
# we are checking it out from CVS
REMOTE3PTIIDIST = $(REMOTE3HOME)/ptII

# Log file to save the results in
REMOTE3LOG = $(GENLOGDIR)/remote3.out

remote3: remote3cvs
remote3cvs:  $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE3HOST) by doing a cvs checkout "
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	ssh -x -n $(REMOTE3HOST) "cd $(GENDIR); make remote3nuke >& $(REMOTE3LOG)"
	# ssh-ing and then using cvs to ssh results in
	# (cd /vol/gauss/gauss2/ptII; cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs checkout ptII; cvs update -P -d)
	# You have no controlling tty and no DISPLAY.  Cannot read passphrase.
	# so we do the cvs co locally
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
	@echo "# Now doing a cvs checkout on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@echo "`uptime`"
	(cd $(REMOTE3HOME); cvs -d $(CVS_REPOSITORY) checkout ptII)


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
	-(cd $(REMOTE3PTIIDIST); cvs -d $(CVS_REPOSITORY) checkout tcl; \
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
			"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
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
REMOTE4HOST = source

# Remote location of Ptolemy II tree for testing
#REMOTE4HOME = /vol/$(REMOTE4HOST)/$(REMOTE4HOST)2/ptII
REMOTE4HOME = /export/home1/bldmastr/pttmp/remote4
REMOTE4PTIIDIST = $(REMOTE4HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE4LOG = $(GENLOGDIR)/remote4.out

fortytwo_test: $(PTII_SRC)
	$(MAKE) REMOTE4HOME=$(GENDIR)/test \
		PTII_FULL=$(PTII_SRC) \
		remote4nuke remote4ptiidist remote4configure \
		remote4distclean remote4build \
		remote4javatest \
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
	cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
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
	(cd $(REMOTE4PTIIDIST);	PTII=$(REMOTE4PTIIDIST) $(MAKE) fast install)

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
REMOTE5HOST = source

# Directory to build in
#REMOTE5HOME = /vol/$(REMOTE5HOST)/$(REMOTE5HOST)1/ptIIsrc
REMOTE5HOME = /export/home1/bldmastr/pttmp/remote5

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
	cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
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
			"JTCLSHFLAGS=-Dptolemy.ptII.timeOutSeconds=46800 -Xmx256M"\
			test_long)



##########################################################################
# Build from a src tar file - similar to remote1, we only generate
# test case coverage from the /test/auto/*.xml tests

# Host to run on
REMOTE6HOST = andrews

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
				"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
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
				"JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true -Xmx256M" \
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


###########################################################################
# Rules to build and install shield version
INSTALLSHIELD_SRC=installshield
INSTALLSHIELD_APP=$(INSTALLSHIELD_SRC)/app
# Contains the application only files in ptolemy/config
INSTALLSHIELD_APP_ONLY=$(INSTALLSHIELD_SRC)/app_only
INSTALLSHIELD_PTINY=$(INSTALLSHIELD_SRC)/ptiny
# Contains the application only files in ptolemy/config and doc/codeDoc
INSTALLSHIELD_PTINY_ONLY=$(INSTALLSHIELD_SRC)/ptiny_only
INSTALLSHIELD_CODEDOC=$(INSTALLSHIELD_SRC)/codeDoc
INSTALLSHIELD_DESIGNDOC=$(INSTALLSHIELD_SRC)/designdoc
INSTALLSHIELD_DOC=$(INSTALLSHIELD_SRC)/doc
INSTALLSHIELD_EXPERIMENTAL=$(INSTALLSHIELD_SRC)/experimental

# DOC is last so that we can get the bits and piecs out of app/doc
# APP_ONLY is generated by PTINY_ONLY
# Run with:
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII1.0beta PTIIADM=/home/eecs/cxh/ptII/adm installshield_app
installshield_app: installshield $(INSTALLSHIELD_APP) \
			$(INSTALLSHIELD_CODEDOC) \
			$(INSTALLSHIELD_DESIGNDOC) \
			$(INSTALLSHIELD_EXPERIMENTAL) \
			$(INSTALLSHIELD_PTINY) \
			$(INSTALLSHIELD_PTINY_ONLY) \
			$(INSTALLSHIELD_DOC)

installshield:
	if [ ! -d $@ ]; then \
		echo "Making $@ directory"; \
		mkdir -p $@; \
	fi

clean_installshield:
	rm -rf $(INSTALLSHIELD_APP) \
	$(INSTALLSHIELD_APPONLY) \
	$(INSTALLSHIELD_CODEDOC) \
	$(INSTALLSHIELD_DESIGNDOC) \
	$(INSTALLSHIELD_DOC) \
	$(INSTALLSHIELD_EXPERIMENTAL) \
	$(INSTALLSHIELD_PTINY) \
	$(INSTALLSHIELD_PTINY_ONLY)

$(INSTALLSHIELD_APP): $(PTII_FULL)
	@echo "#"
	@echo "# Building $@"
	@echo "#"
	(cd $(INSTALLSHIELD_SRC); \
	$(TAR) -xzf ../ptII$(VERSION).tar.gz; \
	 mv ptII$(VERSION) `basename $@`)

INSTALLSHIELD_PTINY_DOMAINS=ct de fsm pn sdf
installshield_ptiny: $(INSTALLSHIELD_PTINY)
$(INSTALLSHIELD_PTINY):
	@echo "#"
	@echo "# Building $@"
	 mkdir -p $@ $@/lib $@/ptolemy/vergil
	 mkdir -p $@/doc/img $@/doc/design
	 -ls -l $@/ptolemy/actor/lib/javasound/javasound.jar \
		$@/ptolemy/media/javasound/javasound.jar
	 -mv $(INSTALLSHIELD_APP)/copyright.txt $@
	 -mv $(INSTALLSHIELD_APP)/doc/*Vergil.htm \
		$(INSTALLSHIELD_APP)/doc/acks.htm \
		$(INSTALLSHIELD_APP)/doc/authors.htm \
		$(INSTALLSHIELD_APP)/doc/default.css \
		$@/doc
	 -mv $(INSTALLSHIELD_APP)/doc/img/ptIIplanet.gif \
		$(INSTALLSHIELD_APP)/doc/img/PtolemyIISmall.gif \
		$@/doc/img
	 # FIXME: ideally, we should include a pdf viewer or the html version
	 -mv $(INSTALLSHIELD_APP)/doc/design/usingVergil.pdf \
		$@/doc/design/
	 -mv $(INSTALLSHIELD_APP)/doc/design/usingVergil \
		$@/doc/design/
	 -mv $(INSTALLSHIELD_APP)/lib/diva.jar $@/lib
	 # We adjust the config directory later in ptiny_only
	 -mv $(INSTALLSHIELD_APP)/ptolemy/ptolemy.jar \
		$(INSTALLSHIELD_APP)/ptolemy/configs/ \
		$@/ptolemy
	 -mv $(INSTALLSHIELD_APP)/ptolemy/vergil/vergil.jar $@/ptolemy/vergil
	 -for domain in $(INSTALLSHIELD_PTINY_DOMAINS); do \
	   mkdir -p $@/ptolemy/domains/$$domain \
		$@/ptolemy/domains/$$domain/lib; \
	   mv $(INSTALLSHIELD_APP)/ptolemy/domains/$$domain/demo/ \
		$@/ptolemy/domains/$$domain; \
	   mv $(INSTALLSHIELD_APP)/ptolemy/domains/$$domain/lib/$${domain}entities.xml \
		$@/ptolemy/domains/$$domain/lib; \
	 done
	 # Empty directory created by the for loop above.
	 rmdir $@/ptolemy/domains/pn/lib
	# FIXME: we should find this in the jar file?
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/sdf/lib/*Spectrum*.xml \
		$@/ptolemy/domains/sdf/lib 
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/ct/lib/*generators.xml \
		$@/ptolemy/domains/ct/lib 
	 # Set up for a few javasound
	 -mkdir -p $@/ptolemy/actor/lib/javasound/demo
	 -mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/javasound/javasound.xml \
	     $(INSTALLSHIELD_APP)/ptolemy/actor/lib/javasound/javasound.jar \
		$@/ptolemy/actor/lib/javasound/
	 -mkdir -p $@/ptolemy/media/javasound
	 -mv $(INSTALLSHIELD_APP)/ptolemy/media/javasound/javasound.jar \
		$@/ptolemy/media/javasound/
	 -mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/javasound/demo/KarplusStrong \
		$@/ptolemy/actor/lib/javasound/demo/
	 # Set up for other demos
	 -mkdir -p $@/ptolemy/moml/demo
	 -mv $(INSTALLSHIELD_APP)/ptolemy/moml/demo/Networked \
		$@/ptolemy/moml/demo/
	 -mkdir -p $@/ptolemy/data/type/demo
	 -mv $(INSTALLSHIELD_APP)/ptolemy/data/type/demo/Router \
		$@/ptolemy/data/type/demo
	 # FIXME: vergil looks for ../actor/lib/logic/logic.xml, so
	 # we don't find it in the jar file
	 -mkdir -p $@/ptolemy/actor/lib/logic
	 -mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/logic/logic.xml \
		$@/ptolemy/actor/lib/logic
	 -mkdir -p $@/ptolemy/actor/lib/conversions
	 -mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/conversions/conversions.xml \
		$@/ptolemy/actor/lib/conversions/
	 -mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/*.xml \
		$@/ptolemy/actor/lib/
	 -mkdir $@/ptolemy/plot
	 -mv $(INSTALLSHIELD_APP)/ptolemy/plot/plotapplication.jar \
		$@/ptolemy/plot


installshield_ptiny_only: $(INSTALLSHIELD_PTINY_ONLY)
$(INSTALLSHIELD_PTINY_ONLY): $(PTIIHOME)/doc/codeDocPtiny/tree.html
	@echo "#"
	@echo "# Building $@"
	mkdir -p $@/doc/codeDoc $@/ptolemy/configs
	-cp -r $(PTIIHOME)/doc/codeDocPtiny/* \
	 	 $@/doc/codeDoc
	# Big hack here.  We need to include intro.htm in ptiny_only, so
	# we move it back
	-mkdir	-p $(INSTALLSHIELD_APP_ONLY)/ptolemy/configs/
	# Move intro.htm into app_only
	-mv $(INSTALLSHIELD_PTINY)/ptolemy/configs/intro.htm \
	 	$(INSTALLSHIELD_APP_ONLY)/ptolemy/configs/
	# Move vergilConfiguration.xml into app
	-mv $(INSTALLSHIELD_PTINY)/ptolemy/configs/vergilConfiguration.xml \
	 	$(INSTALLSHIELD_APP_ONLY)/ptolemy/configs/
	# We then copy introPtiny.htm so as to get doc/demoPtiny.htm
	# instead of doc/demo.htm
	 -cp $(INSTALLSHIELD_PTINY)/ptolemy/configs/introPtiny.htm \
		$@/ptolemy/configs/intro.htm 
	# Copy vergilConfigurationPtiny.xml so that we get the right 
	# directors
	-cp $(INSTALLSHIELD_PTINY)/ptolemy/configs/vergilConfigurationPtiny.xml \
		$@/ptolemy/configs/vergilConfiguration.xml

$(PTIIHOME)/doc/codeDocPtiny/tree.html:
	@echo "#"
	@echo "# Creating $@"
	@echo "#"
	cd $(PTIIHOME)/doc; make codeDocPtiny/tree.html

#configsPtiny.jar: $(INSTALLSHIELD_PTINY)/ptolemy/configs/configsPtiny.jar
#$(INSTALLSHIELD_PTINY)/ptolemy/configs/configsPtiny.jar: \
#	$(INSTALLSHIELD_PTINY)/ptolemy/configs
#	(cd $(INSTALLSHIELD_PTINY); \
#		jar -cf ptolemy/configs/configsPtiny.jar \
#			ptolemy/configs)

#$(INSTALLSHIELD_PTINY)/ptolemy/configs:
#	mkdir $(INSTALLSHIELD_PTINY)/ptolemy/configs
#	mkdir $(INSTALLSHIELD_PTINY)/ptolemy/configs/doc
#	mv $(INSTALLSHIELD_APP)/ptolemy/configs/introPtiny.htm $@/intro.htm
#	mv $(INSTALLSHIELD_APP)/ptolemy/configs/vergilConfigurationPtiny.xml \
#		$@/vergilConfiguration.xml
#	mv $(INSTALLSHIELD_APP)/ptolemy/configs/doc/demosPtiny.htm \
#		$@/doc/demos.htm

installshield_designdoc: $(INSTALLSHIELD_DESIGNDOC)
$(INSTALLSHIELD_DESIGNDOC):
	@echo "#"
	@echo "# Building $@"
	mkdir -p $@/doc/design
	if [ -f $(INSTALLSHIELD_APP)/doc/design/design.pdf ]; then \
		mv $(INSTALLSHIELD_APP)/doc/design/design.pdf $@/doc/design/; \
	else \
		echo "WARNING: *** $(INSTALLSHIELD_APP)/doc/design/design.pdf does not exist"; \
		echo "no design doc yet" > $@/doc/design/design.pdf; \
	fi

$(INSTALLSHIELD_CODEDOC):
	@echo "#"
	@echo "# Building $@"
	 mkdir -p $@/doc
	 mv $(INSTALLSHIELD_APP)/doc/codeDoc $@/doc

$(INSTALLSHIELD_DOC):
	@echo "#"
	@echo "# Building $@"
	 mkdir -p $@
	 mv $(INSTALLSHIELD_APP)/doc $@/doc

$(INSTALLSHIELD_EXPERIMENTAL):
	@echo "#"
	@echo "# Building $@"
	 mkdir -p $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/dde $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/dt $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/giotto $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/gr $@/ptolemy/domains

ISPMP_LIB=/users/ptII/vendors/installshield/ispmp40/lib/

ISPMP_DEBUG_CLASSPATH=/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/classes/src: 

ISPMP_CLASSPATH=/vol/maury/maury1/ptII/vendors/installshield/ispmp40:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/help.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/icebrowserbean.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/icebrowserlitebean.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/ide.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/jhall.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/parser.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/product.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/swing.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/wizard.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/lib/xt.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/ppk/win32ppk.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/ppk/solarisppk.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/ppk/webppk.jar:/vol/maury/maury1/ptII/vendors/installshield/ispmp40/ppk/linuxppk.jar

ptiny.xml: ptII.xml ptiny.xsl
	java -classpath $(ISPMP_CLASSPATH) \
		com.installshield.isje.ISJE ptII.xml \
		-xsl ptiny.xsl \
		-saveas $@

# Run this on doppler with:
# rm unixPtiny.xml; make PTIIHOME=/home/eecs/cxh/ptII build_unixPtiny
unixPtiny.xml: ptiny.xml
	sed -e 's@[cC]:\\users\\cxh\\ptII\\adm\\gen-1.0\\installshield\\@$(GENDIR)/installshield/@' \
	    -e 's@designdoc\\doc\\design\\@designdoc/doc/design/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\jvms\\win32\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/jvms/win32/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\jvms\\solaris\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/jvms/solaris/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\jvms\\linux\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/jvms/linux/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\images\\InstallShield Images\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/images/InstallShield Images/@' \
	    -e 's@<property name="outputDirectory">default</property>@<property name="outputDirectory">$(GENDIR)/installshield</property>@' \
	    -e 's@ptiny\\copyright@ptiny/copyright@' \
		 $< > $@

build_unixPtiny: unixPtiny.xml
	java -classpath $(ISPMP_CLASSPATH) \
		-Dis.debug=12 \
		com.installshield.isje.ISJE $< \
		-build

# Run this on doppler with:
# rm unixPtII.xml; make PTIIHOME=/home/eecs/cxh/ptII build_unixPtII
unixPtII.xml: ptII.xml
	sed -e 's@[cC]:\\users\\cxh\\ptII\\adm\\gen-1.0\\installshield\\@$(GENDIR)/installshield/@' \
	    -e 's@designdoc\\doc\\design\\@designdoc/doc/design/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\jvms\\win32\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/jvms/win32/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\jvms\\solaris\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/jvms/solaris/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\jvms\\linux\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/jvms/linux/@' \
	    -e 's@C:\\Program Files\\InstallShield\\ISMPP40\\images\\InstallShield Images\\@/vol/maury/maury1/ptII/vendors/installshield/ispmp40/images/InstallShield Images/@' \
	    -e 's@<property name="outputDirectory">default</property>@<property name="outputDirectory">$(GENDIR)/installshield</property>@' \
	    -e 's@ptiny\\copyright@ptiny/copyright@' \
		 $< > $@

build_unixPtII: unixPtII.xml
	java -classpath $(ISPMP_CLASSPATH) \
		-Dis.debug=12 \
		com.installshield.isje.ISJE $< \
		-build


SOFTWALLS = softwalls/ptolemy/apps/softwalls
softwalls:
	mkdir softwalls
	(cd $(PTII); tar -cf - ptolemy/apps/softwalls) | (cd softwalls; tar -xf -)
	rm -rf softwalls/ptolemy/apps/softwalls/surfaces \
		$(SOFTWALLS)/CVS
	mkdir -p $(SOFTWALLS)/demo/fixedTime
	cp $(HOME)/src/softwalls/ptolemy/softwallsSimulator/fixedTime.xml \
		$(SOFTWALLS)/demo/fixedTime
	# Copy uncompressed data files
	cp -r $(HOME)/src/softwalls/ptolemy/softwallsSimulator/surfaces/softwall.final.*.data \
		$(SOFTWALLS)/demo/fixedTime
	cp README_Soft_Walls.txt softwalls

softwalls.tar.gz: softwalls
	rm -f softwalls.tar.gz softwalls.tar
	(cd softwalls; tar -cf ../softwalls.tar .)
	gzip -9 softwalls.tar
