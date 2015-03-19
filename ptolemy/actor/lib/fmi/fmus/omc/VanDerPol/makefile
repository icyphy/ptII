# Makefile for Java Ptolemy II Functional Mock-up Unit (FMU) HeatConductorComplex CoSimulation
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2015 The Regents of the University of California.
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

FMU_NAME = VanDerPol

# We can't use $(wildcard *.c) here because the .c files are not
# always present when make is invoked.
# To update, use:
# ls -1 *.c | sort | grep -v _me_ | grep -v _info.c | grep -v _init.c
CFILES = \
	VanDerPol.c \
	VanDerPol_01exo.c \
	VanDerPol_02nls.c \
	VanDerPol_03lsy.c \
	VanDerPol_04set.c \
	VanDerPol_05evt.c \
	VanDerPol_06inz.c \
	VanDerPol_07dly.c \
	VanDerPol_08bnd.c \
	VanDerPol_09alg.c \
	VanDerPol_10asr.c \
	VanDerPol_11mix.c \
	VanDerPol_12jac.c \
	VanDerPol_13opt.c \
	VanDerPol_14lnz.c \
	VanDerPol_FMU.c \
	VanDerPol_functions.c \
	VanDerPol_records.c

VanDerPol_ref.csv: VanDerPol.fmu
	$(HOME)/src/fmi/fmusdk2/fmu20/bin/fmusim_me VanDerPol.fmu 5.0 0.1
	mv result.csv $@

include $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/fmuCS.mk
