//*****************************************************************************
//
// globals.h - Shared configuration and global variables.
//
// Copyright (c) 2007 Luminary Micro, Inc.  All rights reserved.
// 
// Software License Agreement
// 
// Luminary Micro, Inc. (LMI) is supplying this software for use solely and
// exclusively on LMI's microcontroller products.
// 
// The software is owned by LMI and/or its suppliers, and is protected under
// applicable copyright laws.  All rights are reserved.  You may not combine
// this software with "viral" open-source software in order to form a larger
// program.  Any use in violation of the foregoing restrictions may subject
// the user to criminal sanctions under applicable laws, as well as to civil
// liability for the breach of the terms and conditions of this license.
// 
// THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
// OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
// LMI SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
// CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
// 
// This is part of revision 1900 of the Stellaris Peripheral Driver Library.
//
//*****************************************************************************

#ifndef __GLOBALS_H__
#define __GLOBALS_H__

//*****************************************************************************
//
// A set of flags used to track the state of the application.
//
//*****************************************************************************
extern volatile unsigned long g_ulFlags;

//
// Flags indicating that some action needs to be taken.
//
#define FLAG_SYSTICK            0   // A system tick has occurred.
#define FLAG_RXPKT              1   // An ethernet packet has been received.
#define FLAG_TXPKT              2   // An ethernet packet has been transmitted.
#define FLAG_PPSOUT             3   // PPS Output is on.
#define FLAG_PPSOFF             4   // PPS Output should be turned off.
#define FLAG_IPADDR             5   // An IP Address has been assigned.
#define FLAG_PTPDINIT           6   // PTPd has been initialized.

//
// Flags indicating capabilities or operating modes.
//
#define FLAG_HWTIMESTAMP        7   // Using hardware ethernet timestamping.

//
// Mask used to separate mode flags from action flags.
//
#define MODE_FLAG_MASK ((1 << FLAG_SYSTICK) |                              \
                        (1 << FLAG_RXPKT) |                                \
                        (1 << FLAG_TXPKT) |                                \
                        (1 << FLAG_PPSOUT) |                               \
                        (1 << FLAG_PPSOFF) |                               \
                        (1 << FLAG_IPADDR) |                               \
                        (1 << FLAG_PTPDINIT))

#endif // __GLOBALS_H__
