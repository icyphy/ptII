# Makefile for Java Ptolemy Expression Parser classes
#
# @Authors: Christopher Hylands, Neil Smyth, based on a file by Thomas M. Parks
#
# $Id$
#
# @Copyright (c) 1998-2013 The Regents of the University of California.
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

# This directory contains classes used for the Ptolemy II parser, PtParser.
# The parser is generated using JavaCC and JJTree. It creates a syntax
# tree of Node objects. The node classes used by PtrParser when generating
# the tree are the ASTPt*.java files.

ME =		ptolemy/data/expr

DIRS = 		test

# Root of the Ptolemy II directory
ROOT =		../../..

# We include JavaCC.zip twice here so as to handle JavaCC1.1 and JavaCC2.1
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(JAVACC_DIR)/JavaCC.zip$(CLASSPATHSEPARATOR)$(JAVACC_DIR)/bin/lib/JavaCC.zip

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Used to build jar files
PTPACKAGE = 	expr
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AbstractInitializableParameter.java \
	AbstractParseTreeVisitor.java \
	ASTPtArrayConstructNode.java \
	ASTPtAssignmentNode.java \
	ASTPtBitwiseNode.java \
	ASTPtFunctionApplicationNode.java \
        ASTPtFunctionDefinitionNode.java \
	ASTPtFunctionalIfNode.java \
	ASTPtLeafNode.java \
	ASTPtLogicalNode.java \
        ASTPtMatrixConstructNode.java \
        ASTPtMethodCallNode.java \
	ASTPtOrderedRecordConstructNode.java \
	ASTPtProductNode.java \
	ASTPtPowerNode.java \
	ASTPtRecordConstructNode.java \
	ASTPtRelationalNode.java \
	ASTPtRootNode.java \
	ASTPtShiftNode.java \
	ASTPtSumNode.java \
	ASTPtUnaryNode.java \
	ASTPtUnionConstructNode.java \
	CachedMethod.java \
	ChoiceParameter.java \
	ConcreteMatrixToken.java \
	ConcreteScalarToken.java \
	Constants.java \
	ContainmentExtender.java \
	ConstraintMonitor.java \
	ConversionUtilities.java \
	CParseTreeCodeGenerator.java \
	ExpertParameter.java \
	ExplicitScope.java \
	ExpressionFunction.java \
	FileParameter.java \
	FixPointFunctions.java \
	GeneralToken.java \
	MatlabUtilities.java \
	ModelScope.java \
	MoMLUtilities.java \
        NamedConstantsScope.java \
	NameParameter.java \
        NestedScope.java \
	Parameter.java \
	ParseTreeDumper.java \
	ParseTreeEvaluator.java \
	ParseTreeFreeVariableCollector.java \
	ParseTreeFreeVariableRenamer.java \
	ParseTreeSpecializer.java \
	ParseTreeTypeInference.java \
	ParseTreeVisitor.java \
	ParseTreeWriter.java \
        ParserScope.java \
        ScopeExtendingAttribute.java \
        SingletonParameter.java \
	StringConstantParameter.java \
        StringParameter.java \
        TemporaryVariable.java \
	UndefinedConstantOrIdentifierException.java \
	UnknownResultException.java \
	UtilityFunctions.java \
	UnknownToken.java \
	Variable.java \
	XMLParser.java

EXTRA_SRCS =	PtParser.jjt MatrixParser.jjt $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# OPTIONAL_JSRCS files are created by javacc.
# javadoc generates html for files in $(OPTIONAL_JSRCS)
#	ASCII_CharStream.java 
OPTIONAL_JSRCS = \
	JJTMatrixParserState.java \
	JJTPtParserState.java \
	MatrixParserConstants.java \
	MatrixParserTokenManager.java \
	MatrixParserTreeConstants.java \
	Node.java \
	ParseException.java \
	PtParserConstants.java \
	PtParserTokenManager.java \
	PtParserTreeConstants.java \
	SimpleCharStream.java \
	SimpleNode.java \
	Token.java \
	TokenMgrError.java

# OPTIONAL_JSRCS depends on PtParser.java, but we want javadoc
# to generate docs for PtParser, so we modify the javadoc
# rule to also operate on DERIVED_JSRCS
DERIVED_JSRCS = PtParser.java MatrixParser.java

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =  \
	doc \
	$(OPTIONAL_JSRCS) \
	message.txt \
	JJPtParserCalls.class \
	PtParser.java \
	PtParser.jj \
	JJMatrixParserState.class \
	MatrixParser.jj \
	MatrixParser.java \
	'ASTPtProductNode$$1.class' \
	'ASTPtProductNode$$FunctionTerm.class' \
	'CachedMethod$$1.class' \
	'CachedMethod$$2.class' \
	'CachedMethod$$3.class' \
	'CachedMethod$$ArgumentConversion.class' \
	'CachedMethod$$ArrayMapCachedMethod.class' \
	'CachedMethod$$BaseConvertCachedMethod.class' \
	'CachedMethod$$MatrixMapCachedMethod.class' \
	'CachedMethod$$TypeArgumentConversion.class' \
	'ConstraintMonitor$$ConstraintMonitorAttributes.class' \
	'Variable$$CircularDependencyError.class' \
	'Parameter$$ParseList$$Element.class' \
	'Parameter$$ParseList.class' \
	'ParseTreeTypeInference$$1.class' \
	'PtParser$$1.class' \
	'PtParser$$JJCalls.class' \
	'PtParser$$LookaheadSuccess.class' \
	'Variable$$1.class' \
	'Variable$$TypeTerm.class' \
	'Variable$$VariableScope.class' \
	'UtilityFunctions$$TokenComparator.class' \
	'XMLParser$$EntityResolver.class'

JCLASS =  \
	$(JSRCS:%.java=%.class) \
	PtParser.class \
	MatrixParser.class \
	$(OPTIONAL_JSRCS:%.java=%.class)


all:  $(OPTIONAL_JSRCS) $(JCLASS)
install: $(PTCLASSJAR)


$(OPTIONAL_JSRCS): PtParser.java MatrixParser.java

# Uncomment this to work on the parser.
PtParser.java: PtParser.jj
	@echo "===="
	@echo ""
	@echo "Creating PtParser.java from PtParser.jj"
	@echo ""
	@echo "This command runs JavaCC, which can be downloaded from"
	@echo "http://java.net/projects/javacc"
	@echo 'See $PTII/doc/install.htm for details.'
	@echo "You only need install JavaCC if you modify PtParser.jjt"
	@echo "PtParser.java is derived from PtParser.jj, which is in turn"
	@echo "derived from PtParser.jjt."
	@echo "If you modify PtParser.jjt, you should run"
	@echo '   rm `make echo_OPTIONAL_JSRCS`; make'
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch PtParser.jj PtParser.java"
	CLASSPATH="$(CLASSPATH)" $(JAVACC) $(JAVACC_OPTIONS) PtParser.jj

PtParser.jj: PtParser.jjt
	@echo "===="
	@echo ""
	@echo "Creating PtParser.jj from PtParser.jjt"
	@echo ""
	@echo "This command runs JJTree, which is part of JavaCC"
	@echo "JavaCC can be downloaded from"
	@echo "http://java.net/projects/javacc"
	@echo 'See $PTII/doc/install.htm for details.'
	@echo "You only need install JavaCC if you modify PtParser.jjt"
	@echo "PtParser.java is derived from PtParser.jj, which is in turn"
	@echo "derived from PtParser.jjt."
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch PtParser.jj PtParser.java"
	CLASSPATH="$(CLASSPATH)" $(JJTREE) $(JJTREE_OPTIONS) PtParser.jjt

MatrixParser.java: MatrixParser.jj
	@echo "===="
	@echo ""
	@echo "Creating MatrixParser.java from MatrixParser.jj"
	@echo ""
	@echo "This command runs JavaCC, which can be downloaded from"
	@echo "http://java.net/projects/javacc"
	@echo 'See $PTII/doc/install.htm for details.'
	@echo "You only need install JavaCC if you modify MatrixParser.jjt"
	@echo "MatrixParser.java is derived from MatrixParser.jj, which is in turn"
	@echo "derived from MatrixParser.jjt"
	@echo "If you modify MatrixParser.jjt, you should run"
	@echo '   rm `make echo_OPTIONAL_JSRCS`; make'
	@echo "If, during compilation, you get "
	@echo "   error: incompatible types: MatrixParser cannot be converted to PtParser"
	@echo "then run: "
	@echo '   rm `make echo_OPTIONAL_JSRCS`'
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch MatrixParser.jj MatrixParser.java"
	CLASSPATH="$(CLASSPATH)" $(JAVACC) $(JAVACC_OPTIONS) MatrixParser.jj

MatrixParser.jj: MatrixParser.jjt
	@echo "===="
	@echo ""
	@echo "Creating MatrixParser.jj from MatrixParser.jjt"
	@echo ""
	@echo "This command runs JJTree, which is part of JavaCC"
	@echo "JavaCC can be downloaded from"
	@echo "http://java.net/projects/javacc"
	@echo 'See $PTII/doc/install.htm for details.'
	@echo "You only need install JavaCC if you modify MatrixParser.jjt"
	@echo "MatrixParser.java is derived from MatrixParser.jj, which is in turn"
	@echo "derived from MatrixParser.jjt"
	@echo "If, during compilation, you get "
	@echo "   error: incompatible types: MatrixParser cannot be converted to PtParser"
	@echo "then run: "
	@echo '   rm `make echo_OPTIONAL_JSRCS`'
	@echo "If you don't have JavaCC, you can workaround this with"
	@echo "   touch MatrixParser.jj MatrixParser.java"
	CLASSPATH="$(CLASSPATH)" $(JJTREE) $(JJTREE_OPTIONS) MatrixParser.jjt

# This rule is useful for cvs committing the derived files
# For example:
# cvs commit -m "Updated MatrixParser." `make echo_OPTIONAL_JSRCS`
echo_OPTIONAL_JSRCS:
	@echo $(OPTIONAL_JSRCS) PtParser.java PtParser.jj MatrixParser.java MatrixParser.jj

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
