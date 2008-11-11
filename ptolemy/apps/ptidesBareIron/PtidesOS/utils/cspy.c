//*****************************************************************************
//
// cspy.c - Routines for accessing the semi-hosting capabilities of C-Spy.
//
// Copyright (c) 2005-2007 Luminary Micro, Inc.  All rights reserved.
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

#define _DLIB_FILE_DESCRIPTOR 1
#include <stdio.h>
#include <yfuns.h>
#include "diag.h"

//*****************************************************************************
//
// Open a handle for stdio functions (both stdin and stdout).
//
//*****************************************************************************
int
DiagOpenStdio(void)
{
    return(_LLIO_STDOUT);
}

//*****************************************************************************
//
// Open a host file system file.
//
//*****************************************************************************
int
DiagOpen(const char *pcName, int iMode)
{
    static const int piMap[12] = { 0x4000, 0x8000, 0x4002, 0x8002,
                                   0x4301, 0x8301, 0x4302, 0x8302,
                                   0x4109, 0x8109, 0x410a, 0x810a };

    iMode = piMap[iMode];
    return(__open(pcName, iMode));
}

//*****************************************************************************
//
// Close a host file system file.
//
//*****************************************************************************
int
DiagClose(int iHandle)
{
    return(__close(iHandle));
}

//*****************************************************************************
//
// Write data to a host file system file.
//
//*****************************************************************************
int
DiagWrite(int iHandle, const char *pcBuf, unsigned long ulLen, int iMode)
{
    return(__dwrite(iHandle, (const unsigned char *)pcBuf, ulLen));
}

//*****************************************************************************
//
// Read data from a host file system file.
//
//*****************************************************************************
int
DiagRead(int iHandle, char *pcBuf, unsigned long ulLen, int iMode)
{
    if(iHandle == _LLIO_STDOUT)
    {
        iHandle = _LLIO_STDIN;
    }

    return(__read(iHandle, (unsigned char *)pcBuf, ulLen));
}

//*****************************************************************************
//
// Get the length of a host file system file.
//
//*****************************************************************************
long
DiagFlen(int iHandle)
{
    long lPos, lLen;

    lPos = __lseek(iHandle, 0, SEEK_CUR);
    lLen = __lseek(iHandle, 0, SEEK_END);
    __lseek(iHandle, lPos, SEEK_SET);
    return(lLen);
}

//*****************************************************************************
//
// Terminate the application.
//
//*****************************************************************************
void
DiagExit(int iRet)
{
    __exit(iRet);
}

//*****************************************************************************
//
// Get the command line arguments from the debugger.
//
//*****************************************************************************
char *
DiagCommandString(char *pcBuf, unsigned long ulLen)
{
    return(0);
}
