/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.gui.toolbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

/**
 * A model that captures the notion of a list of elements with a
 * single selected element. This is really just a
 * DefaultComboBoxModel, because that class happens to fit our
 * requirements. We subclass DefaultComboBoxModel so we can modify it
 * if any of the behavior should not be quite right.
 *
 * @author John Reekie
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class ListDataModel extends DefaultComboBoxModel {
    /** Create a new model.
     */
    public ListDataModel() {
        super();
    }

    /** Return this model as a list.
     */
    public List getList() {
        ArrayList list = new ArrayList(getSize());

        for (int i = 0; i < getSize(); i++) {
            list.add(getElementAt(i));
        }

        return list;
    }

    /** Return an iterator over the elements in the model.
     */
    public Iterator iterator() {
        return getList().iterator();
    }

    /** Return whether or not the given item is contained
     * by the model
     */
    public boolean contains(Object o) {
        for (int i = 0; i < getSize(); i++) {
            Object o2 = getElementAt(i);

            if (o.equals(o2)) {
                return true;
            }
        }

        return false;
    }
}
