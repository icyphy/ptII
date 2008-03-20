
# -- Jacquard with ACML
# make sure you 'module load acml' first
#CC= pathcc
CC= upcc
#CFLAGS= -O3 
#CFLAGS= -tv
LIBS= $(ACML) -lm -lpathfortran #-lblas #-llapack -lg2c

# -- Franklin with libSci
# you shouldn't need to load the libsci module; it is loaded by default
#CC= cc
#CFLAGS= -O3 
#LIBS= -pgf90libs -lm 



SRCS= driver.c cg.c mmio.c clapack.c 
OBJS= ${SRCS:.c=.o}

cg: Makefile $(OBJS) 
	$(CC) $(CFLAGS) -o cg $(OBJS) $(LIBS)

tgz: tar
	gzip cg.tar
	cp cg.tar.gz cg.tgz

tar:
	tar -cf cg.tar Makefile $(SRCS) *.h

clean:
	rm -f cg *.out *.o *~

indent:
	indent -kr -nut *.c *.h
