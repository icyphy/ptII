# Ptolemy II Java Code generation lexical analyzer nodetypes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

ME =		ptolemy/lang/java/nodetypes

DIRS =

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lang
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \

# Derived java sources generated in the makefile above
DERIVED_JSRCS = \
	AbsentTreeNode.java \
	AllocateAnonymousClassNode.java \
	AllocateArrayNode.java \
	AllocateNode.java \
	ArrayAccessNode.java \
	ArrayInitNode.java \
	ArrayInitTypeNode.java \
	ArrayTypeNode.java \
	AssignNode.java \
	BinaryArithAssignNode.java \
	BinaryArithNode.java \
	BinaryOpAssignNode.java \
	BinaryOpNode.java \
	BitAndAssignNode.java \
	BitAndNode.java \
	BitOrAssignNode.java \
	BitOrNode.java \
	BitXorAssignNode.java \
	BitXorNode.java \
	BitwiseAssignNode.java \
	BitwiseNode.java \
	BlockNode.java \
	BoolLitNode.java \
	BoolTypeNode.java \
	BreakNode.java \
	ByteTypeNode.java \
	CandNode.java \
	CaseNode.java \
	CastNode.java \
	CatchNode.java \
	CharLitNode.java \
	CharTypeNode.java \
	ClassDeclNode.java \
	CompileUnitNode.java \
	ComplementNode.java \
	ConstructorCallNode.java \
	ConstructorDeclNode.java \
	ContinueNode.java \
	CorNode.java \
	DeclaratorNode.java \
	DivAssignNode.java \
	DivNode.java \
	DoubleExprNode.java \
	DoubleLitNode.java \
	DoubleTypeNode.java \
	EQNode.java \
	EmptyStmtNode.java \
	EqualityNode.java \
	ExprNode.java \
	ExprStmtNode.java \
	FieldAccessNode.java \
	FieldDeclNode.java \
	FloatLitNode.java \
	FloatTypeNode.java \
	ForNode.java \
	GENode.java \
	GTNode.java \
	IfExprNode.java \
	IfStmtNode.java \
	ImportNode.java \
	ImportOnDemandNode.java \
	IncrDecrNode.java \
	InstanceInitNode.java \
	InstanceOfNode.java \
	IntLitNode.java \
	IntTypeNode.java \
	InterfaceDeclNode.java \
	InvokableDeclNode.java \
	IterationNode.java \
	JumpStmtNode.java \
	LENode.java \
	LTNode.java \
	LabeledStmtNode.java \
	LeftShiftLogAssignNode.java \
	LeftShiftLogNode.java \
	LiteralNode.java \
	LocalVarDeclNode.java \
	LogCondNode.java \
	LongLitNode.java \
	LongTypeNode.java \
	LoopNode.java \
	MethodCallNode.java \
	MethodDeclNode.java \
	MinusAssignNode.java \
	MinusNode.java \
	ModifiedNode.java \
	MultAssignNode.java \
	MultNode.java \
	NENode.java \
	NameNode.java \
	NamedNode.java \
	NodeClassID.java \
	NotNode.java \
	NullPntrNode.java \
	NullTypeNode.java \
	ObjectFieldAccessNode.java \
	ObjectNode.java \
	OuterClassAccessNode.java \
	OuterSuperAccessNode.java \
	OuterThisAccessNode.java \
	ParameterNode.java \
	PlusAssignNode.java \
	PlusNode.java \
	PostDecrNode.java \
	PostIncrNode.java \
	PreDecrNode.java \
	PreIncrNode.java \
	PrimitiveTypeNode.java \
	ReferenceTypeNode.java \
	RelationNode.java \
	RemAssignNode.java \
	RemNode.java \
	ReturnNode.java \
	RightShiftArithAssignNode.java \
	RightShiftArithNode.java \
	RightShiftLogAssignNode.java \
	RightShiftLogNode.java \
	ShiftAssignNode.java \
	ShiftNode.java \
	ShortTypeNode.java \
	SingleExprNode.java \
	SingleOpNode.java \
	StatementNode.java \
	StaticInitNode.java \
	StringLitNode.java \
	SuperConstructorCallNode.java \
	SuperFieldAccessNode.java \
	SwitchBranchNode.java \
	SwitchNode.java \
	SynchronizedNode.java \
	ThisConstructorCallNode.java \
	ThisFieldAccessNode.java \
	ThisNode.java \
	ThrowNode.java \
	TryNode.java \
	TypeClassAccessNode.java \
	TypeFieldAccessNode.java \
	TypeNameNode.java \
	TypeNode.java \
	UnaryArithNode.java \
	UnaryMinusNode.java \
	UnaryPlusNode.java \
	UserTypeDeclNode.java \
	UserTypeDeclStmtNode.java \
	VarDeclNode.java \
	VarInitDeclNode.java \
	VoidTypeNode.java


EXTRA_SRCS =	$(JSRCS) \
	$(DERIVED_JSRCS) \

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
        GenerateVisitor$ClassField.class \
        GenerateVisitor$MethodSignature.class \
	$(PTCLASSJAR) 

JCLASS = $(JSRCS:%.java=%.class) $(DERIVED_JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

$(DERIVED_JSRCS): ../NodeTypes.def
	(cd ..; make run_GenerateVisitor)

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



