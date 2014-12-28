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
INCLUDE =  -I.

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
	$(MAKE) ARCH=darwin64 PIC= SHARED_LIBRARY_SUFFIX=dylib $(FMU_NAME).dylib

linux32:
	$(MAKE) ARCH=linux32 CBITSFLAGS=-m32 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so $(FMU_NAME).so

linux64:
	$(MAKE) ARCH=linux64 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so $(FMU_NAME).so

win32:
	$(MAKE) ARCH=win32 PIC= SHARED_LIBRARY_SUFFIX=dll $(FMU_NAME).dll

win64:
	$(MAKE) ARCH=win64 PIC= SHARED_LIBRARY_SUFFIX=dll $(FMU_NAME).dll

#####

# CBITSFLAGS is set to -m32 to build linux32 fmus
%.o: %.c
	echo `pwd`
	$(CC) -g -c $(CBITSFLAGS) $(USER_CFLAGS) $(PIC) -Wall $(INCLUDE) $(CFLAGS) $< -o $@

%.o: %.cpp
	echo `pwd`
	$(CXX) -g -c $(CBITSFLAGS) $(USER_CFLAGS) $(PIC) -Wall $(INCLUDE) $(CFLAGS) $< -o $@

%.so: %.o
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CXX) $(CBITSFLAGS) $(USER_CFLAGS) $(PIC) -g -Wall -shared -Wl,-soname,$@ $(INCLUDE) -o $(ARCH_DIR)$@ $< sfmi_runtime.cpp $(CFLAGS)

%.dll: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	cl /LD /wd04090 /nologo $< sfmi_runtime.cpp

# Include the c file on the link line so that the debug .dylib.dSYM directory is created.
%.dylib: %.cpp
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CXX) -dynamiclib -g $(CFLAGS) $(USER_CFLAGS) $(INCLUDE) -o $(ARCH_DIR)$@ $< sfmi_runtime.cpp

FMUDIR=..

$(FMU_NAME).cpp: $(FMU_NAME).mo
	omc +s +simCodeTarget=sfmi $(FMU_NAME).mo
	mv $(FMU_NAME)_FMI.cpp $(FMU_NAME).cpp

%.fmu: %.$(SHARED_LIBRARY_SUFFIX)
	# Remove files that should not be included in the .fmu file.
	(cd $(FMUDIR); rm -rf *.o */*.o *~ \#*)
	(cd $(FMUDIR); zip -r ../$@ * -x '*/.svn/*' '*/#*#' '*/*~')

dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu
