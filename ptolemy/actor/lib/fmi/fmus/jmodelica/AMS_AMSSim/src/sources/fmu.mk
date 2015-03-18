# Build the .fmu files for both co-simulation and model-exchange.
# Typically, this file is included in a makefile that defines the FMU_NAME variable
#
# NOTE: Please don't update this file in the individual fmus/*/src/sources directories.
# Instead, update fmus/fmu.mk and run "make update_makefiles"

# The build_fmu script calls make with the appropriate makefile variables set.
# Below are the defaults for our environment.

# The architecture: linux32, linux64, darwin32, darwin64
ARCH = darwin64

ARCH_DIR = ../binaries/$(ARCH)/

# This is for co-simulation
#INCLUDE = -DFMI_COSIMULATION -I.
# This is for model exhange
#INCLUDE =  -I.

# This is for co-simulation with JModelica.
#INCLUDE =  -DFMUCS20 -I.

# This is for model exchange with JModelica
#INCLUDE =  -DFMUME20 -I.

# This is for co-simulation and model exchange with JModelica
INCLUDE =  -D FMUCS20 -DFMUME20 -I.

# For co-simulation FMUs, modelExchange.mk does not exist.
# For model exchange FMUs, modelExchange.mk defines INCLUDE.
#include modelExchange.mk

# The suffix for shared libraries.
# dylib for Mac OS X, so for Linux
SHARED_LIBRARY_SUFFIX = dylib
#SHARED_LIBRARY_SUFFIX = so

# Empty for Mac OS X, -fPIC for Linux
PIC =
#PIC = -fPIC

# Build an individual FMU_NAMEs.

# Note that there is directory named $(FMU_NAME), so we add a .PHONY
# target so that 'make helloWorld' always runs the build_fmu script.  See
# https://www.gnu.org/software/make/manual/html_node/Phony-Targets.html

.PHONY : $(FMU_NAME)

#####
# This is the main entry point of the makefile.  Typically, we execute
# a script that determines platform-specific values.

# build_fmu might not be executable if we extracted the sources from a jar file.
$(FMU_NAME):
	-chmod a+x build_fmu
	./build_fmu $(FMU_NAME)


#####
# The rules below build for a specific architecture.  These rules are
# called by Ptolemy if a shared library is missing.

darwin64:
	chmod a+x ./userCflags; $(MAKE) ARCH=darwin64 PIC= SHARED_LIBRARY_SUFFIX=dylib USER_CFLAGS="`./userCflags`" $(FMU_NAME).dylib

linux32:
	$(MAKE) ARCH=linux32 CBITSFLAGS=-m32 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so $(FMU_NAME).so

linux64:
	chmod a+x userCflags; $(MAKE) ARCH=linux64 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so USER_CFLAGS="`./userCflags`" $(FMU_NAME).so

win32:
	$(MAKE) ARCH=win32 PIC= SHARED_LIBRARY_SUFFIX=dll $(FMU_NAME).dll

win64:
	$(MAKE) ARCH=win64 PIC= SHARED_LIBRARY_SUFFIX=dll $(FMU_NAME).dll

#####

# CBITSFLAGS is set to -m32 to build linux32 fmus
%.o: %.c
	echo `pwd`
	$(CC) -g -c $(CBITSFLAGS) $(USER_CFLAGS) $(PIC) -Wall $(INCLUDE) $(CFLAGS) $< -o $@


%.so: %.o
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) $(CBITSFLAGS) $(USER_CFLAGS) $(PIC) -g -Wall -shared -Wl,-soname,$@ $(INCLUDE) -o $(ARCH_DIR)$@ $< $(FMU_NAME)_*.c -L/usr/local/jmodelica/lib/RuntimeLibrary/ -L/usr/local/jmodelica/lib/RuntimeLibrary -lfmi2 -ljmi "-L/usr/local/jmodelica/lib"  -llapack -llapack -lblas -lgfortran -lModelicaExternalC -L/usr/local/jmodelica/ThirdParty/Sundials/lib -l:libsundials_kinsol.a -l:libsundials_nvecserial.a -L/usr/local/jmodelica/ThirdParty/Minpack/lib -l:libcminpack.a -lstdc++ -lm -l:libsundials_cvode.a

%.dll: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	cl /LD /wd04090 /nologo $< 

FMI2CSTEMPLATE=/usr/local/jmodelica/CodeGenTemplates/fmi2_functions_cs_template.c

# Include the c file on the link line so that the debug .dylib.dSYM directory is created.
%.dylib: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) -dynamiclib -Wl,-rpath,@loader_path/ -pthread -g -std=c89 -pedantic $(CFLAGS) $(USER_CFLAGS) $(INCLUDE) -o $(ARCH_DIR)$@ $< $(FMU_NAME)_*.c -L/usr/local/jmodelica/lib/RuntimeLibrary -lfmi2 -ljmi "-L/usr/local/jmodelica/lib"  -llapack -llapack -lblas -lgfortran -lModelicaExternalC -static-libstdc++ -L/usr/local/jmodelica/ThirdParty/Sundials/lib /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_kinsol.a /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_nvecserial.a -L/usr/local/jmodelica/ThirdParty/Minpack/lib /usr/local/jmodelica/ThirdParty/Minpack/lib/libcminpack.a /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_cvode.a

FMUDIR=..

%.fmu: %.$(SHARED_LIBRARY_SUFFIX)
	# Remove files that should not be included in the .fmu file.
	(cd $(FMUDIR); rm -rf *.o */*.o *~ \#*)
	(cd $(FMUDIR); zip -r ../$@ * -x '*/.svn/*' '*/#*#' '*/*~')

dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu
