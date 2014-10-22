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

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RTMList

/**
 * A list of Real-Time Maude terms.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class RTMList extends RTMTerm {

    private String separator;
    private String empty;
    private LinkedList<RTMTerm> items;

    public RTMList(String separator, String emptyrepr) {
        super();
        if (separator.trim().equals("")) {
            this.separator = " ";
        } else {
            this.separator = " " + separator.trim() + " ";
        }
        this.empty = emptyrepr;
        this.items = new LinkedList<RTMTerm>();
    }

    public void add(RTMTerm t) {
        items.add(t);
    }

    public void addStr(String s) {
        add(new RTMFragment(s));
    }

    public void addExp(String e, boolean isTime) throws IllegalActionException {
        add(new RTMPtExp(e, isTime));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public String print(int indent, boolean newline) {
        StringBuffer rs = new StringBuffer("");
        if (items.size() > 0) {
            if (newline) {
                rs.append(front(indent));
            }
            rs.append("(");
            for (Iterator<RTMTerm> ti = items.iterator(); ti.hasNext();) {
                rs.append("\n" + ti.next().print(indent + indentWidth, true));
                if (ti.hasNext()) {
                    rs.append(separator);
                }
            }
            rs.append(")");
        } else {
            rs.append(empty);
        }
        return rs.toString();
    }
}
