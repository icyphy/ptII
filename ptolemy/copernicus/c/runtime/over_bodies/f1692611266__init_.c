/*******************************************************************
Dummy C code for overridden method 
<java.lang.String: void <init>()>

Overridden because java.lang.Object.clone() is not implemented yet.

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
    long L_i0;
    i1063877011_Object L_r2;
    iA1_char L_r3;
    extern jmp_buf env;
    extern int epc;
    jmp_buf caller_env;
    int caller_epc;

    caller_epc = epc;
    memcpy(caller_env, env, sizeof(jmp_buf));

    epc = caller_epc;
    Vi1063877011_Object.methods.m442386431__init_((i1063877011_Object)instance);/* specialinvoke r0.<init>() */
    instance->f506377887_hash = (long)0;/* r0.hash = 0 */
    L_i0 = (long)PCCG_ARRAY_LENGTH(p0);/* $i0 = r1lengthof */
    instance->f01486352994_count = (long)L_i0;/* r0.count = $i0 */
    
    //L_r2 = (i1063877011_Object)p0->class->methods.m1911935377_clone((i1063877011_Object)p0);/* $r2 = r1.clone() */
    L_r2 = (i1063877011_Object)p0;

    L_r3 = (iA1_char)(iA1_char)L_r2;/* $r3 = (char[]) $r2 */
    instance->f1860107401_value = (iA1_char)L_r3;/* r0.value = $r3 */
    
    memcpy(env, caller_env, sizeof(jmp_buf));
    epc = caller_epc;
    return ;/* return */

