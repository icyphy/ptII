# Makefile stub used to create fmus for sparse fmi
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id: fmus.mk 71624 2015-02-19 00:50:20Z mwetter@lbl.gov $
#
# @Copyright (c) 2013-2014 The Regents of the University of California.
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


# The fmus in the fmus/ directory should have a makefile that looks like
# FMU_NAME = dqME1
# include ../fmus.mk

# Include definitions and rules common to sparse fmi and regular
# cosimulation.
include $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/fmuBase.mk

src/modelDescription.xml:
	$(MAKE) export

FMU_SRCS = \
	src/modelDescription.xml \
	src/sources/

# This rule differs for sfmi and regular cosimulation.
# Below is the rule for sparse fmi.
$(FMU_NAME).fmu: $(FMU_SRCS)
	(cd src/sources; $(MAKE))

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

KRUFT = $(FMU_NAME)_* $(FMU_NAME).fmu $(FMU_NAME).c modelDescription.xml binaries sources *.so *.dylib *.o src/binaries

