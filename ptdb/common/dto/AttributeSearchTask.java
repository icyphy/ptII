/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
/*
 *
 */
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////////
//// AttributeSearchTask

/**
 * Contain the attribute search criteria to
 * execute attribute search task.
 *
 * @author Ashwini Bijwe
 *
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class AttributeSearchTask extends Task {

    /**
     *  Construct an instance of AttributeSearchTask
     *  and set it as a select task.
     */
    public AttributeSearchTask() {
        _isUpdateTask = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the given attribute to the search attribute list.
     *
     * @param attribute PTDBGenericAttribute to add to the
     * search attribute list.
     */
    public void addAttribute(PTDBGenericAttribute attribute) {
        if (_attributesList == null) {
            _attributesList = new ArrayList<PTDBGenericAttribute>();
        }

        _attributesList.add(attribute);
    }

    /**
     * Return the search attribute list.
     * @see #setAttributesList
     * @return The search attribute list.
     */
    public ArrayList<PTDBGenericAttribute> getAttributesList() {
        return _attributesList;
    }

    /**
     * Set the search attribute list to the given list.
     * @see #getAttributesList
     * @param attributesList List of attributes for search.
     */
    public void setAttributesList(ArrayList<PTDBGenericAttribute> attributesList) {
        _attributesList = attributesList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The search attribute list.
     */
    private ArrayList<PTDBGenericAttribute> _attributesList;
}
