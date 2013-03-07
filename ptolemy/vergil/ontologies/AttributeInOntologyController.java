/* The node controller for icons of attributes in ontology models and
 * ontology solver models.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.vergil.ontologies;

import java.util.List;

import ptolemy.vergil.kernel.AttributeWithIconController;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// AttributeInOntologyController

/** The node controller for icons of attributes in ontology models and
 *  ontology solver models. This subclass of AttributeController removes
 *  the "Listen To Attribute" context menu action since it has no relevance
 *  for attributes inside an ontology model or an ontology solver model.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class AttributeInOntologyController extends AttributeWithIconController {

    /** Create an attribute controller associated with the specified graph
     *  controller.  The attribute controller is given full access.
     *  @param controller The associated graph controller.
     */
    public AttributeInOntologyController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an attribute controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public AttributeInOntologyController(GraphController controller,
            Access access) {
        super(controller);

        // Remove the "Listen To Attribute" menu action since it has no
        // relevance for an ontology models or ontology solvers.
        MenuActionFactory listenToActionFactory = _getListenToMenuActionFactory();
        if (listenToActionFactory != null) {
            _menuFactory.removeMenuItemFactory(listenToActionFactory);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the MenuActionFactory in the _menuFactory object's menu item
     *  factory list that contains the "Listen To Attribute" menu action.
     *  @return the MenuActionFactory that contains the "Listen To Attribute"
     *   menu action, or null if it does not exist.
     */
    private MenuActionFactory _getListenToMenuActionFactory() {
        List menuItemFactories = _menuFactory.menuItemFactoryList();
        for (Object menuItemFactory : menuItemFactories) {
            if (menuItemFactory instanceof MenuActionFactory) {
                if (((MenuActionFactory) menuItemFactory).substitute(
                        _listenToAction, _listenToAction)) {
                    return (MenuActionFactory) menuItemFactory;
                }
            }
        }
        return null;
    }
}
