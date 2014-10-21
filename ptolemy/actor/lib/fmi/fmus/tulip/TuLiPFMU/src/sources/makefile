# : mkfmudir 70349 2014-10-12 22:45:57Z cxh $
# The name of the FMU to be built.
FMU_NAME = TuLiPFMU

ECOSINCLUDEDIR=/usr/local/include/ecos
ECOSLIBDIR=/usr/local/lib
CFLAGS = -I$(ECOSINCLUDEDIR) -I$(ECOSINCLUDEDIR)/adm/include -I$(ECOSINCLUDEDIR)/external/SuiteSparse_config -L$(ECOSLIBDIR) -lecos

# The rules are in a separate file so that we can easily update them.
include fmu.mk
