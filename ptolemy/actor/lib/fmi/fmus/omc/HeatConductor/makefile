
# Makefile for Java Ptolemy II Functional Mock-up Unit (FMU) HeatConductor CoSimulation
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: : mkfmudir 70377 2014-10-21 22:32:43Z cxh $
#
# @Copyright (c) 2014 The Regents of the University of California.
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

FMU_NAME = HeatConductor

include ../fmus.mk

KRUFT = $(FMU_NAME)_* $(FMU_NAME).fmu


export:
	#export LD_LIBRARY_PATH=/usr/local/gcc-4.8.2/lib64:/usr/local/lib:${LD_LIBRARY_PATH}
	export OPENMODELICALIBRARY=`pwd`:/usr/lib/omlibrary:$MODELICAPATH; omc +tearingMethod=noTearing exportFMU.mos
	mkdir -p src/sources
	mv modelDescription.xml src/
	rm $(FMU_NAME).fmu $(FMU_NAME)_FMU.log
	mv HeatConductor_* src/sources/
foo:
	cp ../template/sources/build_fmu src/sources/
	cp ../../template/sources/build_fmu.bat src/sources/
	cp ../template/sources/fmu.mk src/sources/
	cp ../../template/sources/fmuTemplate_fmiv20.c src/sources/fmuTemplate.c
	cp ../../template/sources/fmuTemplate_fmiv20.h src/sources/fmuTemplate.h
	cp ../../template/fmiv20/{fmi2FunctionTypes.h,fmi2Functions.h,fmi2TypesPlatform.h} src/sources/
	cp ../../template/model.png src/
	mkdir -p src/documentation
	sed 's/@FMU_NAME@/$(FMU_NAME)/g' ../../template/documentation/_main.html > src/documentation/_main.html
	echo "# The name of the FMU to be built." > src/sources/makefile
	echo "FMU_NAME = $(FMU_NAME)" >> src/sources/makefile
	echo "" >> src/sources/makefile
	echo "CFLAGS = -I/opt/openmodelica/include -llapack -lblas -L/opt/local/lib/gcc49 -lgfortran" >> src/sources/makefile
	echo "" >> src/sources/makefile
	echo "# The rules are in a separate file so that we can easily update them." >> src/sources/makefile
	echo "include fmu.mk" >> src/sources/makefile


