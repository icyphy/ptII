/*
 *  firmdl3.c
 *
 *  A firmware downloader for the RCX.  Version 3.0.  Supports single and
 *  quad speed downloading.
 *
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS"
 *  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 *  License for the specific language governing rights and limitations
 *  under the License.
 *
 *  The Original Code is Firmdl code, released October 3, 1998.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1998, 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  Contributor(s): Kekoa Proudfoot <kekoa@graphics.stanford.edu>
 *                  Laurent Demailly
 *                  Allen Martin
 *                  Markus Noga
 *                  Gavin Smyth
 *                  Luis Villa
 *                  Jose Solorzano
 */

/*
 *  usage: firmdl [options] srecfile  (e.g. firmdl Firm0309.lgo)
 *
 *  If necessary, set DEFAULTTTY, below, to the serial device you want to use.
 *  Set the RCXTTY environment variable to override DEFAULTTTY.
 *  Use the command-line option --tty=TTY to override RCXTTY and DEFAULTTTY.
 *
 *  Acknowledgements:
 *
 *     Laurent Demailly, Allen Martin, Markus Noga, Gavin Smyth, and Luis
 *     Villa all contributed something to some version of this program.
 *
 *  Version history:
 *
 *     1.x: single speed downloading plus many small revisions
 *     2.0: double speed downloading, improved comm code, never released
 *     3.0: quad speed downloads, misc other features, version numbering
 *     For leJOS 1.0.0beta3: Default transfer slow, etc.
 *
 *  Kekoa Proudfoot
 *  kekoa@graphics.stanford.edu
 *  10/3/98, 10/3/99
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <termios.h>
#include <stdio.h>
#include <string.h>
#include <sys/time.h>
#include <ctype.h>
#include <string.h>
#include "util.h"

#if defined(_WIN32)
  #include <windows.h>
#endif

#include "rcx_comm.h"
#include "srec.h"

/* Machine-dependent defines */

#if defined(LINUX) || defined(linux)
#define DEFAULTTTY   "/dev/ttyS0" /* Linux - COM1 */
#elif defined (_WIN32)
#define DEFAULTTTY   "com1"       /* Cygwin - COM1 */
#elif defined (sun)
#define DEFAULTTTY   "/dev/ttya"  /* Solaris - first serial port - untested */
#else
#define DEFAULTTTY   "/dev/ttyd2" /* IRIX - second serial port */
#endif

/* Global variables */

char *progname;

#include "fastdl.h"

/* Defines */

#define BUFFERSIZE      4096
#define RETRIES         5
#define WAKEUP_TIMEOUT  4000
#define MOST_TIMEOUT    100

#define IMAGE_START     0x8000
#define IMAGE_MAXLEN    0x4c00
#define TRANSFER_SIZE   200

/* Stripping zeros is not entirely legal if firmware expects trailing zeros */
/* Define FORCE_ZERO_STRIPPING to force zero stripping for all files */
/* Normally you do not want to do this */
/* Possibly useful only if you explicitly zero pad for OCX compatiblity */
/* Since zero stripping is okay for Firm0309.lgo, that is done automatically */

#if 0
#define FORCE_ZERO_STRIPPING
#endif

/* Functions */

int srec_load (char *name, unsigned char *image, int maxlen, unsigned short *start)
{
    FILE *file;
    char buf[256];
    srec_t srec;
    int line = 0;
    int length = 0;
    int strip = 0;

    /* Initialize starting address */
    *start = IMAGE_START;

    /* Open file */
    if ((file = fopen(name, "r")) == NULL) {
	fprintf(stderr, "%s: failed to open\n", name);
	exit(1);
    }

    /* Clear image to zero */
    memset(image, 0, maxlen);

    /* Read image file */
    while (fgets(buf, sizeof(buf), file)) {
	int error, i;
	line++;
	/* Skip blank lines */
	for (i = 0; buf[i]; i++)
	    if (!isspace(buf[i]))
		break;
	if (!buf[i])
	    continue;
	/* Decode line */
	if ((error = srec_decode(&srec, buf)) < 0) {
	    if (error != SREC_INVALID_CKSUM) {
		fprintf(stderr, "%s: %s on line %d\n",
			name, srec_strerror(error), line);
		exit(1);
	    }
	}
	/* Detect Firm0309.lgo header, set strip=1 if found */
	if (srec.type == 0) {
	    if (srec.count == 16)
		if (!strncmp(srec.data, "?LIB_VERSION_L00", 16))
		    strip = 1;
	}
	/* Process s-record data */
	else if (srec.type == 1) {
	    if (srec.addr < IMAGE_START ||
		srec.addr + srec.count > IMAGE_START + maxlen) {
		fprintf(stderr, "%s: address out of bounds on line %d\n",
			name, line);
		exit(1);
	    }
	    if (srec.addr + srec.count - IMAGE_START > length)
		length = srec.addr + srec.count - IMAGE_START;
	    memcpy(&image[srec.addr - IMAGE_START], &srec.data, srec.count);
	}
	/* Process image starting address */
	else if (srec.type == 9) {
	    if (srec.addr < IMAGE_START ||
		srec.addr > IMAGE_START + maxlen) {
		fprintf(stderr, "%s: address out of bounds on line %d\n",
			name, line);
		exit(1);
	    }
	    *start = srec.addr;
	}
    }

    /* Strip zeros */
#ifdef FORCE_ZERO_STRIPPING
    strip = 1;
#endif

    if (strip) {
	int pos;
	for (pos = IMAGE_MAXLEN - 1; pos >= 0 && image[pos] == 0; pos--);
	length = pos + 1;
    }

    /* Check length */
    if (length == 0) {
	fprintf(stderr, "%s: image contains no data\n", name);
	exit(1);
    }

    return length;
}

void image_dl(FILEDESCR fd, unsigned char *image, int len, unsigned short start,
	      int use_comp, char *filename)
{
    unsigned short cksum = 0;
    unsigned char send[BUFFERSIZE];
    unsigned char recv[BUFFERSIZE];
    int addr, index, size, i;

    /* Compute image checksum */
    for (i = 0; i < len; i++)
	cksum += image[i];

    /* Delete firmware */
    send[0] = 0x65;
    send[1] = 1;
    send[2] = 3;
    send[3] = 5;
    send[4] = 7;
    send[5] = 11;

    if (rcx_sendrecv(fd, send, 6, recv, 1, MOST_TIMEOUT, RETRIES, use_comp) != 1) {
	fprintf(stderr, "%s: delete firmware failed\n", progname);
	exit(1);
    }

    /* Start firmware download */
    send[0] = 0x75;
    send[1] = (start >> 0) & 0xff;
    send[2] = (start >> 8) & 0xff;
    send[3] = (cksum >> 0) & 0xff;
    send[4] = (cksum >> 8) & 0xff;
    send[5] = 0;

    if (rcx_sendrecv(fd, send, 6, recv, 2, MOST_TIMEOUT, RETRIES, use_comp) != 2) {
	fprintf(stderr, "%s: start firmware download failed\n", progname);
	exit(1);
    }

    /* Transfer data */
    fprintf(stderr, "\rTransferring \"%s\" to RCX...\n", filename);
    addr = 0;
    index = 1;
    for (addr = 0, index = 1; addr < len; addr += size, index++) {
	fprintf(stderr,"\r%3d%%        \r",(100*addr)/len);
	size = len - addr;
	send[0] = 0x45;
	if (index & 1)
	    send[0] |= 0x08;
	if (size > TRANSFER_SIZE)
	    size = TRANSFER_SIZE;
	else if (0)
	    /* Set index to zero to make sound after last transfer */
	    index = 0;
	send[1] = (index >> 0) & 0xff;
	send[2] = (index >> 8) & 0xff;
	send[3] = (size >> 0) & 0xff;
	send[4] = (size >> 8) & 0xff;
	memcpy(&send[5], &image[addr], size);
	for (i = 0, cksum = 0; i < size; i++)
	    cksum += send[5 + i];
	send[size + 5] = cksum & 0xff;

	if (rcx_sendrecv(fd, send, size + 6, recv, 2, MOST_TIMEOUT, RETRIES,
			 use_comp) != 2 || recv[1] != 0) {
	    fprintf(stderr, "%s: transfer data failed\n", progname);
	    exit(1);
	}
    }
    fputs("100%        \n",stderr);

    /* Unlock firmware */
    send[0] = 0xa5;
    send[1] = 76;		// 'L'
    send[2] = 69;		// 'E'
    send[3] = 71;		// 'G'
    send[4] = 79;		// 'O'
    send[5] = 174;	// '®'

    /* Use longer timeout so ROM has time to checksum firmware */
    if (rcx_sendrecv(fd, send, 6, recv, 26, 150, RETRIES, use_comp) != 26) {
	fprintf(stderr, "%s: unlock firmware failed\n", progname);
	exit(1);
    }
}

int main (int argc, char **argv)
{
    unsigned char image[IMAGE_MAXLEN];
    unsigned short image_start;
    unsigned int image_len;
    char *tty = NULL;
    char *fileName = NULL;
    char *dname = NULL;
    int use_fast = 0;
    int usage = 0;
    FILEDESCR fd;
    int status;

    progname = argv[0];
    dname = dirname (progname);
    if (strcmp (dname, "") == 0)
    {
      progname = which (progname);
      if (progname == NULL)
      {
	fprintf (stderr, "Unexpected: %s not in PATH.\n", argv[0]);
	exit (1);
      }
      dname = dirname (progname);
    }

    /* Parse command line */

    argv++; argc--;
    while (argc && argv[0][0] == '-') {
	if (argv[0][1] == '-') {
	    if (!strcmp(argv[0], "--")) {
		argv++; argc--;
		break;
	    }
	    else if (!strcmp(argv[0], "--debug")) {
		extern int __comm_debug;
		__comm_debug = 1;
	    }
	    else if (!strcmp(argv[0], "--fast")) {
		use_fast = 1;
	    }
	    else if (!strcmp(argv[0], "--slow")) {
		use_fast = 0;
	    }
	    else if (!strncmp(argv[0], "--tty", 5)) {
		if (argv[0][5] == '=') {
		    tty = &argv[0][6];
		}
		else if (argc > 1) {
		    argv++; argc--;
		    tty = argv[0];
		}
		else
		    tty = "";
		if (!tty[0]) {
		    fprintf(stderr, "%s: invalid tty: %s\n", progname, tty);
		    exit(1);
		}
	    }
	    else if (!strcmp(argv[0], "--help")) {
		usage = 1;
	    }
	    else {
		fprintf(stderr, "%s: unrecognized option %s\n",
			progname, argv[0]);
		exit(1);
	    }
	}
	else {
	    char *p = &argv[0][1];
	    if (!*p)
		break;
	    while (*p) {
		switch (*p) {
		case 'f': use_fast = 1; break;
		case 's': use_fast = 0; break;
		case 'h': usage = 1; break;
		default:
		    fprintf(stderr, "%s: unrecognized option -- %c\n",
			    progname, *p);
		    exit(1);
		}
		p++;
	    }
	}
	argv++;
	argc--;
    }

    if (argc == 0) {
      char *tinyvmHome;

      tinyvmHome = append (dname, "/..");
      fileName = (char *) malloc (strlen (tinyvmHome) + 32);
      strcpy (fileName, tinyvmHome);
      strcat (fileName, "/bin/lejos.srec");
      if (!usage)
        printf ("Use --help for options.\n");
    } else if (argc == 1) {
      fileName = argv[0];
    } else {
      usage = 1;
    }

    if (usage) {
	char *usage_string =
	    "      --debug      show debug output, mostly raw bytes\n"
	    "  -f, --fast       use fast 4x downloading\n"
	    "  -s, --slow       use slow 1x downloading (default)\n"
	    "      --tty=TTY    assume tower connected to TTY\n"
	    "  -h, --help       display this help and exit\n"
	    ;

	fprintf(stderr, "usage: %s [options] filename\n", progname);
	fprintf(stderr, usage_string);
	exit(1);
    }

    /* Load the s-record file */

    image_len = srec_load(fileName, image, IMAGE_MAXLEN, &image_start);

    /* Get the tty name */

    if (!tty)
	tty = getenv("RCXTTY");
    if (!tty)
    {
	printf ("RCXTTY undefined. Using: %s\n", DEFAULTTTY);
	tty = DEFAULTTTY;
    }

    if (use_fast) {
	/* Try to wake up the tower in fast mode */

	fd = rcx_init(tty, 0);	// Slow startup seems better with low batteries...

	if ((status = rcx_wakeup_tower(fd, WAKEUP_TIMEOUT)) < 0) {
	    fprintf(stderr, "%s: %s\n", progname, rcx_strerror(status));
	    exit(1);
	}

	// Let's put IR in Fast mode.
	rcx_close(fd);
	fd = rcx_init(tty, 1);

	/* Check if already alive in fast mode */
	if (!rcx_is_alive(fd, 0)) {
	    /* Not alive in fast mode, download fastdl in slow mode */

	    rcx_close(fd);
	    fd = rcx_init(tty, 0);

	    if (!rcx_is_alive(fd, 1)) {
		fprintf(stderr, "%s: no response from rcx\n", progname);
		exit(1);
	    }

	    image_dl(fd, fastdl_image, fastdl_len, fastdl_start, 1, "Fast Download Image");

	    /* Go back to fast mode */
	    rcx_close(fd);
	    fd = rcx_init(tty, 1);
	}

	/* Download image in fast mode */

	image_dl(fd, image, image_len, image_start, 0, fileName);
	rcx_close(fd);
    }
    else {
	/* Try to wake up the tower in slow mode */

	fd = rcx_init(tty, 0);

	if ((status = rcx_wakeup_tower(fd, WAKEUP_TIMEOUT)) < 0) {
	    fprintf(stderr, "%s: %s\n", progname, rcx_strerror(status));
	    exit(1);
	}

	if (!rcx_is_alive(fd, 1)) {
	    /* See if alive in fast mode */

	    rcx_close(fd);
	    fd = rcx_init(tty, 1);

	    if (rcx_is_alive(fd, 0)) {
		fprintf(stderr, "%s: rcx is in fast mode\n", progname);
		fprintf(stderr, "%s: turn rcx off then back on to "
			"use slow mode\n", progname);
		exit(1);
	    }

	    fprintf(stderr, "%s: no response from rcx\n", progname);
	    exit(1);
	}

	/* Download image */
	image_dl(fd, image, image_len, image_start, 1, fileName);

	rcx_close(fd);
    }

    return 0;
}
