# Makefile for the ptolemy/actor/gt package
#
# @Authors: Thomas Huining Feng, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2007-2008 The Regents of the University of California.
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
#
#

ME =		ptolemy/actor/gt

DIRS = 		data ingredients test util demo

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	gt
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	data/data.jar \
	demo/demo.jar \
	ingredients/ingredients.jar \
	util/util.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
    AtomicActorMatcher.java \
    CompositeActorMatcher.java \
    Constraint.java \
    DefaultDirectoryAttribute.java \
    DefaultModelAttribute.java \
    FSMMatcher.java \
    GTEntity.java \
    GTEntityUtils.java \
    GTIngredient.java \
    GTIngredientElement.java \
    GTIngredientList.java \
    GTIngredientsAttribute.java \
    GTParameter.java \
    GTTools.java \
    GraphAnalyzer.java \
    GraphMatcher.java \
    GraphTransformationException.java \
    GraphTransformer.java \
    HierarchyFlatteningAttribute.java \
    LocationType.java \
    MalformedStringException.java \
    MatchCallback.java \
    ModalModelMatcher.java \
    ModelGenerator.java \
    ModelView.java \
    NamedObjVariable.java \
    ParameterAttribute.java \
    Pattern.java \
    PatternObjectAttribute.java \
    PortMatcher.java \
    RelationCollapsingAttribute.java \
    RelationHidingAttribute.java \
    Replacement.java \
    ReplacementObjectAttribute.java \
    StateMatcher.java \
    SubgraphMatchingException.java \
    TransformationAttribute.java \
    TransformationException.java \
    TransformationRule.java \
    TransitionMatcher.java \
    ValidationException.java

OTHER_FILES_TO_BE_JARED = \
	ModelGeneratorIcon.xml \
	ModelViewIcon.xml \
	StateMatcherIcon.xml \
	TransformationRuleIcon.xml

EXTRA_SRCS =    $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	data ingredients test util

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
    'GTEntityUtils$$1.class' \
    'GTEntityUtils$$LoadActorIconChangeRequest.class' \
    'GTEntityUtils$$RestoreAppearanceChangeRequest.class' \
    'GTIngredient$$FieldIterator.class' \
    'GTIngredient$$NamedObjMatchResult.class' \
    'GTParameter$$Evaluator.class' \
    'GTParameter$$Scope.class' \
    'GTParameter$$TypeInference.class' \
    'GraphAnalyzer$$IndexedList.class' \
    'GraphAnalyzer$$IndexedLists.class' \
    'GraphAnalyzer$$Path.class' \
    'GraphMatcher$$1.class' \
    'GraphMatcher$$2.class' \
    'GraphMatcher$$LookbackEntry.class' \
    'GraphMatcher$$LookbackList.class' \
    'GraphMatcher$$NameComparator.class' \
    'GraphMatcher$$ObjectList.class' \
    'TransformationRule$$1.class' \
    'TransformationRule$$LastResultsOperation.class' \
    'TransformationRule$$Mode$$1.class' \
    'TransformationRule$$Mode$$2.class' \
    'TransformationRule$$Mode$$3.class' \
    'TransformationRule$$Mode$$4.class' \
    'TransformationRule$$Mode.class' \
    'TransformationRule$$TransformationDirector.class' \
	demo

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
