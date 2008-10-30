;******************************************************************************
;
; cpu.S - Instruction wrappers for special CPU instructions needed by the
;         drivers.
;
; Copyright (c) 2006-2007 Luminary Micro, Inc.  All rights reserved.
; 
; Software License Agreement
; 
; Luminary Micro, Inc. (LMI) is supplying this software for use solely and
; exclusively on LMI's microcontroller products.
; 
; The software is owned by LMI and/or its suppliers, and is protected under
; applicable copyright laws.  All rights are reserved.  You may not combine
; this software with "viral" open-source software in order to form a larger
; program.  Any use in violation of the foregoing restrictions may subject
; the user to criminal sanctions under applicable laws, as well as to civil
; liability for the breach of the terms and conditions of this license.
; 
; THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
; OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
; MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
; LMI SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
; CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
; 
; This is part of revision 1900 of the Stellaris Peripheral Driver Library.
;
;******************************************************************************

;******************************************************************************
;
; The assembly code preamble required to put the assembler into the correct
; configuration.
;
;******************************************************************************
    thumb
    require8
    preserve8

;******************************************************************************
;
; The entire contents of this file go into the code section.
;
;******************************************************************************
    area    ||.text||, code, readonly, align=2

;******************************************************************************
;
; Wrapper function for the CPSID instruction.  Returns the state of PRIMASK
; on entry.
;
;******************************************************************************
    export CPUcpsid
CPUcpsid
    mrs     r0, PRIMASK
    cpsid   i
    bx      lr

;******************************************************************************
;
; Wrapper function for the CPSIE instruction.  Returns the state of PRIMASK
; on entry.
;
;******************************************************************************
    export CPUcpsie
CPUcpsie
    mrs     r0, PRIMASK
    cpsie   i
    bx      lr

;******************************************************************************
;
; Wrapper function for the WFI instruction.
;
;******************************************************************************
    export CPUwfi
CPUwfi
    wfi
    bx      lr

    end
