/* Inlines method calls in Soot

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.util.*;

/**
 * Copies method bodies in the jimple intermediate representation into the
 * methods that call them.  For example,
 * suppose we have these two methods:
 * <p>
 * <code>
 * double dist(double x1, double y1, double x2, double y2) {
 * <br>&nbsp;&nbsp;return Math.sqrt(sq(x1 - x2) + sq(y1 - y2));
 * <br>}
 * <br>
 * <br>double sq(double x) {
 * <br>&nbsp;&nbsp;return x * x;
 * <br>}
 * </code><p>
 * We could inline <code>sq</code> into <code>dist</code> to get this result:
 * <p>
 * <code>
 * double dist(double x1, double y1, double x2, double y2) {
 * <br>&nbsp;&nbsp;return Math.sqrt(((x1&nbsp;-&nbsp;x2) *
 * (x1&nbsp;-&nbsp;x2))+ ((y1&nbsp;-&nbsp;y2) * (y1&nbsp;-&nbsp;y2)));
 * <br>}
 * </code>
 *
 * <p>The only methods that are actually inlined are those for which
 * <code>shouldInline</code> returns true.  By default, it always returns
 * false.  In order to use Inliner, you should extend it and override this
 * method.
 *
 * @author Nathan Kitchen
 */
public class Inliner {
    /**
     * Iterates over <code>body</code> and inlines all methods for which
     * <code>shouldInline</code> returns true
     *
     * @param should be a Jimple body, not Grimp or Baf
     */
    public void inline(Body body) {
	Scene.v().setActiveHierarchy(new Hierarchy());
	SootMethod caller = body.getMethod();
	JimpleBody jimpleBody;
	try {
	    jimpleBody = (JimpleBody) body;
	} catch (ClassCastException cce) {
	    return;
	} // end of try-catch

	boolean changed = false;

	Chain stmtChain = body.getUnits();
	Stmt stmt = (Stmt) stmtChain.getFirst();
	Stmt prevStmt = null;
	Stmt lastStmt = (Stmt) stmtChain.getLast();
	while (true) {
	    //        System.out.println("InvokeStmt: " + stmt + "? " + (stmt instanceof
	    //  							 InvokeStmt));
	    SootMethod callee = null;
	    if (stmt instanceof InvokeStmt) {
		callee = ((InvokeExpr) ((InvokeStmt) stmt).getInvokeExpr())
		    .getMethod();
	    } // end of if
	    else {
		List useBoxes = stmt.getUseBoxes();
		Iterator useBoxItr = useBoxes.iterator();
		while (useBoxItr.hasNext()) {
		    ValueBox valBox = (ValueBox) useBoxItr.next();
		    Value val = valBox.getValue();
		    if (val instanceof InvokeExpr) {
			callee = ((InvokeExpr) val).getMethod();
		    } // end of if ()
		} // end of while ()
	    } // end of else
	    if (callee != null) {
		//System.out.print("Inline: " + callee + "? ");
		if (shouldInline(callee)) {
		    //System.out.println("Y");

		    if (! callee.hasActiveBody()) {
			Body activeBody = callee.retrieveActiveBody();
			if (! (activeBody instanceof JimpleBody)) {
			    String msg = "Active body of method " + callee.getName() +
				" is not a Jimple body; cannot continue.";
			    throw new RuntimeException(msg);
			} // end of if ()
		    } // end of if ()

		    SiteInliner.inlineSite(callee, stmt, caller);
		    if (prevStmt == null) {
			stmt = (Stmt) stmtChain.getFirst();
		    } // end of if ()
		    else {
			stmt = prevStmt;
		    } // end of else
		} // end of if ()
		else {
		    //System.out.println("N");
		} // end of else
	    } // end of if ()

	    if (stmt == lastStmt) {
		break;
	    } // end of if ()
	    prevStmt = stmt;
	    stmt = (Stmt) stmtChain.getSuccOf(stmt);
	} // end of while ()

	// Inlining methods may have created some unnecessary goto statements.
	// Remove them
	UnconditionalBranchFolder.v().transform(body);
    }

    /**
     * Indicates whether <code>sootMethod</code> should be inlined
     */
    protected boolean shouldInline(SootMethod sootMethod) {
	return false;
    }
}
