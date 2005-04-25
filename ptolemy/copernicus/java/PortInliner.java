/* An interface for classes that replaces port methods.

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.java;

import ptolemy.actor.TypedIOPort;

import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;


//////////////////////////////////////////////////////////////////////////
//// PortInliner

/**

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
@Pt.ProposedRating Red (cxh)
@Pt.AcceptedRating Red (cxh)
*/
public interface PortInliner {
    /** Initialize the inliner.  This method will be called by the
     * InlinePortTransformer prior to any of the other methods being
     * called.  It gives the Port inliner the opportunity to create
     * storage buffers that should only be created once.
     */
    public void initialize();

    /** Replace the broadcast invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineBroadcast(JimpleBody body, Stmt stmt, InvokeExpr expr,
            TypedIOPort port);

    /** Replace the get invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineGet(JimpleBody body, Stmt stmt, ValueBox box,
            InvokeExpr expr, TypedIOPort port);

    /** Replace the getInside invocation in the given box
     *  at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineGetInside(JimpleBody body, Stmt stmt, ValueBox box,
            InvokeExpr expr, TypedIOPort port);

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineSend(JimpleBody body, Stmt stmt, InvokeExpr expr,
            TypedIOPort port);

    /** Replace the send command at the given unit in the
     *  given body with a circular array reference.
     */
    public void inlineSendInside(JimpleBody body, Stmt stmt, InvokeExpr expr,
            TypedIOPort port);
}
