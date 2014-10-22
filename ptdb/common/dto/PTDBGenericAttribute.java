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
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////////
//// PTDBGenericAttribute

/**
 * The DTO for transferring the generic attribute information.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class PTDBGenericAttribute {

    /**
     * Construct the PTDBGenericAttribute class.
     *
     * @param attributeName The name of the attribute.
     *
     */
    public PTDBGenericAttribute(String attributeName) {
        _attributeName = attributeName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     * @see #setAttributeName(String)
     */
    public String getAttributeName() {
        return _attributeName;
    }

    /**
     * Get the class name of this attribute.
     *
     * @return The class name of this attribute.
     * @see #setClassName(String)
     */
    public String getClassName() {
        return _className;
    }

    /**
     * Get the values of this attribute.
     *
     * @return The values of the attributes. All the values share the "OR"
     *  relationship.
     * @see #setValues(ArrayList)
     */
    public ArrayList<String> getValues() {
        return _values;
    }

    /**
     * Set the name of the attribute.
     *
     * @param attributeName The name to be set for the attribute.
     * @see #getAttributeName()
     */
    public void setAttributeName(String attributeName) {
        _attributeName = attributeName;
    }

    /**
     * Set the class name of this attribute.
     *
     * @param className The class name to be set for this attribute.
     * @see #getClassName()
     */
    public void setClassName(String className) {
        _className = className;
    }

    /**
     * Set the values of the attribute.
     *
     * @param values The values to be set for the attribute.
     * @see #getValues()
     */
    public void setValues(ArrayList<String> values) {
        _values = values;
    }

    /**
     * Return the String representation of this attribute.
     *
     * @return The String representation of this attribute.
     */

    @Override
    public String toString() {

        return _attributeName + "@" + _className + "@" + _values.toString();
    }

    /**
     * Check whether the given attribute equals to this attribute.
     *
     * @param attribute The given attribute to be checked.
     *
     * @return true - if the given attribute equals to this attribute.<br>
     *          false - if the given attribute does not equal to this attribute.
     */

    @Override
    public boolean equals(Object attribute) {

        if (!(attribute instanceof PTDBGenericAttribute)) {
            return false;
        }

        PTDBGenericAttribute genericAttribute = (PTDBGenericAttribute) attribute;

        if (_className != null && _attributeName != null && _values != null) {
            return _className.equals(genericAttribute.getClassName())
                    && _attributeName.equals(genericAttribute
                            .getAttributeName())
                    && _values.equals(genericAttribute.getValues());
        }

        if (_className == null && _attributeName == null) {
            return genericAttribute.getClassName() == null
                    && genericAttribute.getAttributeName() == null
                    && _values.equals(genericAttribute.getValues());
        }

        if (_attributeName == null && _values == null) {
            return genericAttribute.getAttributeName() == null
                    && genericAttribute.getValues() == null
                    && _className.equals(genericAttribute.getClassName());
        }

        if (_values == null && _className == null) {
            return genericAttribute.getValues() == null
                    && genericAttribute.getClassName() == null
                    && _attributeName.equals(genericAttribute
                            .getAttributeName());
        }

        if (_className == null) {
            return genericAttribute.getClassName() == null
                    && _attributeName.equals(genericAttribute
                            .getAttributeName())
                    && _values.equals(genericAttribute.getValues());
        }

        if (_attributeName == null) {
            return genericAttribute.getAttributeName() == null
                    && _className.equals(genericAttribute.getClassName())
                    && _values.equals(genericAttribute.getValues());
        }

        if (_values == null) {
            return genericAttribute.getValues() == null
                    && _className.equals(genericAttribute.getClassName())
                    && _attributeName.equals(genericAttribute
                            .getAttributeName());
        }

        return false;

    }

    /**
     * Calculate the hash code for this PTDBGenericAttribute instance.
     *
     * @return Return the calculated hash code.
     */

    @Override
    public int hashCode() {
        StringBuffer summaryStringBuffer = new StringBuffer("");

        if (_className != null) {
            summaryStringBuffer.append(_className);
        }

        summaryStringBuffer.append(":");

        if (_attributeName != null) {
            summaryStringBuffer.append(_attributeName);
        }

        summaryStringBuffer.append(":");

        if (_values != null) {
            summaryStringBuffer.append(_values.toString());

        }

        return summaryStringBuffer.toString().hashCode();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _className;

    private String _attributeName;

    private ArrayList<String> _values;

}
