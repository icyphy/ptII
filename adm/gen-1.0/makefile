# makefile to create tar files for distribution
# Version: @(#)makefile	1.90 09/22/00
# Copyright (c) 1994-2000 The Regents of the University of California.
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
MAJOR_VERSION = 	1.0
MINOR_VERSION =		.1

VERSION=	$(MAJOR_VERSION)$(MINOR_VERSION)
# Version string for lame filesystems that don't like more than one .
WINDOWS_VERSION =	1_0_1

# Even if you are building tar files from ptdesign, this should probably
# still say ptolemy
#PTII_SRC =		pt

# Location of utilities
# Don't use /usr/sww/bin/gtar sun4 binaries if you are building on a sol2
TAR = 		/usr/tools/gnu/bin/tar
ZIP =		/usr/sww/bin/zip
COMPRESS =	/usr/sww/bin/gzip
GUNZIP =	/usr/sww/bin/gunzip
NON_GNUTAR =	/bin/tar
SUFFIX =	gz

# Script to split tar files into smaller chunks
SPLITTAR =	/users/ptolemy/adm/bin/splittar

# FTP Directory
# Use this for release
PTII_FTP = /vol/ptolemy/pt0/ptweb/ptolemyII/ptII1.0

# Name and directory of distribution.  For testing, use ptdesign
# For release, use ptolemy
#USER =		ptolemy
# For use under Windows
# make USER=cxh PTIIHOME=//c/users/cxh/ptII/adm/trees/ptII1.0 COMPRESS=gzip TAR=tar PTIIADM=//c/users/cxh/ptII/adm 
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII1.0 PTIIADM=/home/eecs/cxh/ptII/adm
# Run this command on a JDK1.3 machine so we get javasound documentation
# 

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
		README.txt \
		bin \
		com/JLex \
		com/makefile \
		com/microstar \
		config \
		configure \
		configure.in \
		copyright.txt \
		doc \
		lib \
		makefile \
		mk \
		ptolemy \
		util

# Location of the CVS Repository
CVS_REPOSITORY = :ext:gigasource.eecs.berkeley.edu:/home/cvs

##############################################################################
# Files to be produced

PTII_FULL = 	  $(GENDIR)/ptII$(VERSION).tar.$(SUFFIX)
PTII_FULL_ZIP =    $(GENDIR)/ptII$(VERSION).src.zip
# Source only, no .class, .jar or javadoc
PTII_SRC =   $(GENDIR)/ptII$(VERSION).src.tar.$(SUFFIX)

VERGIL = 	  $(GENDIR)/vergil$(VERSION).src.tar.$(SUFFIX)

# Three forms we ship in.
PTII_SRCS = 	$(PTII_FULL) $(basename $(PTII_FULL)) $(PTII_FULL_ZIP)

PTII_DEVEL_SRC = $(GENDIR)/ptolemy.devel$(VERSION).src.tar.$(SUFFIX)

##############################################################################
# Targets
all: $(PTII_FULL) $(PTII_SRC) #$(PTII_FULL_ZIP) #$(PTII_DEVEL_SRC)

# Rebuild the distributions
rebuild: clean all codeDocPtiny installshield_app 

# rm unixPtII.xml; make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII1.0 PTIIADM=/home/eecs/cxh/ptII/adm  rebuildptiny
rebuildptiny: rebuild build_unixPtiny

# rm unixPtII.xml; make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII1.0 PTIIADM=/home/eecs/cxh/ptII/adm  rebuildptII
rebuildptII: rebuild build_unixPtII

# Codedoc directory for ptiny version
codeDocPtiny:
	(cd $(PTIIHOME)/doc; rm -rf codeDocPtiny; \
	make codeDocPtiny/tree.html JAVASOUNDJSRCS="ptolemy.actor.lib.javasound ptolemy.media.javasound")

clean:
	@echo "#"
	@echo "# Now cleaning old tar files in ~ptII/adm/gen-latest"
	@echo "#"
	@echo `date`
	rm -f $(PTII_FULL) $(PTII_FULL_ZIP) $(PTII_DEVEL_SRC) $(PTII_SRC)
	@echo "Move the directories to a different directory and remove"
	@echo "in the background"
	mkdir tobenuked
	-mv $(PTIIDIST) src installshield/* tobenuked
	rm -rf tobenuked &

# First we create a directory containing the distribution,
# then we modify the distribution slightly,
# then we tar it up.
ptiidist: $(PTIIDIST)
$(PTIIDIST):
	@echo "#"
	@echo "# Now creating $(PTIIDIST)"
	@echo "#"
	@echo `date`
	mkdir $@
	@echo "Be sure that ptolemy/media/javasound/javasound.jar exists"
	(cd $(PTIIHOME)/ptolemy/media/javasound; make jars)
	@echo "Be sure that ptolemy/actor/lib/javasound/javasound.jar exists"
	(cd $(PTIIHOME)/ptolemy/actor/lib/javasound; make jars)
	(cd $(PTIIHOME); \
		$(TAR) -c -X $(GENDIR)/src.ex  -X $(GENDIR)/src1.ex \
		$ -f - $(PTII_TOPDIR) )| \
		(cd $@; $(TAR) -xpf -)
	@echo "Removing codeDoc directories and other documentation"
	@echo "  tar -X does not seem to work under Cygwin"
	-find $(PTIIDIST) -name codeDoc -exec rm -rf {} \;
	-find $(PTIIDIST) -name CVS -exec rm -rf {} \;
	rm -rf $(PTIIDIST)/ptolemy/plot/adm  $(PTIIDIST)/adm
	@echo "Regenerate the documentation with gr and javasound"
	@echo "  This requires JDK1.3, so run it on doppler"
	(cd $(PTIIDIST)/doc; make JAVADOC=javadoc JDOCFLAGS="-author -version -J-Xmx132m" GRJSRCS="ptolemy.domains.gr.kernel ptolemy.domains.gr.lib" JAVASOUNDJSRCS="ptolemy.actor.lib.javasound ptolemy.media.javasound")
	#@echo "Do make realclean to clean up any trash"
	#cd $(PTIIDIST); make clean
	#cd $(PTIIDIST); make
	#cd $(PTIIDIST); make javadocs
	rm -f $(PTIIDIST)/.glimpse*
	cp $(PTIIHOME)/.glimpse_exclude $(PTIIDIST)

clean_src:
	rm -f $(PTII_FULL)
src: $(PTII_FULL) #$(PTII_FULL_ZIP)
PTII_FULL: $(PTII_FULL) #$(PTII_FULL_ZIP)
$(PTII_FULL): $(PTIIDIST)
	@echo "Creating $@"
	(cd $(PTIIDIST); chmod -R a+rX .)
	(cd $(PTIIDIST)/..; \
	$(TAR) -c -X $(GENDIR)/src.ex \
	 -f $(basename $@) \
	ptII$(VERSION))
	@echo "We will provide gzipped and non-gzipped tars"
	cp $(basename $@) $(basename $@).bak
	$(COMPRESS) $(basename $@)
	mv $(basename $@).bak $(basename $@)
	chmod g+w $@ $(basename $@)
	-chgrp ptolemy $@ $(basename $@)
	ls -l $@ $(basename $@)

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


ptii_src: $(PTII_SRC)
$(PTII_SRC): $(PTII_FULL)
	@echo "Creating $@"
	-mkdir src
	(cd src; \
		$(TAR) -zxf $(PTII_FULL); cd ptII$(VERSION); \
		PTII=`pwd` ./configure; make -k clean; \
		find . -name "*.class" -ls -exec rm {} \; ;\
		cd ptolemy; find . -name "*.jar" -ls -exec rm {} \; ;\
		cd ..; \
		rm -rf doc/codeDoc; \
		cd ..; \
		echo "#  Starting tar of $@"; \
		$(TAR) -zcf $(PTII_SRC) ptII$(VERSION));

update_ftp: update_ptii_ftp untar
update_ptii_ftp:
	@echo "Copying ptII files to website"
	gzip installshield/ptII$(WINDOWS_VERSION)setup.jar
	cp -p $(PTII_FULL) $(PTII_SRC) \
		installshield/ptII$(WINDOWS_VERSION)setup.jar.gz \
		installshield/applet/ptII$(WINDOWS_VERSION)setup.exe \
		$(PTII_FTP)
untar:
	rm -rf $(PTII_FTP)/ptII$(VERSION)
	cd $(PTII_FTP); /usr/tools/gnu/bin/tar -zxf $(PTII_FULL)

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
	remote4

####################################
# Rules that operate in ~ptII

# clean in ~ptII
ptclean:
	@echo "#"
	@echo "# Now running make clean in ~ptII "
	@echo "#"
	@echo `date`
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
	@echo `date`
	rm -f /users/$(USER)/logs/java`date  '+%m%d'`
	-(cd $(PTIIHOME); cvs update -d -P; PTII=/users/ptII $(MAKE) JFLAGS="-deprecation -g -source 1.4" JDOCFLAGS="-author -version -J-Xmx132m" -k sources install)  > /users/$(USER)/logs/java`date  '+%m%d'` 2>&1

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
REMOTE1HOME = /vol/tycho/tycho1/ptII
REMOTE1PTIIDIST = $(REMOTE1HOME)/ptII$(VERSION)

# Host to run on
REMOTE1HOST = tycho

# Log file to save the results in
REMOTE1LOG = $(GENLOGDIR)/remote1.out

# JavaScope database, gets removed every night
# Note that this must be a directory called jsdatabase in the user's
# home directory.  Overriding this is tricky, it requires setting
# up a javascope.properties file that can be read at runtime
JSDATABASE=/users/ptII/jsdatabase

# Directory to place output in
JSOUTDIR=/users/ptII/public_html/nightly/js

# Create a separate tree and run the test suite with code coverage
# on REMOTE1HOST
remote1: remote1coverage
remote1coverage: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE1HOST) for code coverage"
	@echo "#"
	@echo `date`
	ssh $(REMOTE1HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		remote1nuke remote1ptiidist remote1configure \
		remote1distclean \
		remote1distjsall remote1distjsrestore remote1distjavadocs \
		remote1done >& $(REMOTE1LOG)" &


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
		remote1distjsall \
		remote1distjsrestore \
		remote1build

# Remove the remote dist
remote1nuke:
	rm -rf $(REMOTE1PTIIDIST) $(REMOTE1DONEFILE)

# untarthe remote dist.  Usually this is run on the remote machine
remote1ptiidist: $(REMOTE1PTIIDIST)
$(REMOTE1PTIIDIST): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE1HOME); $(TAR) -xf $(GENDIR)/`basename $(PTII_FULL) .gz`)

remote1configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE1PTIIDIST);	rm -f config.cache; PTII=$(REMOTE1PTIIDIST) configure)

# Clean the remote1 tree
remote1distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE1PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE1PTIIDIST); \
		$(MAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)

remote1distjavadocs:
	@echo "#"
	@echo "# Now generating javadocs in $(PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE1PTIIDIST); $(OTHERMAKE) -k javadocs)

# JSALLDIRS contains the directory that has the java files, not the test dir.
# Kernel first, then data, then actor, then alphabetical
# If you edit JSALLDIRS, then you should probably add the directory to
# $PTII/doc/makefile in two places so that we automatically generate docs.
# Tests in actor/lib use DE, so actor/lib should be after de/lib
# Tests in actor/lib/auto use vergil and moml
# dt/kernel should be run after sdfes
# We include the following directories so that they will appear in the
# code coverage stats even though they are graphical: gui actor/gui
#
# Don't run
#	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib/huffman
# it hangs
#
JSALLDIRS = \
	$(REMOTE1PTIIDIST)/ptolemy/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/kernel/util \
	$(REMOTE1PTIIDIST)/ptolemy/data \
	$(REMOTE1PTIIDIST)/ptolemy/data/expr \
	$(REMOTE1PTIIDIST)/ptolemy/data/type \
	$(REMOTE1PTIIDIST)/ptolemy/actor \
	$(REMOTE1PTIIDIST)/ptolemy/actor/gui \
	$(REMOTE1PTIIDIST)/ptolemy/actor/process \
	$(REMOTE1PTIIDIST)/ptolemy/actor/sched \
	$(REMOTE1PTIIDIST)/ptolemy/actor/util \
	$(REMOTE1PTIIDIST)/ptolemy/actor/corba \
	$(REMOTE1PTIIDIST)/ptolemy/actor/corba/util \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/csp/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/kernel/solver \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/kernel/util \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/ct \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dde/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/de/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/fsm/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/giotto/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/gr/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/gr/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/pn/kernel/event \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rtp/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/rtp/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib/vq \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/lib/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf \
	$(REMOTE1PTIIDIST)/ptolemy/domains/dt/kernel \
	$(REMOTE1PTIIDIST)/ptolemy/graph \
	$(REMOTE1PTIIDIST)/ptolemy/math \
	$(REMOTE1PTIIDIST)/ptolemy/media \
	$(REMOTE1PTIIDIST)/ptolemy/media/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/moml \
	$(REMOTE1PTIIDIST)/com/JLex \
	$(REMOTE1PTIIDIST)/ptolemy/plot \
	$(REMOTE1PTIIDIST)/ptolemy/vergil \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/toolbox \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/icon \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/ptolemy \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/ptolemy/fsm \
	$(REMOTE1PTIIDIST)/ptolemy/vergil/tree \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/conversions \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/gui \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/logic \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/javasound \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/jspaces \
	$(REMOTE1PTIIDIST)/ptolemy/actor/lib/jspaces/util \
	$(REMOTE1PTIIDIST)/ptolemy/gui \
	$(REMOTE1PTIIDIST)/ptolemy/actor/gui

# Run JavaScope Code Coverage tool in the distribution tree
# For command line options, see
#http://www.gigascale.org/pubs/gsrc-downloads/JavaScope/docs/pdf/UGApxTools.pdf
remote1distjsall:
	@echo "#"
	@echo "# Now running make jsall in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "# Generating test coverage data for: "
	@echo "# $(JSALLDIRS)"
	@echo "# To add to the list of directories, edit "
	@echo "# ~ptII/adm/gen-latest/makefile"
	@echo "#"
	@echo `date`

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
				jsall JSINSTRFLAGS=;\
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
	@echo `date`
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


# Remove the JavaScope instrumentation
remote1build:
	@echo "#"
	@echo "# Now running make in $(REMOTE1PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE1PTIIDIST);	PTII=$(REMOTE1PTIIDIST) $(MAKE) -k install)



#####################################################################
# Build on a remote host under JDK1.1

# Remote location of Ptolemy II tree for testing
# Host to run on
REMOTE2HOST = julian

# Directory to place test distribution in
REMOTE2HOME = /vol/$(REMOTE2HOST)/$(REMOTE2HOST)2/ptII

# Actual ptII directory
REMOTE2PTIIDIST = $(REMOTE2HOME)/ptII$(VERSION)

# Log file to save the results in
REMOTE2LOG = $(GENLOGDIR)/remote2.out

remote2: remote2jdk1.1
remote2jdk1.1:  $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE2HOST) with JDK1.1 "
	@echo "#"
	@echo `date`
	ssh $(REMOTE2HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		remote2nuke remote2ptiidist remote2distclean \
		remote2distjdk1.1 remote2distjdk1.1test remote2done \
		>& $(REMOTE2LOG)" &

# Remove the remote dist
# $(REMOTE2DONEFILE) is used to signal when everything is done
remote2nuke:
	rm -rf $(REMOTE2PTIIDIST) $(REMOTE2DONEFILE)

#Untar the remote dist.  Usually this is run on the remote machine
remote2ptiidist: $(REMOTE2PTIIDIST)
$(REMOTE2PTIIDIST): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE2HOME); $(TAR) -xf $(GENDIR)/`basename $(PTII_FULL) .gz`)


# clean the remote tree
remote2distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE2PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE2PTIIDIST); \
		$(OTHERMAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)


# Compile with JDK1.1
remote2distjdk1.1:
	@echo "#"
	@echo "# Now building with JDK1.1"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE2PTIIDIST); rm -f config.status config.cache; \
		PATH=/opt/jdk1.1.7B/bin:$$PATH ./configure; $(MAKE) -k JFLAGS=)
	@echo `date`

# Test with JDK1.1
REMOTE2PTJACL_JAR	= $(REMOTE2PTIIDIST)/ptjacl.jar
$(REMOTE2PTJACL_JAR):
	@echo "#"
	@echo "# Creating $@"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE2PTIIDIST); cvs -d $(CVS_REPOSITORY) checkout tcl; \
		cd tcl;	PATH=/opt/jdk1.1.7B/bin:$$PATH \
		$(MAKE) -k JFLAGS= install)
	ls -l $@
	@echo `date`

remote2distjdk1.1test: $(REMOTE2PTJACL_JAR)
	@echo "#"
	@echo "# Now testing with JDK1.1"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE2PTIIDIST); \
		PATH=/opt/jdk1.1.7B/bin:$$PATH \
		$(MAKE) -k JFLAGS= tests)
	@echo `date`


#####################################################################
# Build on a remote host by doing a cvs checkout

# Host to run on
REMOTE3HOST = gauss

# Directory to build in
REMOTE3HOME = /vol/$(REMOTE3HOST)/$(REMOTE3HOST)2/ptII

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
	@echo `date`
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
	@echo `date`
	(cd $(REMOTE3HOME); cvs -d $(CVS_REPOSITORY) checkout ptII)


# clean the remote tree
remote3distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE3PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE3PTIIDIST); \
		$(OTHERMAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)


# Compile
remote3distbuild:
	@echo "#"
	@echo "# Now building in $(REMOTE3PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE3PTIIDIST); rm -f config.status config.cache; \
		PTII=$(REMOTE3PTIIDIST) ./configure; $(MAKE) -k JFLAGS=)
	@echo `date`

# Build Jacl
REMOTE3PTJACL_JAR	= $(REMOTE3PTIIDIST)/ptjacl.jar
$(REMOTE3PTJACL_JAR):
	@echo "#"
	@echo "# Creating $@"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE3PTIIDIST); cvs -d $(CVS_REPOSITORY) checkout tcl; \
		cd tcl; $(MAKE) JFLAGS= install)
	ls -l $@
	@echo `date`

# Test
remote3disttest: $(REMOTE3PTJACL_JAR)
	@echo "#"
	@echo "# Now testing in $(REMOTE3PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE3PTIIDIST); \
		$(OTHERMAKE) JFLAGS= tests)
	@echo `date`


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


# If the remote done files are present, concatenate the logs and send email
# Note that currently we are not running remote 1 and remote2
remotealldone:
	if [ -f $(REMOTE3DONEFILE) \
			-a -f $(REMOTE4DONEFILE) ]; then \
		date; \
		echo "Remote processes finished"; \
		cat $(REMOTE3LOG) $(REMOTE4LOG) \
			>> $(PTIINIGHTLY_TXT); \
		/bin/sh $(PTIIADM)/bin/ptIInightly-mail $(EMAILTO) ; \
	else \
		date; \
		echo "Still waiting"; \
		ls -l $(REMOTE3DONEFILE) $(REMOTE4DONEFILE); \
	fi

##########################################################################
# Build from a tar file - similar to remote1, but w/o test case coverage

####################################
# Build and generate test case coverage on a remote host

# Use a non-gnu make here for testing purposes
OTHERMAKE = /usr/ccs/bin/make

# Remote location of Ptolemy II tree for testing
REMOTE4HOME = /vol/tycho/tycho2/ptII
REMOTE4PTIIDIST = $(REMOTE4HOME)/ptII$(VERSION)

# Host to run on
REMOTE4HOST = tycho

# Log file to save the results in
REMOTE4LOG = $(GENLOGDIR)/remote4.out

# Create a separate tree and build on REMOTE4HOST
remote4: $(PTII_FULL) $(GENLOGDIR)
	@echo "#"
	@echo "# Now building on $(REMOTE4HOST) from dist tar file"
	@echo "#"
	@echo `date`
	ssh $(REMOTE4HOST) "cd $(GENDIR); make -k EMAILTO=$(EMAILTO) \
		remote4nuke remote4ptiidist remote4configure \
		remote4distclean remote4build \
		 >& $(REMOTE4LOG)" &


# Remove the remote dist
remote4nuke:
	rm -rf $(REMOTE4PTIIDIST) $(REMOTE4DONEFILE)

# untarthe remote dist.  Usually this is run on the remote machine
remote4ptiidist: $(REMOTE4PTIIDIST)
$(REMOTE4PTIIDIST): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE4HOME); $(TAR) -xf $(GENDIR)/`basename $(PTII_FULL) .gz`)

remote4configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE4PTIIDIST);	rm -f config.cache; PTII=$(REMOTE4PTIIDIST) configure)

# Clean the remote4 tree
remote4distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE4PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE4PTIIDIST); \
		$(MAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)

remote4distjavadocs:
	@echo "#"
	@echo "# Now generating javadocs in $(PTIIDIST) on `hostname`"
	@echo "#"
	@echo `date`
	-(cd $(REMOTE4PTIIDIST); $(OTHERMAKE) -k javadocs)

remote4build:
	@echo "#"
	@echo "# Now running make in $(REMOTE4PTIIDIST)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE4PTIIDIST);	PTII=$(REMOTE4PTIIDIST) $(MAKE) install)



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
# make USER=cxh PTIIHOME=/home/eecs/cxh/ptII/adm/trees/ptII1.0 PTIIADM=/home/eecs/cxh/ptII/adm installshield_app
installshield_app: installshield $(INSTALLSHIELD_APP) \
			$(INSTALLSHIELD_CODEDOC) \
			$(INSTALLSHIELD_DESIGNDOC) \
			$(INSTALLSHIELD_EXPERIMENTAL) \
			$(INSTALLSHIELD_PTINY) \
			$(INSTALLSHIELD_PTINY_ONLY) # $(INSTALLSHIELD_DOC)

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
	$(TAR) -xf ../ptII$(VERSION).tar; \
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
		$(INSTALLSHIELD_APP)/ptolemy/configs \
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
$(INSTALLSHIELD_PTINY_ONLY):
	@echo "#"
	@echo "# Building $@"
	mkdir -p $@/doc/codeDoc $@/ptolemy/configs
	-cp -r $(PTIIHOME)/doc/codeDocPtiny/* \
	 	 $@/doc/codeDoc
	# Big hack here.  We need to include intro.htm in app_only, so
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
	 mv $(INSTALLSHIELD_APP)/doc/design/design.pdf $@/doc/design/

$(INSTALLSHIELD_CODEDOC):
	@echo "#"
	@echo "# Building $@"
	 mkdir -p $@/doc
	 mv $(INSTALLSHIELD_APP)/doc/codeDoc $@/doc

$(INSTALLSHIELD_DOC):
	@echo "#"
	@echo "# Building $@"
	 #mkdir -p $@
	 #mv $(INSTALLSHIELD_APP)/doc $@/doc

$(INSTALLSHIELD_EXPERIMENTAL):
	@echo "#"
	@echo "# Building $@"
	 mkdir -p $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/dde $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/dt $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/giotto $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/gr $@/ptolemy/domains

ISPMP_LIB=/users/ptII/vendors/installshield/ipmp401/lib/

ISPMP_DEBUG_CLASSPATH=/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/classes/src: 

ISPMP_CLASSPATH=/vol/maury/maury1/ptII/vendors/installshield/ipmp401:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/help.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/icebrowserbean.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/icebrowserlitebean.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/ide.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/jhall.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/parser.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/product.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/swing.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/wizard.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/lib/xt.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/ppk/win32ppk.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/ppk/solarisppk.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/ppk/webppk.jar:/vol/maury/maury1/ptII/vendors/installshield/ipmp401/ppk/linuxppk.jar

ptiny.xml: ptII.xml ptiny.xsl
	java -classpath $(ISPMP_CLASSPATH) \
		com.installshield.isje.ISJE ptII.xml \
		-xsl ptiny.xsl \
		-saveas $@

# Run this on doppler with:
# rm unixPtiny.xml; make PTIIHOME=/home/eecs/cxh/ptII build_unixPtiny
unixPtiny.xml: ptiny.xml
	sed -e 's@[cC]:\\users\\cxh\\ptII\\adm\\gen-1.0\\installshield\\@$(GENDIR)/installshield/@' \
	    -e 's@[cC]:\\users\\cxh\\ptII\\adm\\gen-1.0@$(GENDIR)/default/@' \
	    -e 's@designdoc\\doc\\design\\@designdoc/doc/design/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\jvms\\win32\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/jvms/win32/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\jvms\\solaris\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/jvms/solaris/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\jvms\\linux\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/jvms/linux/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\images\\InstallShield Images\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/images/InstallShield Images/@' \
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
	    -e 's@[cC]:\\users\\cxh\\ptII\\adm\\gen-1.0@$(GENDIR)/default/@' \
	    -e 's@designdoc\\doc\\design\\@designdoc/doc/design/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\jvms\\win32\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/jvms/win32/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\jvms\\solaris\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/jvms/solaris/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\jvms\\linux\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/jvms/linux/@' \
	    -e 's@C:\\Program Files\\InstallShield\\IPMP401\\images\\InstallShield Images\\@/vol/maury/maury1/ptII/vendors/installshield/ipmp401/images/InstallShield Images/@' \
	    -e 's@<property name="outputDirectory">default</property>@<property name="outputDirectory">$(GENDIR)/installshield</property>@' \
	    -e 's@ptiny\\copyright@ptiny/copyright@' \
		 $< > $@

build_unixPtII: unixPtII.xml
	java -classpath $(ISPMP_CLASSPATH) \
		-Dis.debug=12 \
		com.installshield.isje.ISJE $< \
		-build


