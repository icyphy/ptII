/*
@Copyright (c) 2010 The Regents of the University of California.
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
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////
//// PTDBGenericAttribute

/**
 * The DTO for transferring the generic attribute information. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class PTDBGenericAttribute {

    //////////////////////////////////////////////////////////////////////
    ////                    public methods                            ////

    /**
     * Add more value for OR relationship to this attribute. 
     * 
     * @param newValue The new value to be added. 
     */
    public void addValue(String newValue) {

        if (_values == null) {
            _values = new ArrayList<String>();
        }

        _values.add(newValue);

    }

    /**
     * Get the name of this attribute. 
     * 
     * @return The name of the attribute. 
     */
    public String getAttributeName() {
        return _attributeName;
    }

    /**
     * Get the class name of this attribute. 
     * 
     * @return The class name of this attribute. 
     */
    public String getClassName() {
        return _className;
    }

    /**
     * Get the values of this attribute. 
     * 
     * @return The values of the attributes. All the values share the "OR"
     *  relationship. 
     */
    public ArrayList<String> getValues() {
        return _values;
    }

    /**
     * Set the name of the attribute. 
     * 
     * @param attriubteName The name to be set for the attribute. 
     */
    public void setAttributeName(String attriubteName) {
        _attributeName = attriubteName;
    }

    /**
     * Set the class name of this attribute. 
     * 
     * @param className The class name to be set for this attribute. 
     */
    public void setClassName(String className) {
        _className = className;
    }

    /**
     * Set the values of the attribute. 
     * 
     * @param values The values to be set for the attribute. 
     */
    public void setValues(ArrayList<String> values) {
        _values = values;
    }

    //////////////////////////////////////////////////////////////////////
    ////                    private variables                         ////

    private String _className;

    private String _attributeName;

    private ArrayList<String> _values;

}
