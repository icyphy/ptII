# Ptolemy II Java Code generation lexical analyzer
#
# @Authors: Christopher Hylands, Shuvra Bhattacharyya based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2001 The Regents of the University of California.
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

DIRS =		extended nodetypes tree test

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
	ASTReflect.java \
	ChangeTypesVisitor.java \
	ClassDecl.java \
	ExprUtility.java \
	FieldDecl.java \
	FindExtraImportsVisitor.java \
	FormalParameterDecl.java \
	JavaClassLoader.java \
	JavaCodeGenerator.java \
	JavaConverter.java \
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
	NodeUtil.java \
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
MISC_FILES =	$(DIRS) ptbyacc

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
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) "$(JAVA)" com.JLex.Main Yylex

run_GenerateVisitor JavaVisitor.java: NodeTypes.def ../GenerateVisitor.class
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) "$(JAVA)" ptolemy.lang.GenerateVisitor NodeTypes.def JavaVisitor TreeNode nodetypes
../GenerateVisitor.class: ../GenerateVisitor.java
	(cd ..; $(MAKE) GenerateVisitor.class)

depend:
	@echo "no dependencies in this directory"

# This rule will update the .bin files by reading in .tbl files
update_tables: JavaParser.class yylhs.bin
yylhs.bin: yylhs.tbl
	rm -f *.bin
	@echo "Ignore the output, we just want the .bin files"
	$(ROOT)/bin/ptinvoke ptolemy.lang.java.PrintTree \
		$(ROOT)/$(ME)/Main.java

# This is our modified version of byacc, which is written in C
PTBYACC = $(ROOT)/$(ME)/ptbyacc/ptbyacc
yylhs.tbl: jparser.y
	$(PTBYACC) -j -p ptolemy.lang.java -f JavaParserTmp jparser.y

demo: PrintTree RegenerateCode

PrintTree:
	@echo "Print out the AST"
	CLASSPATH=$(CLASSPATH) "$(JAVA)" \
		ptolemy.lang.java.PrintTree ./nodetypes/IntLitNode.java

RegenerateCode:
	@echo "Regenerate the code"
	CLASSPATH=$(CLASSPATH) "$(JAVA)" \
		ptolemy.lang.java.RegenerateCode ./nodetypes/IntLitNode.java

analysis:
	@echo "Perform static semantic analysis"
	@echo " (You can ignore ambiguous reference warnings)"
	CLASSPATH="$(CLASSPATH)" \
	 "$(JAVA)" ptolemy.lang.java.Main ./nodetypes/IntLitNode.java

analysis2:
	@echo "Perform static semantic analysis"
	@echo " (You can ignore ambiguous reference warnings)"
	CLASSPATH="$(CLASSPATH)" \
	 "$(JAVA)" ptolemy.lang.java.Main $(ROOT)/ptolemy/actor/lib/DiscreteRandomSource.java

codegen: generate_codegen compile_codegen run_codegen
generate_codegen:
	CLASSPATH="$(CLASSPATH)" \
	 "$(JAVA)" ptolemy.domains.sdf.codegen.SDFCodeGenerator \
	 -class  ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom \
	 -iterations 50 \
	 -outdir $(ROOT) -outpkg cg.OrthogonalCom

compile_codegen:
	(cd $(ROOT)/cg/OrthogonalCom; \
	 "$(JAVAC)" $(JFLAGS) -classpath ../.. CG_Main.java)
run_codegen:
	(cd $(ROOT)/cg/OrthogonalCom; \
		"$(JAVA)" -classpath ../.. cg.OrthogonalCom.CG_Main)

# Generate code for a trivial ramp demo - much faster for testing
# To generate code in the current directory:
#   rm -rf ~/ptII/cg; make CGROOT=. ramp
CGROOT=$(ROOT)
ramp: generate_ramp compile_ramp run_ramp
generate_ramp:
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)." \
	"$(JAVA)" ptolemy.domains.sdf.codegen.SDFCodeGenerator \
	-class  ptolemy.domains.sdf.codegen.test.RampSystem \
	-iterations 50 \
	-outdir $(CGROOT) -outpkg cg.RampSystem

compile_ramp:
	(cd $(CGROOT)/cg/RampSystem; "$(JAVAC)" $(JFLAGS) -classpath ../.. CG_Main.java)
run_ramp:
	(cd $(CGROOT)/cg/RampSystem; \
		"$(JAVA)" -classpath ../.. cg.RampSystem.CG_Main)

# Generate code for the ramp demo using shallow loading of ASTs
shallowRamp: generate_shallowRamp compile_shallowRamp run_shallowRamp
generate_shallowRamp:
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)." \
	"$(JAVA)" ptolemy.domains.sdf.codegen.SDFCodeGenerator \
    -shallowLoading \
	-class  ptolemy.domains.sdf.codegen.test.RampSystem \
	-iterations 50 \
	-outdir $(CGROOT) -outpkg cg.ShallowRampSystem

compile_shallowRamp:
	(cd $(CGROOT)/cg/ShallowRampSystem; "$(JAVAC)" $(JFLAGS) \
    -classpath ../.. CG_Main.java)
run_shallowRamp:
	(cd $(CGROOT)/cg/ShallowRampSystem; \
		"$(JAVA)" -classpath ../.. cg.ShallowRampSystem.CG_Main)

# Generate code for a trivial butterfly demo - much faster for testing
butterfly: generate_butterfly compile_butterfly run_butterfly
generate_butterfly:
	CLASSPATH="$(CLASSPATH)" \
	"$(JAVA)" ptolemy.domains.sdf.codegen.SDFCodeGenerator \
	-class  ptolemy.domains.sdf.demo.Butterfly.Butterfly \
	-iterations 50 \
	-outdir $(ROOT) -outpkg cg.Butterfly

compile_butterfly:
	(cd $(ROOT)/cg/Butterfly; "$(JAVAC)" $(JFLAGS) -classpath ../.. CG_Main.java)
run_butterfly:
	(cd $(ROOT)/cg/Butterfly; \
		"$(JAVA)" -classpath ../.. cg.Butterfly.CG_Main)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
