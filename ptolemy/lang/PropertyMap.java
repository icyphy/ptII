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
  public boolean defineProperty(String property) {
    Object obj = setProperty(property, NullValue.instance);

    return (obj == null);
  }

  /**
   *  Get a property.
   *  Throw a RuntimeException if the property in not defined.
   */
  public Object getDefinedProperty(String property) {
    Object retval = super.get(property);

    if (retval == null) {
       throw new RuntimeException("Property " + property + " not defined");
    }
    return retval;
  }

  /**
   *  Get a property. If the property is not defined, returned null.
   */
  public Object getProperty(String property) {
    return super.get(property);
  }

  /**
   *  Set a property.
   *  Throw a RuntimeException if the property in not defined.
   */
  public Object setDefinedProperty(String property, Object obj) {
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
  public Object setProperty(String property, Object obj) {
    if (obj == null) {
       obj = NullValue.instance;
    }
    return super.put(property, obj);
  }

  public Object removeProperty(String property) {
    return super.remove(property);
  }

  /** Return true iff this instance has the specified property. */
  public boolean hasProperty(String property) {
    return super.containsKey(property);
  }
}
