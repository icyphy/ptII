/*******************************************************************
Dummy C code for overridden method 
<java.util.Random: double nextDouble()>

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

/* FIXME : This is not a uniform random distribution. */

double r, s, i;

/* r is between 0 and 1 */
r = rand()/(double)RAND_MAX;

/* r is now between -1 and 1 */
r = r*2.0 - 1.0;

/* s between 0 and 1*/
s = (rand()/(double)RAND_MAX);

/* s between -1 and +1 */
s = s*2.0 - 1.0;

/* s between -36 and +36 */
s = s*36.0;

/* return r*10^s */
for (i = 0; i< s; i++) {
    r = r*10;
}

return r;







