/*
 * Created on 01 sept. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.vergil.navigable;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.tree.PTree;
import thales.vergil.SingleWindowApplication;

/**
 * <p>Titre : NavigationPTree</p>
 * <p>Description : A navigation tree to browse a Ptolemy model</p>
 * <p>Copyright : Copyright (c) 2003</p>
 * <p>Société : Thales Research and technology</p>
 * @author Jérôme Blanc & Benoit Masson
 * 01 sept. 2003
 */

public class NavigationPTree extends PTree {

	/**
	 * Most of the time, we use the tree of the ptolemt.vergil.tree package, 
	 * according to the choosen model, detail level can vary
	 *
	 * @param model the used model to create the tree
	 */
	public NavigationPTree(TreeModel model) {
		super(model);
		addTreeSelectionListener(new selectionListener(this));
		addTreeExpansionListener(new expandListener(this));
		if (model instanceof NavigationTreeModel) {
			NavigationTreeModel navModel = (NavigationTreeModel) model;
			navModel.register(this);
		}
	}

	/**
	 * This listener intends to get the selection from the user, open the correst model but
	 * also to inform all the other referenced Navigation tree of this event
	 * 
	 * @author masson
	 *
	 */
	private class selectionListener implements TreeSelectionListener {
		private NavigationPTree _jTree = null;

		public selectionListener(NavigationPTree sptree) {
			_jTree = sptree;
		}

		public void valueChanged(TreeSelectionEvent e) {
			NamedObj obj = (((NamedObj) e.getPath().getLastPathComponent()));
			if (obj != null && _jTree.getSelectionPath() != null) {
				if (obj instanceof CompositeEntity)
					try {
						SingleWindowApplication
							._mainFrame
							.getConfiguration()
							.openModel(
							obj);
						(
							(NavigationTreeModel) _jTree
								.getModel())
								.setSelectedItem(
							_jTree.getSelectionPath());
					} catch (IllegalActionException e1) {
						e1.printStackTrace();
					} catch (NameDuplicationException e1) {
						e1.printStackTrace();
					}
			}
		}
	};

	/**
	 * This expandListener inform all other Tree of the Path expanded or collapse so that all Tree
	 * have the same expand/collapse state.
	 * 
	 * @author masson
	 *
	 */
	private class expandListener implements TreeExpansionListener {
		private NavigationPTree _jTree = null;

		public expandListener(NavigationPTree sptree) {
			_jTree = sptree;
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
		 */
		public void treeCollapsed(TreeExpansionEvent event) {
			TreePath aPath = event.getPath();
			if (aPath != null) {
				((NavigationTreeModel) _jTree.getModel()).expandPath(
					aPath,
					true);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
		 */
		public void treeExpanded(TreeExpansionEvent event) {
			TreePath aPath = event.getPath();
			if (aPath != null) {
				((NavigationTreeModel) _jTree.getModel()).expandPath(
					aPath,
					false);
			}
		}

	}
}
