/* Token class for DFM domain.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.dfm.data;

import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// DFMToken
/** 
  This is the base token class to be used within DFM domain.  This kind
  of toke is different from those in the data package by adding a tag in
  it fields.  The tag is a string that will be processed by DFM actor.
  These are the tag value: <p>
   "New" - this value on this token is newly produced. <p>
   "Previous Result Valid" - previous result sent from the same channel is 
    still valid. <p>
   "Noop" - No need to do anything. <p>
   "Annotate" - This token give some information to user that annotate to the
     design.  No design will be produced from this type of token.  <p>
  <p>
  The tag is set in the constructor or <code> setTag() </code>.  
  The tag can get with getTag() method.
  <p>    

 @author  William Wu (wbwu@eecs.berkeley.edu)
 @version $id$
 @see ptolemy.data.Token
*/

public class DFMToken extends Token{

    /** Constructor
     * Set tag of this token.
     * @param tag the tag of the token.
     */	
    public DFMToken(String tag) {
        _tag = tag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the tag of the this token.
     * 
     * @return get tag on this token
     * @exception full-classname description
     */	
    public String getTag(){
        return _tag;
    }

    public void setTag(String tag){
        _tag = tag;
    }

    // the derived class should implement this that returns the token
    // value
    public Object getData(){
        return new String("void");
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // String tag
    private String _tag;
}
