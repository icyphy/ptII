/* 
A subclass on which to base objects that have properties.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

*/

package ptolemy.lang;

import java.util.HashMap;
import java.util.LinkedList;

public class PropertyMap extends HashMap {

    public PropertyMap() {
    }

    /**
     *  Define a property.
     *  Return false if the property is already defined.
     */
    public boolean defineProperty(Object property) {
      Object obj = setProperty(property, NullValue.instance);

      return (obj == null);
    }

    /**
     *  Get a property.
     *  Throw a RuntimeException if the property in not defined.
     */
    public Object getDefinedProperty(Object property) {
      Object retval = super.get(property);

      if (retval == null) {
         throw new RuntimeException("Property " + property + " not defined");
      }
      return retval;
    }

    /**
     *  Get a property. If the property is not defined, returned null.
     */
    public Object getProperty(Object property) {
      return super.get(property);
    }

    /**
     *  Set a property.
     *  Throw a RuntimeException if the property in not defined.
     */
    public Object setDefinedProperty(Object property, Object obj) {
      if (obj == null) {
         obj = NullValue.instance;
      }
  
      Object retval = super.put(property, obj);

      if (retval == null) {
         throw new RuntimeException("Property " + property + " not defined");
      }
      return retval;
    }

    /**
     *  Set a property. The property may or may not have been defined already.
     */
    public Object setProperty(Object property, Object obj) {
      if (obj == null) {
         obj = NullValue.instance;
      }
      return super.put(property, obj);
    }

    public Object removeProperty(Object property) {
      return super.remove(property);
    }

    /** Return true iff this instance has the specified property. */
    public boolean hasProperty(Object property) {
      return super.containsKey(property);
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
  
    // reserved properties
  
    public static final Integer NUMBER_KEY = new Integer(-1);
}
