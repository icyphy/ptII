/*
 * Created on 01 sept. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.vergil.navigable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.tree.EntityTreeModel;

/**
 * <p>Titre : NavigationTreeModel</p>
 * <p>Description : used to represent all the entities of a MoML file</p>
 * <p>Copyright : Copyright (c) 2003</p>
 * <p>Société : Thales Research and technology</p>
 * @author Jérôme Blanc & Benoit Masson
 * 01 sept. 2003
 */
public class NavigationTreeModel extends EntityTreeModel {

	public NavigationTreeModel(NamedObj root) {
		super(root);
	}

	/** Return true if the object is a leaf node.  In this base class,
	 *  an object is a leaf node if it is not an instance of CompositeEntity.
	 *  ATTENTION il se peut qu'il faille ne plus faire se test lors de 
	 * l'utilisation de bibliothèque.
	 *  @return True if the node has no children.
	 */
	public boolean isLeaf(Object object) {
		if (!(object instanceof CompositeEntity))
			return true;
		// NOTE: The following is probably not a good idea because it
		// will force evaluation of the contents of a Library prematurely.
		else if (((CompositeEntity) object).numEntities() == 0)
			return true;
		return false;
	}
	
	//private members
	private List listeners = new ArrayList();
	
	/**
	 * Register a listener
	 * @param tree
	 */
	public void register(NavigationPTree tree){
		listeners.add(tree);
	}
	
	/**
	 * remove a listener
	 * @param tree
	 * @return wether the tree was in the listeners or not
	 */
	public boolean unRegister(NavigationPTree tree){
		return listeners.remove(tree);	
	}

	/**
	 * set all listening PTree to the same path
	 * @param obj
	 */
	public void setSelectedItem(TreePath path) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			NavigationPTree aTree = (NavigationPTree) it.next();
			aTree.setSelectionPath(path);
		}
	}

	/**
	 * expand/collapse all the NavigationTree
	 * @param aPath
	 */
	public void expandPath(TreePath aPath, boolean collapse) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			NavigationPTree aTree = (NavigationPTree) it.next();
			if (collapse){
				aTree.collapsePath(aPath);
			}else{
				aTree.expandPath(aPath);
			}
		}
	}
}
