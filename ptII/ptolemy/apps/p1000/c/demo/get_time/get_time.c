/* A demo that simply gets time from the device, and outputs it.

 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @author Thomas Huining Feng
 
 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <errno.h>

#include "ptpHwP1000LinuxDr.h"
#include "p1000_utils.h"

/**
 * The main method of the get_time demo, which reads the device for the current
 * time, converts it, and outputs the result to stdout.
 */
int main() {
	int fd;
	char *devFile = "/dev/ptpHwP1000LinuxDr";
	FPGA_GET_TIME fpgaGetTime;
	int rtn;
	unsigned int secs;
	unsigned int nsecs;

	fd = open(devFile, O_RDWR);
	rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
	if (rtn) {
		fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
		perror("error from ioctl");
		exit(1);
	}
	decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
	printf("%d %d\n", secs, nsecs);
}
