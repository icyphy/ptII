/* TOKEN type, the general super-type of all the token types.

 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @author Thomas Huining Feng
 
 */

#ifndef TOKEN_H_
#define TOKEN_H_

#include "types.h"

/**
 * TOKEN type, the general super-type of all the token types.
 */
typedef struct TOKEN {
	/* TOKEN type is directly inherited from GENERAL_TYPE. */
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	/* The previous token in a token list. */
	struct PORT* prev;
	/* The next token in a token list. */
	struct PORT* next;
} TOKEN;

/**
 * Initiate an object of the TOKEN type.
 * 
 * @param token Reference to the TOKEN object to be initiated.
 * @param actual_ref The actual reference to the object.
 */
void TOKEN_init(TOKEN* token, void* actual_ref);

#endif /*TOKEN_H_*/
