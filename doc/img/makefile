# Makefile for the Ptolemy II doc/img directory
#
# Author: Christopher Hylands (makefile only)
# Version: $Id$
#
# Copyright (c) 2001-2007 The Regents of the University of California.
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

ME =		doc/img

# Sub directories to run make in.
DIRS =

# Root of Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	img
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
	copernicus2.gif \
	copernicusa.gif \
	codegen10.gif \
	codegen11.gif \
	codegen12.gif \
	codegen13.gif \
	codegen14.gif \
	codegen15.gif \
	codegen16.gif \
	codegen17.gif \
	codegen18.gif \
	codegen19.gif \
	codegen2.gif \
	codegen20.gif \
	codegen21.gif \
	codegen22.gif \
	codegen23.gif \
	codegen24.gif \
	codegen25.gif \
	codegen26.gif \
	codegen27.gif \
	codegen28.gif \
	codegen29.gif \
	codegen3.gif \
	codegen30.gif \
	codegen31.gif \
	codegen32.gif \
	codegen33.gif \
	codegen34.gif \
	codegen35.gif \
	codegen36.gif \
	codegen37.gif \
	codegen38.gif \
	codegen39.gif \
	codegen4.gif \
	codegen40.gif \
	codegen41.gif \
	codegen42.gif \
	codegen43.gif \
	codegen44.gif \
	codegen45.gif \
	codegen46.gif \
	codegen47.gif \
	codegen48.gif \
	codegen49.gif \
	codegen5.gif \
	codegen50.gif \
	codegen51.gif \
	codegen52.gif \
	codegen53.gif \
	codegen54.gif \
	codegen6.gif \
	codegen7.gif \
	codegen8.gif \
	codegen9.gif \
	codegena.gif \
	expressions10.gif \
	expressions11.gif \
	expressions12.gif \
	expressions13.gif \
	expressions14.gif \
	expressions15.gif \
	expressions16.gif \
	expressions17.gif \
	expressions18.gif \
	expressions19.gif \
	expressions2.gif \
	expressions20.gif \
	expressions21.gif \
	expressions22.gif \
	expressions23.gif \
	expressions24.gif \
	expressions25.gif \
	expressions26.gif \
	expressions27.gif \
	expressions28.gif \
	expressions29.gif \
	expressions3.gif \
	expressions30.gif \
	expressions31.gif \
	expressions32.gif \
	expressions33.gif \
	expressions34.gif \
	expressions35.gif \
	expressions36.gif \
	expressions37.gif \
	expressions38.gif \
	expressions39.gif \
	expressions4.gif \
	expressions40.gif \
	expressions41.gif \
	expressions42.gif \
	expressions43.gif \
	expressions44.gif \
	expressions45.gif \
	expressions46.gif \
	expressions47.gif \
	expressions48.gif \
	expressions49.gif \
	expressions5.gif \
	expressions50.gif \
	expressions51.gif \
	expressions52.gif \
	expressions53.gif \
	expressions54.gif \
	expressions55.gif \
	expressions56.gif \
	expressions57.gif \
	expressions58.gif \
	expressions59.gif \
	expressions6.gif \
	expressions60.gif \
	expressions61.gif \
	expressions62.gif \
	expressions63.gif \
	expressions64.gif \
	expressions65.gif \
	expressions66.gif \
	expressions67.gif \
	expressions68.gif \
	expressions69.gif \
	expressions7.gif \
	expressions70.gif \
	expressions71.gif \
	expressions72.gif \
	expressions8.gif \
	expressions9.gif \
	expressionsa.gif \
	navnext.gif \
	navprev.gif \
	navtoc.gif \
	next.gif \
	Helen256x256.png  \
	Helen.png  \
	PtolemyII256x256.png \
	ptIIplanet.gif \
	ptIIplanetIcon.gif \
	PtolemyII.jpg \
	PtolemyIISmall.gif

ICONS = \
	histogram.ico \
	hyvisual.ico \
	pdf.ico \
	ptiny.ico \
	ptplot.ico \
	vergil.ico \
	viptos.ico \
	visualsense.ico


EXTRA_SRCS = \
	$(ICONS) \
	$(OTHER_FILES_TO_BE_JARED) \
	HTML.gif \
	HyVisualSplash.gif \
	PDF.gif \
	Postscript.gif \
	PtolemyIIIcon.gif \
	PtolemyIISmallIcon.gif \
        PtolemyIISplash.gif \
	PtolemyIISplash.psd \
	banner.gif \
	banner.psd \
	next.gif \
	next_g.gif \
	prev.gif \
	prev_g.gif \
	ptIIbanner2.gif \
	ptIIbanner2.psd \
	ptIIbanner3.gif \
	ptIIexample.gif \
	ptIIplanet.ico \
	ptolemy32bw.bin \
	ptolemy32bw.bmp \
	ptolemyII480x240plain.gif \
	spacer4.gif \
	up.gif

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	PtolemyIICD.ec3

include $(ROOT)/mk/ptno-compile.mk
