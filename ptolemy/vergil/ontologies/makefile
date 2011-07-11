# Makefile for the vergil/ontologies package
#
# @Authors: Edward A. Lee, based on a file by Christopher Brooks.
#
# @Version: $Id$
#
# @Copyright (c) 2009-2011 The Regents of the University of California.
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


ME =		ptolemy/vergil/ontologies

DIRS =

# Root of the Java directory
ROOT =		../../../

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ontologies
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AttributeInOntologyController.java \
	ConceptController.java \
	ConceptIcon.java \
	ConceptRelationController.java \
	MultipleConceptIcon.java \
	OntologyEntityController.java \
	OntologyGraphController.java \
	OntologyGraphFrame.java \
	OntologyGraphModel.java \
	OntologyGraphTableau.java \
	OntologySolverGraphController.java \
	OntologySolverGraphFrame.java \
	OntologySolverGraphModel.java \
	OntologySolverTableau.java \
	ReportOntologyLatticeStatus.java

OTHER_FILES_TO_BE_JARED = 

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'ConceptRelationController$$1.class' \
	'ConceptController$$ToggleAcceptabilityAction.class' \
	'ConceptRelationController$$RelationDropper.class' \
	'ConceptRelationController$$RelationRenderer.class' \
	'ConceptRelationController$$RelationTarget.class' \
	'OntologyEntityController$$OpenInstanceAction.class' \
	'OntologyGraphController$$1.class' \
	'OntologyGraphController$$NewConceptAction.class' \
	'OntologyGraphController$$RelationCreator.class' \
	'OntologyGraphFrame$$DebugMenuListener.class' \
	'OntologyGraphFrame$$LayoutAction.class' \
	'OntologyGraphModel$$ConceptModel.class' \
	'OntologyGraphModel$$ConceptModel$$1.class' \
	'OntologyGraphModel$$RelationModel.class' \
	'OntologyGraphModel$$RelationModel$$1.class' \
	'OntologyGraphModel$$RelationModel$$2.class' \
	'OntologyGraphModel$$RelationModel$$3.class' \
	'OntologyGraphModel$$RelationModel$$4.class' \
	'OntologyGraphTableau$$Factory.class' \
	'OntologySolverGraphFrame$$DebugMenuListener.class' \
	'OntologySolverGraphFrame$$ImportLibraryAction.class' \
	'OntologySolverGraphFrame$$InsertOntologyAction.class' \
	'OntologySolverGraphFrame$$InstantiateAttributeAction.class' \
	'OntologySolverGraphFrame$$InstantiateEntityAction.class' \
	'OntologySolverGraphFrame$$LayoutAction.class' \
	'OntologySolverGraphFrame$$SaveInLibraryAction.class' \
	'OntologySolverGraphModel$$OntologyModel$$1.class' \
	'OntologySolverGraphModel$$OntologyModel.class' \
	'OntologySolverTableau$$Factory.class' \
	'ReportOntologyLatticeStatus$$1.class'


JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
