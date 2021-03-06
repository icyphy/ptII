# Makefile for the ptolemy/vergil/basic/export package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2011-2014 The Regents of the University of California.
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


ME =		ptolemy/vergil/basic/export

# PTITEXTPDF_DIR is set to itextpdf if iText.jar was found by configure.
DIRS =		$(PTITEXTPDF_DIR) html test web

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	export
PTCLASSJAR =	$(PTPACKAGE).jar
# We don't include the all the subjars because ptiny should not include the export facilities.
PTCLASSALLJAR = \
	web/web.jar 

PTAUXALLJAR = exportFull.jar

# Include the .class files from these jars in PTAUXALLJARS
PTAUXALLJARS = \
		html/html.jar \

# Note that itextpdf/itextpdf.jar is optional

JSRCS = \
	ExportModel.java

EXTRA_SRCS =	$(JSRCS) \
	exportModelX11 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'ExportModel$$1.class' \
	'ExportModel$$2$1.class' \
	'ExportModel$$2.class' \
	'ExportModel$$2$$1.class' \
	'ExportModel$$2$$2.class' \
	'ExportModel$$3.class' \
	'ExportModel$$4.class' \
	'ExportModel$$5.class' \
	'ExportModel$$6.class' \
	'ExportModel$$7.class' \
	'ExportModel$$8.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
