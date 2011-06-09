// Example program to load the FPGA.
// Caution: This is automatically handled by the IEEE1588 application
//
// Contact: Jeff Burch,  jeff_burch@agilent.com
//
#include <stdio.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/mman.h>

#include <errno.h>

#include "ptpHwP1000LinuxDr.h"

int main(int argc, char* argv[])
{
    int fd;
    int r;
    int f;
    long fileLen;
    char* fileData;
    FPGA_LOAD fls;
    int memfd;
    int* fpgaMem;


    if (argc != 3)
        {
            printf("%s error: need xilinx file and device file as parameters\n", argv[
                           0]);
            return -1;
        }

    char *xilinxFile = argv[1];
    char *deviceFile = argv[2];

    // Open fpga image file
    fd = open(xilinxFile, O_RDONLY);
    if (fd < 0)
        {
            printf("error opening xilinx bit file \"%s\"\n", xilinxFile);
            exit(1);
        }

    fileLen = lseek(fd, 0, SEEK_END);
    if (fileLen == -1)
        {
            perror("fpga seek");
            exit(1);
        }
    lseek(fd, 0, 0);

    fileData = (char*)malloc(fileLen);
    if (!fileData)
        {
            fprintf(stderr, "Malloc failed: Out of memory?\n");
            exit(1);
        }

    r = read(fd, fileData, fileLen);
    if (r != fileLen)
        {
            fprintf(stderr, "Read from image file failed: %d\n", r);
            exit(1);
        }
    close(fd);

    fls.load_bytes = fileLen;
    fls.load_data = fileData;

    // Open the fpga device
    f = open(deviceFile, O_RDONLY);
    if (f < 0)
        {
            printf("error opening device file \"%s\"\n", deviceFile);
            exit(1);
        }

    r = ioctl(f, FPGA_IOC_LOAD, &fls);
    if (r)
        {
            fprintf(stderr, "Loading fpga failed: %d, %d\n", r, errno);
            perror("ioctl");
            exit(1);
        }

    // Now, try accessing the thing!!
    memfd = open("/dev/mem", O_RDWR);
    if (memfd == -1)
        {
            fprintf(stderr, "Error opening system memory\n");
            perror("memory open");
            exit(1);
        }
    r = read(fd, fileData, fileLen);
    if (r != fileLen)
        {
            fprintf(stderr, "Read from image file failed: %d\n", r);
            exit(1);
        }
    close(fd);

    fls.load_bytes = fileLen;
    fls.load_data = fileData;

    // Open the fpga device
    f = open(deviceFile, O_RDONLY);
    if (f < 0)
        {
            printf("error opening device file \"%s\"\n", deviceFile);
            exit(1);
        }

    r = ioctl(f, FPGA_IOC_LOAD, &fls);
    if (r)
        {
            fprintf(stderr, "Loading fpga failed: %d, %d\n", r, errno);
            perror("ioctl");
            exit(1);
        }

    // Now, try accessing the thing!!
    memfd = open("/dev/mem", O_RDWR);
    if (memfd == -1)
        {
            fprintf(stderr, "Error opening system memory\n");
            perror("memory open");
            exit(1);
        }

    fpgaMem = (int *) mmap(0, 0x1000, PROT_READ | PROT_WRITE,
            MAP_SHARED, memfd, (off_t)FPGA_START);
    if (fpgaMem == (int*)-1)
        {
            perror("fpga mmap");
            exit(1);
        }

    r = *fpgaMem;
    printf("Fpga address 0: 0x%x\n", r);

    exit(0);
}
