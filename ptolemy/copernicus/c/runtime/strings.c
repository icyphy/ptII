/*******************************************************************
Run-time C code generation functionality for translation of arrays.

Copyright (c) 2001-2003 The University of Maryland.

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
********************************************************************/

/*
Run-time C code generation functionality for converting character arrays to
Strings

@author Ankush Varma 
@version $Id$
*/
#include <string.h>
#include <stdlib.h>

#include "strings.h"


#ifndef A_DEF_iA1_char
#define A_DEF_iA1_char
typedef PCCG_ARRAY_INSTANCE_PTR iA1_char;
#endif

/* Convert a character array to a string Structure so that it can be used by
   string constants.
 */
_STRING_INSTANCE_STRUCT charArrayToString(char *charArray)
{
    _STRING_INSTANCE_STRUCT s; /* dummy string structure */
    iA1_char charArrayStruct;
  
    s = (_STRING_INSTANCE_STRUCT)malloc(sizeof(struct _STRING_INSTANCE_STRUCT));
    /* Initialize charArrayStruct.
       FIXME: "class" not initialized in charArrayStruct.
     */
    charArrayStruct = malloc(sizeof(PCCG_ARRAY_INSTANCE));
    charArrayStruct->array_data = charArray ; 
    /*FIXME: array_length should be a COPY of charArray. memcpy may be
      needed.
     */
    charArrayStruct->array_length = strlen(charArray);
    charArrayStruct->element_size = sizeof(char);
   
    /* Set the fields of the string structure. */
    s->class = &_STRING_CLASS_STRUCT;
    s->f1860107401_value = charArrayStruct;
    s->f01486352994_count = charArrayStruct->array_length;

    /* FIXME: Other fields of String are unitialized. */
    
    return s;
    
}
