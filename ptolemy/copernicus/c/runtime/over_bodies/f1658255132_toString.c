/*******************************************************************
Dummy C code for overridden method 
<java.lang.Throwable: java.lang.String toString()>

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
@author Ankush Varma 
@version $Id$
*/

/* FIXME: Does not display error message thrown. Need to enable
 * dependencies of native methods to do so.
*/

char* name = instance->class->name;
int length = strlen(name) + 1; /* Because   of trailing \0 */
i1195259493_String string = malloc(sizeof(struct i1195259493_String));
string->class = &Vi1195259493_String;
string->f01486352994_count = length;
string->f1860107401_value = pccg_array_allocate(
        (PCCG_CLASS_PTR)malloc(sizeof(PCCG_ARRAY_char_elem)), 
        sizeof(char), 1, 1, length);

strcpy(string->f1860107401_value->array_data, name);

return string;


