/* A top-level dialog window for displaying dependency results.

   Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.Frame;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.table.DefaultTableCellRenderer;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.util.ActorDependencies;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// DependencyResultsDialog

/**
   A non-modal dialog that displays the actor dependency analysis results as a
   list.

   @author Christopher Brooks, Based on SearchResultsDialog by Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class DependencyResultsDialog extends SearchResultsDialog {

    /** Construct a dialog for search results.
     *  @param tableau The DialogTableau.
     *  @param owner The frame that, per the user, is generating the dialog.
     *  @param target The object on which the search is to be done.
     *  @param configuration The configuration to use to open the help screen
     *   (or null if help is not supported).
     */
    public DependencyResultsDialog(DialogTableau tableau, Frame owner,
            Entity target, Configuration configuration) {
        super("Dependency analysis for " + target.getName(), tableau, owner,
                target, configuration);
        _resultsTable.setDefaultRenderer(NamedObj.class,
                new DependencyResultsNamedObjRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize the query dialog.
     *  Derived classes may change the layout of the query dialog.
     */
    @Override
    protected void _initializeQuery() {
        _query.setColumns(2);
        _query.addCheckBox("prerequisites", "Prerequisites", true);
        _query.addCheckBox("dependents", "Dependents", true);
    }

    /** Perform a search and update the results table.
     */
    @Override
    protected void _search() {
        boolean prerequisites = _query.getBooleanValue("prerequisites");
        boolean dependents = _query.getBooleanValue("dependents");
        try {
            Set<NamedObj> results = _findDependencies((Actor) _target,
                    prerequisites, dependents);
            _resultsTableModel.setContents(results);
            if (results.size() == 0) {
                MessageHandler.message("No prerequisites and/or dependents.");
            }
        } catch (KernelException ex) {
            MessageHandler.error("Failed to get prequisites or dependents for "
                    + _target.getFullName() + ".", ex);
        }
    }

    /** Return a list of objects in the model that match the
     *  specified search.
     *  @param actor The actor to be searched.
     *  @param prerequisites True to search for prerequisites.
     *  @param dependents True to search for dependents.
     *  @return The Set of objects in the model that match the specified search.
     *  @exception KernelException If thrown while preinitializing() or wrapping up.
     */
    protected Set<NamedObj> _findDependencies(Actor actor,
            boolean prerequisites, boolean dependents) throws KernelException {

        // FIXME: we could add a field to the search box for specifying the filter
        // class.  However, how would we specify that searching Publishers should
        // have Subscribers as the field?
        Class clazz = AtomicActor.class;
        SortedSet<NamedObj> result = new TreeSet<NamedObj>(
                new NamedObjComparator());
        if (prerequisites) {
            BasicGraphFrame.report(_owner,
                    "Generating prerequisite information.");
            System.out.println("_findDependencies: " + actor);
            result.addAll(ActorDependencies.prerequisites(actor, clazz));
            BasicGraphFrame.report(_owner, "");
        }
        if (dependents) {
            BasicGraphFrame
            .report(_owner, "Generating dependency information.");
            result.addAll(ActorDependencies.dependents(actor, clazz));
            BasicGraphFrame.report(_owner, "");
        }
        return result;
    }

    /** Return a URL that points to the help page.
     *  @return A URL that points to the help page
     */
    @Override
    protected URL _getHelpURL() {
        URL helpURL = getClass().getClassLoader().getResource(
                "ptolemy/vergil/basic/doc/DependencyResultsDialog.htm");
        return helpURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Default renderer for results table. */
    private static class DependencyResultsNamedObjRenderer extends
    DefaultTableCellRenderer {
        // FindBugs indicates that this should be a static class.

        @Override
        public void setValue(Object value) {
            String fullName = ((NamedObj) value).getFullName();
            String strippedName = fullName.substring(fullName.indexOf(".", 1));
            setText(strippedName);
        }
    }
}
