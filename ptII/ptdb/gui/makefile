# Makefile for ptdb - A model database for Ptolemy, gui directory
#
# @Authors: Christopher Brooks (makefile only), based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

ME = 		ptdb/gui

# $PTII/configure runs and looks for PTPOWERMOCK
# $(PTPOWERMOCK_DIR) to test
DIRS =		img $(PTPOWERMOCK_DIR)

# Root of the Ptolemy II directory
ROOT =		../..

# SaveModelToDBFrame imports kernel.bl.save.SaveModelManager, so
# we need the DBXML_JARS
# PTDBXML_JARS is set by configure
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(PTDBXML_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	gui
PTCLASSJAR =

# All the .class files necessary to run a Ptolemy II domain applet
PTCLASSALLJARS = \
	img/img.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	ActorGraphDBFrame.java \
	ActorGraphDBTableau.java \
	AdvancedSimpleSearchFrame.java \
	ArrayModelList.java \
	AttributeListEditFrame.java \
	AttributesListPanel.java \
	AttributesSearchListPanel.java \
	CancelObservable.java \
	ConfigureAttributesFrame.java \
	DatabaseSetupFrame.java \
	DBActorController.java \
	DBReferenceActorInteractionAddon.java \
	GenericAttributePanel.java \
	GraphPatternSearchEditor.java \
	ModelAttributePanel.java \
	ModelMigrationFrame.java \
	ModelPanel.java \
	ModelsListFrame.java \
	ParentHierarchyPanel.java \
	ParentPanel.java \
	PTDBBasicFrame.java \
	PTDBContainedFramesManager.java \
	RenameModelFrame.java \
	SaveModelToDBFrame.java \
	SearchResultPanel.java \
	SearchResultsFrame.java \
	SimpleSearchFrame.java

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	test img

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	'ActorGraphDBFrame$$1.class' \
	'ActorGraphDBFrame$$AllModelsListAction.class' \
	'ActorGraphDBFrame$$ConfigureAttributesAction.class' \
	'ActorGraphDBFrame$$DatabaseSetupAction.class' \
	'ActorGraphDBFrame$$DBHistoryMenuListener.class' \
	'ActorGraphDBFrame$$OpenModelMigrationFrameAction.class' \
	'ActorGraphDBFrame$$RenameModelAction.class' \
	'ActorGraphDBFrame$$SaveModelToDBAction.class' \
	'ActorGraphDBFrame$$SimpleSearchAction.class' \
	'ActorGraphDBTableau$$Factory.class' \
	'AdvancedSimpleSearchFrame$$1.class' \
	'AdvancedSimpleSearchFrame$$2.class' \
	'AttributeListEditFrame$$1.class' \
	'AttributeListEditFrame$$2.class' \
	'AttributeListEditFrame$$3.class' \
	'AttributeListEditFrame$$4.class' \
	'AttributeListEditFrame$$5.class' \
	'AttributeListEditFrame$$6.class' \
	'AttributesListPanel$$1.class' \
	'AttributesListPanel$$2.class' \
	'AttributesListPanel$$3.class' \
	'AttributesListPanel$$4.class' \
	'AttributesListPanel$$5.class' \
	'AttributesSearchListPanel$$1.class' \
	'AttributesSearchListPanel$$2.class' \
	'ConfigureAttributesFrame$$1.class' \
	'ConfigureAttributesFrame$$2.class' \
	'ConfigureAttributesFrame$$3.class' \
	'ConfigureAttributesFrame$$4.class' \
	'ConfigureAttributesFrame$$5.class' \
	'ConfigureAttributesFrame$$6.class' \
	'ConfigureAttributesFrame$$7.class' \
	'ConfigureAttributesFrame$$8.class' \
	'ConfigureAttributesFrame$$9.class' \
	'DBActorController$$OpenActorFromDB.class' \
	'DatabaseSetupFrame$$1.class' \
	'DatabaseSetupFrame$$2.class' \
	'DatabaseSetupFrame$$3.class' \
	'DatabaseSetupFrame$$4.class' \
	'DatabaseSetupFrame$$5.class' \
	'GenericAttributePanel$$1.class' \
	'GenericAttributePanel$$2.class' \
	'GenericAttributePanel$$3.class' \
	'GraphPatternSearchEditor$$DBMatchAction.class' \
	'GraphPatternSearchEditor$$SaveAction.class' \
	'ModelAttributePanel$$1.class' \
	'ModelAttributePanel$$2.class' \
	'ModelAttributePanel$$3.class' \
	'ModelAttributePanel$$4.class' \
	'ModelMigrationFrame$$1.class' \
	'ModelMigrationFrame$$2.class' \
	'ModelMigrationFrame$$3.class' \
	'ModelMigrationFrame$$4.class' \
	'ModelPanel$$1.class' \
	'ModelsListFrame$$1.class' \
	'ModelsListFrame$$2.class' \
	'ModelsListFrame$$3.class' \
	'ModelsListFrame$$4.class' \
	'ModelsListFrame$$5.class' \
	'ParentPanel$$1.class' \
	'RenameModelFrame$$1.class' \
	'RenameModelFrame$$2.class' \
	'RenameModelFrame$$3.class' \
	'SaveModelToDBFrame$$1.class' \
	'SaveModelToDBFrame$$2.class' \
	'SaveModelToDBFrame$$3.class' \
	'SaveModelToDBFrame$$4.class' \
	'SaveModelToDBFrame$$ParentModelItemPanel$$1.class' \
	'SaveModelToDBFrame$$ParentModelItemPanel$$2.class' \
	'SaveModelToDBFrame$$ParentModelItemPanel.class' \
	'SaveModelToDBFrame$$ParentValidateFrame$$1.class' \
	'SaveModelToDBFrame$$ParentValidateFrame$$2.class' \
	'SaveModelToDBFrame$$ParentValidateFrame$$3.class' \
	'SaveModelToDBFrame$$ParentValidateFrame.class' \
	'SearchResultsFrame$$1.class' \
	'SearchResultsFrame$$2.class' \
	'SearchResultsFrame$$3.class' \
	'SimpleSearchFrame$$1.class' \
	'SimpleSearchFrame$$2.class' \
	'SimpleSearchFrame$$3.class' \
	'SimpleSearchFrame$$4.class' \
	'SimpleSearchFrame$$5.class' \
	'SimpleSearchFrame$$ExitSearchCriteriaAction.class' \
	'SimpleSearchFrame$$NewSearchCriteriaAction.class' \
	'SimpleSearchFrame$$OpenPatternSearchFrameAction.class' \
	'SimpleSearchFrame$$OpenSearchCriteriaAction.class' \
	'SimpleSearchFrame$$SaveAsSearchCriteriaAction.class' \
	'SimpleSearchFrame$$SaveSearchCriteriaAction.class' \
	'SimpleSearchFrame$$SearchCriteriaFileFilter.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
