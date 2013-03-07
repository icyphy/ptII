# Makefile for exporting Vergil models as javascript
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2011-2012 The Regents of the University of California.
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


ME =		ptolemy/vergil/basic/export/html/javascript

DIRS =		

# Root of the Java directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	javascript
PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = 

# Include the .class files from these jars in PTCLASSALLJAR
#PTCLASSALLJARS =

JSRCS =

OTHER_FILES_TO_BE_JARED = \
	blank.gif \
	dygraph-combined.js \
	dygraph-license.htm \
	fancy_close.png \
	fancy_loading.png \
	fancy_nav_left.png \
	fancy_nav_right.png \
	fancy_shadow_e.png \
	fancy_shadow_n.png \
	fancy_shadow_ne.png \
	fancy_shadow_nw.png \
	fancy_shadow_s.png \
	fancy_shadow_se.png \
	fancy_shadow_sw.png \
	fancy_shadow_w.png \
	fancy_title_left.png \
	fancy_title_main.png \
	fancy_title_over.png \
	fancy_title_right.png \
	fancybox-license.htm \
	fancybox-x.png \
	fancybox-y.png \
	fancybox.png \
	javascript-license.htm \
	jquery-1.4.3.min.js \
	jquery.easing-1.3.pack.js \
	jquery.fancybox-1.3.4.css \
	jquery.fancybox-1.3.4.js \
	jquery.fancybox-1.3.4.pack.js \
	jquery.mousewheel-3.0.4.pack.js \
	pt-1.0.0.js

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
