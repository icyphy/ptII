# Ptolemy II Java Code generation lexical analyzer
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

ME =		ptolemy/lang/java

DIRS =		extended nodetypes

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lang
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	ChangeTypesVisitor.java \
	ClassDecl.java \
	ExprUtility.java \
	FieldDecl.java \
	FindExtraImportsVisitor.java \
	FormalParameterDecl.java \
	JavaCodeGenerator.java \
	JavaDecl.java \
	JavaParser.java \
	JavaParserManip.java \
	JavaParserval.java \
	JavaStaticSemanticConstants.java \
	LocalVarDecl.java \
	Main.java \
	MemberDecl.java \
	MethodDecl.java \
	Modifier.java \
	NumberDeclVisitor.java \
	NumberNodeVisitor.java \
	PackageDecl.java \
	PackageResolutionVisitor.java \
	PrintTree.java \
	RegenerateCode.java \
	RemovePropertyVisitor.java \
	RemoveTrackVisitor.java \
	RenameJavaVisitor.java \
	ReplacementJavaVisitor.java \
	ResolveClassVisitor.java \
	ResolveFieldVisitor.java \
	ResolveImportsVisitor.java \
	ResolveInheritanceVisitor.java \
	ResolveNameVisitor.java \
	ResolvePackageVisitor.java \
	ResolveTypesVisitor.java \
	ResolveVisitorBase.java \
	SearchPath.java \
	Skeleton.java \
	SkeletonVisitor.java \
	StaticResolution.java \
	StmtLblDecl.java \
	TypeDecl.java \
	TypeIdentifier.java \
	TypePolicy.java \
	TypeUtility.java \
	TypeVisitor.java \
	TypedDecl.java

EXTRA_SRCS =	$(JSRCS) \
	JavaVisitor.java \
	NodeTypes.def \
	Yylex \
	Yylex.java

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
# yyrule.tbl is only called if we set yydebug in JavaParser
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'ResolveFieldVisitor$$FieldContext.class' \
	'ResolveNameVisitor$$NameContext.class' \
	jparser.y \
	yycheck.bin \
	yydefred.bin \
	yydgoto.bin \
	yygindex.bin \
	yylen.bin \
	yylhs.bin \
	yyrindex.bin \
	yyrule.tbl \
	yysindex.bin \
	yytable.bin \

JCLASS = $(JSRCS:%.java=%.class) \
	JavaVisitor.class \
	Yylex.class

all: JavaVisitor.class jclass
install: jclass $(PTCLASSJAR)

Yylex.java: Yylex
	CLASSPATH=$(CLASSPATH) $(JAVA) com.JLex.Main Yylex

run_GenerateVisitor JavaVisitor.java: NodeTypes.def
	CLASSPATH=$(CLASSPATH) $(JAVA) ptolemy.lang.GenerateVisitor NodeTypes.def JavaVisitor TreeNode nodetypes

# Note that we DON'T want to run Skeleton in any directory that contain
# actors, like actor/lib or domains/sdf/lib because the code generator
# needs to transform the code contained in actors.
# If jskel files are present in actor.lib, then you will end up
# with the intialize() and fire() methods being declared native, which
# will result in errors like:
# cxh@maury 15% java -classpath ../.. cg.orthocom.CG_Main
# Exception in thread "main" java.lang.UnsatisfiedLinkError: initialize
#	at cg.orthocom.CG_Main.main(CG_Main.java, Compiled Code)
#
SKELETON_DIRS = \
	ptolemy/actor/sched \
	ptolemy/actor/util \
	ptolemy/kernel \
	ptolemy/kernel/util \
	ptolemy/data \
	ptolemy/data/type \
	ptolemy/graph \
	ptolemy/math 

skeleton:
	for x in $(SKELETON_DIRS); do \
		echo running Skeleton in $(PTII)/$$x ; \
		CLASSPATH=$(CLASSPATH) $(JAVA) ptolemy.lang.java.Skeleton $(PTII)/$$x/*.java; \
	done




depend:
	@echo "no dependencies in this directory"

# This rule will update the .bin files by reading in .tbl files
# Note that this requires the modified version of byacc that 
# we don't actually ship with ptII
update_tables: JavaParser.class Skeleton.class yylhs.bin
yylhs.bin: yylhs.tbl
	$(ROOT)/bin/ptinvoke ptolemy.lang.java.Skeleton $(ROOT)/$(ME)/Main.java
	-rm -f Main.jskel

# We don't actually ship our modified version of byacc
PTBYACC = $(ROOT)/vendors/misc/byacc/ptbyacc
yylhs.tbl: jparser.y 
	$(PTBYACC) -j -p ptolemy.lang.java -f JavaParserTmp jparser.y	

demo: PrintTree RegenerateCode

PrintTree:
	@echo "Print out the AST"
	CLASSPATH=$(CLASSPATH) $(JAVA) \
		ptolemy.lang.java.PrintTree ./nodetypes/IntLitNode.java

RegenerateCode:
	@echo "Regenerate the code"
	CLASSPATH=$(CLASSPATH) $(JAVA) \
		ptolemy.lang.java.RegenerateCode ./nodetypes/IntLitNode.java

# FIXME: we can't ship these
JAVASRC_STUBS=$(PTII)/vendors/sun/src

analysis:
	@echo "Perform static semantic analysis"
	@echo " (You can ignore ambiguous reference warnings)"
	CLASSPATH=$(CLASSPATH)$(CLASSPATHSEPARATOR)$(JAVASRC_STUBS) \
	$(JAVA) ptolemy.lang.java.Main ./nodetypes/IntLitNode.java

codegen: generate_codegen compile_codegen run_codegen 
generate_codegen:
	@if [ ! -d $(JAVASRC_STUBS) ]; then \
		echo "Warning $(JAVASRC_STUBS) does not exist"; \
	fi
	CLASSPATH=$(CLASSPATH)$(CLASSPATHSEPARATOR)$(JAVASRC_STUBS) \
	$(JAVA) -verbose:class  ptolemy.domains.sdf.codegen.SDFCodeGenerator \
	-class  ptolemy.domains.sdf.demo.orthocom.OrthogonalComSystem \
	-iterations 500 \
	-outdir $(ROOT) -outpkg cg.orthocom
compile_codegen:
	(cd $(ROOT)/cg/orthocom; $(JAVAC) -classpath ../.. CG_Main.java)
run_codegen: 	
	(cd $(ROOT)/cg/orthocom; \
		$(JAVA) -classpath ../.. cg.orthocom.CG_Main)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



