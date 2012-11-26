# Build the .fmu files for both co-simulation and model-exchange.
#
# The build_fmu script calls make with the appropriate makefile variables set.

# The architecture: linux32, linux64, darwin32, darwin64
ARCH = darwin64

# Either cs for co-simuluation or me for model exchange.
CSORME = cs
CSORME_INCLUDE = -DFMI_COSIMULATION -I../../co_simulation/include
#CSORME = me
#CSORME_INCLUDE = -I../../model_exchange/include

# The suffix for shared libraries.
# dylib for Mac OS X, so for Linux
SHARED_LIBRARY_SUFFIX = dylib
#SHARED_LIBRARY_SUFFIX = so

# Empty for Mac OS X, -fPIC for Linux
PIC =
#PIC = -fPIC

# Build the .fmu files for both co-simulation and model-exchange
all:
	./build_fmu me bouncingBall
	./build_fmu me dq
	./build_fmu me helloWorld
	./build_fmu me inc
	./build_fmu me values
	./build_fmu me vanDerPol
	./build_fmu cs bouncingBall
	./build_fmu cs dq
	./build_fmu cs helloWorld
	./build_fmu cs inc
	./build_fmu cs values
	./build_fmu cs vanDerPol

# CBITSFLAGS is set to -m32 to build linux32 fmus

%.o: %.c
	echo `pwd`
	$(CC) -g -c $(CBITSFLAGS) $(PIC) -Wall $(CSORME_INCLUDE) $(CFLAGS) $< -o $@

%.so: %.o
	$(CC) $(CBITSFLAGS) -shared -Wl,-soname,$@ -o $@ $<

%.dylib: %.o
	$(CC) -dynamiclib -o $@ $<

%.fmu: %.$(SHARED_LIBRARY_SUFFIX)
	rm -rf fmu
	mkdir -p fmu/binaries/$(ARCH) fmu/sources fmu/documentation
	cp $< fmu/binaries/$(ARCH)
	-cp *.c *.h fmu/sources
	-cp *.html *.png fmu/documentation
	cat modelDescription.xml ../$(CSORME).xml > fmu/modelDescription.xml
	cp model.png fmu
	@if [ ! -d ../../../fmu/$(CSORME) ]; then \
		echo "mkdir -p ../../../fmu/$(CSORME)"; \
		mkdir -p ../../../fmu/$(CSORME); \
	fi
	(cd fmu; zip -q -r ../../../../fmu/$(CSORME)/$@ *)

clean:
	(cd bouncingBall; make dirclean)
	(cd dq; make dirclean)
	(cd inc; make dirclean)
	(cd values; make dirclean)
	(cd vanDerPol; make dirclean)

dirclean:
	rm -rf *.so *.dylib *.o *.fmu *~ fmu
