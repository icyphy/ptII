/*
  File: Cell.java

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
import java.io.Serializable;
/**
 *
 *
 * Cell is the base of a bunch of implementation classes
 * for lists and the like.
 * The base version just holds an Object as its element value
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class Cell implements Cloneable, Serializable{

// instance variables

  private Object   element_;

/**
 * Make a cell with element value v
**/
  public Cell(Object v)                  { element_ = v; }
/**
 * Make A cell with null element value
**/

  public Cell()                          { element_ = null; }

/**
 * return the element value
**/

  public final Object element()          { return element_; }

/**
 * set the element value
**/

  public final void   element(Object v)  { element_ = v; }


  protected Object clone() throws CloneNotSupportedException { 
    return new Cell(element_);
  }

}

