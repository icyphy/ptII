/* An abstract menu factory that creates context menus using item factories

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.moml.*;
import java.util.*;
import diva.util.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.gui.toolbox.*;
import javax.swing.SwingConstants;

//////////////////////////////////////////////////////////////////////////
//// PtolemyMenuFactory
/**
A menu factory that contains a list of item factories.  
When asked to create a context menu, This class first
takes the figure and finds the ptolemy object associated with it.  Then it
passes the ptolemy object to each menu item factory that it contains to add
the menu items.  Lastly, it returns the resulting menu.  This seems simple,
except for the fact that for different types of figures, and different
visual notations, the mapping between figure and the interesting ptolemy
object is different.  Hence, Node and Edge Controllers will often need 
subclasses of this factory to get the correct ptolemy object.

@author Steve Neuendorffer
@version $Id$
*/
public class PtolemyMenuFactory extends MenuFactory {
    
    /** Create a new menu factory that contains no menu item factories.
     */
    public PtolemyMenuFactory() {
	_factoryList = new LinkedList();
    }

    /** Add a menu item factory to this creator.
     */
    public void addMenuItemFactory(MenuItemFactory factory) {
	_factoryList.add(factory);
    }
    
    /**
     * Create an instance of the menu associated with this factory.
     */
    public JContextMenu create(Figure figure) {
	NamedObj object = _getObjectFromFigure(figure);
	JContextMenu menu = new JContextMenu(object, object.getFullName());
	Iterator i = menuItemFactoryList().iterator();
	while(i.hasNext()) {
	    MenuItemFactory factory = (MenuItemFactory)i.next();
	    factory.create(menu, object);
	}
	return menu;
    }
    
    /** Return the list of menu item factories.
     * @return An unmodifiable list.
     */
    public List menuItemFactoryList() {
	return Collections.unmodifiableList(_factoryList);
    }
    
    /** Remove the given menu item factory from the factory list.
     */
    public void removeMenuItemFactory(MenuItemFactory factory) {
        _factoryList.remove(factory);
    }
    
    /** Return the Ptolemy object that the given figure represents.
     *  In this base class, we assume that the figure is attached to a
     *  a diva.graph.model object, and that object is attached to the
     *  correct ptolemy object.  In many cases, this is not the case, 
     *  and you will have to override this function.
     */
    protected NamedObj _getObjectFromFigure(Figure figure) {
	SemanticObjectContainer graphObject = 
	    (SemanticObjectContainer)figure.getUserObject();
	NamedObj object = (NamedObj)graphObject.getSemanticObject();
	return object;
    }
    
    /** The menu item factories.
     */
    private List _factoryList;
}
