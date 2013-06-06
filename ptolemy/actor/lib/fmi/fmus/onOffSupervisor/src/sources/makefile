# Build the .fmu files for both co-simulation and model-exchange.
#
# The build_fmu script calls make with the appropriate makefile variables set.

# The architecture: linux32, linux64, darwin32, darwin64
ARCH = darwin64

ARCH_DIR = ../binaries/$(ARCH)/

# Either cs for co-simuluation or me for model exchange.
INCLUDE = -DFMI_COSIMULATION -I.

# The suffix for shared libraries.
# dylib for Mac OS X, so for Linux
SHARED_LIBRARY_SUFFIX = dylib
#SHARED_LIBRARY_SUFFIX = so

# Empty for Mac OS X, -fPIC for Linux
PIC =
#PIC = -fPIC

# Build an individual FMUs.

# Note that there is directory named onOffSupervisor, so we add a .PHONY
# target so that 'make helloWorld' always runs the build_fmu script.  See
# https://www.gnu.org/software/make/manual/html_node/Phony-Targets.html

.PHONY : onOffSupervisor

#####
# This is the main entry point of the makefile.  Typically, we execute
# a script that determines platform-specific values.

# build_fmu might not be executable if we extracted the sources from a jar file.
onOffSupervisor:
	-chmod a+x build_fmu
	./build_fmu onOffSupervisor


#####
# The rules below build for a specific architecture.  These rules are
# called by Ptolemy if a shared library is missing.

darwin64:
	$(MAKE) ARCH=darwin64 PIC= SHARED_LIBRARY_SUFFIX=dylib onOffSupervisor.dylib
	mv onOffSupervisor.dylib ../binaries/darwin64

linux32:
	$(MAKE) ARCH=linux32 CBITSFLAGS=-m32 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so onOffSupervisor.so
	mv onOffSupervisor.so ../binaries/linux32

linux64:
	$(MAKE) ARCH=linux64 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so onOffSupervisor.so
	mv onOffSupervisor.so ../binaries/linux64

win32:
	$(MAKE) ARCH=win32 PIC= SHARED_LIBRARY_SUFFIX=dll onOffSupervisor.dll
	mv onOffSupervisor.dll ../binaries/win32

win64:
	$(MAKE) ARCH=win64 PIC= SHARED_LIBRARY_SUFFIX=dll onOffSupervisor.dll
	mv onOffSupervisor.dll ../binaries/win64

#####

# CBITSFLAGS is set to -m32 to build linux32 fmus
%.o: %.c
	echo `pwd`
	$(CC) -g -c $(CBITSFLAGS) $(PIC) -Wall $(CSORME_INCLUDE) $(CFLAGS) $< -o $@


%.so: %.o
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) $(CBITSFLAGS) -shared -Wl,-soname,$@ -o $(ARCH_DIR)$@ $<

%.dll: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	cl /LD /wd04090 /nologo $< 

# Include the c file on the link line so that the debug .dylib.dSYM directory is created.
%.dylib: %.c
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) -dynamiclib -g -o $(ARCH_DIR)$@ $<

FMUDIR=../../src
%.fmu: %.$(SHARED_LIBRARY_SUFFIX)
	# Remove files that should not be included in the .fmu file.
	(cd $(FMUDIR); rm -rf *.o */*.o *~)
	(cd $(FMUDIR); zip -q -r ../$@ *)

dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu
