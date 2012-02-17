/* An FMU Type

 Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// FMU Type

/**
 * An base class for FMU Types like Real.
 * 
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public abstract class FMUType {

    /** Construct an variable.
     *  @param name The name of this variable.
     *  @param description A description of this variable.
     */
    public FMUType(String name, String description) {
        _name = name;
        _description = description;
    }

    /** Get the description.
     *  @return The description, which is typically documentation.
     *  @see #setDescription(String)
     */
    public String getDescription() {
        return _description;
    }

    /** Get the name.
     *  @return The name
     *  @see #setName(String)
     */
    public String getName() {
        return _name;
    }


    /** Return true if the start attribute is the initial value of a variable
     *  or false if the start attribute is a guess value.
     *  @return true if the start attribute is the initial value,
     *  false if it is a guess value.
     *  @see setFixed(boolean)
     */
    public boolean isFixed() {
        return _fixed;
    }

    /** Set the description.
     *  @param description the new description, which is typically
     *  documentation.
     *  @see #getDescription()
     */
    public void setDescription(String description) {
        _description = description;
    }

    /** True if the  value is fixed.
     *  @param fixed the new value for fixed.
     *  @see #isFixed()
     */
    public void setFixed(boolean fixed) {
        _fixed = fixed;
    }

    /** Set the name.
     *  @param name the new fmi name.
     *  @see #getName()
     */
    public void setName(String name) {
        _name = name;
    }

    private String _description;

    private boolean _fixed;

    private String _name;
}
