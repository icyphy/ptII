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

import java.util.HashMap;
import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RTMObject

/**
 * A Real-Time Maude object.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class RTMObject extends RTMTerm {

    private String name;
    private String objClass;
    private HashMap<String, RTMTerm> attribute;

    public RTMObject(String name, String classname) {
        super();
        this.name = name;
        this.objClass = classname;
        this.attribute = new HashMap<String, RTMTerm>();
    }

    public void addAttr(String name, RTMTerm attr) {
        attribute.put(name, attr);
    }

    public void addStrAttr(String name, String attr) {
        addAttr(name, new RTMFragment(attr));
    }

    public void addExpAttr(String name, String exp, boolean isTime)
            throws IllegalActionException {
        addAttr(name, new RTMPtExp(exp, isTime));
    }

    public void setClass(String classname) {
        this.objClass = classname;
    }

    @Override
    public String print(int indent, boolean newline) {
        StringBuffer ret = new StringBuffer("");
        if (newline) {
            ret.append(front(indent));
        }
        ret.append("< " + transId(name) + " : " + objClass + " | ");
        for (Iterator<String> ki = attribute.keySet().iterator(); ki.hasNext();) {
            String k = ki.next();
            ret.append("\n" + front(indent + indentWidth) + k + " : ");
            ret.append(attribute.get(k).print(indent + indentWidth, false));
            if (ki.hasNext()) {
                ret.append(", ");
            }
        }
        ret.append(" >");
        return ret.toString();
    }

}
