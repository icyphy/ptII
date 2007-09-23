# Makefile for the ptolemy/actor/gt package
#
# @Authors: Thomas Huining Feng, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

ME =		ptolemy/actor/gt

DIRS = 		data rules test

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
	rules/rules.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AtomicActorMatcher.java \
	CompositeActorMatcher.java \
	GraphMatcher.java \
	GraphTransformationException.java \
	MalformedStringException.java \
	MatchCallback.java \
	NoMatchFoundException.java \
	Rule.java \
	RuleAttribute.java \
	RuleEditor.java \
	RuleList.java \
	RuleListAttribute.java \
	RuleValidationException.java \
	SingleRuleTransformer.java \
	SubgraphMatchingException.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'GraphMatcher$$1.class' \
	'GraphMatcher$$2.class' \
	'GraphMatcher$$IndexedList.class' \
	'GraphMatcher$$IndexedLists.class' \
	'GraphMatcher$$LookbackEntry.class' \
	'GraphMatcher$$LookbackList.class' \
	'GraphMatcher$$NameComparator.class' \
	'GraphMatcher$$ObjectList.class' \
	'GraphMatcher$$Path.class' \
	'GraphMatcher$$PortRulePair.class' \
	'Rule$$FieldIterator.class' \
	'RuleEditor$$1.class' \
	'RuleEditor$$2.class' \
	'RuleEditor$$3.class' \
	'RuleEditor$$4.class' \
	'RuleEditor$$AttributesEditor.class' \
	'RuleEditor$$ComboElement.class' \
	'RuleEditor$$Factory.class' \
	'RuleEditor$$JPanelCellEditor.class' \
	'RuleEditor$$Row$$1.class' \
	'RuleEditor$$Row$$CheckBoxActionListener.class' \
	'RuleEditor$$Row$$ClassSelectorEditor.class' \
	'RuleEditor$$Row.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
