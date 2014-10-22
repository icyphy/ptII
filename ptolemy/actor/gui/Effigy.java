/* A named object that represents a ptolemy model.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.ContainmentExtender;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Effigy

/**
 An effigy represents model metadata, and is contained by the
 model directory or by another effigy. The effigy, for example,
 keeps track of where the model originated (from a URI or file)
 and whether the model has been modified since the URI or file was
 read. In design automation, such information is often called
 "metadata." When we began to design this class, we called it
 ModelModel, because it was a model of a Ptolemy II model.
 However, this name seemed awkward, so we changed it to Effigy.
 We also considered the name Proxy for the class. We rejected that
 name because of the common use of the word "proxy" in distributed
 object-oriented models.
 <p>
 The Effigy class extends CompositeEntity, so an instance of Effigy
 can contain entities.  By convention, an effigy contains all
 open instances of Tableau associated with the model. It also
 contains a string attribute named "identifier" with a value that
 uniquely identifies the model. A typical choice (which depends on
 the configuration) is the canonical URI for a MoML file that
 describes the model.  In the case of an effigy contained by another,
 a typical choice is the URI of the parent effigy, a pound sign "#",
 and a name.
 <p>
 An effigy may contain other effigies.  The master effigy
 in such a containment hierarchy is typically associated with a
 URI or file.
 Contained effigies are associated with the same file, and represent
 structured data within the top-level representation in the file.
 The masterEffigy() method returns that master effigy.
 The topEffigy() method in this base class returns the same
 master effigy. However, in derived classes, a master effigy
 may be contained by another effigy, so the top effigy is not
 the same as the master effigy. The top effigy is directly contained
 by the ModelDirectory in the Configuration.
 <p>
 NOTE: It might seem more natural for the identifier to match the name
 of the effigy rather than recording the identifier in a string attribute.
 But in Ptolemy II, an entity name cannot have periods in it, and a URI
 typically does have periods in it.

 <p> To determine the Effigy of a NamedObj, use
 {@link ptolemy.actor.gui.Configuration#findEffigy(NamedObj)}.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (celaine)
 @see ModelDirectory
 @see Tableau
 */
public class Effigy extends CompositeEntity {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public Effigy(Workspace workspace) {
        super(workspace);

        try {
            identifier = new StringAttribute(this, "identifier");
            identifier.setExpression("Unnamed");
            uri = new URIAttribute(this, "uri");
        } catch (Throwable throwable) {
            throw new InternalErrorException(this, throwable,
                    "Can't create identifier!");
        }
    }

    /** Construct an effigy with the given name and container.
     *  @param container The container.
     *  @param name The name of the effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Effigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        identifier = new StringAttribute(this, "identifier");
        identifier.setExpression("Unnamed");
        uri = new URIAttribute(this, "uri");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The identifier for the effigy.  The default value is "Unnamed". */
    public StringAttribute identifier;

    /** The URI for the effigy.  The default value is null. */
    public URIAttribute uri;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>identifier</i> parameter, then set
     *  the title of all contained Tableaux to the value of the parameter;
     *  if the argument is the <i>uri</i> parameter, then check to see
     *  whether it is writable, and call setModifiable() appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == identifier) {
            Iterator tableaux = entityList(Tableau.class).iterator();

            while (tableaux.hasNext()) {
                Tableau tableau = (Tableau) tableaux.next();
                tableau.setTitle(identifier.getExpression());
            }
        } else if (attribute == uri) {
            URI uriValue = uri.getURI();

            if (uriValue == null) {
                // A new model, with no URI, is by default modifiable.
                _modifiableURI = true;
            } else {
                String protocol = uriValue.getScheme();

                if (!protocol.equals("file")) {
                    _modifiableURI = false;
                } else {
                    // Use just the path here in case we
                    // are passed a URI that has a fragment.
                    // If we had file:/C%7C/foo.txt#bar
                    // then bar is the fragment.  Unfortunately,
                    // new File(file:/C%7C/foo.txt#bar) will fail,
                    // so we add the path.
                    String path = uriValue.getPath();
                    if (path != null) {
                        File file = new File(path);

                        try {
                            if (path.indexOf("%20") == -1) {
                                _modifiableURI = file.canWrite();
                            } else {
                                // FIXME: we need a better way to check if
                                // a URL is writable.

                                // Sigh.  If the filename has spaces in it,
                                // then the URL will have %20s.  However,
                                // the file does not have %20s.
                                // See
                                // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=153
                                path = StringUtilities.substitute(path, "%20",
                                        " ");
                                file = new File(path);
                                _modifiableURI = file.canWrite();
                            }
                        } catch (java.security.AccessControlException accessControl) {
                            // If we are running in a sandbox, then canWrite()
                            // may throw an AccessControlException.
                            _modifiableURI = false;
                        }
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Close all tableaux contained by this effigy, and by any effigies
     *  it contains.
     *  @return False if the user cancels on a save query, and true
     *   if all tableaux are successfully closed.
     */
    public boolean closeTableaux() {
        Iterator effigies = entityList(Effigy.class).iterator();

        while (effigies.hasNext()) {
            Effigy effigy = (Effigy) effigies.next();

            if (!effigy.closeTableaux()) {
                return false;
            }
        }

        Iterator tableaux = entityList(Tableau.class).iterator();

        while (tableaux.hasNext()) {
            Tableau tableau = (Tableau) tableaux.next();

            if (!tableau.close()) {
                return false;
            }
        }

        return true;
    }

    /** Find the effigy associated with the top level of the object, and if not
     *  found but the top level has a ContainmentExtender attribute, use that
     *  attribute to find the containment extender of the top level and continue
     *  the search.
     *
     *  @param object The object.
     *  @return The effigy, or null if not found.
     *  @exception IllegalActionException If attributes cannot be retrieved, or
     *   the container that an attribute points to is invalid.
     */
    public static Effigy findToplevelEffigy(NamedObj object)
            throws IllegalActionException {
        // FIXME: Should topEffigy call this method?
        NamedObj toplevel;
        do {
            toplevel = object.toplevel();
            Effigy effigy = Configuration.findEffigy(toplevel);
            if (effigy != null) {
                return effigy;
            }
            ContainmentExtender extender = (ContainmentExtender) toplevel
                    .getAttribute("_containmentExtender",
                            ContainmentExtender.class);
            object = toplevel;
            if (extender != null) {
                object = extender.getExtendedContainer();
            }
        } while (toplevel != object);
        return null;
    }

    /** Get a tableau factory that offers views of this effigy, or
     *  null if none has been specified.  The tableau factory can be
     *  used to create visual renditions of or editors for the
     *  associated model.  It can be used to find out what sorts of
     *  views are available for the model.
     *  @return A tableau factory offering multiple views.
     *  @see #setTableauFactory(TableauFactory)
     */
    public TableauFactory getTableauFactory() {
        return _factory;
    }

    /** Return a writable file for the URI given by the <i>uri</i>
     *  parameter of this effigy, if there is one, or return
     *  null if there is not.  This will return null if the file does
     *  not exist, or it exists and is not writable, or the <i>uri</i>
     *  parameter has not been set.
     *  @return A writable file, or null if one cannot be created.
     */
    public File getWritableFile() {
        File result = null;
        URI fileURI = uri.getURI();

        if (fileURI != null) {
            String protocol = fileURI.getScheme();

            if (protocol == null || protocol.equals("file")) {
                File tentativeResult = new File(fileURI);

                if (tentativeResult.canWrite()) {
                    result = tentativeResult;
                }
            }
        }

        return result;
    }

    /** Return whether the model data is modifiable.  This is delegated
     *  to the effigy returned by masterEffigy().  If this is the master
     *  effigy, then whether the data is modifiable depends on whether
     *  setModifiable() has been called, and if not, on whether there
     *  is a URI associated with this effigy and whether that URI is
     *  writable.
     *  @see #masterEffigy()
     *  @return False to indicate that the model is not modifiable.
     */
    public boolean isModifiable() {
        Effigy master = masterEffigy();
        if (!master._modifiable) {
            return false;
        } else {
            return master._modifiableURI;
        }
    }

    /** Return the data associated with the master effigy (as
     *  returned by masterEffigy()) has been modified.
     *  This method is intended to be used to
     *  keep track of whether the data in the file or URI associated
     *  with this data has been modified.  The method is called by
     *  an instance of TableauFrame to determine whether it is safe
     *  to close.
     *  @see #masterEffigy()
     *  @see #setModifiable(boolean)
     *  @return True if the data has been modified.
     */
    public boolean isModified() {
        return masterEffigy()._modified;
    }

    /** Return whether this effigy is a system effigy.  System effigies
     *  are not automatically removed when they have no tableaux.
     *  @return True if the model is a system effigy.
     */
    public boolean isSystemEffigy() {
        return _isSystemEffigy;
    }

    /** Return the effigy that is "in charge" of this effigy.
     *  In this base class, this is the same as calling topEffigy().
     *  But in derived classes, particularly PtolemyEffigy, it will
     *  be different.
     *  @see #topEffigy()
     *  @return The effigy in charge of this effigy.
     */
    public Effigy masterEffigy() {
        return topEffigy();
    }

    /** Return the total number of open tableau for this effigy
     *  effigy and all effigies it contains.
     *  @return A non-negative integer giving the number of open tableaux.
     */
    public int numberOfOpenTableaux() {
        int result = 0;
        List tableaux = entityList(Tableau.class);
        result += tableaux.size();

        List containedEffigies = entityList(Effigy.class);
        Iterator effigies = containedEffigies.iterator();

        while (effigies.hasNext()) {
            result += ((Effigy) effigies.next()).numberOfOpenTableaux();
        }

        return result;
    }

    /** Override the base class so that tableaux contained by this object
     *  are removed before this effigy is removed from the ModelDirectory.
     *  This causes the frames associated with those tableaux to be
     *  closed.  Also, if the argument is null and there is a URI
     *  associated with this model, then purge any record of the
     *  model that the MoMLParser class is keeping so that future
     *  efforts to open the model result in re-parsing.
     *  @param container The directory in which to list this effigy.
     *  @exception IllegalActionException If the proposed container is not
     *   an instance of ModelDirectory, or if the superclass throws it.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the specified name.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container == null) {
            // Remove all tableaux.
            Iterator tableaux = entityList(Tableau.class).iterator();

            while (tableaux.hasNext()) {
                ComponentEntity tableau = (ComponentEntity) tableaux.next();
                tableau.setContainer(null);
            }

            // Remove all contained effigies as well.
            Iterator effigies = entityList(Effigy.class).iterator();

            while (effigies.hasNext()) {
                ComponentEntity effigy = (ComponentEntity) effigies.next();
                effigy.setContainer(null);
            }

            if (uri != null) {
                try {
                    URL url = uri.getURL();
                    MoMLParser.purgeModelRecord(url);
                } catch (MalformedURLException e) {
                    // This might occur as a result of failure
                    // to read the URL in the first place, so we
                    // have to do nothing.
                }
            }
        }

        super.setContainer(container);
    }

    /** If the argument is false, the specify that that the model is not
     *  modifiable, even if the URI associated with this effigy is writable.
     *  This always sets a flag in the master effigy (as returned by
     *  masterEffigy()).
     *  If the argument is true, or if this method is never called,
     *  then whether the model is modifiable is determined by whether
     *  the URI can be written to.
     *  Notice that this does not automatically result in any tableaux
     *  that are contained switching to being uneditable.  But it will
     *  prevent them from writing to the URI.
     *  @see #masterEffigy()
     *  @see #isModifiable()
     *  @see #isModified()
     *  @see #setModified(boolean)
     *  @param flag False to prevent writing to the URI.
     */
    public void setModifiable(boolean flag) {
        masterEffigy()._modifiable = flag;
    }

    /** Record whether the data associated with this effigy has been
     *  modified since it was first read or last saved.  If you call
     *  this with a true argument, then subsequent calls to isModified()
     *  will return true.  This is used by instances of TableauFrame.
     *  This is recorded in the entity returned by topEntity(), which
     *  is the one associated with a file.
     *  This always sets a flag in the master effigy (as returned by
     *  masterEffigy()).
     *  @see #masterEffigy()
     *  @see #isModifiable()
     *  @see #isModified()
     *  @see #setModifiable(boolean)
     *  @param modified True if the data has been modified.
     */
    public void setModified(boolean modified) {
        // NOTE: To see who is setting this true, uncomment this:
        //if (modified == true) (new Exception("Effigy.setModified()" + this)).printStackTrace();
        masterEffigy()._modified = modified;
    }

    /** Set the effigy to be a system effigy if the given flag is true.
     *  System effigies are not removed automatically if they have no
     *  tableaux.
     *  @param isSystemEffigy True if this is to be a system effigy.
     */
    public void setSystemEffigy(boolean isSystemEffigy) {
        _isSystemEffigy = isSystemEffigy;
    }

    /** Specify a tableau factory that offers multiple views of this effigy.
     *  This can be used by a contained tableau to set up a View menu.
     *  @param factory A tableau factory offering multiple views.
     *  @see #getTableauFactory()
     */
    public void setTableauFactory(TableauFactory factory) {
        _factory = factory;
    }

    /** Make all tableaux associated with this effigy and any effigies it
     *  contains visible by raising or deiconifying them.
     *  If there is no tableau contained directly by
     *  this effigy, then create one by calling createPrimaryTableau()
     *  in the configuration.
     *  @return The first tableau encountered, or a new one if there are none.
     */
    public Tableau showTableaux() {
        Iterator effigies = entityList(Effigy.class).iterator();

        while (effigies.hasNext()) {
            Effigy effigy = (Effigy) effigies.next();
            effigy.showTableaux();
        }

        Iterator tableaux = entityList(Tableau.class).iterator();
        Tableau result = null;

        while (tableaux.hasNext()) {
            Tableau tableau = (Tableau) tableaux.next();
            tableau.show();

            if (result == null) {
                result = tableau;
            }
        }

        if (result == null) {
            // Create a new tableau.
            Configuration configuration = (Configuration) toplevel();
            result = configuration.createPrimaryTableau(this);
        }

        return result;
    }

    /** Return the top-level effigy that (deeply) contains this one.
     *  If this effigy is contained by another effigy, then return
     *  the result of calling this method on that other effigy;
     *  otherwise, return this effigy.
     *  @return The top-level effigy that (deeply) contains this one.
     */
    public Effigy topEffigy() {
        Nameable container = getContainer();

        // FIXME: Should topEffigy Effigy.findToplevelEffigy?
        if (container instanceof Effigy) {
            return ((Effigy) container).topEffigy();
        } else {
            return this;
        }
    }

    /** Write the model associated with this effigy
     *  to the specified file.  This base class throws
     *  an exception, since it does not know how to write model data.
     *  Derived classes should override this method to write model
     *  data.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    public void writeFile(File file) throws IOException {
        throw new IOException("I do not know how to write this model data.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this entity, i.e., ModelDirectory or Effigy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.
     */
    protected void _checkContainer(CompositeEntity container)
            throws IllegalActionException {
        if (container != null && !(container instanceof ModelDirectory)
                && !(container instanceof Effigy)) {
            throw new IllegalActionException(this, container,
                    "The container can only be set to an "
                            + "instance of ModelDirectory or Effigy.");
        }
    }

    /** Remove the specified entity from this container. If this effigy
     *  is a system effigy and there are no remaining tableaux
     *  contained by this effigy or any effigy it contains, then remove
     *  this object from its container.
     *  @param entity The tableau to remove.
     */
    @Override
    protected void _removeEntity(ComponentEntity entity) {
        super._removeEntity(entity);

        if (numberOfOpenTableaux() == 0 && !isSystemEffigy()) {
            try {
                setContainer(null);
            } catch (Exception ex) {
                throw new InternalErrorException(this, ex,
                        "Cannot remove effigy!");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // A tableau factory offering multiple views.
    private TableauFactory _factory = null;

    // Indicator that the effigy is a system effigy.
    private boolean _isSystemEffigy = false;

    /** Indicator that the data represented in the window has been modified. */
    private boolean _modified = false;

    /** Indicator that the URI must not be written to (if false). */
    private boolean _modifiable = true;

    /** Indicator that the URI can be written to. */
    private boolean _modifiableURI = true;
}
