/*
 *  srec.c
 *
 *  S-record routines.
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

#include <stdio.h>
#include <ctype.h>

#include "srec.h"

/* Tables */

static signed char ctab[256] = {
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
     0, 1, 2, 3, 4, 5, 6, 7,   8, 9,-1,-1,-1,-1,-1,-1,
    -1,10,11,12,13,14,15,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,10,11,12,13,14,15,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
    -1,-1,-1,-1,-1,-1,-1,-1,  -1,-1,-1,-1,-1,-1,-1,-1,
};

static int ltab[10] = {4,4,6,8,0,4,0,8,6,4};

/* Macros */

#define C1(l,p)    (ctab[l[p]])
#define C2(l,p)    ((C1(l,p)<<4)|C1(l,p+1))

/* Static functions */

int
srec_decode(srec_t *srec, char *_line)
{
    int len, pos = 0, count, alen, sum = 0;
    unsigned char *line = (unsigned char *)_line;

    if (!srec || !line)
	return SREC_NULL;

    for (len = 0; line[len]; len++)
	if (line[len] == '\n' || line[len] == '\r')
	    break;

    if (len < 4)
	return SREC_INVALID_HDR;

    if (line[0] != 'S')
	return SREC_INVALID_HDR;

    for (pos = 1; pos < len; pos++) {
	if (C1(line, pos) < 0)
	    return SREC_INVALID_CHAR;
    }

    srec->type = C1(line, 1);
    count = C2(line, 2);

    if (srec->type > 9)
	return SREC_INVALID_TYPE;
    alen = ltab[srec->type];
    if (alen == 0)
	return SREC_INVALID_TYPE;
    if (len < alen + 6)
	return SREC_TOO_SHORT;
    if (count > alen + SREC_DATA_SIZE + 2)
	return SREC_TOO_LONG;
    if (len != count * 2 + 4)
	return SREC_INVALID_LEN;

    sum += count;

    len -= 4;
    line += 4;

    srec->addr = 0;
    for (pos = 0; pos < alen; pos += 2) {
	unsigned char value = C2(line, pos);
	srec->addr = (srec->addr << 8) | value;
	sum += value;
    }

    len -= alen;
    line += alen;

    for (pos = 0; pos < len - 2; pos += 2) {
	unsigned char value = C2(line, pos);
	srec->data[pos / 2] = value;
	sum += value;
    }

    srec->count = count - (alen / 2) - 1;

    sum += C2(line, pos);

    if ((sum & 0xff) != 0xff)
	return SREC_INVALID_CKSUM;

    return SREC_OK;
}

int
srec_encode(srec_t *srec, char *line)
{
    int alen, count, sum = 0, pos;

    if (srec->type > 9)
	return SREC_INVALID_TYPE;
    alen = ltab[srec->type];
    if (alen == 0)
	return SREC_INVALID_TYPE;

    line += sprintf(line, "S%d", srec->type);

    if (srec->count > 32)
	return SREC_TOO_LONG;
    count = srec->count + (alen / 2) + 1;
    line += sprintf(line, "%02X", count);
    sum += count;

    while (alen) {
	int value;
	alen -= 2;
	value = (srec->addr >> (alen * 4)) & 0xff;
	line += sprintf(line, "%02X", value);
	sum += value;
    }

    for (pos = 0; pos < srec->count; pos++) {
	line += sprintf(line, "%02X", srec->data[pos]);
	sum += srec->data[pos];
    }

    sprintf(line, "%02X\n", (~sum) & 0xff);

    return SREC_OK;
}

char *
srec_strerror (int error)
{
    switch (error) {
    case SREC_OK: return "no error";
    case SREC_NULL: return "null string error";
    case SREC_INVALID_HDR: return "invalid header";
    case SREC_INVALID_CHAR: return "invalid character";
    case SREC_TOO_SHORT: return "line too short";
    case SREC_TOO_LONG: return "line too long";
    case SREC_INVALID_LEN: return "length error";
    case SREC_INVALID_CKSUM: return "checksum error";
    default: return "unknown error";
    }
}
