# $Id$
# The name of the FMU to be built.
FMU_NAME = AMS_AMSSim

GFORTRAN_LIBRARY_DIRECTORY=/usr/local/matlab/recent/sys/os/glnxa64
#CFLAGS = -I/usr/local/jmodelica/include/RuntimeLibrary -I/usr/local/jmodelica/ThirdParty/FMI/2.0

#USER_CFLAGS =  -L/usr/local/jmodelica/lib/RuntimeLibrary/ -lfmi2 -ljmi
#USER_CFLAGS = -L/usr/local/jmodelica/lib/RuntimeLibrary   -lfmi2 -ljmi -lModelicaExternalC /usr/local/jmodelica/lib/RuntimeLibrary/liblapack.a /usr/local/jmodelica/lib/RuntimeLibrary/libblas.a -L$(GFORTRAN_LIBRARY_DIRECTORY) -lgfortran -L/usr/local/jmodelica/ThirdParty/Sundials/lib /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_kinsol.a /usr/local/jmodelica/ThirdParty/Sundials/lib/libsundials_nvecserial.a -L/usr/local/jmodelica/ThirdParty/Minpack/lib /usr/local/jmodelica/ThirdParty/Minpack/lib/libcminpack.a

# See also the userCflags file.

# The rules are in a separate file so that we can easily update them.
include fmu.mk
