#include <stdio.h>
#include <stdarg.h>

#include "srec.h"

void
fatal (char *fmt, ...)
{
    va_list arglist;
    va_start(arglist, fmt);
    vfprintf(stderr, fmt, arglist);
    va_end(arglist);
    exit(1);
}

int
main (int argc, char **argv)
{
    FILE *file;
    char buf[256];
    int line = 0;
    srec_t srec;

    if (argc != 2) 
	fatal("usage: %s filename\n", argv[0]);

    if (!(file = fopen(argv[1], "r")))
	fatal("%s: couldn't open\n", argv[1]);

    while (fgets(buf, sizeof(buf), file)) {
	int error;
	line++;
	if ((error = srec_decode(&srec, buf)) < 0) {
	    if (error != S_INVALID_CKSUM) {
		fatal("%s: %s on line %d\n",
		      argv[1], srec_strerror(error), line);
	    }
	}
	if (srec.type == 0) {
	    srec.data[srec.count] = '\0';
	    printf("S0: %s\n", srec.data);
	}
    }
}


    
