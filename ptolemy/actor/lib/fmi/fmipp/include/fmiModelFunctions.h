/* Copyright (c) 2008-2009, MODELISAR consortium. All rights reserved.
 *
 * This file is licensed by the copyright holders under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 *
 * ----------------------------------------------------------------------------
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ----------------------------------------------------------------------------
 *
 * with the extension:
 *
 * You may distribute or publicly perform any modification only under the
 * terms of this license.
 *
 * ----------------------------------------------------------------------------*/

#ifndef fmiModelFunctions_h
#define fmiModelFunctions_h


#include "fmiModelTypes.h"
#include <stdlib.h>

/** Make sure all compiler use the same alignment policies for structures. **/
#ifdef WIN32
#pragma pack(push,8)
#endif

/** Type definitions. **/
typedef void  (*fmiCallbackLogger)        ( fmiComponent c, fmiString instanceName, fmiStatus status,
                                            fmiString category, fmiString message, ... );
typedef void* (*fmiCallbackAllocateMemory)( size_t nobj, size_t size );
typedef void  (*fmiCallbackFreeMemory)    ( void* obj );

typedef struct {
        fmiCallbackLogger         logger;
        fmiCallbackAllocateMemory allocateMemory;
        fmiCallbackFreeMemory     freeMemory;
} fmiCallbackFunctions;


#endif // fmiModelFunctions_h
