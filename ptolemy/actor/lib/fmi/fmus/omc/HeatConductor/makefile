
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

#TEMPDIR := $(shell mktemp -u)

export:
        #Export the fmu. This is merely done to get the modelDescription.xml file
#	echo "Making $(TEMPDIR)"
#	mkdir -p $(TEMPDIR)
#	echo "loadModel(HeatConductor); getErrorString();" >> $(TEMPDIR)/exportFMU.mos
#	echo "OpenModelica.Scripting.translateModelFMU(" >> $(TEMPDIR)/exportFMU.mos
#	echo "  className=HeatConductor," >> $(TEMPDIR)/exportFMU.mos
#	echo "  version=\"2.0\");" >> $(TEMPDIR)/exportFMU.mos
#	echo "  getErrorString();" >> $(TEMPDIR)/exportFMU.mos
#	(cd $(TEMPDIR); omc +tearingMethod=noTearing exportFMU.mos)

        # Create folders for fmu
	rm -rf src
	mkdir -p src/sources
#	mv $(TEMPDIR)/modelDescription.xml src/
#	mv $(TEMPDIR)/*makefile src/sources/
	rm -rf $(TEMPDIR)

#	mv $(FMU_NAME).fmu src/
#	(cd src; unzip $(FMU_NAME).fmu)
        # Copy template files
	cp ../../../fmus/inc20/src/sources/build_fmu src/sources/
	cp ../../../fmus/inc20/src/sources/build_fmu.bat src/sources/
	cp ../template/sources/fmu.mk src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/template/fmiv20/fmi2FunctionTypes.h src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/template/fmiv20/fmi2Functions.h src/sources/
	cp $(PTII)/ptolemy/actor/lib/fmi/fmus/template/fmiv20/fmi2TypesPlatform.h src/sources/
	mkdir -p src/documentation

        # Delete the source code that we just generated as we exported the fmu only to get the modelDescription.xml file
#	rm -rf src/sources/$(FMU_NAME)_*.c
#	rm -rf src/sources/$(FMU_NAME)_*.cpp
#	rm -rf src/sources/$(FMU_NAME)_*.h

        # Delete sources that are not needed as the fmu already contains the source files
#	rm -f $(FMU_NAME)_* modelDescription.xml 

        # Copy the .mo source code. This is required as we recompile the fmu for sparse evaluation
	cp *.mo src/sources/
        # Copy files for sparse fmu evaluation
	cp ../template/sources/sfmi_runtime.h src/sources/
	cp ../template/sources/sfmi_runtime.cpp src/sources/

        # Generate the c files
	@echo "*** Generating C files"
	(cd src/sources; omc +s +simCodeTarget=sfmi HeatConductor.mo; mv HeatConductor_FMI.cpp HeatConductor.cpp)
        #FIXME: omc generates a cpp file. The FMI standard wants C code. build_fmu expects a HeatConductor.c.
        #       However, using mv HeatConductor_FMI.cpp HeatConductor.c won't work as the compilation fails on ubuntu 
        #       with HeatConductor.c:8:18: fatal error: cstdio: No such file or directory
        #       Hence, we hack it so that dsbuild_fmu expect HeatConductor.cpp and not HeatConductor.c
	sed -e 's/fmuBaseName\.c/fmuBaseName\.cpp/g' -i src/sources/build_fmu


        # Generate makefile
	echo "# The name of the FMU to be built." > src/sources/makefile
	echo "FMU_NAME = $(FMU_NAME)" >> src/sources/makefile
	echo "" >> src/sources/makefile
        # FIXME: The flags are not correct for Linux, but they don't seem to be needed (at least for this small model)
	echo "CFLAGS = -I/opt/openmodelica/include -llapack -lblas -L/opt/local/lib/gcc49 -lgfortran" >> src/sources/makefile
	echo "" >> src/sources/makefile
	echo "# The rules are in a separate file so that we can easily update them." >> src/sources/makefile
	echo "include fmu.mk" >> src/sources/makefile

        # Generate .so files
	(cd src/sources; make)

        # Create the fmu
	(cd src; zip -r ../$(FMU_NAME).fmu *)

#	mkdir -p src/sources
#	mv modelDescription.xml src/
#	rm $(FMU_NAME).fmu $(FMU_NAME)_FMU.log
#	mv HeatConductor_* src/sources/
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


