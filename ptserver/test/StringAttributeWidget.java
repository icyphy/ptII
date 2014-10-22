/*

Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptserver.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;

/**
 * StringAttributeWidget class.
 *
 * @author pdf
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class StringAttributeWidget extends TypedAtomicActor {

    public StringAttributeWidget(CompositeEntity container, String name,
            ValueListener listener) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _attribute = new StringAttribute(container, name);
        _attribute.addValueListener(listener);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        System.out.println("Attribute value = " + getAttributeValue());

    }

    /**
     * @return the _attribute
     */
    public String getAttributeValue() {
        return _attribute.getExpression();
    }

    /** Set the value of _attribute.
     * @param value the new value of _attribute.
     */
    public void setAttributeValue(String value) {
        try {
            _attribute.setExpression(value);
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private final StringAttribute _attribute;
}
