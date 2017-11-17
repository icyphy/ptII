# Makefile for the Ptolemy II doc/img directory
#
# Author: Christopher Hylands (makefile only)
# Version: $Id$
#
# Copyright (c) 2001-2016 The Regents of the University of California.
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
	eclipseAccessorsCheckOutAs.png \
	eclipseAccessorsNewProject.png \
	eclipseBuildPathPreference.png \
	eclipseCheckOutAs.png \
	eclipseNewJavaProject.png \
	eclipseNewJavaProject2.png \
	eclipseNewJavaProjectSVN.png \
	eclipseNewProject.png \
	eclipsePreferencesGeneralWorkspace.png \
	eclipsePreferencesGeneralWorkspaceWindows.png \
	eclipseWorkspaceLauncherWindows.png \
	eclipseWorkspaceLauncher.png \
	navnext.gif \
	navprev.gif \
	navtoc.gif \
	next.gif \
	Helen256x256.png  \
	Helen.png  \
	MacJNLPUnsignedApp.png \
	pt_cover_online.png \
	PtolemyII256x256.png \
	ptIIplanet.gif \
	ptIIplanetIcon.gif \
	PtolemyII.jpg \
	PtolemyIISmall.gif \
	question.png \
	vol1Figure3-1.png \
	vol1Figure3-2.png \
	vol1Figure3-3.png \
	vol1Figure3-4.png \
	vol1Figure3-5.png \
	vol1Figure3-6.png \
	vol1Figure3-7.png \
	vol1Figure3-8.png \
	vol1Figure3-9.png \
	vol1Figure3-10.png \
	vol1Figure3-11.png


ICONS = \
	capecode.ico \
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
	ptolemyMacIcon.tif \
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
