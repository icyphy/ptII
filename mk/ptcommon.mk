# Makefile rules that are common to most files.
#
# Version Identification:
# $Id$
# Copyright (c) 1990-2003 The Regents of the University of California.
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

# External makefile variables that this file uses
#
# Directories:
# DIRS		Subdirectories to run make in.
# LIBDIR	The destination directory for any libraries created.
#			Usually this is an architecture dependent library.
#
# Files:
# SRCS 		Files that are compiled, such as .c, .cc and .java files.
# EXTRA_SRCS	Files that are not compiled, such as .tcl and .itcl files.
# HTMLS		HTML files
# ITCL_SRCS	.itcl files
# TCL_SRCS	.tcl files
# HDRS		.h files.
# JSRCS		.java files
# OPTIONAL_JSRCS derived .java files (i.e. created by javacc)
# DERIVED_JSRCS master derived .java file (i.e. created by javacc)
# JCLASS	.class files
# OBJS		.o files
# LIBR		The name of the library being created.  We can't just call
#		this LIB because of problems with the LIB environment variable
#		under NT
# EXP		???
# MISC_FILES	Non-source files such as README files.
# OPTIONAL_FILES Files that are derived from other files, but we don't
#			want 'make checkjunk' to complain about

# Variables used by cleaning rules:
# KRUFT		Files to be removed by 'make clean'
# DISTCLEAN_STUFF Files to be removed by 'make distclean'
#
# Variables used by tests:
# SIMPLE_TESTS	Itcl tests that don't require a graphical front end
# GRAPHICAL_TESTS	Itcl tests that do require a graphical front end
#
# Scripts:
# ITCLSH	The Itcl 'itclsh' binary
#
# C and C++ Compiler variables:
# CC		The C Compiler
# CPLUSPLUS	The C++ Compiler
# C_SHAREDFLAGS CC_SHAREDFLAGS  Flags to build shared objects for C and C++
# CFLAGS GPPFLAGS  	The C and C++ Compiler Flags
# C_INCL INCL		The C and C++ Include Flags
#
# Java Variables
# PTJAVA_DIR	The home of the Java Developer's Kit (JDK)
# JAVAC		The 'javac' compiler.
# JFCHOME	The home of the Java Foundation Classes (JFC) aka Swing
# JFLAGS	Flags to pass to javac.
# JAVADOC	The 'javadoc' program
# JDOCFLAGS	Flags to pass to javadoc.
# PTCLASSJAR	Jar file of classes to be produced.
# JDIST		The name and version of the tar.gz and jar files of the sources
# JTESTHTML	Test html file for a java class.
# JTCLSH	TclBlend Tcl/Java interface shell.
#
# The variables below are for the SunTest JavaScope code coverage tool
# See http://www.suntest.com/JavaScope
# JSINSTR	The 'jsinstr' command, which instruments Java code.
# JSINSTRFLAGS	Flags to pass to jsinstr.
# JSRESTORE	The 'jsrestore' command which uninstruments Java code.

##############
# Under no circumstances should this makefile include 'all', 'install'
# or 'depend' rules.  These rules should go in the makefile that
# includes this makefile, or into no-compile.mk
# The reason is that we want to avoid duplicate 'all', 'install'
# and 'depend' rules without using the possibly unportable double-colon
# makefile convention.

# Run make all in the subdirs
suball:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making all in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) all ;\
			) \
		    fi ; \
		done ; \
	fi

# Run make install in the subdirs
subinstall:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making install in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) install ;\
			) \
		    fi ; \
		done ; \
	fi


# Run make demo in the subdirs
# Note that if DIRS include the demo directory, and MISC_FILES includes
# DIRS, then 'make sources' will run 'make demo', which is not what we want
demo: subdemo
subdemo:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making demos in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) demo ;\
			) \
		    fi ; \
		done ; \
	fi

# Run make check in the subdirs
check:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making check in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) check ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ "x$(JSRCS)" != "x" ]; then \
		echo "Running '$(ROOT)/util/testsuite/chkjava $(JSRCS)'"; \
		$(ROOT)/util/testsuite/chkjava $(JSRCS); \
		echo "Running '$(ROOT)/util/testsuite/ptspell $(JSRCS)'"; \
		$(ROOT)/util/testsuite/ptspell $(JSRCS); \
	fi

# Create the directory that contains the scripts
bin_install_dir:
	if [ ! -d "$(BIN_INSTALL_DIR)" ]; then \
		echo "Creating '$(BIN_INSTALL_DIR)' directory"; \
		mkdir -p "$(BIN_INSTALL_DIR)"; \
	fi

# Quickly attempt to build the tree
# 'make fast' is a hack.  If it does not work for you, either fix it or
# don't use it and use 'make' instead.
fast:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making fast in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) fast ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ "x$(JSRCS)" != "x" ]; then \
		echo "fast build with 'CLASSPATH=\"$(CLASSPATH)$(AUXCLASSPATH)\" "$(JAVAC)" $(JFLAGS) *.java' in `pwd`"; \
		CLASSPATH="$(CLASSPATH)$(AUXCLASSPATH)" "$(JAVAC)" $(JFLAGS) *.java; \
	fi
	@if [ "x$(PTLIB)" != "x" ]; then \
		$(MAKE) $(PTLIB); \
	fi

# "make sources" will do SCCS get on anything where SCCS file is newer.
sources::	$(SRCS) $(EXTRA_SRCS) $(HDRS) $(MISC_FILES) makefile
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

##############
# Java rules

.SUFFIXES: .class .java
.java.class:
	rm -f `basename $< .java`.class
	CLASSPATH="$(CLASSPATH)$(AUXCLASSPATH)" "$(JAVAC)" $(JFLAGS) $<

# Build all the Java class files.
# Run in the subdirs first in case the subpackages need to be compiled first.

jclass:	$(DERIVED_JSRCS) $(JSRCS) subjclass $(JCLASS)

subjclass:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jclass in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jclass ;\
			) \
		    fi ; \
		done ; \
	fi

# Compile the classes that require JDK1.1 and later.
jclass1_1: $(JSRCS1_1) $(JCLASS1_1)

# Build the Java documentation.
docs: javadocs
javadocs: doc/codeDoc/tree.html
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making javadocs in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) javadocs ;\
			) \
		    fi ; \
		done ; \
	fi

jhtml: doc/codeDoc/tree.html
# $(DERIVED_JSRCS) is used in ptolemy/data/expr
doc/codeDoc/tree.html:	$(JSRCS) $(OPTIONAL_JSRCS) $(DERIVED_JSRCS)
	@if [ "$(JSRCS)" = "" ]; then \
		echo "No java sources, so we don't run javadoc";\
	else \
	if [ ! -d doc/codeDoc ]; then mkdir -p doc/codeDoc; fi; \
	rm -f doc/codeDoc/*.html; \
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTJAVA_DIR)/lib/classes.zip$(AUXCLASSPATH)" \
	   "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc \
		$(JSRCS) $(OPTIONAL_JSRCS) $(DERIVED_JSRCS); \
	for x in doc/codeDoc/*.html; do \
		echo "Fixing paths in $(ME)/$$x"; \
		sed -e 's|<a href="java|<a href="$(JAVAHTMLDIR)/java|g' \
		-e 's|<img src="images/|<img src="$(JAVAHTMLDIR)/images/|g' \
			$$x > $$x.bak; \
		mv $$x.bak $$x; \
	done; \
	fi

# Generate index.xml from all the Java classes in a lib directory
index.xml: makefile $(ROOT)/mk/ptcommon.mk
	echo "<?xml version=\"1.0\" standalone=\"no\"?>" > $@
	echo "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"" >> $@
	echo "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">" >> $@
	echo "<!-- Do not Edit - Automatically generated by make -->" >> $@
	echo "<entity name=\"$(ME)\" class=\"ptolemy.moml.EntityLibrary\">" >> $@
	echo "<doc>$(TITLE)</doc>" >> $@
	echo "  <configure>" >> $@
	echo "    <?moml" >> $@
	echo "      <group>" >> $@
	for actor in $(JSRCS); do \
		baseactor=`basename $$actor .java`; \
		class=`echo $(ME) | sed 's@/@\.@g'`; \
	        echo "" >> $@; \
		echo "<entity name=\"$$baseactor\" class=\"$$class.$$baseactor\">" >> $@; \
		echo "<doc></doc>" >> $@; \
		echo "</entity>" >> $@; \
	done
	echo "      </group>" >> $@
	echo "    ?>" >> $@
	echo "  </configure>" >> $@
	echo "</entity>" >> $@

# Bring up the appletviewer on a test file.
jtest: $(JTESTHTML) $(JCLASS)
	CLASSPATH="$(CLASSPATH)" appletviewer $(TESTHTML)

htest-netscape: $(JTESTHTML) $(JCLASS)
	CLASSPATH="$(CLASSPATH)" netscape $(TESTHTML)

# Build the jar file

# Directory to unjar things in.
# Be very careful here, we rely on relative paths
PTJAR_TMPDIR =  ptjar_tmpdir

# OTHER_FILES_TO_BE_JARED is used in ptolemy/vergil/lib/makefile
# We need to use PTJAR_TMPDIR because not all directories
# have OTHER_FILES_TO_BE_JARED set, so we need to copy
# rather than refer to $(ME)/$(OTHER_FILES_TO_BE_JARED)
jars: $(PTCLASSJAR) $(PTAUXJAR) subjars $(PTCLASSALLJAR) $(PTAUXALLJAR) \
		$(OTHER_FILES_TO_BE_JARED) $(OTHER_JARS)
$(PTCLASSJAR): $(JSRCS) $(JCLASS)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy any class files from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp *.class $(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .; $(JAR_INDEX))
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

subjars:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jars in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jars ;\
			) \
		    fi ; \
		done ; \
	fi

# Jar file consisting of the jar file in the current dir
# and any jar files listed in

alljars: $(PTCLASSALLJAR)
$(PTCLASSALLJAR): $(PTCLASSALLJARS) $(JCLASS) $(OTHER_FILES_TO_BE_JARED)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy any class files from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp *.class $(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	for jar in $(PTCLASSALLJARS) ; do \
		echo "Unjarring $$jar"; \
		(cd $(PTJAR_TMPDIR); "$(JAR)" -xf ../$$jar); \
	done
	rm -rf $(PTJAR_TMPDIR)/META-INF
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .; $(JAR_INDEX))
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)


# Occasionally, we need to build a second jar file that includes
# a subset of all of the subjars included in PTCLASSALLJAR above.
# ptolemy/ptsupport.jar is an example
$(PTAUXALLJAR): $(PTAUXALLJARS) $(JCLASS) $(OTHER_FILES_TO_BE_JARED)
	# Building Auxiliary jar file
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy any class files from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	-cp *.class $(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	for jar in $(PTAUXALLJARS) $(JCLASS) $(OTHER_FILES_TO_BE_JARED); do \
		echo "Unjarring $$jar"; \
		(cd $(PTJAR_TMPDIR); "$(JAR)" -xvf ../$$jar); \
	done
	rm -rf $(PTJAR_TMPDIR)/META-INF
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .; $(JAR_INDEX))
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)

##############
# Rules for testing
# Most users will not run these rules.

# Instrument Java code for use with JavaScope.
jsinstr:
	$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS)
# If the jsoriginal directory does not exist, then instrument the Java files.
# If JSSKIP is set, then we skip running JavaScope on them. 
# JSSKIP is used in mescal/domains/mescalPE/kernel/makefile
jsoriginal:
	@if [ ! -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS)"; \
		$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS); \
		if [ "$(JSSKIP)" != "" ]; then \
			set $(JSSKIP); \
			for x do \
				echo "Restoring $$x so that JavaScope is not run on it"; \
				cp jsoriginal/$$x .; \
			done; \
		fi; \
	fi

# Back out the instrumentation.
jsrestore:
	if [ -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "Running jsrestore in `pwd`"; \
		$(JSRESTORE) $(JSRCS); \
		rm -f jsoriginal/README; \
		rmdir jsoriginal; \
		$(MAKE) clean; \
	else \
		echo "no jsoriginal directory, or no java sources"; \
	fi

# Compile the instrumented Java classes and include JavaScope.zip
# We run make fast first, then make all so as to avoid problems
# if files in actor/lib do not compile.	
jsbuild:
	$(MAKE) -k AUXCLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" JFLAGS="$(JFLAGS)" fast all:

# Run the test_jsimple rule with the proper classpath
jstest_jsimple:
	$(MAKE) AUXCLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" \
		test_jsimple
	@echo "To view code coverage results, run javascope or jsreport"
	@echo "To get a summary, run jsreport or jsreport -HTML or"
	@echo "jssummary -HTML -PROGRESS -OUTFILE=\$$HOME/public_html/private/js/coverage.html"
	@echo "jsreport -HTML -PROGRESS -RECURSIVE -OUTDIR=\$$HOME/public_html/private/js"

# Run the test_jgraphical rule with the proper classpath
jstest_jgraphical:
	$(MAKE) AUXCLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" \
		test_jgraphical
	@echo "To view code coverage results, run javascope or jsreport"
	@echo "To get a summary, run jssummary or jssummary -HTML"
	@echo "Note that output sometimes ends up in ~/jsreport"

# Run the test_auto rule with the proper classpath
jstest_jauto:
	$(MAKE) AUXCLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" \
		test_auto

# If necessary, instrument the classes, then rebuild, then run the tests
jsall: jsoriginal
	$(MAKE) clean
	$(MAKE) jsbuild
	if [ -w test ] ; then \
	   (cd test; $(MAKE) jstest_jsimple); \
	fi

# Run the tests in nightly mode so that the checks in NonStrictTest
# and TypeTest work.
nightly:
	$(MAKE) JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true tests

# Run all the tests
tests:: makefile
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi


# alltests.itcl is used to source all the tests
alltests.itcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in ptcommon.mk' > $@
	echo '# This file will source the .itcl files list in the' >> $@
	echo '# makefile SIMPLE_TESTS and GRAPHICAL_TESTS variables' >> $@
	echo '# This file is different from all.itcl in that all.itcl' >> $@
	echo '# will source all the .itcl files in the current directory' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo "if {![info exists reallyExit]} {set reallyExit 0}" >> $@
	echo '# Exiting when there are no more windows is wrong' >> $@
	echo "::tycho::TopLevel::exitWhenNoMoreWindows 0" >> $@
	echo "#Do an update so that we are sure tycho is done displaying" >> $@
	echo "update" >> $@
	echo "set savedir \"[pwd]\"" >> $@
	echo "if {\"$(SIMPLE_TESTS)\" != \"\"} {foreach i [list $(SIMPLE_TESTS)] {puts \$$i; cd \"\$$savedir\"; if [ file exists \$$i ] {source \$$i}}}" >> $@
	if [ "x$(GRAPHICAL_TESTS)" != "x" ]; then \
		for x in $(GRAPHICAL_TESTS); do \
			echo "puts stderr $$x" >> $@; \
			echo "cd \"\$$savedir\"" >> $@; \
			echo "source $$x" >> $@; \
		done; \
	fi
	echo "catch {doneTests}" >> $@
	echo "# IMPORTANT: DON'T CALL exit HERE." >> $@
	echo "# If exit is present, then the Builder will exit Tycho" >> $@
	echo "# whenever the tests are run" >> $@
	echo "#exit" >> $@

# alljtests.tcl is used to source all the tcl files that use Java
alljtests.tcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in ptcommon.mk' > $@
	echo '# This file will source all the Tcl files that use Java. ' >> $@
	echo '# This file will source the tcl files list in the' >> $@
	echo '# makefile SIMPLE_JTESTS and GRAPHICAL_JTESTS variables' >> $@
	echo '# This file is different from all.itcl in that all.itcl' >> $@
	echo '# will source all the .itcl files in the current directory' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo "if {![info exists reallyExit]} {set reallyExit 0}" >> $@
	echo '# Exiting when there are no more windows is wrong' >> $@
	echo "#::tycho::TopLevel::exitWhenNoMoreWindows 0" >> $@
	echo '# If there is no update command, define a dummy proc.  Jacl needs this' >> $@
	echo 'if {[info command update] == ""} then { ' >> $@
	echo '    proc update {} {}' >> $@
	echo '}' >> $@
	echo "#Do an update so that we are sure tycho is done displaying" >> $@
	echo "update" >> $@
	echo "set savedir \"[pwd]\"" >> $@
	echo "if {\"$(JSIMPLE_TESTS)\" != \"\"} {foreach i [list $(JSIMPLE_TESTS)] {puts \$$i; cd \"\$$savedir\"; if [ file exists \$$i ] { if [ catch {source \$$i} msg] {puts \"Error: \$$msg\"}}}}" >> $@
	if [ "x$(JGRAPHICAL_TESTS)" != "x" ]; then \
		for x in $(JGRAPHICAL_TESTS); do \
			echo "puts stderr $$x" >> $@; \
			echo "cd \"\$$savedir\"" >> $@; \
			echo "if [ file exists $$x ] { if [catch {source $$x} msg] {puts \"Error: \$$msg\"}}" >> $@; \
		done; \
	fi
	echo "catch {doneTests}" >> $@
	echo "exit" >> $@

# alljsimpletests.tcl is used to source only the non-graphical tests
alljsimpletests.tcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in ptcommon.mk' > $@
	echo '# This file will source all the Tcl files that use Java. ' >> $@
	echo '# This file will source the tcl files list in the' >> $@
	echo '# makefile SIMPLE_JTESTS variable' >> $@
	echo '# This file is different from all.itcl in that all.itcl' >> $@
	echo '# will source all the .itcl files in the current directory' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo "if {![info exists reallyExit]} {set reallyExit 0}" >> $@
	echo '# Exiting when there are no more windows is wrong' >> $@
	echo "#::tycho::TopLevel::exitWhenNoMoreWindows 0" >> $@
	echo '# If there is no update command, define a dummy proc.  Jacl needs this' >> $@
	echo 'if {[info command update] == ""} then { ' >> $@
	echo '    proc update {} {}' >> $@
	echo '}' >> $@
	echo "#Do an update so that we are sure tycho is done displaying" >> $@
	echo "update" >> $@
	echo "set savedir \"[pwd]\"" >> $@
	echo "if {\"$(JSIMPLE_TESTS)\" != \"\"} {foreach i [list $(JSIMPLE_TESTS)] {puts \$$i; cd \"\$$savedir\"; if [ file exists \$$i ] {source \$$i}}}" >> $@
	echo "catch {doneTests}" >> $@
	echo "exit" >> $@

# all.itcl is used to source all the *.itcl files
all.itcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in ptcommon.mk' > $@
	echo '# This file will source all the .itcl files in the current' >> $@
	echo '# directory.  This file is different from alltest.itcl' >> $@
	echo '# in that alltest.itcl will source only the itcl files' >> $@
	echo '# that are listed in the makefile' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo 'set reallyExit 0' >> $@
	echo 'set PASSED 0' >> $@
	echo 'set FAILED 0' >> $@
	echo 'foreach file [glob *.itcl] {' >> $@
	echo '    if {$$file != "all.itcl" && $$file != "alltests.itcl"} {' >> $@
	echo '         source $$file' >> $@
	echo '    }' >> $@
	echo '}' >> $@

# Run all the xml files in the auto subdirectory.
test_auto:
	$(JTCLSH) $(ROOT)/util/testsuite/auto.tcl

# Generate html files from itcl files, requires itclsh and tycho
# We use a GNU make extension here
HTMLS=$(filter %.html,  $(EXTRA_SRCS))
# weblint finds problems with html pages
# ftp://ftp.cre.canon.co.uk/pub/weblint/weblint.tar.gz
weblint:
	@if [ "$(HTMLS)" != "" ]; then \
		echo "Running weblint on $(HTMLS)"; \
		weblint -x Netscape,Java -d heading-order $(HTMLS); \
	fi
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

# Check html docs for problems
# htmlchek is not shipped with tycho, see:
# 	ftp://ftp.cs.buffalo.edu/pub/htmlchek/
HTMLCHEK=/usr/tools/www/htmlchek
HTMLCHEKOUT=htmlchekout
htmlchek:
	rm -f $(HTMLCHEKOUT)*
	HTMLCHEK=$(HTMLCHEK); export HTMLCHEK; \
	sh $(HTMLCHEK)/runachek.sh `pwd` $(HTMLCHEKOUT) `pwd` \
		map=1 netscape=1 nowswarn=1 arena=1 strictpair=TCL,AUTHOR

# Script used to find files that shold not be shipped
CHKEXTRA =	$(PTII)/util/testsuite/chkextra
checkjunk:
	@"$(CHKEXTRA)" $(SRCS) $(HDRS) $(EXTRA_SRCS) $(MISC_FILES) \
		$(OPTIONAL_FILES) $(JSRCS) makefile SCCS CVS \
		$(JCLASS) $(OBJS) $(LIBR) $(PTDISTS) \
		$(PTCLASSJAR) $(PTCLASSALLJAR) $(PTAUXALLJAR)
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

##############
# Rules for cleaning

CRUD=*.o *.so core *~ *.bak ,* LOG* *.class \
	config.cache config.log config.status manifest.tmp \
	$(JCLASS) $(PTCLASSJAR) $(PTAUXJAR) \
	$(PTCLASSALLJAR) $(PTAUXALLJAR) \
	$(PTDISTS) $(PTCLASSJAR) $(KRUFT)

clean:
	rm -f $(CRUD)
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

# Cleaner than 'make clean'
# Remove the stuff in the parent directory after processing
# the child directories incase something in the child depends on
# something we will be removing in the parent
# DISTCLEAN_STUFF - Files to be removed by 'make distclean'
distclean:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi
	rm -f $(CRUD) configure $(DISTCLEAN_STUFF)
	-rm -f doc/codeDoc/* $(OPTIONAL_FILES) $(HTMLCHEKOUT)*



# Remove the sources too, so that we can get them back from sccs
extraclean:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi
	rm -f $(CRUD) $(DISTCLEAN_STUFF) $(EXTRA_SRCS) $(JSRCS)
	-rm -f doc/codeDoc/* $(OPTIONAL_FILES) $(HTMLCHEKOUT)*
