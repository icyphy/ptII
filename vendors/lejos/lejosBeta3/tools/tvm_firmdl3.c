#define MOST_TIMEOUT    100
/**
 * tvm_firmdl3.c
 * This is essentially a copy of Kekoa Proudfoot's firmdl3.c,
 * available at http://graphics.stanford.edu/~kekoa/rcx/firmdl3.c.
 * License information shown below.
 * 
 * Jose's changes:
 *   08/23/00 -- This file replaces the old tvmfirmdl.c,
 *               which was based on the first version of firmdl.c.
 *               By default, it downloads ../bin/tinyvm.srec. 
 *               It requires RCXTTY. Made --slow the default.
 *   08/24/00 -- Sending delete-firmware twice, like Lego's
 *               GUI. Added sleep_us(), which is called in between
 *               data transfers. 
 *   09/15/00 -- Made sure calls to nbread use exact lengths, as
 *               a workaround for a bug in the select system call
 *               in the latest CygWin version.
 */

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
 */

/*
 *  usage: firmdl [options] srecfile  (e.g. firmdl Firm0309.lgo)
 *
 *  Under IRIX, Linux, and possibly Solaris, you should be able to compile
 *  this program using cc firmdl3.c -o firmdl.  Under Cygwin, try compiling
 *  with gcc.  Other version of Unix might also work.
 *
 *  If necessary, set DEFAULTTTY, below, to the serial device you want to use.
 *  Set the RCXTTY environment variable to override DEFAULTTTY.
 *  Use the command-line option --tty=TTY to override RCXTTY and DEFAULTTTY.
 *
 *  This is file contains a join of the following files: fastdl.h,
 *  rcx_comm.h, rcx_comm.c, srec.h, srec.c, and firmdl.c.  This simplifies
 *  distribution and compilation in some cases.  If you want to use the
 *  files separately, up-to-date versions are available in a .tgz file at:
 *
 *      http://graphics.stanford.edu/~kekoa/rcx/tools.html
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

/* Machine-dependent defines */

#if defined(LINUX) || defined(linux)
#define DEFAULTTTY   "/dev/ttyS0" /* Linux - COM1 */
#elif defined (WINNT)
#define DEFAULTTTY   "com1"       /* Cygwin - COM1 */
#elif defined (sun)
#define DEFAULTTTY   "/dev/ttya"  /* Solaris - first serial port - untested */
#else
#define DEFAULTTTY   "/dev/ttyd2" /* IRIX - second serial port */
#endif

/*** rcx_comm.h ***/

#ifndef RCX_COMM_H_INCLUDED
#define RCX_COMM_H_INCLUDED

#define RCX_OK             0
#define RCX_NO_TOWER      -1
#define RCX_BAD_LINK      -2
#define RCX_BAD_ECHO      -3
#define RCX_NO_RESPONSE   -4
#define RCX_BAD_RESPONSE  -5

#define DEFAULT_FIRMWARE "lejos.srec"

/* Get a file descriptor for the named tty, exits with message on error */
extern int rcx_init (char *tty, int is_fast);

/* Close a file descriptor allocated by rcx_init */
extern void rcx_close (int fd);

/* Try to wakeup the tower for timeout ms, returns error code */
extern int rcx_wakeup_tower (int fd, int timeout);

/* Try to send a message, returns error code */
/* Set use_comp=1 to send complements, use_comp=0 to suppress them */
extern int rcx_send (int fd, void *buf, int len, int use_comp);

/* Try to receive a message, returns error code */
/* Set use_comp=1 to expect complements */
/* Waits for timeout ms before detecting end of response */
extern int rcx_recv (int fd, void *buf, int maxlen, int timeout, int use_comp);

/* Try to send a message and receive its response, returns error code */
/* Set use_comp=1 to send and receive complements, use_comp=0 otherwise */
/* Waits for timeout ms before detecting end of response */
extern int rcx_sendrecv (int fd, void *send, int slen, void *recv, int rlen, int timeout, int retries, int use_comp);

/* Test whether or not the rcx is alive, returns 1=yes, 0=no */
/* Set use_comp=1 to send complements, use_comp=0 to suppress them */
extern int rcx_is_alive (int fd, int use_comp);

/* Convert an error code to a string */
extern char *rcx_strerror(int error);

#endif /* RCX_COMM_H_INCLUDED */

/*** rcx_comm.c ***/

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <termios.h>
#include <stdio.h>
#include <sys/time.h>
#include <ctype.h>
#include <string.h>

/* Defines */

#define BUFFERSIZE  1024


#ifndef O_BINARY
#define O_BINARY 0
#endif

#define TRACE 0

/* Globals */

int __comm_debug = 0;

/* Timer routines */

typedef struct timeval timeval_t;

#define tvupdate(tv)  gettimeofday(tv,NULL)
#define tvsec(tv)     ((tv)->tv_sec)
#define tvmsec(tv)    ((tv)->tv_usec * 1e-3)

static float
timer_reset(timeval_t *timer)
{
    tvupdate(timer);
    return 0;
}

static float
timer_read(timeval_t *timer)
{
    timeval_t now;
    tvupdate(&now);
    return tvsec(&now) - tvsec(timer) + (tvmsec(&now) - tvmsec(timer)) * 1e-3;
}

/* Hexdump routine */

#define LINE_SIZE   16
#define GROUP_SIZE  4
#define UNPRINTABLE '.'

static void sleep_us (unsigned long us)
{
  struct timeval tval;

  #if TRACE
  printf ("sleeping\n");
  #endif

  tval.tv_sec = us / 1000000L;
  tval.tv_usec = us % 1000000L;
  select (0, NULL, NULL, NULL, &tval);	

  #if TRACE
  printf ("done sleeping\n");
  #endif
}

static void
hexdump(char *prefix, void *buf, int len)
{
    unsigned char *b = (unsigned char *)buf;
    int i, j, w;
    
    for (i = 0; i < len; i += w) {
        w = len - i;
        if (w > LINE_SIZE)
            w = LINE_SIZE;
	if (prefix)
	    printf("%s ", prefix);
        printf("%04x: ", i);
        for (j = 0; j < w; j++, b++) {
            printf("%02x ", *b);
            if ((j + 1) % GROUP_SIZE == 0)
                putchar(' ');
        }
        putchar('\n');
    }
}

/* Timeout read routine */

static int
nbread (int fd, void *buf, int maxlen, int timeout)
{
    char *bufp = (char *)buf;
    int len = 0;

    while (len < maxlen) {
	fd_set fds;
	fd_set fdexcep;
	struct timeval tv;
	int count;
	int numReady;

	FD_ZERO(&fds);
	FD_ZERO(&fdexcep);
	FD_SET(fd, &fds);
	FD_SET(fd, &fdexcep);

	tv.tv_sec = timeout / 1000;
	tv.tv_usec = (timeout % 1000) * 1000;

	#if TRACE
	printf ("select %ld:%ld\n", (long) tv.tv_sec, (long) tv.tv_usec);
	#endif
	
	if ((numReady = select(FD_SETSIZE, &fds, NULL, &fdexcep, &tv)) < 0) {
	    perror("select");
	    exit(1);
	}
	
	#if TRACE
	printf ("select returned %d\n", (int) numReady);
	#endif
	
        if (numReady == 0)
	{
	  #if TRACE
	  printf ("select timeout\n");
	  #endif
          return len;
	}
	
	if (!FD_ISSET(fd, &fds))
	    break;
 
        if (FD_ISSET(fd, &fdexcep))
	{
	  #if TRACE
	  printf ("Exception condition\n");
	  #endif
	  return len;
	}
	
	#if TRACE
	printf ("reading %d\n", (int) (maxlen - len));
	#endif
	
	if ((count = read(fd, &(bufp[len]), maxlen - len)) < 0) {
	    perror("read");
	    exit(1);
	}

	#if TRACE
	printf ("read %d\n", (int) count);
	#endif
	
	len += count;
    }

    return len;
}

/* RCX routines */

int
rcx_init(char *tty, int is_fast)
{
    int fd;
    struct termios ios;

    if (__comm_debug) printf("mode = %s\n", is_fast ? "fast" : "slow");

    if ((fd = open(tty, O_RDWR | O_BINARY)) < 0) {
	perror(tty);
	exit(1);
    }

    if (!isatty(fd)) {
	close(fd);
	fprintf(stderr, "%s: not a tty\n", tty);
	exit(1);
    }

    memset(&ios, 0, sizeof(ios));

    if (is_fast) {
	ios.c_cflag = CREAD | CLOCAL | CS8;
	cfsetispeed(&ios, B4800);
	cfsetospeed(&ios, B4800);
    }
    else {
	ios.c_cflag = CREAD | CLOCAL | CS8 | PARENB | PARODD;
	cfsetispeed(&ios, B2400);
	cfsetospeed(&ios, B2400);
    }

    if (tcsetattr(fd, TCSANOW, &ios) == -1) {
	perror("tcsetattr");
	exit(1);
    }

    return fd;
}

void
rcx_close(int fd)
{
    close(fd);
}

int
rcx_wakeup_tower (int fd, int timeout)
{
    char msg[] = { 0x10, 0xfe, 0x10, 0xfe };
    char buf[BUFFERSIZE];
    timeval_t timer;
    int count = 0;
    int len;

    #if TRACE
    printf ("Resetting timer\n");
    #endif
    
    timer_reset(&timer);

    do {
	#if TRACE
	printf ("Writing message of size %d\n", (int) sizeof(msg));
	#endif
	
	if (write(fd, msg, sizeof(msg)) != sizeof(msg)) {
	    perror("Unable to write: Probably a resource allocation problem: "
	           "Close programs or reboot");
	    exit(1);
	}
	
	#if TRACE
	printf ("Back from write\n");
	#endif
	
	//count += (len = nbread(fd, buf, BUFFERSIZE, 50));
	count += (len = nbread(fd, buf, sizeof(msg), MOST_TIMEOUT));
	
	#if TRACE
	printf ("Response length = %d\n", (int) len);
	#endif
	
	if (len == sizeof(msg) && !memcmp(buf, msg, sizeof(msg)))
	    return RCX_OK; /* success */
    } while (timer_read(&timer) < (float)timeout / 1000.0f);

    if (!count)
	return RCX_NO_TOWER; /* tower not responding */
    else
	return RCX_BAD_LINK; /* bad link */
}

int
rcx_send (int fd, void *buf, int len, int use_comp)
{
    char *bufp = (char *)buf;
    char buflen = len;
    char msg[BUFFERSIZE];
    char echo[BUFFERSIZE];
    int msglen, echolen;
    int sum;

    /* Encode message */

    msglen = 0;
    sum = 0;
    
    if (use_comp) {
	msg[msglen++] = 0x55;
	msg[msglen++] = 0xff;
	msg[msglen++] = 0x00;
	while (buflen--) {
	    msg[msglen++] = *bufp;
	    msg[msglen++] = (~*bufp) & 0xff;
	    sum += *bufp++;
	}
	msg[msglen++] = sum;
	msg[msglen++] = ~sum;
    }
    else {
	msg[msglen++] = 0xff;
    	while (buflen--) {
	    msg[msglen++] = *bufp;
	    sum += *bufp++;
	}
	msg[msglen++] = sum;
    }

    /* Send message */
    
    if (write(fd, msg, msglen) != msglen) {
	perror("Unable to write");
	exit(1);
    }

    /* Receive echo */

    echolen = nbread(fd, echo, msglen, 100);

    if (__comm_debug) {
	printf("msglen = %d, echolen = %d\n", msglen, echolen);
	hexdump("C", echo, echolen);
    }

    /* Check echo */
    /* Ignore data, since rcx might send ack even if echo data is wrong */

    if (echolen != msglen /* || memcmp(echo, msg, msglen) */ ) {

	/* Flush connection if echo is bad */

	//echolen = nbread(fd, echo, BUFFERSIZE, 200);

	return RCX_BAD_ECHO;
    }

    return len;
}

int
rcx_recv (int fd, void *buf, int maxlen, int timeout, int use_comp)
{
    char *bufp = (char *)buf;
    unsigned char msg[BUFFERSIZE];
    int msglen;
    int sum;
    int pos;
    int len;
    int expectedLen;

    /* Receive message */

    #if TRACE
    printf ("rcx_recv maxlen=%d\n", (int) maxlen);
    #endif
    
    expectedLen = use_comp ? (maxlen + 2) * 2 + 1 : BUFFERSIZE;
    msglen = nbread(fd, msg, expectedLen, timeout);

    if (__comm_debug) {
	printf("recvlen = %d\n", msglen);
	hexdump("R", msg, msglen);
    }

    /* Check for message */

    if (!msglen)
	return RCX_NO_RESPONSE;

    /* Verify message */

    if (use_comp) {
	if (msglen < 5 || (msglen - 3) % 2 != 0)
	    return RCX_BAD_RESPONSE;
	
	if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
	    return RCX_BAD_RESPONSE;

	for (sum = 0, len = 0, pos = 3; pos < msglen - 2; pos += 2) {
	    if (msg[pos] != ((~msg[pos+1]) & 0xff))
		return RCX_BAD_RESPONSE;
	    sum += msg[pos];
	    if (len < maxlen)
		bufp[len++] = msg[pos];
	}

	if (msg[pos] != ((~msg[pos+1]) & 0xff))
	    return RCX_BAD_RESPONSE;
    
	if (msg[pos] != (sum & 0xff))
	    return RCX_BAD_RESPONSE;

	/* Success */
	return len;
    }
    else {
	if (msglen < 4)
	    return RCX_BAD_RESPONSE;

	if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
	    return RCX_BAD_RESPONSE;

	for (sum = 0, len = 0, pos = 3; pos < msglen - 1; pos++) {
	    sum += msg[pos];
	    if (len < maxlen)
		bufp[len++] = msg[pos];
	}

	/* Return success if checksum matches */
	if (msg[pos] == (sum & 0xff))
	    return len;

	/* Failed.  Possibly a long message? */
	/* Long message if opcode is complemented and checksum okay */
	/* If long message, checksum does not include opcode complement */
	for (sum = 0, len = 0, pos = 3; pos < msglen - 1; pos++) {
	    if (pos == 4) {
		if (msg[3] != ((~msg[4]) & 0xff))
		    return RCX_BAD_RESPONSE;
	    }
	    else {
		sum += msg[pos];
		if (len < maxlen)
		    bufp[len++] = msg[pos];
	    }
	}

	if (msg[pos] != (sum & 0xff))
	    return RCX_BAD_RESPONSE;

	/* Success */
	return len;
    }
}

int
rcx_sendrecv (int fd, void *send, int slen, void *recv, int rlen,
	      int timeout, int retries, int use_comp)
{
    int status = 0;

    #if TRACE
    printf ("rcx_sendrecv: slen=%d, rlen=%d, use_comp=%d\n",
            (int) slen, (int) rlen, use_comp); 
    #endif
    
    if (__comm_debug) printf("sendrecv %d:\n", slen);

    while (retries--) {
	if ((status = rcx_send(fd, send, slen, use_comp)) < 0) {
	    if (__comm_debug) printf("status = %s\n", rcx_strerror(status));
	    sleep_us (500000L);
	    continue;
	}
	if ((status = rcx_recv(fd, recv, rlen, timeout, use_comp)) < 0) {
	    if (__comm_debug) printf("status = %s\n", rcx_strerror(status));
	    sleep_us (500000L);
	    continue;
	}
	break;
    }

    if (__comm_debug) {
	if (status > 0)
	    printf("status = %s\n", rcx_strerror(0));
	else
	    printf("status = %s\n", rcx_strerror(status));
    }

    return status;
}

int
rcx_is_alive (int fd, int use_comp)
{
    unsigned char send[1] = { 0x10 };
    unsigned char recv[1];

    return (rcx_sendrecv(fd, send, 1, recv, 1, MOST_TIMEOUT, 5, use_comp) == 1);
}

char *
rcx_strerror (int error)
{
    switch (error) {
    case RCX_OK: return "no error";
    case RCX_NO_TOWER: return "tower not responding";
    case RCX_BAD_LINK: return "bad ir link";
    case RCX_BAD_ECHO: return "bad ir echo";
    case RCX_NO_RESPONSE: return "no response from rcx";
    case RCX_BAD_RESPONSE: return "bad response from rcx";
    default: return "unknown error";
    }
}

/*** srec.h ***/

#ifndef SREC_H_INCLUDED
#define SREC_H_INCLUDED

typedef struct {
    unsigned char type;
    unsigned long addr;
    unsigned char count;
    unsigned char data[32];
} srec_t;

/* This function decodes a line into an srec; returns negative on error */
extern int srec_decode (srec_t *srec, char *line);

/* This function encodes an srec into a line; returns negative on error */
extern int srec_encode (srec_t *srec, char *line);

/* Error values */
#define S_OK               0
#define S_NULL            -1
#define S_INVALID_HDR     -2
#define S_INVALID_CHAR    -3
#define S_INVALID_TYPE    -4
#define S_TOO_SHORT       -5
#define S_TOO_LONG        -6
#define S_INVALID_LEN     -7
#define S_INVALID_CKSUM   -8

/* Use srec_strerror to convert error codes into strings */
extern char *srec_strerror (int errno);

#endif /* SREC_H_INCLUDED */

/*** srec.c ***/

#include <stdio.h>
#include <ctype.h>

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
	return S_NULL;

    for (len = 0; line[len]; len++)
	if (line[len] == '\n' || line[len] == '\r')
	    break;

    if (len < 4)
	return S_INVALID_HDR;

    if (line[0] != 'S')
	return S_INVALID_HDR;

    for (pos = 1; pos < len; pos++) {
	if (C1(line, pos) < 0)
	    return S_INVALID_CHAR;
    }

    srec->type = C1(line, 1);
    count = C2(line, 2);

    if (srec->type > 9)
	return S_INVALID_TYPE;
    alen = ltab[srec->type];
    if (alen == 0)
	return S_INVALID_TYPE;
    if (len < alen + 6)
	return S_TOO_SHORT;
    if (count > 37)
	return S_TOO_LONG;
    if (len != count * 2 + 4)
	return S_INVALID_LEN;

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
	return S_INVALID_CKSUM;

    return S_OK;
}

int
srec_encode(srec_t *srec, char *line)
{
    int alen, count, sum = 0, pos;

    if (srec->type > 9)
	return S_INVALID_TYPE;
    alen = ltab[srec->type];
    if (alen == 0)
	return S_INVALID_TYPE;

    line += sprintf(line, "S%d", srec->type);

    if (srec->count > 32)
	return S_TOO_LONG; 
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

    return S_OK;
}

char *
srec_strerror (int error)
{
    switch (error) {
    case S_OK: return "no error";
    case S_NULL: return "null string error";
    case S_INVALID_HDR: return "invalid header";
    case S_INVALID_CHAR: return "invalid character";
    case S_TOO_SHORT: return "line too short";
    case S_TOO_LONG: return "line too long";
    case S_INVALID_LEN: return "length error";
    case S_INVALID_CKSUM: return "checksum error";
    default: return "unknown error";
    }
}

/*** fastdl.h ***/

/* Image file generated from fastdl.srec by mkimg. */

int fastdl_len = 88;
unsigned char fastdl_image[] = {
    121,  6,  0, 15,107,134,238,128,121,  6,238,100,109,246,121,  6,
    238,116, 94,  0, 59,154, 11,135,121,  6,238, 94, 94,  0,  6,136,
    127,216,114, 80,254,103, 62,217, 24,238,106,142,239, 81,254,  2,
    106,142,239,  6,254, 13,106,142,238, 94, 84,112, 68,111, 32,121,
    111,117, 32, 98,121,116,101, 44, 32,119,104,101,110, 32, 73, 32,
    107,110,111, 99,107, 63,  0,  0,
};
unsigned short fastdl_start = 0x8000;

/*** firmdl.c ***/

/* Global variables */

char *progname;

/* Defines */

#define RETRIES         5
#define WAKEUP_TIMEOUT  1000

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

int
srec_load (char *name, unsigned char *image, int maxlen, unsigned short *start)
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
	    if (error != S_INVALID_CKSUM) {
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

void
image_dl (int fd, unsigned char *image, int len, unsigned short start,
	  int use_comp)
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
    sleep_us (10000);
    if (rcx_sendrecv(fd, send, 6, recv, 2, MOST_TIMEOUT, RETRIES, use_comp) != 2) {
	fprintf(stderr, "%s: start firmware download failed\n", progname);
	exit(1);
    }

    /* Transfer data */
    addr = 0;
    index = 1;
    for (addr = 0, index = 1; addr < len; addr += size, index++) {
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
	sleep_us (20000);
    }

    /* Unlock firmware */
    send[0] = 0xa5;
    send[1] = 76;
    send[2] = 69;
    send[3] = 71;
    send[4] = 79;
    send[5] = 174;

    /* Use longer timeout so ROM has time to checksum firmware */
    if (rcx_sendrecv(fd, send, 6, recv, 26, 150, RETRIES, use_comp) != 26) {
	fprintf(stderr, "%s: unlock firmware failed\n", progname);
	exit(1);
    }
}

int
main (int argc, char **argv)
{
    unsigned char image[IMAGE_MAXLEN];
    unsigned short image_start;
    unsigned int image_len;
    char *tty = NULL;
    char *fileName = NULL;
    char *dname = NULL;
    int use_fast = 0;
    int usage = 0;
    int fd;
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

    //printf ("program = %s\n", progname);
    //printf ("dirname = %s\n", dname);
     
    /* Parse command line */

    #if TRACE
    printf ("Parsing command line\n");
    #endif

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
      printf ("Downloading default file: %s ...\n", fileName);
    } else if (argc == 1) {
      fileName = argv[0];
    } else {
      usage = 1;
    }

    if (usage) {
	char *usage_string =
	    "      --debug      show debug output, mostly raw bytes\n"
	    "  -f, --fast       use fast 4x downloading\n"
	    "  -s, --slow       use slow 1x downloading (DEFAULT)\n"
	    "      --tty=TTY    assume tower connected to TTY\n"
	    "  -h, --help       display this help and exit\n"
	    ;

	fprintf(stderr, "usage: %s [options] [filename]\n", progname);
	fprintf(stderr, usage_string);
	exit(1);
    }

    /* Load the s-record file */

    #if TRACE
    printf ("Loading srec file\n");
    #endif

    image_len = srec_load(fileName, image, IMAGE_MAXLEN, &image_start);

    /* Get the tty name */

    if (!tty)
	tty = getenv("RCXTTY");
    if (!tty) {
      fprintf (stderr, "RCXTTY undefined. It should be something like %s.\n", DEFAULTTTY);
      exit (1);
    }	

    if (use_fast) {
	/* Try to wake up the tower in fast mode */

	fd = rcx_init(tty, 1);
	
	#if TRACE
	printf ("Waking up tower in fast mode\n");
	#endif
	
	if ((status = rcx_wakeup_tower(fd, WAKEUP_TIMEOUT)) < 0) {
	    fprintf(stderr, "%s: %s\n", progname, rcx_strerror(status));
	    exit(1);
	}

	#if TRACE
	printf ("Checking if RCX is alive\n");
	#endif
	
	/* Check if already alive in fast mode */
	if (!rcx_is_alive(fd, 0)) {
	    /* Not alive in fast mode, download fastdl in slow mode */
	    
            #if TRACE
	    printf ("RCX not alive\n");
	    #endif
	    
	    close(fd);
	    fd = rcx_init(tty, 0);

	    if (!rcx_is_alive(fd, 1)) {
		fprintf(stderr, "%s: no response from rcx\n", progname);
		exit(1);
	    }

	    image_dl(fd, fastdl_image, fastdl_len, fastdl_start, 1);
	    
	    /* Go back to fast mode */
	    close(fd);
	    fd = rcx_init(tty, 1);
	}
	
	#if TRACE
	printf ("Transfering image\n");
	#endif
	
	/* Download image in fast mode */
	
	image_dl(fd, image, image_len, image_start, 0);
	close(fd);
    }	
    else {
	/* Try to wake up the tower in slow mode */

        #if TRACE
        printf ("Opening tty\n");
        #endif

	fd = rcx_init(tty, 0);
       
        #if TRACE
        printf ("Opening tower in slow mode\n");
        #endif

	if ((status = rcx_wakeup_tower(fd, WAKEUP_TIMEOUT)) < 0) {
	    fprintf(stderr, "%s: %s\n", progname, rcx_strerror(status));
	    exit(1);
	}

        #if TRACE
        printf ("Checking if RCX is alive\n");
        #endif

	if (!rcx_is_alive(fd, 1)) {
	    /* See if alive in fast mode */
	    
	    close(fd);
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
	
        #if TRACE
        printf ("Downloading image\n");
        #endif

	/* Download image */
	image_dl(fd, image, image_len, image_start, 1);
	
	close(fd);
    }

    return 0;
}

