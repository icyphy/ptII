/*
Header file for PCCG run-time library.

Copyright (c) 2001-2003 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

@author Shuvra S. Bhattacharyya
@version $Id$
*/

#ifndef _runtime_h
#define _runtime_h

#include "pccg.h"
#include <string.h> /* Solaris needs this so that memcpy is defined. */
#include "name_defs.h"
#include "java/lang/Exception.h"
#include <setjmp.h>


/* data to enable exception-catching */
extern jmp_buf env;
extern int epc;
extern _EXCEPTION_INSTANCE exception_id;

/* Macro to simplify "instanceof" calls. */
#define PCCG_instanceof(operand, checkIndex) \
    ((PCCG_CLASS_PTR)operand->class)->instanceOf\
    ((PCCG_CLASS_PTR)operand->class, checkIndex)

/* Function to allocate zeroed-out, garbage-collectible memory */
void* PCCG_malloc(size_t size);

#endif
