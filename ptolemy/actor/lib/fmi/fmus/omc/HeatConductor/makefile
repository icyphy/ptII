
# Makefile for Java Ptolemy II Functional Mock-up Unit (FMU) HeatConductor CoSimulation
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: : mkfmudir 70377 2014-10-21 22:32:43Z cxh $
#
# @Copyright (c) 2014-2015 The Regents of the University of California.
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

#TEMPDIR := $(shell mktemp -u)

export:
	rm -rf src
	mkdir -p src/sources
	rm -rf $(TEMPDIR)

        # Copy template files.
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/inc20/src/sources/build_fmu src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/template/sources/fmu.mk src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/template/fmiv20/fmi2FunctionTypes.h src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/template/fmiv20/fmi2Functions.h src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/template/fmiv20/fmi2TypesPlatform.h src/sources/
	mkdir -p src/documentation

        # Copy the .mo source code. This is required as we recompile the fmu for sparse evaluation.
	cp *.mo src/sources/
        # Copy files for sparse fmu evaluation.
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/template/sources/sfmi_runtime.h src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/template/sources/sfmi_runtime.cpp src/sources/

        # Generate the c files
	@echo "### Generating C files"
	(cd src/sources; omc +s +simCodeTarget=sfmi HeatConductor.mo; mv HeatConductor_FMI.cpp HeatConductor.cpp)
	mv src/sources/modelDescription.xml src/
        #FIXME: omc generates a cpp file. The FMI standard wants C code. build_fmu expects a HeatConductor.c.
        #       However, using mv HeatConductor_FMI.cpp HeatConductor.c won't work as the compilation fails on ubuntu 
        #       with HeatConductor.c:8:18: fatal error: cstdio: No such file or directory
        #       Hence, we hack it so that dsbuild_fmu expect HeatConductor.cpp and not HeatConductor.c
        # Can't use sed -i here under Mac OS X.
	sed -e 's/fmuBaseName\.c/fmuBaseName\.cpp/g' < $(PTII)/ptolemy/actor/lib/fmi/fmus/inc20/src/sources/build_fmu > src/sources/build_fmu

        # Generate makefile
	echo "# The name of the FMU to be built." > src/sources/makefile
	echo "FMU_NAME = $(FMU_NAME)" >> src/sources/makefile
	echo "" >> src/sources/makefile
        # FIXME: The flags are not correct for Linux, but they don't seem to be needed (at least for this small model)
	echo "CFLAGS = -I$(OPENMODELICAHOME)/include/omc/c -L/opt/local/lib/gcc49 -L$(OPENMODELICAHOME)/lib/omc -Wl,-rpath,'$(OPENMODELICAHOME)/lib/omc' -lSimulationRuntimeC -L/usr/lib64 -llapack -lblas -lm -lgc -lpthread -rdynamic -L. -llis -lSimulationRuntimeC"  >> src/sources/makefile
	echo "" >> src/sources/makefile
	echo "# The rules are in a separate file so that we can easily update them." >> src/sources/makefile
	echo "include fmu.mk" >> src/sources/makefile

        # Generate .so files
	(cd src/sources; make)

        # Create the fmu
	(cd src; zip -r ../$(FMU_NAME).fmu *)

OTHER_OTHER_FILES_TO_BE_JARED = \
	$(FMU_NAME).mo \
	$(FMU_NAME)Simple.mo \
	$(FMU_NAME).mos \
	$(FMU_NAME).xml

# Place the include after the first rule so that it is the default.

include $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/fmuSFMI.mk

KRUFT = $(FMU_NAME)_* $(FMU_NAME).fmu $(FMU_NAME).c modelDescription.xml binaries sources *.so *.dylib *.o src
