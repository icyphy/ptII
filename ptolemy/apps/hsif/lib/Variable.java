/* A variable is used to indicate the state of the DNHA.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.apps.hsif.lib;

public class Variable {

  public Variable(String name) {
    this(name, "unknown", "unknown");
 }

  public Variable(String name, String type) {
    this(name, type, "unknown");
 }

  public Variable(String name, String type, String IOType) {
    _name = name;
    _type = type;
    _IOType = IOType;
 }

  ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

  /** Return the name of the variable.
   */
  public String getName() {
    return _name;
  }

  /** Return the type of the variable.
   */
  public String getType() {
    return _type;
  }

  public String getIOType() {
    return _IOType;
  }

  public void setIOType(String IOType) {
    _IOType = IOType;
  }

  /** Set the name of the agent.
   *  @param name The new name for the agent.
   */
  public void setName(String name) {
    _name = name;
  }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

  private String _name = "";
  private String _type = "";
  private String _IOType = "";
}