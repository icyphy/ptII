/** A GUI widget for configuring arguments. Largely inspired of PortConfigurer

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

@ProposedRating Yellow (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
 */

package jni;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.gui.MessageHandler;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.BoxLayout;

//////////////////////////////////////////////////////////////////////////
//// ArgumentConfigurer
/**
 * This class is an editor to configure the arguments of an object.
 * It supports setting kind :input, output, in-output or return and a type
 * and adding and removing arguments. Only arguments that extend the Argument
 * class are listed, since more primitive Argument cannot be configured
 * in this way.
 *
 * @see Configurer
 * @author Steve Neuendorffer, Edward A. Lee, V. Arnould
 * @version $Id$
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
                Iterator args = _object.argList().iterator();
                while (args.hasNext()) {
                        Object candidate = args.next();
                        if (candidate instanceof Argument) {
                                Argument arg = (Argument) candidate;
                                setColumns(1);
                                addLine(
                                        arg.getName() + "Name",
                                        arg.getName() + "Name",
                                        arg.getName());
                                addLine(
                                        arg.getName() + "CType",
                                        arg.getName() + "C or C++ Type",
                                        arg.getCType());
                                Set optionsDefault = new HashSet();
                                if (arg.isInput())
                                        optionsDefault.add("input");
                                if (arg.isOutput())
                                        optionsDefault.add("output");
                                if (arg.isReturn())
                                        optionsDefault.add("return");
                                addSelectButtons(
                                        arg.getName() + "Kind",
                                        arg.getName() + "Kind:",
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
                Iterator args = _object.argList().iterator();
                NamedObj parent = null;
                while (args.hasNext()) {
                        Object candidate = args.next();
                        if (candidate instanceof Argument) {
                                Argument arg = (Argument) candidate;
                                String type = arg.getName() + "CType";
                                String name = arg.getName() + "Name";
                                String kind = arg.getName() + "Kind";
                                if (!type.equals(arg.getCType())&&
                                    !name.equals(arg.getName())&&
                                    !kind.equals(arg.getKind())) {

                                        String newName = getStringValue(name);
                                        try {
                                                arg.setName(newName);
                                        } catch (Exception e) {
                                                MessageHandler.error(
                                                        "This name is already used ! : ",
                                                        e);
                                                continue;
                                                }
                                        String newCType = getStringValue(type);
                                        arg.setCType(newCType);
                                        String newKind = getStringValue(kind);
                                        arg.setKind(newKind);
                                        arg._checkType();
                                        foundOne = true;
                                        }
                                if (foundOne) {
                                        arg.setExpression();

                                        _object =
                                                (
                                                        GenericJNIActor) MoMLChangeRequest
                                                                .getDeferredToParent(
                                                        arg);
                                        if (_object == null) {
                                                _object = (GenericJNIActor) arg.getContainer();
                                        }
                                        try {
                                                arg.validate();
                                        } catch (IllegalActionException e) {
                                                MessageHandler.error(
                                                        "TRT :No way to update MoML! : ",
                                                        e);
                                        }

                                } //end if found one
                        } //end if instanceOf
                } // end while
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
        /*The set of names of args that have changed.
         */
        private Set _changed = new HashSet();
        /* The object that this configurer configures.
         */
        private GenericJNIActor _object;
        /* The possible configurations for a argument.
         */
        private String[] _optionsArray = { "input", "output", "return" };
}
