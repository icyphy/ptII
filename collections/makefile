# Ptolemy style makefile for the collections classes written by Doug Lea
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

ME =		ptII/collections

DIRS =

# Root of the Java directory
ROOT =		..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Used to build jar files
PTPACKAGE = 	collections
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =



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


EXTRA_SRCS =	$(JSRCS) \
	index.html

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	doc images

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	#codeDoc

JCLASS = $(JSRCS:%.java=%.class)


all: suball jclass ptcollections.jar
install: subinstall jclass ptcollections.jar 
docs: javadocs
PTAUXJAR = ptcollections.jar

# jar file containing the .class files that Ptolemy II commonly uses
# Once we upgrade to JDK1.2, this jar file is likely to go away
PTCOLLECTIONS_CLASSES = \
	collections/Assertable.class \
	collections/Bag.class \
	collections/CEImpl.class \
	collections/CLCell.class \
	collections/CLEnumeration.class \
	collections/Cell.class \
	collections/CircularList.class \
	collections/Collection.class \
	collections/CollectionEnumeration.class \
	collections/Comparator.class \
	collections/CorruptedEnumerationException.class \
	collections/ElementSortedCollection.class \
	collections/HTEnumeration.class \
	collections/HashTableParams.class \
	collections/HashedMap.class \
	collections/HashedSet.class \
	collections/IllegalElementException.class \
	collections/ImplementationCheckable.class \
	collections/ImplementationError.class \
	collections/Keyed.class \
	collections/KeySortedCollection.class \
	collections/LLCell.class \
	collections/LLCellEnumeration.class \
	collections/LLMap.class \
	collections/LLPair.class \
	collections/LLPairEnumeration.class \
	collections/LinkedList.class \
	collections/Map.class \
	collections/Predicate.class \
	collections/Pair.class \
	collections/Seq.class \
	collections/Set.class \
	collections/SortableCollection.class \
	collections/UpdatableCollection.class \
	collections/UpdatableImpl.class \
	collections/UpdatableMap.class \
	collections/UpdatableMapImpl.class \
	collections/UpdatableSeq.class \
	collections/UpdatableSeqImpl.class \
	collections/UpdatableSet.class \
	collections/UpdatableSetImpl.class

PTCOLLECTIONS_DEPENDENCIES = \
	Assertable.class \
	Bag.class \
	CEImpl.class \
	Cell.class \
	Collection.class \
	CollectionEnumeration.class \
	Comparator.class \
	CorruptedEnumerationException.class \
	ElementSortedCollection.class \
	HTEnumeration.class \
	HashTableParams.class \
	HashedMap.class \
	HashedSet.class \
	IllegalElementException.class \
	ImplementationCheckable.class \
	ImplementationError.class \
	Keyed.class \
	KeySortedCollection.class \
	LLCell.class \
	LLCellEnumeration.class \
	LLPair.class \
	LinkedList.class \
	Map.class \
	Predicate.class \
	Pair.class \
	Seq.class \
	Set.class \
	SortableCollection.class \
	UpdatableCollection.class \
	UpdatableImpl.class \
	UpdatableMap.class \
	UpdatableMapImpl.class \
	UpdatableSeq.class \
	UpdatableSeqImpl.class \
	UpdatableSet.class \
	UpdatableSetImpl.class

$(PTAUXJAR): $(PTCOLLECTIONS_DEPENDENCIES)
	(cd $(ROOT); $(JAR) cf collections/$@ $(PTCOLLECTIONS_CLASSES) )

depend:
	echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
