# Build the .fmu files for both co-simulation and model-exchange.
# Typically, this file is included in a makefile that defines the FMU_NAME variable
#
# NOTE: Please don't update this file in the individual fmus/*/src/sources directories.
# Instead, update fmus/fmu.mk and run "make update_makefiles"

# The build_fmu script calls make with the appropriate makefile variables set.
#
# The makefile sets some variables and then includes fmu.mk.
#
# Useful variables to set in the makefile are:
# FMU_NAME - always needs to be set
# CFLAGS
# USERLIBS

# Below are the defaults for our environment.

# The architecture: linux32, linux64, darwin32, darwin64
ARCH = darwin64

ARCH_DIR = ../binaries/$(ARCH)/

# This is for co-simulation
INCLUDE = -DFMI_COSIMULATION -I.
# This is for model exhange
#INCLUDE =  -I.

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
	$(MAKE) ARCH=linux32 CBITSFLAGS="-m32 -fvisibility=hidden" PIC=-fPIC SHARED_LIBRARY_SUFFIX=so $(FMU_NAME).so

linux64:
	$(MAKE) ARCH=linux64 CBITSFLAGS=-fvisibility=hidden PIC=-fPIC SHARED_LIBRARY_SUFFIX=so $(FMU_NAME).so

win32:
	$(MAKE) ARCH=win32 PIC= SHARED_LIBRARY_SUFFIX=dll $(FMU_NAME).dll

win64:
	$(MAKE) ARCH=win64 PIC= SHARED_LIBRARY_SUFFIX=dll $(FMU_NAME).dll

#####

# CBITSFLAGS is set to -m32 to build linux32 fmus
%.o: %.c
	echo `pwd`
	$(CC) -g -c $(CBITSFLAGS) $(PIC) -Wall $(INCLUDE) $(CFLAGS) $< -o $@


%.so: %.o
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) $(CBITSFLAGS) -g -Wall -shared -Wl,-soname,$@ $(INCLUDE) -o $(ARCH_DIR)$@ $< $(USERLIBS)

%.dll: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	# Make users should try mingw32.  build_fmu.bat will run cl
	#cl /LD /wd04090 /nologo $(ARCH_DIR)$< 
	# FIXME: mingw32-gcc might not be in the path.
	i686-pc-mingw32-gcc -shared -Wl,--out-implib,$@  $(INCLUDE) -o $(ARCH_DIR)$@ $< $(USERLIBS)

# Include the c file on the link line so that the debug .dylib.dSYM directory is created.
%.dylib: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) -dynamiclib -g $(INCLUDE) $(CFLAGS) -o $(ARCH_DIR)$@ $< $(USERLIBS)

FMUDIR=..

%.fmu: %.$(SHARED_LIBRARY_SUFFIX)
	# Remove files that should not be included in the .fmu file.
	(cd $(FMUDIR); rm -rf *.o */*.o *~ \#*)
	(cd $(FMUDIR); zip -r ../$@ * -x '*/.svn/*' '*/#*#' '*/*~')

dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu
