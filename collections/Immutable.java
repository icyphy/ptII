/*
  File: Immutable.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Renamed just to `Immutable'

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * Immutable is a root interface of immutable classes; i.e.,
 * those with objects that may be looked at but not updated. 
 * By necessity in Java, all implementation classes supporting
 * interface Immutable must be <CODE>final</CODE> classes.
 * <P>
 * There are
 * no operations defined in this interface. This interface is used only
 * to provide a name for this `Purity' property.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public interface Immutable {

}


