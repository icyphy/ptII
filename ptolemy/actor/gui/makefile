# Makefile for Ptolemy GUI components for actors and configurations.
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
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

# This directory contains polymorphic actors.

ME =		ptolemy/actor/gui

# PTJACL_DIR is set to ptjacl by configure in $PTII/mk/ptII.mk if
# ptjacl.jar was found and the Ptolemy Tcl implementation is available

# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.

# $PTII/configure looks for lib/bsh*.jar and sets PTBSH_RUN_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTBSH_RUN_DIR is empty.

DIRS = 		doc exec jnlp properties $(PTBSH_RUN_DIR) style test $(PTJACL_DIR) $(PTJYTHON_DIR)

# Root of the Java directory
ROOT =		../../..

# PlotTableauFrame.java requires diva.jar
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	gui
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
# We do not include ptjacl/ptjacl.jar, it gets included separately 
# because it requires lib/ptjacl.jar
PTCLASSALLJARS = \
	doc/doc.jar \
	properties/properties.jar \
	style/style.jar

#	run/run.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
# Except: NOTE: HTMLEffigy has to come before Configuration
# because its inner class, HTMLEffigy$Factory, is referenced.
JSRCS = \
	ActorGraphicalMessageHandler.java \
	AbstractPlaceableActor.java \
	AbstractPlaceableJavaSE.class \
	ArrayOfRecordsPane.java \
	AWTContainer.class \
	AWTContainer.java \
	AbstractPlaceableJavaSE.java \
	BrowserEffigy.java \
	BrowserLauncher.java \
	BrowserTableau.java \
	CheckModelSize.java \
	ColorAttribute.java \
	CommunicationAspectMonitor.java \
	CompositeActorApplication.java \
	CompositeActorSimpleApplication.java \
	Console.java \
	ConfigurationApplication.java \
	CustomQueryBoxParameter.java \
	Configuration.java \
	ConfigurationEffigy.java \
	Configurer.java \
	DebugListenerTableau.java \
	DialogTableau.java \
	Editable.java \
	EditorFactory.java \
	EditorPaneFactory.java \
	EditParametersDialog.java \
	Effigy.java \
	EffigyFactory.java \
	ExecutionAspectPlotterEditorFactory.java \
	ExpressionShellEffigy.java \
	ExpressionShellFrame.java \
	ExpressionShellTableau.java \
	ExternalTextEffigy.java \
	ExternalTextTableau.java \
	FileOrURLEditorFactory.java \
	GenerateCopyrights.java \
	HTMLAbout.java \
	HTMLEffigy.java \
	HTMLEffigyFactory.java \
	HTMLViewer.java \
	HTMLViewerTableau.java \
	InsideEntityEditorPaneFactory.java \
	JNLPUtilities.java \
	JVMTableau.java \
	LevelSkippingTableauFactory.java \
	LiveLink.java \
	LocationAttribute.java \
	MatrixPane.java \
	MatrixTokenTableau.java \
	ModelDirectory.java \
	ModelFrame.java \
	ModelPane.java \
	ModelRepaintController.java \
	MoMLApplet.java \
	MoMLApplication.java \
	MoMLSimpleApplication.java \
	MoMLSimpleStatisticalApplication.java \
	OpenInstanceDialog.java \
	PasswordAttribute.java \
	Placeable.java \
	PlotEffigy.java \
	PlotTableau.java \
	PlotTableauFrame.java \
	PortConfigurer.java \
	PortConfigurerDialog.java \
	PtExecuteApplication.java \
	PtolemyApplet.java \
	PtolemyApplication.java \
	PtolemyDialog.java \
	PtolemyEffigy.java \
	PtolemyQuery.java \
	PtolemyTableauFactory.java \
	PtolemyFrame.java \
	PtolemyPreferences.java \
	QueryUtilities.java \
	RenameConfigurer.java \
	RenameDialog.java \
	RunTableau.java \
	SizeAttribute.java \
	TableauFrame.java \
	TextEditor.java \
	TextEditorTableau.java \
	TextEffigy.java \
	Tableau.java \
	TableauFactory.java \
	TopPack.java \
	TokenEffigy.java \
	TokenTableau.java \
	TokenEffigy.java \
	UserActorLibrary.java \
	WelcomeWindow.java \
	WelcomeWindowTableau.java \
	WindowPropertiesAttribute.java

# Files to be included in the jar file
OTHER_FILES_TO_BE_JARED = \
	BrowserLauncher-license.htm \
	PtolemyIISmallIcon.gif

# Note that PtolemyIISmallIcon.gif is also in doc/img.
# We place a duplicate copy here to make it easy to ship
# jar files that contain all the appropriate images.
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'AbstractPlaceableActor$$WindowClosingAdapter.class' \
	'AbstractPlaceableJavaSE$$WindowClosingAdapter.class' \
	'ArrayOfRecordsPane$$1.class' \
	'ArrayOfRecordsPane$$ArrayAsTable.class' \
	'ArrayOfRecordsPane$$EmptyTableModel.class' \
	'ArrayOfRecordsPane$$SimpleTable.class' \
	'BrowserEffigy$$Factory.class' \
	'BrowserTableau$$Factory.class' \
	'CompositeActorApplication$$1.class' \
	'CompositeActorApplication$$StateReporter.class' \
	'Configuration$$1.class' \
	'Configuration$$2.class' \
	'ConfigurationApplication$$1.class' \
	'ConfigurationApplication$$ConfigurationFilenameFilter.class' \
	'ConfigurationApplication$$IgnoreErrorHandler.class' \
	'Configurer$$1.class' \
	'Configurer$$2$$1.class' \
	'Configurer$$2.class' \
	'Configurer$$3.class' \
	'CommunicationAspectMonitor$$CommunicationAspectMonitorEditorFactory.class' \
	'Console$$ConsoleFrame.class' \
	'Console$$ConsoleFrame$$MyByteArrayOutputStream.class' \
	'Console$$Factory.class' \
	'DebugListenerTableau$$1.class' \
	'DebugListenerTableau$$DebugListenerFrame.class' \
	'EditParametersDialog$$1.class' \
	'EditParametersDialog$$2.class' \
	'EditParametersDialog$$3.class' \
	'EditParametersDialog$$4.class' \
	'EditParametersDialog$$ChangeExecutedRunnable.class' \
	'EditParametersDialog$$ChangeFailedRunnable.class' \
	'ExpressionShellEffigy$$ShellFactory.class' \
	'ExpressionShellTableau$$1.class' \
	'ExpressionShellTableau$$Factory.class' \
	'HTMLEffigy$$Factory.class' \
	'HTMLViewer$$1.class' \
	'HTMLViewerTableau$$Factory.class' \
	'JTextAreaExec$$2$1.class' \
	'JTextAreaExec$$4$1.class' \
	'JTextAreaExec$$6$1.class' \
	'JVMTableau$$1.class' \
	'JVMTableau$$Factory.class' \
	'JVMTableau$$JVMFrame$$1.class' \
	'JVMTableau$$JVMFrame.class' \
	'MatrixPane$$1.class' \
	'MatrixPane$$2.class' \
	'MatrixPane$$EmptyTableModel.class' \
	'MatrixPane$$MatrixAsTable.class' \
	'MatrixTokenTableau$$Factory.class' \
	'ModelFrame$$FileMenuListener.class' \
	'ModelFrame$$HelpMenuListener.class' \
	'ModelPane$$1.class' \
	'ModelPane$$2.class' \
	'ModelPane$$3.class' \
	'ModelPane$$4.class' \
	'ModelPane$$ClickListener.class' \
	'ModelPane$$CommandListener.class' \
	'ModelRepaintController$$1.class' \
	'ModelRepaintController$$2.class' \
	'MoMLApplication$$1.class' \
	'MoMLApplication$$ConfigurationFilenameFilter.class' \
	'MoMLApplication$$IgnoreErrorHandler.class' \
	'OpenInstanceDialog$$1.class' \
	'OpenInstanceDialog$$ColumnNames.class' \
	'OpenInstanceDialog$$ParametersTableModel.class' \
	'OpenInstanceDialog$$StringCellRenderer.class' \
	'PlotEffigy$$Factory.class' \
	'PlotTableau$$Factory.class' \
	'PlotTableauFrame$$EPSFileFilter.class' \
	'PlotTableauFrame$$ExportImageAction.class' \
	'PlotTableauFrame$$FormatAction.class' \
	'PlotTableauFrame$$SpecialMenuListener.class' \
	'PortConfigurerDialog$$10.class' \
	'PortConfigurerDialog$$11.class' \
	'PortConfigurerDialog$$12.class' \
	'PortConfigurerDialog$$13.class' \
	'PortConfigurerDialog$$1.class' \
	'PortConfigurerDialog$$2.class' \
	'PortConfigurerDialog$$3.class' \
	'PortConfigurerDialog$$4.class' \
	'PortConfigurerDialog$$5.class' \
	'PortConfigurerDialog$$6.class' \
	'PortConfigurerDialog$$7.class' \
	'PortConfigurerDialog$$8.class' \
	'PortConfigurerDialog$$9.class' \
	'PortConfigurerDialog$$CellValidator.class' \
	'PortConfigurerDialog$$ColumnNames.class' \
	'PortConfigurerDialog$$PortBooleanCellRenderer.class' \
	'PortConfigurerDialog$$PortTableModel.class' \
	'PortConfigurerDialog$$StringCellEditor.class' \
	'PortConfigurerDialog$$StringCellRenderer.class' \
	'PortConfigurerDialog$$ValidatingComboBoxCellEditor$$1.class' \
	'PortConfigurerDialog$$ValidatingComboBoxCellEditor.class' \
	'PortConfigurerDialog$$ValidatingJTextFieldCellEditor$$1.class' \
	'PortConfigurerDialog$$ValidatingJTextFieldCellEditor$$2.class' \
	'PortConfigurerDialog$$ValidatingJTextFieldCellEditor$$3.class' \
	'PortConfigurerDialog$$ValidatingJTextFieldCellEditor$3.class' \
	'PortConfigurerDialog$$ValidatingJTextFieldCellEditor.class' \
	'PtolemyApplet$$1.class' \
	'PtolemyApplet$$ExportButtonListener.class' \
	'PtolemyApplet$$GoButtonListener.class' \
	'PtolemyApplet$$StopButtonListener.class' \
	'PtolemyApplication$$1.class' \
	'PtolemyDialog$$1.class' \
	'PtolemyDialog$$2.class' \
	'PtolemyEffigy$$Factory.class' \
	'PtolemyEffigy$$FactoryWithoutNew.class' \
	'PtolemyFrame$$PrintChangeRequest.class' \
	'PtolemyFrame$$XMLOrMoMLFileFilter.class' \
	'PtolemyQuery$$1.class' \
	'PtolemyQuery$$2.class' \
	'PtolemyQuery$$3.class' \
	'PtolemyQuery$$4.class' \
	'PtolemyQuery$$5.class' \
	'PtolemyQuery$$6.class' \
	'PtolemyQuery$$7.class' \
	'PtolemyQuery$$HierarchicalConfigurer.class' \
	'PtolemyTop$$ViewMenuListener.class' \
	'RunTableau$$Factory.class' \
	'RunTableau$$RunFrame.class' \
	'RunTableau$$RunFrame$$DebugMenuListener.class' \
	'RunTableau$$TopFactory.class' \
	'ShellTextArea$$3$1.class' \
	'Tableau$$1.class' \
	'Tableau$$WindowClosedAdapter.class' \
	'TableauFactory$$1.class' \
	'TableauFrame$$MenuItemListener.class' \
	'TableauFrame$$ExtensionFileFilter.class' \
	'TableauFrame$$ViewMenuListener.class' \
	'TextEditorTableau$$Factory.class' \
	'TextEditor$$ExportImageAction.class' \
	'TextEffigy$$Factory.class' \
	'TokenEffigy$$Factory.class' \
	'TokenTableau$$Factory.class' \
	'UserActorLibrary$$1.class' \
	'WelcomeWindow$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

TableauFrame.class: HTMLEffigy.class

install: jclass jars

demo: all demo_ptolemy demo_vergil demo_caa
demo_ptolemy: all
	$(ROOT)/bin/ptolemy $(ROOT)/ptolemy/moml/demo/modulation.xml
demo_vergil: all
	$(ROOT)/bin/vergil $(ROOT)/ptolemy/moml/demo/modulation.xml

# Demonstrate CompositeActorApplication
demo_caa: all
	CLASSPATH="$(ROOT)$(CLASSPATHSEPARATOR)" \
	 "$(JAVA)" ptolemy.actor.gui.CompositeActorApplication \
	 -class ptolemy.domains.sdf.demo.Butterfly.Butterfly

# Demonstrate MoMLSimpleApplication, which does not necessarily
# require graphics, though this demo does use the
demo_momlsimple:
	CLASSPATH="$(ROOT)$(CLASSPATHSEPARATOR)" \
	 "$(JAVA)" ptolemy.actor.gui.MoMLSimpleApplication \
		$(ROOT)/ptolemy/actor/gui/test/test.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
