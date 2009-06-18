# Makefile for Java Ptolemy II Graph Transformation Patterns
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/actor/gt/patterns

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	patterns
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

PTCLASSALLJAR =

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED = \
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
    Match3ActorsRelationCollapsing_rule.xml

EXTRA_SRCS =    $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demos, run 'make demo'"

install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
