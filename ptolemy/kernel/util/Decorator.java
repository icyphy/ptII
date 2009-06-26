/** An interface for decorators.

 Copyright (c) 2009 The Regents of the University of California.
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

 */
package ptolemy.kernel.util;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
////Decorator


/**
A decorator is a class that decorates other NamedObj
with extra attributes that are specific to both the decorator
and the NamedObj.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 7.2
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public interface Decorator {

    /** Return the name of the decorator.
     * @return The name of the decorator.
     */
    public String getDecoratorName();

    /** Return the decorated attributes for the target NamedObj.
     * @param target The NamedObj that will be decorated.
     * @return A list of decorated attributes for the target NamedObj.
     */
    public List<DecoratedAttribute> getDecoratorAttributes(NamedObj target);
}
