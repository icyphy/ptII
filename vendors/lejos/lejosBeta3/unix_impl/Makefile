
VMSRC=../vmsrc
LD=ld
CC=gcc

## No -O2 option below because gcc 2.95.2 crashes!

CFLAGS=-I../vmsrc -I. -Wall -Wno-unused

DUMP_OBJ=dump.o load.o

VMEMUL_OBJ=interpreter.o threads.o exceptions.o \
	   memory.o language.o \
           tvmemul.o traceemul.o nativeemul.o load.o opcodeinfo.o

DUMP_TARGET=../bin/emu-dump
VMEMUL_TARGET=../bin/emu-lejosrun

# EXTRADEP used in vmsrc/Makefile.include

EXTRADEP=platform_config.h

default: programs

programs: ${VMEMUL_TARGET} ${DUMP_TARGET}

include ../vmsrc/Makefile.include


platform_config.h: dump_config
	./dump_config > platform_config.h

dump_config: dump_config.c
	${CC} ${CFLAGS} dump_config.c -o dump_config

	
%.o: %.c platform_config.h
	${CC} ${CFLAGS} -c $*.c
	
${DUMP_TARGET}: ${DUMP_OBJ}
	${CC} ${DUMP_OBJ} -o ${DUMP_TARGET}

${VMEMUL_TARGET}: ${VMEMUL_OBJ}
	${CC} ${VMEMUL_OBJ} -o ${VMEMUL_TARGET}

clean:
	rm -rf ${VMSRC}/*.o *.o ../bin/emu-*.exe core platform_config.h ${DUMP_TARGET} ${VMEMUL_TARGET}

