/*
 *  rcx_comm.c
 *
 *  RCX communication routines.
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

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <termios.h>
#include <sys/time.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>

#if defined(_WIN32)
  #include <windows.h>
#endif

#include "rcx_comm.h"

/* Defines */

#define BUFFERSIZE  4096

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

void myperror(char *str) {
#if defined(_WIN32)
    fprintf(stderr, "Error %lu: %s\n", (unsigned long) GetLastError(), str);
#else
    perror(str);
#endif
}

/* Timeout read routine */

static int nbread (FILEDESCR fd, void *buf, int maxlen, int timeout)
{
    char *bufp = (char *)buf;
    int len = 0;

    while (len < maxlen) {

#if defined(_WIN32)
	DWORD count;
	COMMTIMEOUTS CommTimeouts;

	GetCommTimeouts (fd, &CommTimeouts);

        // Change the COMMTIMEOUTS structure settings.
        CommTimeouts.ReadIntervalTimeout = MAXDWORD;
        CommTimeouts.ReadTotalTimeoutMultiplier = 0;
        CommTimeouts.ReadTotalTimeoutConstant = timeout;
        CommTimeouts.WriteTotalTimeoutMultiplier = 10;
        CommTimeouts.WriteTotalTimeoutConstant = 1000;

        // Set the time-out parameters for all read and write operations
        // on the port.
        SetCommTimeouts(fd, &CommTimeouts);

        if (ReadFile(fd, &bufp[len], maxlen - len, &count, NULL) == FALSE) {
            myperror("ReadFile");
	    fprintf(stderr, "nb_read - error reading tty: %lu\n", (unsigned long) GetLastError());
	    exit(1);
	}

        len += count;

        if (count == 0) {
            //timeout
	    break;
        }
#else
	int count;
	fd_set fds;
	struct timeval tv;

	FD_ZERO(&fds);
	FD_SET(fd, &fds);

	tv.tv_sec = timeout / 1000;
	tv.tv_usec = (timeout % 1000) * 1000;

	if (select(fd+1, &fds, NULL, NULL, &tv) < 0) {
	    perror("select");
	    exit(1);
	}

	if (!FD_ISSET(fd, &fds))
	    break;

	if ((count = read(fd, &bufp[len], maxlen - len)) < 0) {
	    perror("read");
	    exit(1);
	}

        len += count;
#endif

    }

    return len;
}

/* discard all characters in the input queue of tty */
static void rx_flush(FILEDESCR fd)
{
#if defined(_WIN32)
    PurgeComm(fd, PURGE_RXABORT | PURGE_RXCLEAR);
#else
    char echo[BUFFERSIZE];
    nbread(fd, echo, BUFFERSIZE, 200);
#endif
}

int mywrite(FILEDESCR fd, const void *buf, size_t len) {
#if defined(_WIN32)
    DWORD nBytesWritten=0;
    WriteFile(fd, buf, len, &nBytesWritten, NULL);
    return nBytesWritten;
#else
    return write(fd, buf, len);
#endif
}

/* RCX routines */

FILEDESCR rcx_init(char *tty, int is_fast)
{
    FILEDESCR fd;

#if defined(_WIN32)
    DCB dcb;
#else
    struct termios ios;
#endif

    if (__comm_debug) printf("mode = %s\n", is_fast ? "fast" : "slow");

#if defined(_WIN32)
    if ((fd = CreateFile(tty, GENERIC_READ | GENERIC_WRITE,
                              0, NULL, OPEN_EXISTING,
                              0, NULL)) == INVALID_HANDLE_VALUE) {
	fprintf(stderr, "Error %lu: Opening %s\n", (unsigned long) GetLastError(), tty);
	exit(1);
    }

    // Serial settings
    FillMemory(&dcb, sizeof(dcb), 0);
    if (!GetCommState(fd, &dcb)) {	// get current DCB
        // Error in GetCommState
	myperror("GetCommState");
        exit(1);
    } else {
	dcb.ByteSize = 8;
	dcb.Parity   = (is_fast ? 0 : 1);	// 0-4=no,odd,even,mark,space
	dcb.StopBits = 0;			// 0,1,2 = 1, 1.5, 2
	dcb.fBinary  = TRUE ;
	dcb.fParity  = (is_fast ? FALSE : TRUE) ;
	dcb.fAbortOnError = FALSE ;
	dcb.BaudRate = (is_fast ? CBR_4800 : CBR_2400);	// Update DCB rate.

	// Set new state.
	if (!SetCommState(fd, &dcb)) {
	    // Error in SetCommState. Possibly a problem with the communications
            // port handle or a problem with the DCB structure itself.
	    myperror("SetCommState");
	    exit(1);
        }

    }

#else

    if ((fd = open(tty, O_RDWR)) < 0) {
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
#endif

    return fd;
}

void rcx_close(FILEDESCR fd)
{
#if defined(_WIN32)
    CloseHandle(fd);
#else
    close(fd);
#endif
}

int rcx_wakeup_tower (FILEDESCR fd, int timeout)
{
    char msg[] = { 0x10, 0xfe, 0x10, 0xfe };
    char keepalive = 0xff;
    char buf[BUFFERSIZE];
    timeval_t timer;
    int count = 0;
    int len;

    // First, I send a KeepAlive Byte to settle IR Tower...
    mywrite(fd, &keepalive, 1);
    usleep(20000);
    rx_flush(fd);

    timer_reset(&timer);

    do {
	if (__comm_debug) {
	    printf("writelen = %d\n", sizeof(msg));
	    hexdump("W", msg, sizeof(msg));
	}
	if (mywrite(fd, msg, sizeof(msg)) != sizeof(msg)) {
	    myperror("write");
	    exit(1);
	}
	count += len = nbread(fd, buf, BUFFERSIZE, 50);
	if (len == sizeof(msg) && !memcmp(buf, msg, sizeof(msg)))
	    return RCX_OK; /* success */
	if (__comm_debug) {
	    printf("recvlen = %d\n", len);
	    hexdump("R", buf, len);
	}
	rx_flush(fd);
    } while (timer_read(&timer) < (float)timeout / 1000.0f);

    if (!count)
	return RCX_NO_TOWER; /* tower not responding */
    else
	return RCX_BAD_LINK; /* bad link */
}

/* Hexdump routine */

#define LINE_SIZE   16
#define GROUP_SIZE  4
#define UNPRINTABLE '.'

void hexdump(char *prefix, void *buf, int len)
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


int rcx_send (FILEDESCR fd, void *buf, int len, int use_comp)
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

    if (mywrite(fd, msg, msglen) != msglen) {
	myperror("write");
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
	rx_flush(fd);
	return RCX_BAD_ECHO;
    }

    return len;
}

int rcx_recv (FILEDESCR fd, void *buf, int maxlen, int timeout, int use_comp)
{
    char *bufp = (char *)buf;
    unsigned char msg[BUFFERSIZE];
    int msglen;
    int sum;
    int pos;
    int len;

    /* Receive message */

    msglen = nbread(fd, msg, BUFFERSIZE, timeout);

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

	/* Failed.  Possibly a 0xff byte queued message? (legos unlock firmware) */
	for (sum = 0, len = 0, pos = 3; pos < msglen - 2; pos++) {
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

int rcx_sendrecv (FILEDESCR fd, void *send, int slen, void *recv, int rlen,
		  int timeout, int retries, int use_comp)
{
    int status = 0;

    if (__comm_debug) printf("sendrecv %d:\n", slen);

    while (retries--) {
	if ((status = rcx_send(fd, send, slen, use_comp)) < 0) {
	    if (__comm_debug) printf("status = %s\n", rcx_strerror(status));
	    continue;
	}
	if ((status = rcx_recv(fd, recv, rlen, timeout, use_comp)) < 0) {
	    if (__comm_debug) printf("status = %s\n", rcx_strerror(status));
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

int rcx_is_alive (FILEDESCR fd, int use_comp)
{
    unsigned char send[1] = { 0x10 };
    unsigned char recv[1];

    return (rcx_sendrecv(fd, send, 1, recv, 1, 50, 5, use_comp) == 1);
}

char *rcx_strerror (int error)
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

