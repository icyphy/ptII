#******************************************************************************
#
# Makefile - Rules for building the driver library.
#
# Copyright (c) 2005-2007 Luminary Micro, Inc.  All rights reserved.
# 
# Software License Agreement
# 
# Luminary Micro, Inc. (LMI) is supplying this software for use solely and
# exclusively on LMI's microcontroller products.
# 
# The software is owned by LMI and/or its suppliers, and is protected under
# applicable copyright laws.  All rights are reserved.  You may not combine
# this software with "viral" open-source software in order to form a larger
# program.  Any use in violation of the foregoing restrictions may subject
# the user to criminal sanctions under applicable laws, as well as to civil
# liability for the breach of the terms and conditions of this license.
# 
# THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
# OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
# LMI SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
# CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
# 
# This is part of revision 1900 of the Stellaris Peripheral Driver Library.
#
#******************************************************************************

#
# Include the common make definitions.
#
include ../makedefs

#
# Augment the CFLAGS based on the compiler in use.
#
ifeq (${COMPILER}, ewarm)
CFLAGS += --library_module
endif
ifeq (${COMPILER}, gcc)
AFLAGS += -ffunction-sections -fdata-sections
CFLAGS += -ffunction-sections -fdata-sections
endif
ifeq (${COMPILER}, rvmdk)
CFLAGS += --split-sections
endif
ifeq (${COMPILER}, sourcerygxx)
AFLAGS += -ffunction-sections -fdata-sections
CFLAGS += -ffunction-sections -fdata-sections
endif

#
# The list of objects to be included in the library.
#
OBJS=${COMPILER}/adc.o       \
     ${COMPILER}/can.o       \
     ${COMPILER}/comp.o      \
     ${COMPILER}/cpu.o       \
     ${COMPILER}/ethernet.o  \
     ${COMPILER}/flash.o     \
     ${COMPILER}/gpio.o      \
     ${COMPILER}/hibernate.o \
     ${COMPILER}/i2c.o       \
     ${COMPILER}/interrupt.o \
     ${COMPILER}/mpu.o       \
     ${COMPILER}/pwm.o       \
     ${COMPILER}/qei.o       \
     ${COMPILER}/ssi.o       \
     ${COMPILER}/sysctl.o    \
     ${COMPILER}/systick.o   \
     ${COMPILER}/timer.o     \
     ${COMPILER}/uart.o      \
     ${COMPILER}/watchdog.o

#
# The default rule, which causes the library to be built.
#
all: ${COMPILER}             \
     ${COMPILER}/libdriver.a

#
# The rule to clean out all the build products.
#
clean:
	@rm -rf ${COMPILER} settings
	@rm -rf ${wildcard *~} ${wildcard *.Bak} ${wildcard *.dep}

#
# The rule to create the target directory.
#
${COMPILER}:
	@mkdir -p ${COMPILER}

#
# The rule for building the driver library from the constituent object files.
#
${COMPILER}/libdriver.a: ${OBJS}

#
# The rule for building the set of object files from each C source file when
# using ewarm.  Unique --module-name= arguments must be supplied to each
# invocation of the compiler (to avoid the filename based default, which would
# unnecessarily restrict the file namespace), mandating this rule.
#
ifeq (${COMPILER}, ewarm)
${COMPILER}/%.o: %.c
	@if [ 'x${VERBOSE}' = x ];                           \
	 then                                                \
	     echo "  CC    ${<}";                            \
	 else                                                \
	     echo ${CC} ${CFLAGS} -D${COMPILER}              \
	          -o ${@} --module_name=__lib_${<:.c=} ${<}; \
	 fi
	@${CC} ${CFLAGS} -D${COMPILER}                 \
	     -o ${@} --module_name=__lib_${<:.c=} ${<}
	@sed -e "s,.*\.o:,${@}:,g" -e "s,\\\\,/,g" -e "s, ,\\\\ ,g" \
	     -e "s,:\\\\ ,: ,g" ${notdir ${<:.c=.d}} > ${@:.o=.d}
	@rm ${notdir ${<:.c=.d}}
ifneq ($(findstring CYGWIN, ${os}), )
	@perl -i.bak -p -e 's/ ([A-Za-z]):/ \/cygdrive\/\1/g' ${@:.o=.d}
	@rm -f ${@:.o=.d.bak}
endif
endif

#
# Include the automatically generated dependency files.
#
-include ${wildcard ${COMPILER}/*.d} __dummy__
