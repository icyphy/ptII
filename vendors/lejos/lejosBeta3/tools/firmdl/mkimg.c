/*
 *  mkimg.c
 *
 *  A program to make an image .c file given an s-record file.
 *
 *  Compile with: cc mkimg.c srec.c -o mkimg
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
 */

#include <string.h>
#include <stdio.h>
#include <ctype.h>

#include "srec.h"

#define IMAGE_START     0x8000
#define IMAGE_MAXLEN    0x4000

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

char *get_base_name (char *path)
{
    char *last = strrchr(path, '/');
    return (last) ? last + 1 : path;
}

char *build_image_name (char *dst, char *src)
{
    char *base = get_base_name(src);
    while (*base) {
	if ((*base >= 'a' && *base <= 'z') || (*base >= 'A' && *base <= 'A') ||
	    (*base >= '0' && *base <= '9') || *base == '_')
	    *dst++ = *base;
	else if (*base == '.')
	    break;
	else
	    *dst++ = '_';
	base++;
    }
    *dst++ = '\0';

    return dst;
}

int main (int argc, char **argv)
{
    unsigned char image_name[64];
    unsigned char image[IMAGE_MAXLEN];
    unsigned short image_start;
    unsigned int image_len;
    int i;

    argv[0] = get_base_name(argv[0]);

    if (argc != 2) {
	fprintf(stderr, "usage: %s filename\n", argv[0]);
	exit(1);
    }

    build_image_name(image_name, argv[1]);

    /* Load the s-record file */

    image_len = srec_load(argv[1], image, IMAGE_MAXLEN, &image_start);

    /* Dump a .c file */

    printf("/* Image file generated from %s by %s. */\n", argv[1],  argv[0]);
    printf("/* See source for %s for license info. */\n", argv[1]);
    printf("\n");
    printf("int %s_len = %d;\n", image_name, image_len);
    printf("unsigned char %s_image[] = {", image_name);
    for (i = 0; i < image_len; i++) {
	if (i % 16 == 0)
	    printf("\n    ");
	printf("%3d,", image[i]);
    }
    printf("\n");
    printf("};\n");
    printf("unsigned short %s_start = 0x%04x;\n", image_name, image_start);

    return 0;
}




