# Ptolemy style makefile for the collections classes written by Doug Lea
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

# Note that the Java classes contained in this directory were written
# by Sun.  These classes came from tcljava, see license.terms for more info.

# Root of the Java directory
ROOT =		../..

ME =		java/collections

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

DIRS = 		

# Used to build zip files
JPACKAGE = 	collections
JPACKAGE_DIR = 	collections
JVERSION =	1.0
JDIST =		$(JPACKAGE)$(JVERSION)
JZIP =		collections/collections.zip
JARFILE = 	$(JPACKAGE).jar

CLASSPATH = ..

JSRCS = \
	ArrayEnumeration.java \
	Assertable.java \
	Bag.java \
	BinaryFunction.java \
	CEImpl.java \
	CLCell.java \
	CLEnumeration.java \
	Cell.java \
	CheckedBag.java \
	CheckedCollection.java \
	CheckedMap.java \
	CheckedSeq.java \
	CheckedSet.java \
	CircularList.java \
	Collection.java \
	CollectionEnumeration.java \
	Comparator.java \
	CorruptedEnumerationException.java \
	DAEnumeration.java \
	DefaultComparator.java \
	DefaultImplementations.java \
	Dynarray.java \
	ElementSortedCollection.java \
	FilteringEnumeration.java \
	Function.java \
	HTEnumeration.java \
	HTPairEnumeration.java \
	HashTableParams.java \
	HashedMap.java \
	HashedSet.java \
	IllegalElementException.java \
	Immutable.java \
	ImplementationCheckable.java \
	ImplementationError.java \
	IncrBag.java \
	IncrCollectionEnumeration.java \
	IncrImpl.java \
	IncrMap.java \
	IncrSeq.java \
	IncrSet.java \
	InterleavingEnumeration.java \
	KeySortedCollection.java \
	Keyed.java \
	LBEnumeration.java \
	LLCell.java \
	LLCellEnumeration.java \
	LLMap.java \
	LLPair.java \
	LLPairEnumeration.java \
	LinkedBuffer.java \
	LinkedList.java \
	Map.java \
	MappingEnumeration.java \
	Pair.java \
	Predicate.java \
	Procedure.java \
	RBCell.java \
	RBCellEnumeration.java \
	RBMap.java \
	RBPair.java \
	RBPairEnumeration.java \
	RBTree.java \
	Seq.java \
	Set.java \
	SortableCollection.java \
	UpdatableBag.java \
	UpdatableBagImpl.java \
	UpdatableCollection.java \
	UpdatableImpl.java \
	UpdatableMap.java \
	UpdatableMapImpl.java \
	UpdatableSeq.java \
	UpdatableSeqImpl.java \
	UpdatableSet.java \
	UpdatableSetImpl.java \


EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	doc images

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =	#codeDoc

JCLASS = $(JSRCS:%.java=%.class)

# Needed for compiling under Cygwin on NT
LIB = dummylib

all: suball jclass
install: subinstall jclass javadocs $(JARFILE)
docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk



