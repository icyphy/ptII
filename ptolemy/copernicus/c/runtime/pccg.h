/*
Header file for the Ptolemy C code generator PCCG).

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


#ifndef _pccg_h
#define _pccg_h

/* Boolean data type, and associated constants */
#define boolean int
#define false 0
#define true 1

/* Null pointer */
#define null ((void *)0)

/** Common data for structures that implement classes.
 *  Caution: this must be kept consistent with the generated
 *  code. In particular, the sequence of fields (order, names, and types)
 *  must match the sequence in the generated class descriptors.
 */
typedef struct _pccg_class {

    struct _pccg_class *superclass;

    /* other class-specific information follows */

} PCCG_CLASS;


/** Common data for structures that implement class instances.
 *  Caution: this must be kept consistent with the generated
 *  code. In particular, the sequence of fields (order, names, and types)
 *  must match the sequence in the generated class instances.
 */
typedef struct {
    
    PCCG_CLASS *class;
    
    /* other instance-specific information follows */

} PCCG_CLASS_INSTANCE; 

typedef PCCG_CLASS *PCCG_CLASS_PTR;
typedef PCCG_CLASS_INSTANCE *PCCG_CLASS_INSTANCE_PTR;

#endif
