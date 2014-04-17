# $Id$
# The name of the FMU to be built.
FMU_NAME = CoupledClutches

CFLAGS = -I/usr/local/jmodelica/include/RuntimeLibrary

USER_CFLAGS =  -L/usr/local/jmodelica/lib/RuntimeLibrary/ -lfmi2 -ljmi

# The rules are in a separate file so that we can easily update them.
include fmu.mk
