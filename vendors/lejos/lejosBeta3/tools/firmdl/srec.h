/*
 *  srec.h
 *
 *  Header file for s-record routines.
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

#ifndef SREC_H_INCLUDED
#define SREC_H_INCLUDED

#ifdef SREC_STRICT
#define SREC_DATA_SIZE 32
#else
#define SREC_DATA_SIZE 256
#endif

typedef struct {
    unsigned char type;
    unsigned long addr;
    unsigned char count;
    unsigned char data[SREC_DATA_SIZE];
} srec_t;

/* This function decodes a line into an srec; returns negative on error */
extern int srec_decode (srec_t *srec, char *line);

/* This function encodes an srec into a line; returns negative on error */
extern int srec_encode (srec_t *srec, char *line);

/* Error values */
#define SREC_OK               0
#define SREC_NULL            -1
#define SREC_INVALID_HDR     -2
#define SREC_INVALID_CHAR    -3
#define SREC_INVALID_TYPE    -4
#define SREC_TOO_SHORT       -5
#define SREC_TOO_LONG        -6
#define SREC_INVALID_LEN     -7
#define SREC_INVALID_CKSUM   -8

/* Use srec_strerror to convert error codes into strings */
extern char *srec_strerror (int errno);

#endif /* SREC_H_INCLUDED */

