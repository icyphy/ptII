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
MINOR_VERSION =		beta

VERSION=	$(MAJOR_VERSION)$(MINOR_VERSION)

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
FTP_DEST =	/vol/ptolemy/pt0/ftp
# Use this for testing, comment out for release.
#FTP_DEST =	/vol/brahe/brahe1/ftp

# Destination of the Ptolemy tar files
PT_DEST =	$(FTP_DEST)/pub/ptolemy/$(PTOLEMY_SRC)$(VERSION)
# Destination of the GNU tar files
GNU_DEST =	$(FTP_DEST)/pub/gnu/$(PTOLEMY_SRC)$(VERSION)

# Name and directory of distribution.  For testing, use ptdesign
# For release, use ptolemy
#USER =		ptolemy
# For use under Windows
# make USER=cxh PTIIHOME=//c/users/cxh/ptII TAR=tar 
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

PTII_SRC = 	  $(GENDIR)/ptII$(VERSION).src.tar.$(SUFFIX)
PTII_SRC_ZIP =   $(GENDIR)/ptII$(VERSION).src.zip

VERGIL = 	  $(GENDIR)/vergil$(VERSION).src.tar.$(SUFFIX)

# Three forms we ship in.
PTII_SRCS = 	$(PTII_SRC) $(basename $(PTII_SRC)) $(PTII_SRC_ZIP)

PTII_DEVEL_SRC = $(GENDIR)/ptolemy.devel$(VERSION).src.tar.$(SUFFIX)

##############################################################################
# Targets
all: $(PTII_SRC) $(PTII_SRC_ZIP) #$(PTII_DEVEL_SRC)
clean:
	@echo "#"
	@echo "# Now cleaning old tar files in ~ptII/adm/gen-latest"
	@echo "#"
	@echo `date`
	rm -f $(PTII_SRC) $(basename $(PTII_SRC)) $(PTII_SRC_ZIP) $(PTII_DEVEL_SRC)
	rm -rf $(PTIIDIST)

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
	(cd $(PTIIHOME); \
		$(TAR) -c -X $(GENDIR)/src.ex  -X $(GENDIR)/src1.ex \
		$ -f - $(PTII_TOPDIR) )| \
		(cd $@; $(TAR) -xpf -)
	@echo "Removing codeDoc directories and other documentation"
	-find $(PTIIDIST) -name codeDoc -exec rm -rf {} \;
	@echo "Regenerate the documentation"
	(cd $(PTIIDIST)/doc; make JDOCFLAGS="-author -version -J-Xmx132m")
	#@echo "Do make realclean to clean up any trash"
	#cd $(PTIIDIST); make clean
	#cd $(PTIIDIST); make
	#cd $(PTIIDIST); make javadocs
	rm -f $(PTIIDIST)/.glimpse*
	cp $(PTIIHOME)/.glimpse_exclude $(PTIIDIST)

clean_src:
	rm -f $(PTII_SRC)
src: $(PTII_SRC) $(PTII_SRC_ZIP)
ptii_src: $(PTII_SRC) $(PTII_SRC_ZIP)
$(PTII_SRC): $(PTIIDIST)
	@echo "Creating $@"
	(cd $(PTIIDIST)/..; \
	$(TAR) -c -X $(GENDIR)/src.ex \
	 -f $(basename $@) \
	ptII$(VERSION))
	@echo "We will provide gzipped and non-gzipped tars"
	cp $(basename $@) $(basename $@).bak
	$(COMPRESS) $(basename $@)
	mv $(basename $@).bak $(basename $@)
	chmod g+w $@ $(basename $@)
	chgrp ptolemy $@ $(basename $@)
	ls -l $@ $(basename $@)

$(PTII_SRC_SPLIT):: $(PTII_SRC)
	$(SPLITTAR) $^

src.zip: $(PTII_SRC_ZIP)
ptii.src.zip: $(PTII_SRC_ZIP)
$(PTII_SRC_ZIP): $(PTIIDIST)
	@echo "Creating $@"
	(cd $(PTIIDIST)/..; \
	$(ZIP) -r -q $@ \
	ptII$(VERSION) \
	)
	chmod g+w $@
	chgrp ptolemy $@
	ls -l $@


vergil: $(VERGIL)
$(VERGIL): $(PTIIDIST)
	@echo "Creating $@"
	(cd $(PTIIHOME); \
	$(TAR) -c -X $(GENDIR)/src.ex \
	 -f $(basename $@) \
	ptolemy/vergil)
	@echo "We will provide gzipped and non-gzipped tars"
	cp $(basename $@) $(basename $@).bak
	$(COMPRESS) $(basename $@)
	mv $(basename $@).bak $(basename $@)
	chmod g+w $@ $(basename $@)
	chgrp ptolemy $@ $(basename $@)
	ls -l $@ $(basename $@)

update_ftp: update_ptii_ftp update_ptii_itcl untar
update_ptii_itcl:
	@echo "Copying itcl files to website"
	#cp -p $(ITCL_ALL) $(PTII_FTP)
	cp -p $(ITCL_DIST) $(PTII_FTP)
update_ptii_ftp:
	@echo "Copying ptII files to website"
	cp -p $(PTII_SRCS) $(PTII_FTP)
untar:
	rm -rf $(PTII_FTP)/$(VERSION)
	cd $(PTII_FTP); /usr/tools/gnu/bin/tar -zxf $(PTII_SRC)

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
	-(cd $(PTIIHOME); cvs update -d -P; PTII=/users/ptII $(MAKE) JFLAGS="-deprecation -g" JDOCFLAGS="-author -version -J-Xmx132m" -k sources install)  > /users/$(USER)/logs/java`date  '+%m%d'` 2>&1

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
remote1coverage: $(PTII_SRC) $(GENLOGDIR)
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
		JFLAGS="-deprecation -g" \
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
$(REMOTE1PTIIDIST): $(PTII_SRC)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE1HOME); $(TAR) -xf $(GENDIR)/`basename $(PTII_SRC) .gz`)

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
# sdf/codegen should be run after lang and codegen
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
	$(REMOTE1PTIIDIST)/ptolemy/lang \
	$(REMOTE1PTIIDIST)/ptolemy/lang/java \
	$(REMOTE1PTIIDIST)/ptolemy/lang/java/tree \
	$(REMOTE1PTIIDIST)/ptolemy/codegen \
	$(REMOTE1PTIIDIST)/ptolemy/codegen/saveasjava \
	$(REMOTE1PTIIDIST)/ptolemy/domains/sdf/codegen \
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
remote2jdk1.1:  $(PTII_SRC) $(GENLOGDIR)
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
$(REMOTE2PTIIDIST): $(PTII_SRC)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE2HOME); $(TAR) -xf $(GENDIR)/`basename $(PTII_SRC) .gz`)


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
remote4: $(PTII_SRC) $(GENLOGDIR)
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
$(REMOTE4PTIIDIST): $(PTII_SRC)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo `date`
	(cd $(REMOTE4HOME); $(TAR) -xf $(GENDIR)/`basename $(PTII_SRC) .gz`)

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
INSTALLSHIELD_RUNTIME=$(INSTALLSHIELD_SRC)/runtime
INSTALLSHIELD_DESIGNDOC=$(INSTALLSHIELD_SRC)/designdoc
INSTALLSHIELD_DOC=$(INSTALLSHIELD_SRC)/doc
INSTALLSHIELD_EXPERIMENTAL=$(INSTALLSHIELD_SRC)/experimental

# Runtime is last so we don't get the kruft from the doc dir
installshield_app: $(INSTALLSHIELD_APP) \
	$(INSTALLSHIELD_DESIGNDOC) \
	$(INSTALLSHIELD_DOC) \
	$(INSTALLSHIELD_EXPERIMENTAL) \
	$(INSTALLSHIELD_RUNTIME) \

clean_installshield:
	rm -rf $(INSTALLSHIELD_APP) \
	$(INSTALLSHIELD_DESIGNDOC) \
	$(INSTALLSHIELD_DOC) \
	$(INSTALLSHIELD_EXPERIMENTAL) \
	$(INSTALLSHIELD_RUNTIME)

$(INSTALLSHIELD_APP): #$(PTII_SRC)
	(cd $(INSTALLSHIELD_SRC); \
	 tar -xf ../ptII$(VERSION).src.tar; \
	 mv ptII$(VERSION) `basename $@`)

INSTALLSHIELD_RUNTIME_DOMAINS=ct csp de fsm pn sdf
$(INSTALLSHIELD_RUNTIME): bar
	 mkdir -p $@ $@/lib $@/ptolemy/vergil
	 mkdir -p $@/doc/codeDoc/ptolemy/actor
	 mkdir -p $@/moml/demo/Networked
	 -mv $(INSTALLSHIELD_APP)/copyright.txt $@
	 -mv $(INSTALLSHIELD_APP)/doc $@
	 -mv $(INSTALLSHIELD_APP)/lib/diva.jar $@/lib
	 -mv $(INSTALLSHIELD_APP)/ptolemy/ptolemy.jar \
		$(INSTALLSHIELD_APP)/ptolemy/configs \
		$@/ptolemy
	 -mv $(INSTALLSHIELD_APP)/ptolemy/vergil/vergil.jar $@/ptolemy/vergil
	 for domain in $(INSTALLSHIELD_RUNTIME_DOMAINS); do \
	  mkdir -p $@/ptolemy/domains/$$domain; \
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/$$domain/$$domain.jar \
	 	$(INSTALLSHIELD_APP)/ptolemy/domains/$$domain/demo \
		$(INSTALLSHIELD_APP)/ptolemy/domains/$$domain/doc \
		$@/ptolemy/domains/$$domain; \
	 mv $(INSTALLSHIELD_DOC)/doc/codeDoc/ptolemy/$$domain \
		 $@/doc/codeDoc/ptolemy
	 done
	 # We do want to ship actor/lib docs
	 mv $(INSTALLSHIELD_DOC)/doc/codeDoc/ptolemy/actor/lib \
		$@/doc/codeDoc/ptolemy/actor
	 # Set up for a few demos
	 mkdir -p $@/ptolemy/actor/lib/javasound/demo
	 mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/javasound/demo/KarplusStrong \
		$@/ptolemy/actor/lib/javasound/demo
	 mkdir -p $@/ptolemy/moml/demo
	 mv $(INSTALLSHIELD_APP)/ptolemy/moml/demo/Networked \
		$@/ptolemy/moml/demo
	 mkdir -p $@/ptolemy/data/type/demo
	 mv $(INSTALLSHIELD_APP)/ptolemy/data/type/demo/Router \
		$@/ptolemy/data/type/demo
	 # FIXME: vergil looks for ../actor/lib/logic/logic.xml, so
	 # we don't find it in the jar file
	 mkdir -p $@/ptolemy/actor/lib/logic
	 mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/logic/logic.xml \
		$@/ptolemy/actor/lib/logic
	 mkdir -p $@/ptolemy/actor/lib/conversions
	 mv $(INSTALLSHIELD_APP)/ptolemy/actor/lib/conversions/conversions.xml \
		$@/ptolemy/actor/lib/conversions

configsRuntime.jar: $(INSTALLSHIELD_RUNTIME)/ptolemy/configs/configsRuntime.jar
$(INSTALLSHIELD_RUNTIME)/ptolemy/configs/configsRuntime.jar: \
	$(INSTALLSHIELD_RUNTIME)/ptolemy/configs
	(cd $(INSTALLSHIELD_RUNTIME); \
		jar -cf ptolemy/configs/configsRuntime.jar \
			ptolemy/configs)

$(INSTALLSHIELD_RUNTIME)/ptolemy/configs:
	mkdir $(INSTALLSHIELD_RUNTIME)/ptolemy/configs
	mkdir $(INSTALLSHIELD_RUNTIME)/ptolemy/configs/doc
	mv $(INSTALLSHIELD_APP)/ptolemy/configs/introRuntime.htm $@/intro.htm
	mv $(INSTALLSHIELD_APP)/ptolemy/configs/vergilConfigurationRuntime.xml \
		$@/vergilConfiguration.xml
	mv $(INSTALLSHIELD_APP)/ptolemy/configs/doc/demosRuntime.htm \
		$@/doc/demos.htm

$(INSTALLSHIELD_DESIGNDOC):
	 mkdir -p $@/doc/design
	 mv $(INSTALLSHIELD_APP)/doc/design/design.pdf $@/doc/design

$(INSTALLSHIELD_DOC):
	 mkdir -p $@/doc
	 mv $(INSTALLSHIELD_APP)/doc/codeDoc $@/doc

$(INSTALLSHIELD_EXPERIMENTAL):
	 mkdir -p $@/ptolemy $@/ptolemy/domains/sdf
	 mv $(INSTALLSHIELD_APP)/ptolemy/codegen $@/ptolemy
	 mv $(INSTALLSHIELD_APP)/ptolemy/lang $@/ptolemy
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/sdf/codegen \
		$@/ptolemy/domains/sdf
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/dde $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/dt $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/giotto $@/ptolemy/domains
	 mv $(INSTALLSHIELD_APP)/ptolemy/domains/gr $@/ptolemy/domains

