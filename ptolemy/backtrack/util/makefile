# Makefile for the Ptolemy backtracking package
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

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
	java/util/Vector.java
	
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
	SourceOutputStream.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

gcc: jclass gcc-source transform gcc-compile

GCC_DIR = gcc/libjava

GCC_REPOSITORY = \
	:ext:anoncvs@savannah.gnu.org:/cvsroot/gcc
	
SOURCE_DATE = 20040418

gcc-source:
	@echo Checking out GCC source files...
	@rm -rf gcc
	@for javafile in $(GCC_FILES); do \
	  files="$$files $(GCC_DIR)/$$javafile"; \
	done && \
	cvs \
	  -d $(GCC_REPOSITORY) \
	  -z3 \
	  checkout \
	  -P \
	  -D $(SOURCE_DATE) \
	  $$files
	@patch -p0 < gcc-patch
	@echo Compiling GCC source files...
	@for javafile in $(GCC_FILES); do \
	  files="$$files $(GCC_DIR)/$$javafile"; \
	done && \
	"$(JAVAC)" \
	  -classpath gcc/libjava \
	  $$files

transform: gcc-source
	@echo Transform GCC source files.
	@rm -rf java
	@for javafile in $(GCC_FILES); do \
	  files="$$files $(GCC_DIR)/$$javafile"; \
	done && \
	"$(JAVA)" \
	  -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTBACKTRACK_JARS)$(AUXCLASSPATH)$(CLASSPATHSEPARATOR)gcc/libjava" \
	  -Xbootclasspath/p:gcc/libjava \
	  ptolemy.backtrack.ast.Transformer \
	  -prefix ptolemy.backtrack.util \
	  -output "$(ROOT)" \
	  -classpath gcc/libjava \
	  $$files
	@ echo Add GCC license header to each source file.
	@for javafile in $(GCC_FILES); do \
	  mv $$javafile $$javafile.bak; \
	  cat gcc-header > $$javafile; \
	  cat $$javafile.bak >> $$javafile; \
	  rm $$javafile.bak; \
	done

gcc-compile: $(GCC_CLASSES)

KRUFT = gcc java/util/*.class