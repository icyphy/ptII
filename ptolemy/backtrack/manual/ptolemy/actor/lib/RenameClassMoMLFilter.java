/* 

Copyright (c) 2005-2006 The Regents of the University of California.
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

package ptolemy.backtrack.manual.ptolemy.actor.lib;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// RenameClassMoMLFilter
/**

 @see ptolemy.moml.filter.ClassChanges
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RenameClassMoMLFilter implements MoMLFilter {

    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue) {
        if (attributeName.equals("class")) {
            String newClassName = _newClassName(attributeValue);
            if (newClassName != null) {
                MoMLParser.setModified(true);
                return newClassName;
            }
        }
        return null;
    }

    public void filterEndElement(NamedObj container, String elementName) {
    }

    public static String AUTOMATIC_PREFIX = "ptolemy.backtrack.automatic";
    public static String MANUAL_PREFIX = "ptolemy.backtrack.manual";
    
    private boolean _classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private String _newClassName(String oldClassName) {
        String automaticClass = AUTOMATIC_PREFIX + "." + oldClassName;
        String manualClass = MANUAL_PREFIX + "." + oldClassName;
        if (_classExists(manualClass)) {
            return manualClass;
        } else if (_classExists(automaticClass)) {
            return automaticClass;
        } else {
            return null;
        }
    }
}
