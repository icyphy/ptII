# Makefile for the Java classes used to test the xslt actors
#
# @Authors: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 2003-2009 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY

# Location of this directory, relative to the Ptolemy II directory
ME =        ptolemy/actor/gt/test

# Root of the Ptolemy II directory
ROOT =      ../../../..

# XSLTransformer requires saxon
CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \
	AllMatchingTestHelper.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	DefaultModelAttribute.tcl \
	DesignPatternImporter.tcl \
	GeneratedModel1.tcl \
	GeneratedModel2.tcl \
	Match3Actors.tcl \
	MatchHierarchy2Actors.tcl \
	MatchHierarchy2Actors2Composites.tcl \
	Match4Actors.tcl \
	Match4ActorsSubclasses.tcl \
	Match2LevelHierarchyFlattening.tcl \
	Match3ActorsRelationCollapsing.tcl

# Graphical Java tests.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS = \
	DesignPatternImporterPersistenceBugPattern.xml \
	DesignPatternImporterPersistenceBug.xml \
	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

PATTERNS = \
	Match3Actors_succ1.xml \
	Match3Actors_fail2.xml \
	Match3Actors_succ3.xml \
	Match3Actors_succ4.xml \
	Match3Actors_succ5.xml \
	Match3Actors_fail6.xml \
	Match3Actors_succ7.xml \
	Match3Actors_succ8.xml \
	MatchHierarchy2Actors_fail1.xml \
	MatchHierarchy2Actors_succ2.xml \
	MatchHierarchy2Actors_fail3.xml \
	MatchHierarchy2Actors_succ4.xml \
	MatchHierarchy2Actors_succ5.xml \
	MatchHierarchy2Actors_succ6.xml \
	MatchHierarchy2Actors2Composites_fail1.xml \
	MatchHierarchy2Actors2Composites_fail2.xml \
	MatchHierarchy2Actors2Composites_succ3.xml \
	MatchHierarchy2Actors2Composites_succ4.xml \
	Match4Actors_1match1.xml \
	Match4Actors_2matches2.xml \
	Match4Actors_3matches3.xml \
	Match4Actors_4matches4.xml \
	Match4Actors_6matches5.xml \
	Match4Actors_8matches6.xml \
	Match4ActorsSubclasses_succ1.xml \
	Match4ActorsSubclasses_fail2.xml \
	Match4ActorsSubclasses_succ3.xml \
	Match2LevelHierarchyFlattening_succ1.xml \
	Match2LevelHierarchyFlattening_succ2.xml \
	Match3ActorsRelationCollapsing_succ1.xml \
	Match3ActorsRelationCollapsing_fail2.xml \
	Match3Actors_rule.xml \
	MatchHierarchy2Actors_rule.xml \
	MatchHierarchy2Actors2Composites_rule.xml \
	Match4Actors_rule.xml \
	Match4ActorsSubclasses_rule.xml \
	Match2LevelHierarchyFlattening_rule.xml \
	Match3ActorsRelationCollapsing_rule.xml \


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =    alljtests.tcl  junit \
		auto

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'AllMatchingTestHelper$$1.class'

JCLASS =    $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl test_auto
	$(JTCLSH) alljtests.tcl

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
