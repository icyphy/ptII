/** A GUI widget for configuring arguments. Largely inspired of PortConfigurer

Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/

package jni;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;

import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ArgumentConfigurer
/**
This class is an editor to configure the arguments of an object.
It supports setting kind :input, output, in-output or return and a type
and adding and removing arguments. Only arguments that extend the Argument
class are listed, since more primitive Argument cannot be configured
in this way.

@author Steve Neuendorffer, Edward A. Lee, V. Arnould (Thales)
@version $Id$
@since Ptolemy II 2.3
*/
public class ArgumentConfigurer extends Query implements QueryListener {
    /** Construct a argument configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public ArgumentConfigurer(GenericJNIActor object) {
        super();
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(15);
        setColumns(1);
        _object = object;
        Iterator arguments = _object.argumentsList().iterator();
        while (arguments.hasNext()) {
            Object candidate = arguments.next();
            if (candidate instanceof Argument) {
                Argument argument = (Argument) candidate;
                setColumns(1);
                addLine(
                        argument.getName() + "Name",
                        argument.getName() + "Name",
                        argument.getName());
                addLine(
                        argument.getName() + "CType",
                        argument.getName() + "C or C++ Type",
                        argument.getCType());
                Set optionsDefault = new HashSet();
                if (argument.isInput()) {
                    optionsDefault.add("input");
                }
                if (argument.isOutput()) {
                    optionsDefault.add("output");
                }
                if (argument.isReturn()) {
                    optionsDefault.add("return");
                }
                addSelectButtons(
                        argument.getName() + "Kind",
                        argument.getName() + "Kind:",
                        _optionsArray,
                        optionsDefault);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Apply the changes by configuring the arguments that have changed.
     */
    public void apply() {
        boolean foundOne = false;
        Iterator arguments = _object.argumentsList().iterator();
        while (arguments.hasNext()) {
            Object candidate = arguments.next();
            if (candidate instanceof Argument) {
                Argument argument = (Argument) candidate;
                String type = argument.getName() + "CType";
                String name = argument.getName() + "Name";
                String kind = argument.getName() + "Kind";
                if (!type.equals(argument.getCType())&&
                        !name.equals(argument.getName())&&
                        !kind.equals(argument.getKind())) {

                    String newName = getStringValue(name);
                    try {
                        argument.setName(newName);
                    } catch (Exception e) {
                        MessageHandler.error(
                                "This name is already used ! : ",
                                e);
                        continue;
                    }
                    String newCType = getStringValue(type);
                    argument.setCType(newCType);
                    String newKind = getStringValue(kind);
                    argument.setKind(newKind);
                    argument._checkType();
                    foundOne = true;
                }
                if (foundOne) {
                    argument.setExpression();

                    _object = (GenericJNIActor) argument.getContainer();
                    try {
                        argument.validate();
                    } catch (IllegalActionException e) {
                        MessageHandler.error(
                                "TRT :No way to update MoML! : ",
                                e);
                    }
                }
            }
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed.add(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of names of arguments that have changed.
    private Set _changed = new HashSet();

    // The object that this configurer configures.
    private GenericJNIActor _object;

    // The possible configurations for a argument.
    private String[] _optionsArray = { "input", "output", "return" };
}
