# common include file for directories that don't get compiled and don't
# need dependencies generated
#
# Version Identification:
# $Id$
# Copyright (c) 1990-2000 The Regents of the University of California.
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


# This makefile is to be included if we don't need the compiler
# and we don't need to generate any dependencies.  Please don't use
# GNU make extensions in this file, such as 'ifdef'.
#
# The primary difference between a makefile that uses compile.mk and one
# that uses no-compile.mk is that in a directory that uses
# no-compile.mk, all the 'work' is done by make sources, and the make
# all command usually does nothing.
# 
# Another difference is that no-compile.mk should probably never appear
# in a make.template, since if there is a make.template, then we are
# calculating dependencies on the fly and creating a makefile, which
# probably means that we are compiling.

# Provide an initial value for LIB_DEBUG so we don't get messages about
# multiply defined rules for $(LIB)/$(LIB_DEBUG) if LIB_DEBUG is empty.
LIBR_DEBUG =	libdummy_g

all install TAGS: $(EXTRA_SRCS) $(HDRS) $(MISC_FILES)
	@echo "Nothing to be done in this directory"

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
