# Makefile for ontologies
#
# @Authors: Christopher Brooks (makefile only)
#
# $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

ME =		ptolemy/data/ontologies

DIRS = 		adapters lattices util demo #test

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ontologies
# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	adapters/adapters.jar \
	lattices/lattices.jar \
	util/util.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AnnotationAttribute.java \
	Concept.java \
	ConceptAttribute.java \
	ConceptLattice.java \
	ConceptRelation.java \
	ConstraintManager.java \
	Ontology.java \
	OntologySolver.java \
	ParseTreeASTNodeHelperCollector.java \
	ParseTreeAnnotationEvaluator.java \
	ParseTreeConstraintAnnotationEvaluator.java \
	PropertyConstraintASTNodeHelper.java \
	PropertyConstraintHelper.java \
	PropertyHelper.java \
	PropertyMoMLHandler.java \
	PropertyResolutionException.java \
	PropertySolver.java \
	PropertySolverBase.java \
	PropertyTermFactory.java \
	PropertyTermManager.java \
	SharedUtilities.java

OTHER_FILES_TO_BE_JARED = \
	ontologyLibrary.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# if $(DIRS) includes demo, then do not set MISC_FILES= $(DIRS) 
MISC_FILES = adapters lattices util #test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	'OntologySolver$$ConstraintType.class' \
	'PropertyConstraintHelper$$Inequality.class' \
	'PropertyHelper$$Channel.class' \
	'PropertyHelper$$Inequality.class' \
	'PropertySolver$$DirectoryNameFilter.class' \
	'PropertyTermManager$$InequalityTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
