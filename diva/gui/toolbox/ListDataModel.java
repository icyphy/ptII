/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class ListDataModel extends DefaultComboBoxModel {

    /** Create a new model.
     */
    public ListDataModel () {
        super();
    }

    /** Return this model as a list.
     */
    public List getList() {
        ArrayList list = new ArrayList(getSize());
        for(int i = 0; i < getSize(); i++) {
            list.add(getElementAt(i));
        }
        return list;
    }

    /** Return an iterator over the elements in the model.
     */
    public Iterator iterator () {
        return getList().iterator();
    }

    /** Return whether or not the given item is contained
     * by the model
     */
    public boolean contains(Object o) {
        for(int i = 0; i < getSize(); i++) {
            Object o2 = getElementAt(i);
            if(o.equals(o2)) {
                return true;
            }
        }
        return false;
    }
}


