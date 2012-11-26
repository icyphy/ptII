# fmusim makefile for Mac OS X

EXECS = \
	fmusim_cs \
	fmusim_me

# Build simulators for co_simulation and model_exchange and then build the .fmu files.
all: $(EXECS)
	(cd models; $(MAKE))

clean:
	rm -f $(EXECS)
	rm -rf  *.dSYM
	rm -f cosimulation/fmusim_cs/*.o
	rm -f model_exchange/fmusim_me/*.o
	(cd models; $(MAKE) clean)

# Sources shared between co-simulation and model exchange
SHARED_SRCS = \
	shared/sim_support.c \
	shared/stack.c \
	shared/xml_parser.c

# Dependencies for only fmusim_cs
CO_SIMULATION_DEPS = \
	co_simulation/fmusim_cs/main.c \
	co_simulation/fmusim_cs/fmi_cs.h \
	co_simulation/include/fmiFunctions.h \
	co_simulation/include/fmiPlatformTypes.h 

# Dependencies for only fmusim_me
MODEL_EXCHANGE_DEPS = \
	model_exchange/fmusim_me/main.c \
	model_exchange/fmusim_me/fmi_me.h \
	model_exchange/include/fmiModelFunctions.h \
	model_exchange/include/fmiModelTypes.h

# Dependencies shared between both fmusim_cs and fmusim_me
SHARED_DEPS = \
	shared/expat.h \
	shared/expat_external.h \
	shared/sim_support.c \
	shared/sim_support.h \
	shared/stack.c \
	shared/stack.h \
	shared/xml_parser.c \
	shared/xml_parser.h

# Set CFLAGS to -m32 to build for linux32
#CFLAGS=-m32
# See also models/build_fmu

# Create the binaries in the current directory because co_simulation already has
# a directory named "fmusim_cs"
fmusim_cs: $(CO_SIMULATION_DEPS) $(SHARED_DEPS)
	$(CC) $(CFLAGS) -g -Wall -DFMI_COSIMULATION -Ico_simulation/fmusim_cs -Ico_simulation/include \
		-Ishared \
		co_simulation/fmusim_cs/main.c $(SHARED_SRCS) \
		-o $@ -lexpat -ldl
	cp fmusim_cs ../bin

fmusim_me: $(MODEL_EXCHANGE_DEPS) $(SHARED_DEPS)
	$(CC) $(CFLAGS) -g -Wall -Imodel_exchange/fmusim_me -Imodel_exchange/include -Ishared \
		model_exchange/fmusim_me/main.c $(SHARED_SRCS) \
		-o $@ -lexpat -ldl
	cp fmusim_me ../bin
