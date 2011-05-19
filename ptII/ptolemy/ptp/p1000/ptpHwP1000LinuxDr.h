/*
Copyright (c) 2005, Agilent Technologies, Inc.  All rights reserved.

Redistribution and use in source and binary forms of the Exemplary
1588 Stack and FPGA Code provided with the LXI 1588 Demo Kit, with or
without modification, are permitted provided that the following
conditions are met:
 * Use is only allowed within devices compliant with both the standard
  known as IEEE 1588 (IEEE Standard for a Precision Clock
  Synchronization Protocol for Networked Measurement and Control
  Systems) and those portions of the LXI specification that require
  use of IEEE 1588, copies of which can be retrieved from IEEE
  (http://www.ieee.org) and LXI (http://www.lxistandard.org/home),
  respectively.
 * May not be used in the planning, construction, maintenance or direct
  operation of a nuclear facility, nor for use in on line control or
  fail safe operation of aircraft navigation, control or communication
  systems, weapon systems or direct life support systems.
 * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
 * Neither the name Agilent, Agilent Technologies, Inc. nor the names
  of its contributors may be used to endorse or promote products
  derived from this software without specific prior written
  permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Implementation of IEEE 1588 - Precision Time Protocol (Jan 2006)
 * Contact : Jeff Burch, Bruce Hamilton
 * Email: jeff_burch@agilent.com, bruce_hamilton@agilent.comn
 *
 * Filename: ptpHwP1000LinuxDr.h
 *
 * Linux Device Driver implementation for P1000 Linux design
 */
#ifndef INCLUDE_PTPHWP1000LINUXDR
#define INCLUDE_PTPHWP1000LINUXDR
#ifdef linux
#include <linux/ioctl.h>
#include <linux/types.h>
#endif

#ifdef sun
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/ioccom.h>
#endif

#ifdef __CYGWIN32__
#include <sys/ioctl.h>
#include <sys/types.h>
// Get _IOW
#include <asm/socket.h> 
#endif

#define FPGA_IOC_MAGIC  'f'
// ----------------------------------------------------------------
// Starting address of FPGA registers in physical memory
#define FPGA_START 0x60000000

// ----------------------------------------------------------------
// The FPGA used a HW encoding for nsecs field
// To convert from TAI nsec to HW nsec, multiply by pow(2,30)/1e9
// or 0.93132257461548
typedef struct fpga_time_struct
{
    unsigned int secs;   // TAI seconds for time-trigger
    unsigned int hwNsec; // HW version TAI nsecs for time-trigger
} FPGA_TIME;

// Struct for FPGA_IOC_LOAD
typedef struct fpga_load
{
    int load_bytes;
    char* load_data;
} FPGA_LOAD;

// Struct for FPGA_IOC_SET_TIMETRIGGER
// Set a time trigger at the desired trigger time
// If 'force' is true, override an existing time-trigger
// If secs and nsec == 0, disable time-trigger
typedef struct fpga_set_timetrigger
{
    int num;             // Time-Trigger number (must be 0 for now)
    int force;

    FPGA_TIME timeVal;
} FPGA_SET_TIMETRIGGER;

// Struct for FPGA_IOC_GET_TIMESTAMP
// Get the last time stamp. Returns an empty record if the log is empty
typedef struct fpga_get_timestamp
{
    int num;             // Time-Stamp number (will be 0 for now)
    int seqNum;
    FPGA_TIME timeVal;
} FPGA_GET_TIMESTAMP;

// Struct for FPGA_IOC_SET_TIME
// Set the time for the HW clock. The update occurs at the end of the second
// represented by the "time_update" value.
typedef struct fpga_set_time
{
    unsigned int time_update;   // TAI seconds when time is set
    FPGA_TIME timeVal;     // new time
    FPGA_TIME timeAltVal;  // new time less 10 nsec
} FPGA_SET_TIME;

// Struct for FPGA_IOC_GET_TIME
// Set the time for the HW clock
typedef struct fpga_get_time
{
    FPGA_TIME timeVal; // new time
} FPGA_GET_TIME;

// Struct for FPGA_IOC_CLEAR_TIMESTAMP
// Clear the timestamp log
typedef struct fpga_clear_timestamp
{
    unsigned int dummy;
} FPGA_CLEAR_TIMESTAMP;


// ---------------------- IOCTL Values -------------------------------
// Load the FPGA with the proper Xilinx bit file
#define FPGA_IOC_LOAD _IOW(FPGA_IOC_MAGIC, 1, FPGA_LOAD)

// Set a time trigger
#define FPGA_IOC_SET_TIMETRIGGER _IOW(FPGA_IOC_MAGIC, 2, FPGA_SET_TIMETRIGGER)

// Get a timestamps from the timestamp log. This will return the last timestamp
// when the log is empty
#define FPGA_IOC_GET_TIMESTAMP _IOW(FPGA_IOC_MAGIC, 3, FPGA_GET_TIMESTAMP)

// Set the IEEE1588 HW Clock's time
#define FPGA_IOC_SET_TIME _IOW(FPGA_IOC_MAGIC, 4, FPGA_SET_TIME)

// Set get the IEEE1588 HW Clock's time
#define FPGA_IOC_GET_TIME _IOW(FPGA_IOC_MAGIC, 5, FPGA_GET_TIME)

// Clear any pending timestamps from the log
#define FPGA_IOC_CLEAR_TIMESTAMP _IOW(FPGA_IOC_MAGIC, 6, FPGA_CLEAR_TIMESTAMP)

// ---------------------- read Values -------------------------------
// reading from the device returns a 32-bit status code:
// Status, and Mask register bit definitions

// Get a timestamps from the timestamp log. This will return the last timestamp
// when the log is empty
#define FPGA_IOC_GET_TIMESTAMP _IOW(FPGA_IOC_MAGIC, 3, FPGA_GET_TIMESTAMP)

// Set the IEEE1588 HW Clock's time
#define FPGA_IOC_SET_TIME _IOW(FPGA_IOC_MAGIC, 4, FPGA_SET_TIME)

// Set get the IEEE1588 HW Clock's time
#define FPGA_IOC_GET_TIME _IOW(FPGA_IOC_MAGIC, 5, FPGA_GET_TIME)

// Clear any pending timestamps from the log
#define FPGA_IOC_CLEAR_TIMESTAMP _IOW(FPGA_IOC_MAGIC, 6, FPGA_CLEAR_TIMESTAMP)

// ---------------------- read Values -------------------------------
// reading from the device returns a 32-bit status code:
// Status, and Mask register bit definitions
#define TIME_UPDATE_PENDING     0x00000001
#define IRQ_OUTPUT              0x00000002

#define TIMESTAMP_0_PEND        0x00000010
#define TIMESTAMP_1_PEND        0x00000020
#define TIMESTAMP_2_PEND        0x00000040
#define TIMESTAMP_3_PEND        0x00000080

#define TIMEBOMB_0_PEND         0x00000100
#define TIMEBOMB_1_PEND         0x00000200
#define TIMEBOMB_2_PEND         0x00000400
#define TIMEBOMB_3_PEND         0x00000800

#define EXT_CLK_MISSING         0x00004000

#define TIME_UPDATED            0x00010000
#define TX_CAPTURED             0x00020000
#define RX_CAPTURED             0x00040000
#define PPS_FIRE                0x00080000

#define TIMESTAMP_0_RCV         0x00100000
#define TIMESTAMP_1_RCV         0x00200000
#define TIMESTAMP_2_RCV         0x00400000
#define TIMESTAMP_3_RCV         0x00800000

#define TIMEBOMB_0_FIRE         0x01000000
#define TIMEBOMB_1_FIRE         0x02000000
#define TIMEBOMB_2_FIRE         0x04000000
#define TIMEBOMB_3_FIRE         0x08000000



#endif // INCLUDE_PTPHWP1000LINUXDR
