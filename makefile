# Makefile for Java classes that Tycho uses
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# @Copyright (c) 1996- The Regents of the University of California.
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

# Order matters here.
# Go into util first so we get the latest version of the testsuite
# Don't go down into collections, it does not have a makefile
DIRS = util ptolemy doc gui #collections

# Root of Ptolemy II directory
ROOT =		.

ME =
# True source directory
VPATH =		$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/tydir.mk

# Glimpse is a tool that prepares an index of a directory tree.
# glimpse is not included with Tycho, see http://glimpse.cs.arizona.edu
GLIMPSEINDEX =	/usr/sww/bin/glimpseindex
glimpse: .glimpse_exclude
	@echo "Saving .glimpse_exclude, removing the .glimpse* files"
	rm -f glimpse_exclude
	cp .glimpse_exclude glimpse_exclude
	rm -f .glimpse*
	cp  glimpse_exclude .glimpse_exclude
	$(GLIMPSEINDEX) -H `pwd` `pwd`
	chmod a+r .glimpse_*
	rm -f glimpse_exclude
