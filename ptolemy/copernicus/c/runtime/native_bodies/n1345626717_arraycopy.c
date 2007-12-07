/*******************************************************************
Native code for
java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)

Copyright (c) 2001-2005 The University of Maryland.

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
*********************************************************************/

/*
@author Ankush Varma
@version $Id$
*/
PCCG_ARRAY_INSTANCE_PTR src, dest;
long srcpos, destpos, length;

src = (PCCG_ARRAY_INSTANCE_PTR)n0;
dest = (PCCG_ARRAY_INSTANCE_PTR)n2;
srcpos = n1;
destpos = n3;
length = n4;

/* NULL arrays cannot be copied. If such a copy is requested, do nothing */
if (src == NULL) {
    /*printf("Warning: ArrayCopy asked to copy NULL array");*/
    return;
}

/* check for out of bounds arrays.*/
if (destpos*(dest->element_size) + length*(src->element_size)
        > (dest->array_length)*(dest->element_size)) {
    printf("Error in System.arraycopy(): out of bounds access");
}

/* Because a char is 1 byte. */
memcpy(((char*)dest->array_data) + destpos*dest->element_size,
       ((char*)src ->array_data) + srcpos*src->element_size,
        (src->element_size)*length);

