# Build the .fmu files for both co-simulation and model-exchange.
#
# The build_fmu script calls make with the appropriate makefile variables set.

# The architecture: linux32, linux64, darwin32, darwin64
ARCH = darwin64

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

# Note that there is directory named stepCounter, so we add a .PHONY
# target so that 'make helloWorld' always runs the build_fmu script.  See
# https://www.gnu.org/software/make/manual/html_node/Phony-Targets.html
.PHONY : stepCounter
stepCounter:
	./build_fmu stepCounter


# CBITSFLAGS is set to -m32 to build linux32 fmus

ARCH_DIR = ../binaries/$(ARCH)/
%.o: %.c
	echo `pwd`
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


dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu
