# Makefile for the Ptolemy backtracking package
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005-2009 The Regents of the University of California.
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

ME =		ptolemy/backtrack/util

DIRS = 		test

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	util
PTCLASSJAR =
# We use OTHER_JARS instead of PTCLASSJAR because the directories under
# this one do not follow our coding standards.
OTHER_JARS =    $(PTPACKAGE).jar

# GCC files.
GCC_FILES = \
	java/util/AbstractCollection.java \
	java/util/AbstractList.java \
	java/util/AbstractMap.java \
	java/util/AbstractSequentialList.java \
	java/util/AbstractSet.java \
	java/util/ArrayList.java \
	java/util/Collection.java \
	java/util/Collections.java \
	java/util/HashMap.java \
	java/util/HashSet.java \
	java/util/Hashtable.java \
	java/util/List.java \
	java/util/LinkedHashMap.java \
	java/util/LinkedHashSet.java \
	java/util/LinkedList.java \
	java/util/Map.java \
	java/util/Random.java \
	java/util/Set.java \
	java/util/SortedMap.java \
	java/util/SortedSet.java \
	java/util/Stack.java \
	java/util/TreeMap.java \
	java/util/TreeSet.java \
	java/util/Vector.java \
	java/util/WeakHashMap.java

GCC_CLASSES = $(GCC_FILES:%.java=%.class)

# Don't use GNU make extensions here
#    $(wildcard *.java)
JSRCS = \
	$(GCC_FILES) \
	CheckpointRecord.java \
	ClassFileLoader.java \
	FieldRecord.java \
	FieldRecordState.java \
	PathFinder.java \
	SourceOutputStream.java \
	Strings.java

OTHER_FILES_TO_BE_JARED = \
	gcc-header \
	gcc-patch \
	gcj-copyright.html \

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Needed for "make fast"
GCC_FILES_CLASS = $(GCC_FILES:%.java=%.class)
EXTRA_TARGETS = $(GCC_FILES_CLASS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	java

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'CheckpointRecord$$Record.class' \
	'FieldRecord$$CombinedIterator.class' \
	'FieldRecord$$IndividualIterator.class' \
	'FieldRecord$$Record.class' \
	'FieldRecord$$RecordList.class' \
	'PathFinder$$DirectoryFilter.class' \
	'PathFinder$$PostfixFilter.class' \
	$(OTHER_JARS)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Build the .class files in util/java before running the tests
tests:: jclass

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk


gcc: gcc-source transform gcc-compile

GCJ_DIR = branches/gcc-4_1-branch/libjava/classpath/

GCC_REPOSITORY = \
	svn://gcc.gnu.org/svn/gcc/

SOURCE_DATE = 20040418

gcc-source:
	@echo Checking out GCC source files...
	@rm -rf gcc
	@mkdir -p gcc/java/util/; \
	cd gcc/java/util/; \
	for javafile in $(GCC_FILES); do \
	  svn export "$(GCC_REPOSITORY)$(GCJ_DIR)$$javafile"; \
	done
	@patch -p0 < gcc-patch
	@echo Compiling GCC source files...
	@for javafile in $(GCC_FILES); do \
	  files="$$files gcc/$$javafile"; \
	done && \
	"$(JAVAC)" \
	  -classpath gcc \
	  $$files

transform:
	@echo Transform GCC source files.
	@rm -rf java
	@for javafile in $(GCC_FILES); do \
	  files="$$files gcc/$$javafile"; \
	done && \
	"$(JAVA)" \
	  -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTBACKTRACK_ECLIPSE_JARS)$(AUXCLASSPATH)$(CLASSPATHSEPARATOR)gcc" \
	  -Xbootclasspath/p:gcc \
	  ptolemy.backtrack.eclipse.ast.Transformer \
	  -prefix ptolemy.backtrack.util \
	  -output "$(ROOT)" \
	  -classpath gcc \
	  $$files
	# @ echo Add GCC license header to each source file.
	# @for javafile in $(GCC_FILES); do \
	#   mv $$javafile $$javafile.bak; \
	#   cat gcc-header > $$javafile; \
	#   cat $$javafile.bak >> $$javafile; \
	#   rm $$javafile.bak; \
	# done

gcc-compile: $(GCC_CLASSES)

util.jar: $(JSRCS) $(JCLASS) $(OTHER_FILES_TO_BE_JARED) all
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	# Copy any class files from this directory
	mkdir -p $(PTJAR_TMPDIR)/$(ME)
	"$(JAR)" -c `find . -name "*.xml" -o -name "*.class"` | (cd $(PTJAR_TMPDIR)/$(ME); "$(JAR)" -x)
	-cp $(OTHER_FILES_TO_BE_JARED) $(PTJAR_TMPDIR)/$(ME)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" $(JAR_FLAGS) -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	$(JAR_INDEX)
	rm -rf $(PTJAR_TMPDIR)

KRUFT = gcc java/util/*.class util.jar
