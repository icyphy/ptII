# $Id$
# The name of the FMU to be built.
FMU_NAME = AMS_AMSSim

CFLAGS = -I/usr/local/jmodelica/include/RuntimeLibrary -I/usr/local/jmodelica/ThirdParty/FMI/2.0

#USER_CFLAGS =  -L/usr/local/jmodelica/lib/RuntimeLibrary/ -lfmi2 -ljmi
USER_CFLAGS = -L/usr/local/jmodelica/lib/RuntimeLibrary   -lfmi2 -ljmi -lModelicaExternalC /usr/local/jmodelica/lib/RuntimeLibrary/liblapack.a /usr/local/jmodelica/lib/RuntimeLibrary/libblas.a -L/usr/lib64 -lgfortran -L/usr/local/jmodelica/ThirdParty/Sundials/lib /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_kinsol.a /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_nvecserial.a -L/usr/local/jmodelica/ThirdParty/Minpack/lib /usr/local/jmodelica/ThirdParty/Minpack/lib/libcminpack.a

# See also the userCflags file.

# The rules are in a separate file so that we can easily update them.
include fmu.mk
