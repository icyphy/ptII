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

import java.util.Collection;
import java.util.Set;
import java.util.HashMap;

public class PropertyMap implements Cloneable {

    public PropertyMap() {}

    /** Make a deep copy of the property map, so that the two instances can have 
     *  different values for the same property. 
     */
    public Object clone() {    
        PropertyMap pm = null;
        // There's no reason that clone() should fail, so just catch the
        // theoretical exception.        
        try {
          pm = (PropertyMap) super.clone();
        } catch (CloneNotSupportedException e) {
          throw new RuntimeException("clone not supported on PropertyNode");
        }
        
        // make a shallow copy of keys and values
        pm._propertyMap = (HashMap) _propertyMap.clone();
        return pm;
    }    
        

    /** Define a property. Return false if the property is already defined. */
    public boolean defineProperty(Object property) {
        Object obj = setProperty(property, NullValue.instance);
        return (obj == null);
    }

    /** Get a property. Throw a RuntimeException if the property in not defined. */
    public Object getDefinedProperty(Object property) {
      Object retval = _propertyMap.get(property);
      if (retval == null) {
         throw new RuntimeException("Property " + property + " not defined");
      }
      return retval;
    }

    /** Get a property. If the property is not defined, returned null. */
    public Object getProperty(Object property) {
      return _propertyMap.get(property);
    }
    
    /** Set a property. Throw a RuntimeException if the property in not defined. */
    public Object setDefinedProperty(Object property, Object obj) {
        if (obj == null) {
           obj = NullValue.instance;
        }
  
        Object retval = _propertyMap.put(property, obj);

        if (retval == null) {
           throw new RuntimeException("Property " + property + " not defined");
        }
        return retval;
    }

    /** Return the a Set of the defined properties. */
    public Set keySet() {
        return _propertyMap.keySet();
    }
   

    /** Set a property. The property may or may not have been defined already. */
    public Object setProperty(Object property, Object obj) {
        if (obj == null) {
           obj = NullValue.instance;
        }
        return _propertyMap.put(property, obj);
    }

    /** Remove a property, returning the value of the property if the property is
     *  defined. If the property is not defined, return null.
     */
    public Object removeProperty(Object property) {
        return _propertyMap.remove(property);
    }

    /** Return true iff this instance has the specified property. */
    public boolean hasProperty(Object property) {
        return _propertyMap.containsKey(property);
    }

    /** Return a Collection containing all of the property values. */
    public Collection values() {
        return _propertyMap.values();
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////
  
    // reserved properties
    
    /** The property indicating a numbering. */  
    public static final Integer NUMBER_KEY = new Integer(-1);


    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////
    
    protected HashMap _propertyMap = new HashMap();
}
