#ifndef P1000_UTILS_H_
#define P1000_UTILS_H_

#include "ptpHwP1000LinuxDr.h"

// Encode nsec into the HW form
void encodeHwNsec( FPGA_TIME *hwTime,
        const unsigned int secs,
        const unsigned int nsec);

// Decode sec, nsec from the HW form. Deal with even and odd values
void decodeHwNsec(
        const FPGA_TIME *hwTime,
        unsigned int *secs,
        unsigned int *nsec);

#endif /*P1000_UTILS_H_*/
