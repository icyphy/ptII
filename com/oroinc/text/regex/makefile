# Makefile oroinc regular expression parser
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
##########################################################################

DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

# $(ME) is used in ptdir.mk
ME =		com/oroinc/text/regex

# True source directory
VPATH =		$(ROOT)/$(ME)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


JCLASS = \
	1.class \
	2.class \
	3.class \
	4.class \
	MalformedPatternException.class \
	MatchResult.class \
	Pattern.class \
	PatternCompiler.class \
	PatternMatcher.class \
	PatternMatcherInput.class \
	Perl5Compiler.class \
	Perl5Debug.class \
	Perl5Matcher.class \
	Perl5Pattern.class \
	Perl5StreamInput.class \
	Util.class

# Include rules for directories that don't have subdirectories
include $(ROOT)/mk/ptno-compile.mk

# Don't include *.class in CRUD, since these files are checked in
# and we don't have any sources, so make clean should not remove them
CRUD= *~
