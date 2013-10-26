/* -------------------------------------------------------------------------
 * stack.c
 * A stack of pointers.
 *
 * Copyright 2010 QTronic GmbH. All rights reserved.
 *
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * -------------------------------------------------------------------------*/

#ifndef _FMIPP_STACK_H
#define _FMIPP_STACK_H


typedef struct {
        void** stack;
        int stackSize;    // allocated size of stack
        int stackPos;     // array index of top element, -1 if stack is empty.
        int initialSize;  // how many element to allocate initially
        int inc;          // how many elements to allocate when stack gets full
} Stack;


Stack* stackNew( int initialSize, int inc );
int stackIsEmpty( Stack* s );
int stackPush( Stack* s, void* e );
void* stackPeek( Stack* s );
void* stackPop( Stack* s );
void** stackPopAllAsArray( Stack* s, int *size );
void** stackLastPopedAsArray0( Stack* s, int n );
void stackFree( Stack* s );


#endif // _FMIPP_STACK_H
