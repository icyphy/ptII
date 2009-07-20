/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.data.properties;

public class PropertyableObject implements Propertyable {

    public PropertyableObject(Object object) {
        _object = object;
    }

    public void clearHighlight() {
        // Do nothing.

    }

    public void clearProperty(String useCase) {
        // Do nothing.
    }

    public void clearShowProperty() {
        // Do nothing.
    }

    public Property getProperty() {
        return _property;
    }

    public void highlight(String color) {
        // Do nothing.
    }

    public void setProperty(Property property) {
        _property = property;
    }

    public void showProperty(String property) {
        // Do nothing.
    }

    public void updateProperty(String useCase, Property property) {
        // Do nothing.
    }

    protected Object _object;

    private Property _property;

}
