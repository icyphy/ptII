#include <stdio.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <errno.h>

#include "ptpHwP1000LinuxDr.h"
#include "p1000_utils.h"

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
