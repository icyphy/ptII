/* INT_TOKEN, a token that contains an integer as its value.

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

#ifndef INT_TOKEN_H_
#define INT_TOKEN_H_

#include "types.h"
#include "token.h"

/**
 * INT_TOKEN type.
 * 
 * FIXME: C type's int has different meanings, depending on the target machine.
 */
typedef struct INT_TOKEN {
	/* INT_TOKEN type is directly inherited from TOKEN. */
	DECLARE_SUPER_TYPE(TOKEN)
	
	/* The value in the token. */
	int value;
} INT_TOKEN;

/**
 * Initiate an object of the INT_TOKEN type.
 * 
 * @param int_token Reference to the INT_TOKEN object to be initiated.
 * @param actual_ref The actual reference to the object.
 */
void INT_TOKEN_init(INT_TOKEN* int_token, void* actual_ref);

#endif /*INT_TOKEN_H_*/
