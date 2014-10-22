/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2014 The Regents of the University of California.
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
 */
package ptolemy.verification.kernel.maude;

//////////////////////////////////////////////////////////////////////////
//// RTMOpTerm

/**
 * A Real-Time Maude operation term.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class RTMOpTerm extends RTMTerm {

    // args1 frag1 arg2 frag2 ...
    private String[] op;
    private RTMTerm[] terms;

    protected RTMOpTerm(String[] op, RTMTerm[] args) {
        this.op = op;
        this.terms = args;
    }

    @Override
    public String print(int indent, boolean newline) {
        StringBuffer r = new StringBuffer(op[0]);
        int i;
        for (i = 0; i < Math.min(op.length - 1, terms.length); i++) {
            r.append(terms[i].print(indent, false) + op[i + 1]);
        }
        if (terms.length > op.length - 1) {
            for (int j = i; j < terms.length; j++) {
                r.append(terms[j].print(indent, false));
            }
        } else {
            for (int j = i; j < op.length - 1; j++) {
                r.append(op[j + 1]);
            }
        }
        if (newline) {
            return front(indent) + r.toString();
        } else {
            return r.toString();
        }
    }

}
