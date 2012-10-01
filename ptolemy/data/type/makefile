# Makefile for Java Ptolemy type classes
#
# @Authors: Christopher Hylands, Neil Smyth based on a file by Thomas M. Parks
#
# $Id$
#
# @Copyright (c) 1999-2012 The Regents of the University of California.
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

ME =		ptolemy/data/type

DIRS = 		test demo

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	type
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	ActorType.java \
	ArrayType.java \
	AssociativeType.java \
	BaseType.java \
	FixType.java \
	FunctionType.java \
	HasTypeConstraints.java \
	MatrixType.java \
	MonotonicFunction.java \
	ObjectType.java \
	RecordType.java \
	StructuredType.java \
	TupleType.java \
	Type.java \
	Typeable.java \
	TypeConstant.java \
	TypeLattice.java \
	UnionType.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include $(DIRS) here, we don't want make sources to run make demo
MISC_FILES =	test #$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	'ArrayType$$1.class' \
	'ArrayType$$2.class' \
	'ArrayType$$ArrayBottomTypeTerm.class' \
	'ArrayType$$TypeableSizedArrayTypeTerm.class' \
	'ArrayType$$TypeableArrayTypeTerm.class' \
	'ArrayType$$TypeableElementTypeTerm.class' \
	'ArrayType$$ElementTypeTerm.class' \
	'BaseType$$ArrayBottomType.class' \
	'BaseType$$FloatType.class' \
	'BaseType$$NilType.class' \
	'BaseType$$ShortType.class' \
	'BaseType$$UnsizedFixType.class' \
	'MatrixType$$BooleanMatrixType.class' \
	'MatrixType$$ComplexMatrixType.class' \
	'MatrixType$$DoubleMatrixType.class' \
	'MatrixType$$FixMatrixType.class' \
	'MatrixType$$IntMatrixType.class' \
	'MatrixType$$LongMatrixType.class' \
	'BaseType$$1.class' \
	'BaseType$$10.class' \
	'BaseType$$11.class' \
	'BaseType$$12.class' \
	'BaseType$$13.class' \
	'BaseType$$14.class' \
	'BaseType$$15.class' \
	'BaseType$$16.class' \
	'BaseType$$17.class' \
	'BaseType$$18.class' \
	'BaseType$$19.class' \
	'BaseType$$2.class' \
	'BaseType$$20.class' \
	'BaseType$$3.class' \
	'BaseType$$4.class' \
	'BaseType$$5.class' \
	'BaseType$$6.class' \
	'BaseType$$7.class' \
	'BaseType$$8.class' \
	'BaseType$$9.class' \
	'BaseType$$BooleanMatrixType.class' \
	'BaseType$$BooleanType.class' \
	'BaseType$$ByteType.class' \
	'BaseType$$ComplexMatrixType.class' \
	'BaseType$$ComplexType.class' \
	'BaseType$$DoubleMatrixType.class' \
	'BaseType$$DoubleType.class' \
	'BaseType$$FixMatrixType.class' \
	'BaseType$$FixType.class' \
	'BaseType$$GeneralType.class' \
	'BaseType$$IntMatrixType.class' \
	'BaseType$$IntType.class' \
	'BaseType$$LongMatrixType.class' \
	'BaseType$$LongType.class' \
	'BaseType$$MatrixType.class' \
	'BaseType$$NumericalType.class' \
	'BaseType$$ObjectType.class' \
	'BaseType$$PetiteType.class' \
	'BaseType$$ScalarType.class' \
	'BaseType$$StringType.class' \
	'BaseType$$UnknownType.class' \
	'BaseType$$ConvertOperator.class' \
	'BaseType$$EventType.class' \
	'BaseType$$UnsignedByteType.class' \
	'BaseType$$XmlTokenType.class' \
	'FunctionType$$1.class' \
	'FunctionType$$FieldTypeTerm.class' \
	'ObjectType$$BottomClass.class' \
	'SizedMatrixType$$BooleanMatrixType.class' \
	'SizedMatrixType$$ComplexMatrixType.class' \
	'SizedMatrixType$$DoubleMatrixType.class' \
	'SizedMatrixType$$FixMatrixType.class' \
	'SizedMatrixType$$IntMatrixType.class' \
	'SizedMatrixType$$LongMatrixType.class' \
	'RecordType$$1.class' \
	'RecordType$$FieldType.class' \
	'TupleType$$1.class' \
	'TupleType$$FieldTypeTerm.class' \
	'TypeLattice$$1.class' \
	'TypeLattice$$TheTypeLattice.class' \
	'UnionType$$1.class' \
	'UnionType$$FieldType.class' \
	'UnsizedMatrixType$$BooleanMatrixType.class' \
	'UnsizedMatrixType$$ComplexMatrixType.class' \
	'UnsizedMatrixType$$DoubleMatrixType.class' \
	'UnsizedMatrixType$$FixMatrixType.class' \
	'UnsizedMatrixType$$IntMatrixType.class' \
	'UnsizedMatrixType$$LongMatrixType.class' \

JCLASS= $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)
# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
