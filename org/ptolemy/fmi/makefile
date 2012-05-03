# Makefile for Java FMU Interface
#
# @Authors: Christopher Brooks
#
# @Version: $Id: makefile 61917 2011-08-23 01:24:07Z cxh $
#
# @Copyright (c) 2012 The Regents of the University of California.
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

ME =		org/ptolemy/fmi

DIRS = 		driver type 

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	fmu
PTVERSION =	1.0
PTDIST =	javafmu$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		driver/driver.jar \
		type/type.jar

PTCLASSALLJAR = 	fmi.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	FMICallbackFunctions.java \
	FMIEventInfo.java \
	FMILibrary.java \
	FMIModelDescription.java \
	FMIScalarVariable.java \
	FMUFile.java \
	FMULog.java \
	NativeSizeT.java

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS)


# Non-source files such as README files and subdirectories
MISC_FILES = driver type

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	manifest.tmp \
	$(PTCLASSALLJAR) \
	$(PTAUXJAR) \

KRUFT =

JCLASS = $(JSRCS:%.java=%.class)

# make jars will build $(OTHER_JARS)
OTHER_JARS = $(PXGRAPHAPPLETJAR) $(PLOTMLAPPLETJAR) $(PLOTAPPLICATIONJAR)

all: sources jclass

install: all jars 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk


FIND=/usr/bin/find
PTTMPME= $(PTTMPDIR)/$(PTDIST)/$(ME)
# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "all makefiles sources install docs depend checkjunk sccsinfo sccsclean test extraclean clean: mk/ptII.mk" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd com/microstar/xml/; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '# Generate ptII.mk by running configure' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo 'mk/ptII.mk: configure mk/ptII.mk.in' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	./configure' >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "realclean itcldocs tests idocs weblint dists:" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "	echo \"Nothing need be done for $$@ in ptolemy/plot)\"" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Ptplot is a set of two dimensional signal plotters" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "implemented in Java." >>  $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Ptplot is embeddable in applets and applications." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "For more information, see ptolemy/plot/doc/index.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Installer notes:" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "If you are using the auto installer, then note that" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "under Windows, the Ptplot programs will be added to" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "your start menu under Ptolemy -> Ptplot.  Note further" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "that directory names with spaces do not work very well" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo " because of limitations Java 1.4.0" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "If you are building from a tar or zip file, then note that" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "the file ptplot${PTVERSION}.tar.gz or" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "ptplot${PTVERSION}.zip, will unpack into a directory" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "called ptplot${PTVERSION}, and the configure-make-make install" \
		>> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "process will not install any files outside of this " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "ptplot${PTVERSION} directory." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "So, if you unpack the TAR file into /usr/local, after " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "installing you will have /usr/local/ptplot${PTVERSION}," >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "and you may want to tell users to add " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "/usr/local/ptplot${PTVERSION}/bin to their PATH." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "The scripts in the bin directory will run the plotter" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "as a standalone application, or they can add" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "/usr/local/ptplot${PTVERSION} to their Java CLASSPATH." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "To build under Solaris:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Set the PTII variable to this directory: " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "PTII=`pwd` " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Then configure and make:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "./configure" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "make install" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "bin/ptplot" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	@echo "Remove test from ptolemy/plot/makefile"
	cat "$(PTII)/ptolemy/plot/makefile" | \
		sed "s/ test / /" | \
		sed "s/\ttest/\t/" > \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/plot/makefile
	#@echo "JShield needs the ico file to be in the top level directory"
	#cp $(PTTMPME)/doc/img/ptplot.ico \
	#	$(PTTMPDIR)/$(PTDIST)
	@echo "Copy over xml stuff"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/com
	cp -r "$(PTII)/com/microstar" "$(PTII)/com/makefile" \
		$(PTTMPDIR)/$(PTDIST)/com
	@echo "Copy over bin"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/bin
	cp -r   "$(PTII)/bin/ptinvoke.in" \
		$(PTTMPDIR)/$(PTDIST)/bin
	@echo "Create bin/makefile"
	echo "ME =		bin" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "ROOT =		.." >> 	$(PTTMPDIR)/$(PTDIST)/bin/makefile
	# Use awk here so that make variables don't get substituted
	echo "foo" | \
		awk '{	  printf("CONFIG =\t%c%s/mk/ptII.mk\n",36,"(ROOT)"); \
			 printf("include %c%s\n", 36,"(CONFIG)"); \
		}'  >> 		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "SHELL_FILES = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	histogram \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptplot \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	pxgraph" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# DOS batch files" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "BAT_FILES = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	histogram.bat \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptplot.bat \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	pxgraph.bat " >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "EXTRA_SRCS = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptinvoke.in \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "foo" | \
		awk '{	 printf("\t%c%s\n", 36, "(BAT_FILES)")}' \
		>> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo 'all: ptinvoke $$(BAT_FILES)' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	-chmod a+x ptinvoke" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f histogram" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke histogram" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f ptplot" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke ptplot" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f pxgraph" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke pxgraph" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "install: all" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "foo" | \
		awk '{	 printf("\t-chmod a+x %c%s %c%s\n", 36, "(SHELL_FILES)", 36, "(BAT_FILES)")}' >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "ptinvoke: ptinvoke.in ../config.status" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	(cd ..; ./config.status)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# zip might not get the permissions of configure right, so we run /bin/sh" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "../config.status: ../configure" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	(cd ..; /bin/sh ./configure)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo 'makebat: ptinvoke' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo '	rm -f makebat' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo '	ln -s ptinvoke makebat' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo '$$(BAT_FILES): makebat' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	# We need $@ in bin/makefile, so we use awk to avoid substitution
	echo "foo" | \
		awk '{	 printf("\trm -f %c%c%c%c\n", 34, 36, 64, 34); printf("\t./makebat -q %c%cbasename %c%c .bat%c%c\n", 34, 96, 36, 64, 96, 34); }' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# Get the rest of the rules" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "foo" | \
		awk '{	 printf("include %c%s/mk/ptcommon.mk\n", 36, "(ROOT)")}' >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	#
	# Setting up doc/codeDoc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/codeDoc
	cd $(PTTMPDIR)/$(PTDIST); \
	CLASSPATH=. "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc \
		org.ptolemy.fmi \
		org.ptolemy.fmi.driver \
		org.ptolemy.fmi.type
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPME)/doc/download
	mkdir $(PTTMPME)/doc/download
	echo "<html>" > $(PTTMPME)/doc/download/index.htm
	echo "<head>" >> $(PTTMPME)/doc/download/index.htm
	echo "<title>Download Ptplot</title>" >> $(PTTMPME)/doc/download/index.htm
	echo '<link href="../default.css" rel="stylesheet" type="text/css">' >> $(PTTMPME)/doc/download/index.htm
	echo '<body bgcolor="#ffffff">'  >> $(PTTMPME)/doc/download/index.htm
	echo "<H1>Download Ptplot</H1>" >> $(PTTMPME)/doc/download/index.htm
	echo "The Ptplot distribution itself does not include all the available download formats." >> $(PTTMPME)/doc/download/index.htm
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/java/ptplot" target="_top"><code>http://ptolemy.eecs.berkeley.edu/java/ptplot</code></a>' >> $(PTTMPME)/doc/download/index.htm
	echo "</body>" >> $(PTTMPME)/doc/download/index.htm
	echo "</html>" >> $(PTTMPME)/doc/download/index.htm
	chmod -R a+r $(PTTMPDIR)/$(PTDIST)
	$(FIND) $(PTTMPDIR)/$(PTDIST) -name "*.class" -exec rm {} \;

# Update the ftp site
# Update a location with the files necessary to download
DIST_BASE = /export/home/pt0/ptweb/java
DIST_DIR = $(DIST_BASE)/$(PTDIST)


update_ftp:
	ssh bennett rm -rf $(DIST_DIR)
	cat $(PTDIST).tar.gz | ssh bennett "cd $(DIST_BASE); gtar -zxf -"
	ssh bennett "cd $(DIST_BASE)/$(PTDIST)_tmp/; tar -cf - . | (cd $(DIST_DIR)/ptolemy/plot/doc/download; tar -xpf -)"
	scp doc/download/index.htm bennett:$(DIST_DIR)/ptolemy/plot/doc/download
	scp $(PTDIST).tar.gz bennett:$(DIST_DIR)/ptolemy/plot/
	scp $(PTDIST).zip bennett:$(DIST_DIR)/ptolemy/plot/
