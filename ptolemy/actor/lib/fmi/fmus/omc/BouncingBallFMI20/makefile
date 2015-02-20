# Makefile for FMI 2.0 BouncingBall from Modelica
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

# Copied from openmodelica/testsuite/openmodelica/fmi/ModelExchange/2.0/BouncingBall.mos
#
# See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/OpenModelicaFMI#UsingOpenModelicaTestFMUs

FMU_NAME = BouncingBallFMI20

# We can't use $(wildcard *.c) here because the .c files are not
# always present when make is invoked.
# To update, use:
# ls -1 *.c | sort | grep -v _me_ | grep -v _info.c | grep -v _init.c
CFILES = \
	BouncingBallFMI20_01exo.c \
	BouncingBallFMI20_02nls.c \
	BouncingBallFMI20_03lsy.c \
	BouncingBallFMI20_04set.c \
	BouncingBallFMI20_05evt.c \
	BouncingBallFMI20_06inz.c \
	BouncingBallFMI20_07dly.c \
	BouncingBallFMI20_08bnd.c \
	BouncingBallFMI20_09alg.c \
	BouncingBallFMI20_10asr.c \
	BouncingBallFMI20_11mix.c \
	BouncingBallFMI20_12jac.c \
	BouncingBallFMI20_13opt.c \
	BouncingBallFMI20_14lnz.c \
	BouncingBallFMI20.c \
	BouncingBallFMI20_FMU.c \
	BouncingBallFMI20_functions.c \
	BouncingBallFMI20_records.c

include $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/fmuCS.mk

