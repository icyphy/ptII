# makefile fragment include by ECSL demonstrations
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2004 The Regents of the University of California.
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

# This makefile fragment expects $(MODEL) to be defined

# Convert ECSL to MoML
ECSL2MOML = 	$(ROOT)/ptolemy/actor/lib/ecsl/ecsl2moml

# The $PTII/vendors directory that is not usually shipped with Ptolemy
ECSL_VENDORS_DIR = $(ROOT)/vendors/ecsl_dp

demo: jclass $(MODEL)ECSL.moml
	$(PTII)/bin/vergil $(MODEL)ECSL.moml


# Read in Simulink, generate intermediate.xml
$(MODEL).xml: $(MODEL).mdl
	$(ECSL_VENDORS_DIR)/MDL2XML/bin/MDL2XML $<

# Read in intermediate .xml, generate ECSL
$(MODEL)ECSL.xml: $(MODEL).xml
	$(ECSL_VENDORS_DIR)/XML2ECSL/bin/Xml2Ecsl_d $< $(MODEL)ECSL.xml

# Read in ECSL, generate MoML
$(MODEL)ECSL.moml: $(MODEL)ECSL.xml $(ECSL2MOML)
	$(ECSL2MOML) $<

KRUFT = $(MODEL).xml $(MODEL)ECSL.xml $(MODEL)ECSL.moml


# FIXME: remove graphical elements from model before calling copernicus
# pushd $PTII/ptolemy/copernicus/shallow/
# $PTII/bin/copernicus -codeGenerator shallow $(MODEL)ECSL.moml
# cp -r cg ~/ptII/vendors/ecsl_dp/ptolemy

TARGETPATH = $(ME)/cg
cg/$(MODEL)ECSL:
	$(PTII)/bin/copernicus -codeGenerator shallow \
		-outputDirectory $(PTII)/$(TARGETPATH) \
		-targetPath $(TARGETPATH) \
		$(MODEL)ECSL.moml

run_cg:
	$(JAVA) \
	-classpath "cg/$(MODEL)ECSL$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
        "-Dptolemy.ptII.dir=c:/cxh/ptII" \
        "ptolemy.actor.gui.CompositeActorSimpleApplication" \
        "-class" \
	$(MODEL)ECSL.CG$(MODEL)ECSL

# The place where treeshake scripts are.
TREESHAKE=$(ROOT)/util/testsuite/treeshake

$(MODEL)_treeshake.jar:
	"$(TREESHAKE)" "$(JAR)" $(MODEL)_treeshake.jar \
		-main ptolemy.actor.gui.CompositeActorApplication \
		"$(JAVA)" \
			-classpath "cg/$(MODEL)ECSL$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
			ptolemy.actor.gui.CompositeActorSimpleApplication \
		        "-class" \
			$(MODEL)ECSL.CG$(MODEL)ECSL


$(MODEL)2.jar:
	"$(TREESHAKE)" "$(JAR)" $(MODEL)_treeshake.jar \
		-main ptolemy.actor.gui.MoMLSimpleApplication \
		"$(JAVA)" \
			-classpath "$(PTII)/$(TARGETPATH)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
			ptolemy.actor.gui.MoMLSimpleApplication \
			$(MODEL)ECSL.moml


run_treeshake: $(MODEL)_treeshake.jar
	$(JAVA) -classpath "$(CLASSPATHSEPARATOR)$(MODEL)_treeshake.jar" \
		ptolemy.actor.gui.CompositeActorSimpleApplication \
		-class $(MODEL)ECSL.CG$(MODEL)ECSL


run_treeshake2: $(MODEL)_treeshake.jar
	$(JAVA) \
			-classpath "$(PTII)/$(TARGETPATH)$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
			ptolemy.actor.gui.PtExecuteApplication \
		-class $(MODEL)ECSL.CG$(MODEL)ECSL




# Rules to run gcj, the GNU Java -> Native compiler                            
# This code is very experimental, and not likely to work                       
GCJ_DIR =       /usr/local

# The GNU C/Java compiler                                                      
GCJ =           gcj
GCJ_LIBDIR =    $(GCJ_DIR)/lib

GCJ_JAR = /usr/share/java/libgcj-3.4.1.jar

# GCJ Options, see http://gcc.gnu.org/onlinedocs/                              
# If there is no -g or -O option, then the default is -g1                      
#GCJ_FLAGS =    -static                                                        
#GCJ_FLAGS =    -pg -g0 -O3 -fno-bounds-check --classpath=$(GCJ_JAR)           
GCJ_FLAGS =     -g -O3 -fno-bounds-check  
GCJ_FLAGS =     -g -Dptolemy.ptII.dir=$(PTII) --classpath=$(GCJ_JAR)

gcj:
	"$(GCJ)" $(GCJ_FLAGS) \
		--main=ptolemy.actor.gui.CompositeActorSimpleApplication \
		-o $(MODEL)_gcj $(MODEL)_treeshake.jar

run_gcj:
	(cd cg ; ../$(MODEL)_gcj -class $(MODEL)ECSL.CG$(MODEL)ECSL )

run:
	$(JAVA) \
		 -classpath $(PTII) ptolemy.actor.gui.MoMLSimpleApplication \
		$(MODEL)ECSL.moml
# Ptolemy II dirs
dirs:
	jar -tvf $(MODEL)_treeshake.jar | awk '{ print $$NF}' | awk -F / '{printf("%s", $$1); for(i=2;i<NF;i++){printf("/%s", $$i)} printf("\n")}' | sort | uniq

JDIRS = \
	com/microstar/xml \
	ptolemy/actor \
	ptolemy/actor/gui \
	ptolemy/actor/gui/style \
	ptolemy/actor/lib \
	ptolemy/actor/sched \
	ptolemy/actor/util \
	ptolemy/data \
	ptolemy/data/expr \
	ptolemy/data/type \
	ptolemy/data/unit \
	ptolemy/domains/ct/kernel \
	ptolemy/domains/ct/kernel/solver \
	ptolemy/domains/ct/kernel/util \
	ptolemy/domains/ct/lib \
	ptolemy/graph \
	ptolemy/graph/analysis \
	ptolemy/graph/analysis/analyzer \
	ptolemy/graph/analysis/strategy \
	ptolemy/kernel \
	ptolemy/kernel/attributes \
	ptolemy/kernel/undo \
	ptolemy/kernel/util \
	ptolemy/math \
	ptolemy/moml \
	ptolemy/moml/filter \
	ptolemy/util \
	vendors/ecsl_dp/ptolemy

gcj_dirs:
		set $(JDIRS); \
		for x do \
		    if [ -w $(PTII)/$$x ] ; then \
			( cd $(PTII)/$$x ; \
			echo running gcj in $(PTII)/$$x ; \
			$(GCJ) -c -I$(PTII) $(GCJ_FLAGS) *.java ;\
			) \
		    fi ; \
		done ; \


# Find the names of missing files
missing: missing1 run_missing2
missing1:
	grep "undefined reference" gcj2.out | grep ::class | awk '{print substr($$5, 2, length($$5)-2)}' | sed -e 's@::class\$$@.class@' -e 's@::@/@g' | sort | uniq  | grep -v XMLParser.class | grep -v Document.class




run_missing2:
	./missing2 | grep -v XMLParser.class | grep -v Document.class

#		ptolemy/actor/gui/PtolemyQuery.class
#		ptolemy/gui/Top.class 

update_jar: org/w3c/dom/Document.class 	ptolemy/data/expr/XMLParser.class
	cp c.jar $(MODEL)_treeshake.jar
	$(JAR) -uf $(MODEL)_treeshake.jar \
		org/w3c/dom/Document.class \
		ptolemy/data/expr/XMLParser.class
	(cd $(PTII); $(JAR) -uf vendors/ecsl_dp/ptolemy/$(MODEL)_treeshake.jar `cat u3`)

org/w3c/dom/Document.class: org/w3c/dom/Document.java
	(cd org/w3c/dom; $(JAVAC) -classpath $(PTII) Document.java)

ptolemy/data/expr/XMLParser.class: ptolemy/data/expr/XMLParser.java
	(cd ptolemy/data/expr/XMLParser; $(JAVAC) -classpath $(PTII) XMLParser.java)

