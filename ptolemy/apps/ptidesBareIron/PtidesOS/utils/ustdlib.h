//*****************************************************************************
//
// ustdlib.h - Prototypes for simple standard library functions.
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

#ifndef __USTDLIB_H__
#define __USTDLIB_H__

#include <stdarg.h>

#ifdef __cplusplus
extern "C"
{
#endif

//*****************************************************************************
//
// A structure that contains the broken down date and time.
//
//*****************************************************************************
typedef struct
{
    //
    // The number of years since 0 AD.
    //
    unsigned short usYear;

    //
    // The month, where January is 0 and December is 11.
    //
    unsigned char ucMon;

    //
    // The day of the month.
    //
    unsigned char ucMday;

    //
    // The day of the week, where Sunday is 0 and Saturday is 6.
    //
    unsigned char ucWday;

    //
    // The number of hours.
    //
    unsigned char ucHour;

    //
    // The number of minutes.
    //
    unsigned char ucMin;

    //
    // The number of seconds.
    //
    unsigned char ucSec;
}
tTime;

//*****************************************************************************
//
// Prototypes for the APIs.
//
//*****************************************************************************
extern int uvsnprintf(char *pcBuf, unsigned long ulSize, const char *pcString,
                      va_list vaArgP);
extern int usprintf(char *, const char *pcString, ...);
extern int usnprintf(char *pcBuf, unsigned long ulSize, const char *pcString,
                     ...);
extern void ulocaltime(unsigned long ulTime, tTime *psTime);
extern unsigned long ustrtoul(const char *pcStr, const char **ppcStrRet,
                              int iBase);

#ifdef __cplusplus
}
#endif

#endif // __USTDLIB_H__
