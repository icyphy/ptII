// Example program to issue a series of time triggers and read back the
// timestamps
//
// Contact: Jeff Burch,  jeff_burch@agilent.com
//
#include <stdio.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>


#include "ptpHwP1000LinuxDr.h"

// --------------------------------------------------------------------------
// Encode nsec into the HW form
void encodeHwNsec( FPGA_TIME *hwTime,
        const unsigned int secs,
        const unsigned int nsec)
{
    static double nsecToHw = pow(2,30) / 1e9;

    if (hwTime)
        {
            hwTime->hwNsec = (unsigned int) ( nsec * nsecToHw); // 0.93132257461548);
            hwTime->secs = secs;
        }
}

// Decode sec, nsec from the HW form. Deal with even and odd values
void decodeHwNsec(
        const FPGA_TIME *hwTime,
        unsigned int *secs,
        unsigned int *nsec)
{
    static double hwToNsec = 1e9 / pow(2,30);

    if (hwTime)
        {

            *nsec = (unsigned int) ( (hwTime->hwNsec & 0x7fffffff) * hwToNsec); //1.073741824);
    *secs = hwTime->secs;
    if ( hwTime->hwNsec & 0x80000000)
        {
            // Must add 10 nsec
            *nsec += 10;
            if (*nsec >= 1000000000)
                {
                    *secs = *secs + 1;
                    *nsec = *nsec - 1000000000;
                }

        }
}

}

// -----------------------------------------------------
int main(int argc, char* argv[])
{
const double fromHwNsec = pow(2,30)/1e9;

if (argc != 2)
{
    printf("%s error, need device file\n", argv[0]);
    printf("%s /dev/ptpHwP1000LinuxDr\n", argv[0]);
    return -1;
}

 char *devFile = argv[1];

 // Open the fpga device
 int fd = open(devFile, O_RDWR);
 if (fd < 0)
     {
         printf("Error opening device file \"%s\"\n", devFile);
         exit(1);
     }

 // Read the current time from the IEEE1588 clock
 FPGA_GET_TIME fpgaGetTime;
 int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
 if (rtn)
     {
         fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
         exit(1);
     }

 // Scale from HW to TAI nsec
 unsigned int secs;
 unsigned int nsecs;
 decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
 printf("Get Time: %.9d.%9.9d\n", secs, nsecs);
 // Clear the timestamp log
 FPGA_CLEAR_TIMESTAMP fpgaClearTimestamp; // no data needed at this time;
 rtn = ioctl(fd,  FPGA_IOC_CLEAR_TIMESTAMP, &fpgaClearTimestamp);
 if (rtn)
     {
         fprintf(stderr, "ioctl to clear timestamp log failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
         exit(1);
     }

 // Set a time trigger five seconds from now
 secs += 5;
 nsecs = 0;
 FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;

 fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
 fpgaSetTimetrigger.force = 0; // Don't force
    encodeHwNsec( &fpgaSetTimetrigger.timeVal, secs, nsecs);

 rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
 if (rtn)
     {
         fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
         exit(1);
     }


 // Block until the next interrupt
 unsigned int status;
 do
     {
         int num = read( fd, &status, sizeof(status));
         if (num != sizeof( status))
             {
                 fprintf(stderr, "Error reading status, %d\n", num);
                 exit(1);
             }

     } while ((status & TIMEBOMB_0_FIRE) == 0); // Got it!

 // Read all available timestamps
 FPGA_GET_TIMESTAMP fpgaGetTimestamp;
 int lastSeqNum = -1;
 do
     {
         // Read a timestamp from the log
         rtn = ioctl(fd,  FPGA_IOC_GET_TIMESTAMP, &fpgaGetTimestamp);
         if (rtn)
             {
                 fprintf(stderr, "ioctl to get timestamp failed: %d, %d\n", rtn, errno);
                 perror("error from ioctl");
                 exit(1);
             }

         // Stop when empty
         if (fpgaGetTimestamp.seqNum == 0 &&
      fpgaGetTimestamp.timeVal.secs == 0 &&
                 fpgaGetTimestamp.timeVal.hwNsec == 0) break; // done

         // Decode
         decodeHwNsec( &fpgaGetTimestamp.timeVal, &secs, &nsecs);
         printf("Timestamp: %4d %.9d.%9.9d\n", fpgaGetTimestamp.seqNum, secs, nsecs);


         lastSeqNum = fpgaGetTimestamp.seqNum;

     } while (1);

 // Read the current time from the IEEE1588 clock
 rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
 if (rtn)
     {
         fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
         exit(1);
     }

 // Scale from HW to TAI nsec
 decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
 printf("Get Time: %.9d.%9.9d\n", secs, nsecs);

 // Done
 close( fd);
 return 0;
}
