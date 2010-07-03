/*
 * 
 */
package ptdb.gui;

import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

///////////////////////////////////////////////////////////////
//// ArrayModelList

/**
 * Extends AbstractListModel and implements the function through ArrayList. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class ArrayModelList extends AbstractListModel {

    /**
     * Construct the ArrayModelList through the passed ArrayList object. 
     * 
     * @param items The passed list instance. 
     */
    public ArrayModelList(List<String> items) {
        _items = items;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a new item to this list. 
     * 
     * @param newItem The new item to be added into the list. 
     */
    public void addItem(String newItem) {
        int index = _items.size();
        _items.add(newItem);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Get the item at a certain index. 
     * 
     * @param i The index of item to be gotten. 
     * @return The item at the given index. 
     */
    public Object getElementAt(int i) {
        return _items.get(i);
    }

    /**
     * Get the size of the stored list. 
     * 
     * @return The size of the stored list. 
     */
    public int getSize() {
        return _items.size();
    }

    /**
     * Remove the given item from the list. 
     * 
     * @param item The item to be removed from the list. 
     */
    public void removeItem(String item) {
        for (Iterator iterator = _items.iterator(); iterator.hasNext();) {
            String existingItem = (String) iterator.next();
            if (existingItem.equals(item)) {
                int index = _items.indexOf(item);

                _items.remove(existingItem);

                fireIntervalRemoved(this, index, index);
                break;
            }
        }
    }

    /**
     * Update the item at a given index. 
     * 
     * @param item The new item to put into the list. 
     * @param index The index of the item to be replaced. 
     */
    public void updateItem(String item, int index) {
        _items.set(index, item);

        fireIntervalAdded(this, index, index);
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private List<String> _items;
}
