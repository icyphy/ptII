# Makefile for Ptolemy II Java deep code generation
#
# @Authors: Steve Neuendorffer, Christopher Hylands
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

ME =		ptolemy/copernicus/java

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(ROOT)/vendors/soot/1.2.2/soot/classes$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	java
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	ActorTransformer.java \
	CommandLineTemplate.java \
	CommandLineTransformer.java \
	ConstantLoopUnroller.java \
	DeadObjectEliminator.java \
	ExceptionEliminator.java \
	FieldsForAttributesTransformer.java \
	FieldsForPortsTransformer.java \
	FieldOptimizationTransformer.java \
	InlineDirectorTransformer.java \
	InlineParameterTransformer.java \
	InlinePortTransformer.java \
	InlineTokenTransformer.java \
	Main.java \
	ModelTransformer.java \
	NamedObjEliminator.java \
	NamedObjEqualityEliminator.java \
	TestApplication.java \
	TokenConstructorAnalysis.java \
	TokenInstanceofEliminator.java \
	TokenToNativeTransformer.java \
	TokenTypeAnalysis.java \
	TypeSpecializer.java \
	TypeSpecializerAnalysis.java \
	TypeTag.java \
	UnreachableMethodRemover.java \
	ValueTag.java

#	NamedObjConstructorAnalysis.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	cg \
	'CommandLineTemplate$$1.class' \
	'TypeSpecializer$$ConstantTerm.class' \
	'TypeSpecializer$$VariableTerm.class' \
	'TypeSpecializerAnalysis$$ConstantTerm.class' \
	'TypeSpecializerAnalysis$$VariableTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: jclass $(PTCLASSJAR)

demo: compileDemo runDemo
timeDemo: compileDemo timeOrthoCommDemo

# OrthogonalCom
MODEL =			OrthogonalCom
#SOURCECLASS =		ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom
SOURCECLASS =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#  make SOURCECLASS=../../../ptolemy/actor/lib/test/auto/CommDistDiscard.xml MODEL=CommDistDiscard compileDemo runDemo 
# To run on ArrayAppend:
#  make SOURCECLASS=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml MODEL=ArrayAppend compileDemo runDemo

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.java.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/java/cg/$(MODEL)

TARGETCLASS = $(TARGETPACKAGE).$(MODEL)

# Package that the decompiler creates classes in
DECOMPILERTARGETPACKAGE =	$(TARGETPACKAGE)
# Directory that the decompiler creates classes in
DECOMPILERTARGETPATH =		$(ROOT)/$(TARGETPATH)

JVM_SIZE = -Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT =     600000
# Number of Iterations to run the generated code for.
ITERATIONS =		1000
ITERATIONS_PARAMETER =	,iterations:$(ITERATIONS)

compileDemo: $(JCLASS)
	@echo "generate deep code for $(SOURCECLASS)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	 -Dptolemy.ptII.dir=$(PTII) \
	ptolemy.copernicus.java.Main $(SOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.watchDog time:$(WATCH_DOG_TIMEOUT) \
		-p wjtp.at targetPackage:$(TARGETPACKAGE) \
		-p wjtp.mt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.clt \
		targetPackage:$(TARGETPACKAGE)$(ITERATIONS_PARAMETER) \
		-p wjtp.fot targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffpt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.idt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.iat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ipt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.itt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ttn disabled,debug,targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ts targetPackage:$(TARGETPACKAGE) \
		-p wjtp.noe targetPackage:$(TARGETPACKAGE) \
		-p wjtp.nee targetPackage:$(TARGETPACKAGE) \
		-p wjtp.snapshot1 outDir:$(ROOT)/$(TARGETPATH)/jimple1 \
		-p wjtp.snapshot2 outDir:$(ROOT)/$(TARGETPATH)/jimple2 \
		-p wjtp.snapshot3 outDir:$(ROOT)/$(TARGETPATH)/jimple3 \
		-p wjtp.snapshot4 outDir:$(ROOT)/$(TARGETPATH)/jimple4 \
		-p wjtp.snapshot5 outDir:$(ROOT)/$(TARGETPATH)/jimple5 \
		-p wjtp.snapshot6 outDir:$(ROOT)/$(TARGETPATH)/jimple6 \
		-p wjtp.finalSnapshot outDir:$(ROOT)/$(TARGETPATH) \
		-p wjtp.iee enabled \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(TARGETCLASS)

runDemo:
	@echo "Run the deep model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).Main 


TREESHAKE_PREFIX = $(ROOT)/$(TARGETPATH)/treeshake
TREESHAKE=$(ROOT)/util/testsuite/treeshake
treeShakeDemo:
	@echo "Create the minimal jar file and run it" 
	$(TREESHAKE) $(JAR) $(TREESHAKE_PREFIX).jar \
		$(JAVA)	-Xfuture -classpath "$(CLASSPATH)" \
		$(TARGETPACKAGE).Main 
	ls -l $(TREESHAKE_PREFIX).jar

# Directories that contain snapshots
SNAPSHOTS=jimple1 jimple2 jimple3 jimple4 jimple5 jimple6

STATSFILE=runSnapShot.stats
# $PTII/util/testsuite/codegen.tcl calls runDemoTest
runDemoTest: treeShakeAll

# Treeshake everything and run it
treeShakeAll: treeShakeWithoutCodegen runTreeShakeWithoutCodegen \
		treeShakeAndRunSnapshots \
		treeShakeCodegen runTreeShakeCodegen \
		compareTreeShakeJars

treeShakeWithoutCodegen:
	@echo "Treeshake $(MODEL) without codegen"
	@CLASSPATH="$(CLASSPATH)" \
		$(TREESHAKE) $(JAR) $(TREESHAKE_PREFIX).jar \
			$(JAVA) ptolemy.copernicus.java.TestApplication \
			$(SOURCECLASS) \
			| tail -1
	ls -l $(TREESHAKE_PREFIX).jar
	@rm -f  $(STATSFILE)
runTreeShakeWithoutCodegen:
	@echo "Run without codegen with treeshake.jar"
	@for repeat in 1 2 3 4; \
	do \
		$(JAVA) \
			-classpath $(TREESHAKE_PREFIX).jar \
			ptolemy.copernicus.java.TestApplication \
			$(SOURCECLASS) \
			| tail -1 | \
			tee -a $(STATSFILE); \
	done
	$(ROOT)/util/testsuite/memorystats "$(MODEL) interpreted" < $(STATSFILE)

treeShakeAndRunSnapshots:
	@echo "Treeshake $(MODEL) and then run snapshots with treeshake.jar"
	for snapshot in $(SNAPSHOTS); \
	do \
		echo "Running the code from $(TARGETPATH)/$${snapshot}"; \
		echo "Treeshake $$snapshot"; \
		$(TREESHAKE) $(JAR) $(TREESHAKE_PREFIX)_$${snapshot}.jar \
			$(JAVA) \
				-Xfuture -classpath \
	"$(ROOT)/$(TARGETPATH)/$$snapshot$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
				$(TARGETPACKAGE).Main| tail -1 ; \
		ls -l $(TREESHAKE_PREFIX)_$${snapshot}.jar; \
		rm -f  $(STATSFILE); \
		for repeat in 1 2 3 4; \
		do \
			$(JAVA) \
				-classpath $(TREESHAKE_PREFIX)_$${snapshot}.jar \
				$(TARGETPACKAGE).Main | tail -1 | \
			tee -a $(STATSFILE); \
		done; \
		$(ROOT)/util/testsuite/memorystats "$(MODEL) $$snapshot" < $(STATSFILE); \
	done

treeShakeCodegen:
	@echo "Treeshake the final codegen version"
	@$(TREESHAKE) $(JAR) $(TREESHAKE_PREFIX)_codegen.jar \
		$(JAVA) \
			-classpath "$(CLASSPATH)" \
			$(TARGETPACKAGE).Main | tail -1
	@ls -l $(TREESHAKE_PREFIX)_codegen.jar;

runTreeShakeCodegen:
	@echo "Run the final codegen version"
	@rm -f  $(STATSFILE)
	@for repeat in 1 2 3 4; \
	do \
		$(JAVA) \
			-classpath $(TREESHAKE_PREFIX)_codegen.jar \
			$(TARGETPACKAGE).Main | tail -1 | \
			tee -a $(STATSFILE); \
	done
	@$(ROOT)/util/testsuite/memorystats "$(MODEL) codegen" < $(STATSFILE);
	@rm -f $(STATSFILE)

compareTreeShakeJars:
	@echo "See {TREESHAKE_PREFIX}_jardiff.txt
	@echo "  for the .class files are present in the regular jar,"
	@echo "  but not in the codegen jar"
	@jar -tvf ${TREESHAKE_PREFIX}.jar | \
		awk '{print $$NF}' | sort > /tmp/treeshake_toc
	@jar -tvf ${TREESHAKE_PREFIX}_codegen.jar | \
		awk '{print $$NF}' | sort > /tmp/treeshake_codegen_toc
	@comm -23 /tmp/treeshake_toc /tmp/treeshake_codegen_toc \
		> ${TREESHAKE_PREFIX}_jardiff.txt
	@rm -f /tmp/treeshake_toc /tmp/treeshake_codegen_toc	
	ls -l ${TREESHAKE_PREFIX}*

############################################################################
# Rules to run gcj, the GNU Java -> Native compiler
# This code is very experimental, and not likely to work


GCJ_DIR =	/home/eecs/cxh/src/ptII/vendors/gcj
#GCJ_DIR =	/usr/local
# The GNU C/Java compiler 
GCJ =		$(GCJ_DIR)/bin/gcj
GCJ_LIBDIR =	$(GCJ_DIR)/lib

# GCJ Options, see http://gcc.gnu.org/onlinedocs/
# If there is no -g or -O option, then the default is -g1
#GCJ_FLAGS =	-static
GCJ_FLAGS =	-static -pg -g0 -O3 -fno-bounds-check


# GCJ flags used at link time
#GCJ_LINK_FLAGS = -v -Wl,-R,$(GCJ_LIBDIR)
GCJ_LINK_FLAGS = -v
# -L$(GCJ_LIBDIR) should be first so we get the right libgcj.spec
#GCJ_LINK_LIBS =  -L$(GCJ_LIBDIR)

gcj:
	echo "To compile a demo with gcj:" 
	echo " 1) Run 'make gcjSetup gcjFix'"
	echo " 2) Fix CGoutputBitDisplay.java so that 'make gcjFixCompile' works"
	echo " 3) Run 'make gcj2'"
	echo " 4) Run 'make gcjRun'"

gcjSetup:	demo treeShakeCodegen runTreeShakeCodegen


gcjFix: $(TARGETPATH)/CGoutputBitDisplay.java
$(TARGETPATH)/CGoutputBitDisplay.java:
	@echo "gcj fails with stack underflow on CGoutputBitDisplay,"
	@echo " so we regenerate"
	cd $(ROOT)/$(TARGETPATH); \
		"$(PTII)/bin/jode" \
		ptolemy.copernicus.java.cg.OrthogonalCom.CGoutputBitDisplay | \
		awk '{if ($$0 ~ /^package /) {printit=1}; if (printit == 1) { print $$0}} ' | \
		sed 's@final@/* final */@g' > CGoutputBitDisplay.java
	@echo "Now edit $(ROOT)/$(TARGETPATH)/CGoutputBitDisplay.java"
	@echo "and run make gcjFixCompile"

gcjFixCompile:
	cd $(ROOT)/$(TARGETPATH); \
		$(JAVAC) -classpath "$(PTII)" CGoutputBitDisplay.java

gcj1:
	@echo "---- This will crap out with 'stack underflow' unless"
	@echo "     We regenerate CGoutputBitDisplay.java by running"
	@echo "     'make gcjFix'"
	cd $(ROOT)/$(TARGETPATH); \
		CLASSPATH=$(PTII) \
		$(GCJ) $(GCJ_FLAGS) \
			--main=ptolemy.copernicus.java.cg.$(MODEL).Main \
			-o ortho *.class;
	@echo "Even so, this fails when linking"

gcj2: $(ROOT)/$(TARGETPATH)/gcj gcj2_0 gcj2_1 gcj2_2

# Unjar the treeshake_codegen jar file for use with gcj
# Use $(PTII)/$(TARGETPATH)/treeshake_codegen.jar insetead of
# $(TREESHAKE_PREFIX)_codegen.jar because of relative path name problems
unjar_treeshake_codegen: $(ROOT)/$(TARGETPATH)/gcj
$(ROOT)/$(TARGETPATH)/gcj: $(PTII)/$(TARGETPATH)/treeshake_codegen.jar
	rm -rf $@
	mkdir $@
	cd $@; jar -xf "$(PTII)/$(TARGETPATH)/treeshake_codegen.jar"

# A list of directories that are required to compile the treeshaken directories
# This is necessary because we treeshake by class, so the .class files
# that the treeshaker includes may have methods that are not called
# that contain references to classes that were not included in the
# treeshaker output.  However, gcj does a C style link, where all
# the symbols in a .o file must be resolved.  So we include these
# other classes.
GCJ_AUX_DIRS = ptolemy/data/expr ptolemy/data/unit

gcj2_0:
	@echo "For each directory in the treeshaken output,"
	@echo "Copy all the class files from corresponding directory"
	@echo "in $PTII"
	@echo " Ignore any '*.class not found' messages" 
	cd $(ROOT)/$(TARGETPATH)/gcj; \
		dirs=`find . -type d -print`; \
		for dir in $$dirs; \
		do \
			files=`ls -1 $$dir/*.class | awk 'END {print NR}'`; \
			if [ $$files -gt 1 ]; then \
				echo "---Copying .class files from \$$PTII to $$dir"; \
				cp $(PTII)/$$dir/*.class $$dir; \
			fi; \
		done; \
		for dir in $(GCJ_AUX_DIRS); \
		do \
			if [ ! -d $$dir ]; then \
				mkdir $$dir; \
			fi; \
			echo "---Copying aux .class from \$$PTII to $$dir"; \
			cp $(PTII)/$$dir/*.class $$dir; \
		done;

gcj2_1:
	@echo "--- Run gcj on all the .class files in the jar file dir"
	cd $(ROOT)/$(TARGETPATH)/gcj; \
		dirs=`find . -type d -print`; \
		for dir in $$dirs; \
		do \
			files=`ls -1 $$dir/*.class | awk 'END {print NR}'`; \
			if [ $$files -gt 1 ]; then \
				echo "$$retval---Running gcj in $$dir"; \
				(cd $$dir; CLASSPATH="."; \
					$(GCJ) -g -c \
					$(GCJ_FLAGS) \
					--CLASSPATH=$(PTII)/$(TARGETPATH)/gcj \
					--encoding=UTF-8 *.class); \
			fi; \
		done;

gcj2_2:
	@echo "--- Run gcj on all the .o files in the jar file dir"
	@echo "    Note that here, we end up needing .class files other"
	@echo "    than the ones in the tree shaken jar file"
	cd $(ROOT)/$(TARGETPATH)/gcj; \
		objs=`find . -name "*.o" -print | awk -F / '{for(i=1;i<NF;i++) {printf("%s/", $$i) } printf("*.o\n")}' | sort | uniq`; \
		$(GCJ) -g --main=ptolemy.copernicus.java.cg.$(MODEL).Main \
			$(GCJ_FLAGS) \
			$(GCJ_LINK_FLAGS) \
			-o $(MODEL)_gcj $$objs \
			$(GCJ_LINK_LIBS);
	ls -l $(ROOT)/$(TARGETPATH)/gcj/$(MODEL)_gcj

gcjRun:
	cd $(ROOT)/$(TARGETPATH)/gcj; \
		./$(MODEL)_gcj

gcjRunStats:
	@echo "Run the gcj output and collect stats"
	@rm -f  $(STATSFILE)
	@for repeat in 1 2 3 4; \
	do \
		(cd $(ROOT)/$(TARGETPATH)/gcj; \
			./$(MODEL)_gcj) | tail -1 | \
			tee -a $(STATSFILE); \
	done
	@$(ROOT)/util/testsuite/memorystats "$(MODEL) gcj" < $(STATSFILE);
	@rm -f $(STATSFILE)

GDB=/usr/local/bin/gdb 
gcjGdb:
	echo "handle SIGPWR nostop noprint" > .gdbinit
	echo "handle SIGXCPU nostop noprint" >> .gdbinit
	echo "directory $(PTII)" >> .gdbinit 
	$(GDB) $(ROOT)/$(TARGETPATH)/gcj/$(MODEL)_gcj



AR =		ar
LIBNONSHAREDSUFFIX =	a
RANLIB =	ranlib
LIBDIR =	$(PTII)/$(TARGETPATH)/gcj/lib
gcj3_1:
	@echo "--- Determine what directories are in the jar file"
	@echo "    then create libraries in "
	@echo "    $(PTII)/$(TARGETPATH)/gcj/lib/"
	if [ ! -d $(PTII)/$(TARGETPATH)/gcj/lib ]; then \
		echo "making $(PTII)/$(TARGETPATH)/gcj/lib"; \
		mkdir $(PTII)/$(TARGETPATH)/gcj/lib; \
	fi
	cd $(ROOT)/$(TARGETPATH)/gcj; \
		dirs=`find . -type d -print`; \
		echo "dirs=$$dirs"; \
		for dir in $$dirs; \
		do \
			files=`ls -1 $$dir/*.o | awk 'END {print NR}'`; \
			if [ $$files -gt 1 ]; then \
				echo "---Running ar in $$dir"; \
				ptlib=lib`basename $$dir`.$(LIBNONSHAREDSUFFIX); \
				(cd $$dir; \
					pwd; \
					rm -f $$ptlib; \
					$(AR) cq $$ptlib \
						*.o; \
					$(RANLIB) $$ptlib; \
					rm -f $(LIBDIR)/$$ptlib; \
					ln $$ptlib \
						$(LIBDIR); \
					); \
			fi; \
		done;
		ls -l $(LIBDIR)


gcj3_2:
	@echo "--- Link using the libraries in "
	@echo "    $(PTII)/$(TARGETPATH)/gcj/lib/"
	cd $(ROOT)/$(TARGETPATH)/gcj; \
		$(GCJ) -g --main=ptolemy.copernicus.java.cg.$(MODEL).Main \
			$(GCJ_FLAGS) \
			$(GCJ_LINK_FLAGS) \
			-o $(MODEL)_lib_gcj  \
			$(TARGETPATH)/*.o \
			ptolemy/data/Numerical.o \
			$(GCJ_LINK_LIBS) \
			-L$(LIBDIR) \
			-ldata -lexpr -ltype -lunit -lexpr -ldata -lmath -lgraph -lutil;
	ls -l $(ROOT)/$(TARGETPATH)/gcj/$(MODEL)_lib_gcj

#############################################################################


timeOrthoCommDemo:
	@echo "Time the model for $(ITERATIONS) iterations"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).Main  | tail

runStaticDemo:
	@echo "Run the deep model static"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).StaticMain 
# -iterations 50

JODE=$(ROOT)/bin/jode
# Class to disassemble
DECOMPILERTARGETCLASS =		Main

# Generate a .java file from a class file and recompile it
jodeDecompile: jodeDecompileDemo jodeRecompileDemo jodeRunRecompiledDemo
jodeDecompileDemo:
	@echo "Convert .class file back to .java file, compile it, run it"
	classes=`cd $(DECOMPILERTARGETPATH); ls -1 *.class | sed 's/.class//g'`; \
	for class in $$classes; \
	do \
		echo "--- Decompiling $(DECOMPILERTARGETPACKAGE).$$class"; \
		$(JODE) -d $(DECOMPILERTARGETPATH) $(DECOMPILERTARGETPACKAGE).$$class; \
	done

jodeRecompileDemo:
	$(JAVAC) -classpath \
		"$(DECOMPILERTARGETPATH)$(CLASSPATHSEPARATOR)$(ROOT)" \
		-d $(DECOMPILERTARGETPATH)/$(TARGETPATH) \
		$(DECOMPILERTARGETPATH)/$(TARGETPATH)/$(DECOMPILERTARGETCLASS).java

jodeRunRecompiledDemo:
	$(JAVA) -Xfuture -classpath "$(DECOMPILERTARGETPATH)" \
		$(TARGETPACKAGE).$(DECOMPILERTARGETCLASS)


JAD=jad
jadDecompile:
	$(JAD) -r -o -d $(ROOT)/$(TARGETPATH)/jad $(ROOT)/$(TARGETPATH)/*.class; \
	for file in $(ROOT)/$(TARGETPATH)/jad/$(TARGETPATH)/*.jad; \
	do \
		echo "Fixing $$file"; \
		javafile=`dirname $$file`/`basename $$file .jad`.java;\
		sh ../shallow/test/jad2java $$file \
				> $$javafile; \
		echo "Compiling $$javafile"; \
		$(JAVAC) -classpath "$(ROOT)/$(TARGETPATH)/jad$(CLASSPATHSEPARATOR)$(ROOT)" \
			$$javafile; \
	done; \

# Generate .java files from the snapshots
recompileSnapshots:
	for snapshot in $(SNAPSHOTS); \
	do \
		echo "Recompiling in $(TARGETPATH)/$$snapshot"; \
		$(JAD) -r -o -d $(ROOT)/$(TARGETPATH)/$$snapshot/jad $(ROOT)/$(TARGETPATH)/$$snapshot/$(TARGETPATH)/*.class; \
		for file in $(ROOT)/$(TARGETPATH)/$$snapshot/jad/$(TARGETPATH)/*.jad; \
		do \
			echo "Fixing $$file"; \
			javafile=`dirname $$file`/`basename $$file .jad`.java;\
			sh ../shallow/test/jad2java $$file \
				> $$javafile; \
			echo "Compilinmg $$javafile"; \
			$(JAVAC) -classpath "$(ROOT)/$(TARGETPATH)/$$snapshot/jad$(CLASSPATHSEPARATOR)$(ROOT)" \
				$$javafile; \
		done; \
	done


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

