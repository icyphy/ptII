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

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "stack.h"


Stack* stackNew( int initialSize, int inc ) {
        Stack* s = (Stack*) malloc( sizeof( Stack ) );
        s->stack = NULL;
        s->stackSize = 0;
        s->stackPos = -1;
        s->initialSize = initialSize;
        s->inc = inc;
        return s;
}


int stackIsEmpty( Stack* s ) {
        return s->stackPos == -1;
}


// add an element to stack and grow stack if required
// returns 1 to indicate success and 0 for error
int stackPush( Stack* s, void* e ) {
        s->stackPos++;
        if ( s->stackPos==s->stackSize ) {
                s->stackSize += ( s->stack ? s->inc: s->initialSize );
                s->stack = (void**) realloc( s->stack, s->stackSize * sizeof(void*) );
                if ( !s->stack ) return 0; // error;
        }
        s->stack[s->stackPos] = e;
        return 1; // success
}


// return top element (possibly NULL), if stack not empty
// runtime error if stack is empty
void* stackPeek( Stack* s ){
        assert( !stackIsEmpty( s ) );
        return s->stack[s->stackPos];
}


// remove top element (possibly NULL) from stack and return it
// runtime error if stack is empty
void* stackPop( Stack* s ) {
        assert( !stackIsEmpty( s ) );
        return s->stack[s->stackPos--];
}


// return the last n elements as null terminated array,
// or NULL if memory allocation fails
void** stackLastPopedAsArray0( Stack* s, int n ) {
        int i;
        void** array = (void**) malloc( ( n + 1 )*sizeof( void* ) );
        if ( !array ) return NULL; // failure
        for ( i=0; i<n; ++i ) {
                array[i] = s->stack[i+ s->stackPos + 1];
        }
        array[n]=NULL; // terminating NULL
        return array;
}


// return stack as possibly empty array, or NULL if memory allocation fails
// On sucessful return, the stack is empty.
void** stackPopAllAsArray( Stack* s, int *size ) {
        int i;
        void** array = (void**) malloc( ( s->stackPos + 1 )*sizeof( void* ) );
        if ( !array ) return NULL; // failure
        *size = s->stackPos + 1;
        for ( i=0; i<*size; ++i )
                array[i] = s->stack[i];
        s->stackPos = -1;
        return array;
}


// release the given stack
void stackFree( Stack* s ){
        if ( s->stack ) free( s->stack );
        free( s );
}
