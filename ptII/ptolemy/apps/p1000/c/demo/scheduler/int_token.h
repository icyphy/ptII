/* IntToken, a token that contains an integer as its value.

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
 * IntToken, a token that contains an integer as its value.
 * 
 * FIXME: int in C has different meanings, depending on the target machine.
 */
typedef struct IntToken {
	/* IntToken is directly inherited from Token. */
	Token super;
	
	/* The value in the token. */
	int value;
} IntToken;

/*
 * IntToken's static type data.
 */
typedef struct IntToken_TypeData {
	Token_TypeData inheritedTypeData;
} IntToken_TypeData;

extern IntToken_TypeData IntToken_typeData;

/*
 * Initiate IntToken_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void IntToken_TypeData_init(IntToken_TypeData* type_data);

/**
 * Initiate an int token.
 * 
 * @param int_token The int token to be initiated.
 * @param actual_type_data The type data of the int token's actual type, or
 *  NULL. When NULL is given (which is usually the case when called by the
 *  user), IntToken_typeData is used.
 */
void IntToken_init(IntToken* int_token, IntToken_TypeData* actual_type_data);

#endif /*INT_TOKEN_H_*/
