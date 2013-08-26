# Build the .fmu files for both co-simulation and model-exchange.
#
# The build_fmusim2 script calls make with the appropriate makefile variables set.

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
# target so that 'make helloWorld' always runs the build_fmusim2 script.  See
# https://www.gnu.org/software/make/manual/html_node/Phony-Targets.html

.PHONY : onOffSupervisor

#####
# This is the main entry point of the makefile.  Typically, we execute
# a script that determines platform-specific values.

# build_fmusim2 might not be executable if we extracted the sources from a jar file.
all:
	-chmod a+x build_fmusim2
	./build_fmusim2

fast: all

install jars jclass sources tests:
	@echo "Nothing to do in fmusim2 for 'make $@'."

#####
# The rules below build for a specific architecture.  These rules are
# called by Ptolemy if a shared library is missing.

darwin64:
	$(MAKE) ARCH=darwin64 PIC= SHARED_LIBRARY_SUFFIX=dylib fmusim2
	mv onOffSupervisor.dylib ../binaries/darwin64

linux32:
	$(MAKE) ARCH=linux32 CBITSFLAGS=-m32 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so fmusim2
	mv onOffSupervisor.so ../binaries/linux32

linux64:
	$(MAKE) ARCH=linux64 PIC=-fPIC SHARED_LIBRARY_SUFFIX=so fmusim2
	mv onOffSupervisor.so ../binaries/linux64

win32:
	$(MAKE) ARCH=win32 PIC= SHARED_LIBRARY_SUFFIX=dll fmusim2
	mv onOffSupervisor.dll ../binaries/win32

win64:
	$(MAKE) ARCH=win64 PIC= SHARED_LIBRARY_SUFFIX=dll fmusim2
	mv onOffSupervisor.dll ../binaries/win64

#####

CFLAGS=-DFMI_COSIMULATION -DFMI_VERSION=2

# CBITSFLAGS is set to -m32 to build linux32 fmus
%.o: %.c
	$(CC) -g -c $(CBITSFLAGS) $(PIC) -Wall $(CSORME_INCLUDE) $(CFLAGS) $< -o $@


%.so: %.o
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) $(CBITSFLAGS) -shared -Wl,-soname,$@ -o $(ARCHDIR)$@ $<

%.dylib: %.o
	@if [ ! -d $(ARCH_DIR) ]; then \
		echo "Creating $(ARCH_DIR)"; \
		mkdir -p $(ARCH_DIR); \
	fi
	$(CC) -dynamiclib -o $(ARCH_DIR)$@ $<

FMUDIR=../../src
%.fmu: %.$(SHARED_LIBRARY_SUFFIX)
	# Remove files that should not be included in the .fmu file.
	(cd $(FMUDIR); rm -rf *.o */*.o *~)
	(cd $(FMUDIR); zip -q -r ../$@ *)

fmusim2: main.o stack.o sim_support.o xml_parser.o
	$(CC) -g $(CBITSFLAGS) $(PIC) -Wall $(CSORME_INCLUDE) $(CFLAGS) $^ -o $@ -lexpat -ldl

run: fmusim2
	./fmusim2 ../onOffSupervisor/onOffSupervisor.fmu 

clean: dirclean
dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu fmuTmp* fmusim2 result.csv
checkjunk:
	@echo "checkjunk rule not implemented."
