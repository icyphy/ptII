/* Abstract base class for data capsules.

 Copyright (c) 1997- The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// Token
/** 
Abstract base class for data capsules.
This class declares that tokens are cloneable and promotes the
protected clone() method of the Object base class to public.
In addition, it defines interfaces to initialize the token from
a string and to return a description of the token as a string.
Not all derived classes are required to implement these methods,
so the default implementation here triggers an exception.

@author Neil Smyth, Edward A. Lee
@version $Id$
@see java.lang.Object
@see pt.data.parser.PtParser
*/

package pt.data;

import pt.kernel.*;
// import pt.data.parser.*;

public abstract class Token implements Cloneable {


    /////////////////////////////////////////////////////////////////////////
    ////                         public methods                          ////

    /** Add the value of the argument Token to the current Token. It should be 
     *  overridden in derived classes to provide type specific actions for 
     *  add. It is here mainly for the parser. 
     *  @param a The token whose value we add to this Token
     *  @exception IllegalActionException Thrown if this method is not 
     *  supported by the derived class
     */
    public Token add(Token a) throws IllegalActionException {
        String str = "Add method not supported on ";
        str = str + this.getClass().getName() + " objects";
        throw new IllegalActionException(str);
    }

    /** Subtract the value of the argument Token from the current Token. It 
     *  should be overridden in derived classes to provide type specific 
     *  actions for subtract. It is here mainly for the parser. 
     *  @param a The token whose value we sutract from this Token
     *  @exception IllegalActionException Thrown if this method is not 
     *  supported by the derived class
     */
    public Token subtract(Token a) throws  IllegalActionException {
        String str = "Subtract method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Multiply the value of the argument Token with the value of this Token.
     *  It  should be overridden in derived classes to provide type specific 
     *  actions for multiply. It is here mainly for the parser. 
     *  @param a The token whose value we multiply the value of this Token with
     *  @exception IllegalActionException Thrown if this method is not 
     *  supported by the derived class
     */
    public Token multiply(Token a) throws  IllegalActionException {
        String str = "Multiply method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Divide the value of this Token with the value of the argument Token.
     *  It  should be overridden in derived classes to provide type specific 
     *  actions for divide. It is here mainly for the parser. 
     *  @param a The token whose value we divide the value of this Token by
     *  @exception IllegalActionException Thrown if this method is not 
     *  supported by the derived class
     */
    public Token divide(Token a) throws  IllegalActionException {
        String str = "Divide method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str); 
    }

    /** Find the result of the value of this Token modulo the value of the 
     *  argument Token. Set the value of this token to the result.
     *  It  should be overridden in derived classes to provide type specific 
     *  actions for modulo. It is here mainly for the parser. 
     *  @param a The token whose value we do modulo with
     *  @exception IllegalActionException Thrown if this method is not 
     *  supported by the derived class
     */
    public Token modulo(Token a) throws  IllegalActionException {
        String str = "Modulo method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Test for equality of the values of this Token and the argument Token.
     *  It  should be overridden in derived classes to provide type specific 
     *  actions for equality testing. It is here mainly for the parser. 
     *  @param a The token with which to test equality
     *  @exception IllegalActionException Thrown if this method is not 
     *  supported by the derived class
     */
    public BooleanToken equality(Token a) throws  IllegalActionException {
        String str = "Equality method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }


    /** Promote the clone method of the base class Object to public.
     *  @see java.lang.Object#clone()
     *  @return An identical token.
     *  @exception CloneNotSupportedException May be thrown by derived classes.
     */	
    public Object clone() 
            throws CloneNotSupportedException {
        return super.clone();
    }

    /** Initialize the value of the token from the given string.
     *  In this base class, we just throw an exception.
     *  @param init The String to set the current value from.
     *  @exception IllegalActionException Initialization of this token
     *   from a string is not supported.
     */	
    public void fromString(String init) 
            throws IllegalActionException {
        // FIXME: Should throw a new exception: FormatException
        Class myclass = getClass();
        throw new IllegalActionException("Tokens of class "
                + myclass.getName() + " cannot be initialized from a string.");
    }
    
    /** Return the Parser object associated with this Token. 
     */
    /* FIXME
    public PtParser getParser() {
        if (_parser == null) {
            _parser = new PtParser();
        }
        return _parser;
    }
    */
    /** Return the Publisher object associated with this Token. 
     */
    public TokenPublisher getPublisher() {
        if (_publisher == null) {
            _publisher = new TokenPublisher(this);
        }
        return _publisher;
    }

    /** This method should be overridden where appropriate in subclasses
     */
    public boolean isArray() {
        return false;
    }

    /** Used to notify any objects that may depend on the value of
     *  this Token. It is basically just syntactic sugar :)
     */
    public void notifySubscribers() {
        if (_publisher != null) {
            _publisher.notifyObservers(this);
        }
    }
       

    /** Attach a new TokenPublisher to this token. This method is 
     *  only intended for use when placing a new Token in a Param.
     *  This method should be called by a param and be synchronized.
     *  @param publ The new TokenPublisher associated with this Token
     *  @return The previous Publisher
     */
     public TokenPublisher setPublisher(TokenPublisher publ) {
         TokenPublisher old = _publisher;
         _publisher = publ;
         _publisher.setToken(this);
         return old;
     }

      /* This method is used to set the value of the token from a 
      * String. It relies on each derived class having an appropriate 
      * definition of fromString(). 
      * FIXME: This method is not final, but perhaps should be?
      * @param value The string to be parsed to get the tokens value
      * @param params The params that this tokens value can depend on
      */
    /* FIXME
      public void setValue(String value, NamedList params) throws IllegalArgumentException {
          try {
              Token result;
              if (params == null) {
                  result = getParser().parseExpression(value);
              } else {
                  result = getParser().parseExpression(value, params);
              }
              this.fromString(result.toString());
          } catch (Exception ex) {
              throw new IllegalArgumentException("Cannot parse argument "+value);
          }     
          notifySubscribers();
      }         
      */

     /** This method should be overridden where appropriate in subclasses
     */
    public int size() {
        return 1;
    }

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     */	
    public String toString() {
        return getClass().getName();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // the associated TokenPublisher
    private TokenPublisher _publisher;
    // the associated Parser
    // private PtParser _parser;
}
