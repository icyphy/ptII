/*
  File: DefaultComparator.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 *
 * DefaultComparator provides a general-purpose but slow compare
 * operation.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class DefaultComparator implements Comparator {

/**
 * Try various downcasts to find a basis for
 * comparing two elements. If all else fails, just compare
 * hashCodes(). This can be effective when you are
 * using an ordered implementation data structure like trees,
 * but don't really care about ordering.
 *
 * @param fst first argument
 * @param snd second argument
 * @return a negative number if fst is less than snd; a
 * positive number if fst is greater than snd; else 0
**/


  public int compare(Object fst, Object snd) {

    Object a = fst;
    Object b = snd;
    if (fst instanceof Keyed)  a = ((Keyed)(fst)).key();
    if (snd instanceof Keyed)  b = ((Keyed)(snd)).key();
    if (a == b)
      return 0;
    else if ((a instanceof String) && (b instanceof String)) {
      return ((String)(a)).compareTo((String)(b));
    }
    else if ((a instanceof Number) && (b instanceof Number)) {
      double diff = ((Number)(a)).doubleValue() -
        ((Number)(b)).doubleValue();
      if (diff < 0.0) return -1;
      else if (diff > 0.0) return 1;
      else return 0;
    }
    else if (a.equals(b))
      return 0;
    else
      return a.hashCode() - b.hashCode();
  }
}
