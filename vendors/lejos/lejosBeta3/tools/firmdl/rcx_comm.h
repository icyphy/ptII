/*
 *  rcx_comm.h
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

#ifndef RCX_COMM_H_INCLUDED
#define RCX_COMM_H_INCLUDED

#define RCX_OK             0
#define RCX_NO_TOWER      -1
#define RCX_BAD_LINK      -2
#define RCX_BAD_ECHO      -3
#define RCX_NO_RESPONSE   -4
#define RCX_BAD_RESPONSE  -5

#if defined(_WIN32)
  #define FILEDESCR	HANDLE
  #define BADFILE	NULL
#else
  #define FILEDESCR	int
  #define BADFILE	-1
#endif


/* Get a file descriptor for the named tty, exits with message on error */
extern FILEDESCR rcx_init (char *tty, int is_fast);

/* Close a file descriptor allocated by rcx_init */
extern void rcx_close (FILEDESCR fd);

/* Try to wakeup the tower for timeout ms, returns error code */
extern int rcx_wakeup_tower (FILEDESCR fd, int timeout);

/* Try to send a message, returns error code */
/* Set use_comp=1 to send complements, use_comp=0 to suppress them */
extern int rcx_send (FILEDESCR fd, void *buf, int len, int use_comp);

/* Try to receive a message, returns error code */
/* Set use_comp=1 to expect complements */
/* Waits for timeout ms before detecting end of response */
extern int rcx_recv (FILEDESCR fd, void *buf, int maxlen, int timeout, int use_comp);

/* Try to send a message and receive its response, returns error code */
/* Set use_comp=1 to send and receive complements, use_comp=0 otherwise */
/* Waits for timeout ms before detecting end of response */
extern int rcx_sendrecv (FILEDESCR fd, void *send, int slen, void *recv, int rlen, int timeout, int retries, int use_comp);

/* Test whether or not the rcx is alive, returns 1=yes, 0=no */
/* Set use_comp=1 to send complements, use_comp=0 to suppress them */
extern int rcx_is_alive (FILEDESCR fd, int use_comp);

/* Convert an error code to a string */
extern char *rcx_strerror(int error);

/* Hexdump routine */
extern void hexdump(char *prefix, void *buf, int len);


#endif /* RCX_COMM_H_INCLUDED */

