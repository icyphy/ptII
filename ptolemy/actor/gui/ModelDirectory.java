/* A directory of open models.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ModelDirectory
/**
A directory of open models. This is a static class, with no instances
nor any way to create instances.  It is static so that any application
that opens Ptolemy models can use it to make sure that a model is not
opened more than once.  The key for each entry is any object, often an
instance of String.  Typical choices (which depend on the application)
are the fully qualified class name, or the canonical URL or file name
for a MoML file that describes the model.

@author Edward A. Lee
@version $Id$
*/
public class ModelDirectory {

    /** Private constructor prevents instances from being created.
     */
    private ModelDirectory() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Change a key from the one specified to the new one.
     *  Find all the instances of WindowAttribute in the model
     *  corresponding to the old key and change the titles on
     *  the associated windows to the string returned by the
     *  toString() method of the new key.  If the old key
     *  does not exist, then do nothing.  If the old key and the
     *  new key are the same (as tested by their equals() method),
     *  then also do nothing.
     *  @param oldKey The old key.
     *  @param newKey The new key.
     */
    public static void changeKey(Object oldKey, Object newKey) {
        CompositeActor model = get(oldKey);
        if (model != null && !newKey.equals(oldKey)) {
            put(newKey, model);
            remove(oldKey);
        }
        Iterator attributes =
               model.attributeList(WindowAttribute.class).iterator();
        while (attributes.hasNext()) {
            WindowAttribute attribute = (WindowAttribute)attributes.next();
            attribute.getFrame().setKey(newKey);
        }            
    }

    /** Return the collection of models in the directory.
     *  @returns The collection of models in the directory.
     */
    public static Collection models() {
        return _directory.values();
    }

    /** Return the model with the specified key, or null if there is no
     *  no such model.
     *  @param key A key identifying the model.
     *  @returns The model with the specified key.
     */
    public static CompositeActor get(Object key) {
        return (CompositeActor)_directory.get(key);
    }

    /** Return the set of keys in the directory.
     *  @returns The set of keys in the directory.
     */
    public static Set keySet() {
        return _directory.keySet();
    }

    /** Add a model to the directory with the specified key.
     *  @param key A key identifying the model.
     *  @param model The model.
     */
    public static void put(Object key, CompositeActor model) {
        _directory.put(key, model);
    }

    /** If the specified key is present in the directory, then find
     *  all instances of WindowAttribute that it contains, and make
     *  sure they are visible; otherwise, read the specified stream
     *  by delegating to the registered model reader.
     *  If no model reader has been registered, then do nothing.
     *  It is up to the model reader to register the model with this
     *  class by calling put().
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input stream.
     *  @param key The key to use to uniquely identify the model.
     *  @exception IOException If the stream cannot be read.
     */
    public static void read(URL base, URL in, Object key)
            throws IOException {
        CompositeActor model = get(key);
        if (model == null) {
            if (_modelReader != null) {
                _modelReader.read(base, in, key);
            }
        } else {
            // Model already exists.
            Iterator attributes =
                   model.attributeList(WindowAttribute.class).iterator();
            while (attributes.hasNext()) {
                WindowAttribute attribute = (WindowAttribute)attributes.next();
                // FIXME: Is this the right thing to do?
                attribute.getFrame().toFront();
            }
        }
    }

    /** Remove the model with the specified key.  If there are no more
     *  models in the directory, then exit the application.
     *  If there is no such model, do nothing.
     *  @param key A key identifying the model.
     */
    public static void remove(Object key) {
        _directory.remove(key);
        if (_directory.size() == 0) {
            System.exit(0);
        }
    }

    /** Specify the object to which reading of a model will be delegated.
     *  @param reader The object that will handle reading a model.
     */
    public static void setModelReader(ModelReader reader) {
        _modelReader = reader;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The directory.
    private static HashMap _directory = new HashMap();

    // The model reader, if one has been registered.
    private static ModelReader _modelReader = null;
}
