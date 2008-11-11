//*****************************************************************************
//
// gdb.S - Routines for accessing the semi-hosting capabilities of gdb.
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

#include "../asmdefs.h"

//*****************************************************************************
//
// The entire contents of this file go into the code section.
//
//*****************************************************************************
    __TEXT__

//*****************************************************************************
//
// Open a handle for stdio functions (both stdin and stdout).
//
//*****************************************************************************
    __EXPORT__ DiagOpenStdio
    __THUMB_LABEL__
DiagOpenStdio __LABEL__
    movs    r0, #1
    bx      lr

//*****************************************************************************
//
// Open a host file system file.
//
//*****************************************************************************
    __EXPORT__ DiagOpen
    __THUMB_LABEL__
DiagOpen __LABEL__
    push    {r0-r2}
    movs    r1, #0
    __THUMB_LABEL__
strlen_loop __LABEL__
        ldrb    r2, [r0], #1
        cmp     r2, #0
        it      ne
        addne   r1, #1
        bne     strlen_loop
    str     r1, [r13, #0x8]
    movs    r0, #0x1
    mov     r1, r13
    bkpt    0xab
    add     r13, r13, #0xc
    bx      lr

//*****************************************************************************
//
// Close a host file system file.
//
//*****************************************************************************
    __EXPORT__ DiagClose
    __THUMB_LABEL__
DiagClose __LABEL__
    push    {r0}
    movs    r0, #2
    mov     r1, r13
    bkpt    0xab
    add     r13, r13, #0x4
    bx      lr

//*****************************************************************************
//
// Write data to a host file system file.
//
//*****************************************************************************
    __EXPORT__ DiagWrite
    __THUMB_LABEL__
DiagWrite __LABEL__
    push    {r0-r3}
    movs    r0, #0x5
    mov     r1, r13
    bkpt    0xab
    add     r13, r13, #0x10
    bx      lr

//*****************************************************************************
//
// Read data from a host file system file.
//
//*****************************************************************************
    __EXPORT__ DiagRead
    __THUMB_LABEL__
DiagRead __LABEL__
    push    {r0-r3}
    cmp     r0, #1
    itt     eq
    subeq   r0, r0, r0
    streq   r0, [sp]
    movs    r0, #0x6
    mov     r1, r13
    bkpt    0xab
    add     r13, r13, #0x10
    bx      lr

//*****************************************************************************
//
// Get the length of a host file system file.
//
//*****************************************************************************
    __EXPORT__ DiagFlen
    __THUMB_LABEL__
DiagFlen __LABEL__
    push    {r0}
    movs    r0, #0xc
    mov     r1, r13
    bkpt    0xab
    add     r13, r13, #0x4
    bx      lr

//*****************************************************************************
//
// Terminate the application.
//
//*****************************************************************************
    __EXPORT__ DiagExit
    __THUMB_LABEL__
DiagExit __LABEL__
    ldr     r1, =0x00020026
    movs    r0, #0x18
    bkpt    0xab
    b       .

//*****************************************************************************
//
// Get the command line arguments from the debugger.
//
//*****************************************************************************
    __EXPORT__ DiagCommandString
    __THUMB_LABEL__
DiagCommandString __LABEL__
    push    {r0-r1, r4, lr}
    mov     r4, r0
    movs    r0, #0x15
    mov     r1, r13
    bkpt    0xab
    cmp     r0, #0
    ite     eq
    moveq   r0, r4
    movne   r0, #0
    add     r13, r13, #0x8
    pop     {r4, pc}
    nop

    __END__
