/*
 * Created on 01 sept. 2003
 *
 * @ProposedRating Yellow (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.actor.gui;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.ParserAttribute;
import ptolemy.util.StringUtilities;
import thales.vergil.navigable.NavigationTreeModel;

/**
 * <p>Titre : NavigableEffigy</p>
 * <p>Description : An effigy for navigable models</p>
Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * <p>Société : Thales Research and technology</p>
 * @author Jérôme Blanc & Benoit Masson
 * 01 sept. 2003
 */
public class NavigableEffigy extends PtolemyEffigy {

        private NavigationTreeModel _navigModel = null;

        /*
         * this is to fix a TableauFrame bug in the _close() method
         * @see thales.vergil.navigable.NavigableActorGraphFrame#_close()
         */
        private boolean _lieAtNumberOfOpendTableaux = false;

        /**
         * @param workspace
         */
        public NavigableEffigy(Workspace workspace) {
                super(workspace);
        }

        /**
         * @param container
         * @param name
         * @throws IllegalActionException
         * @throws NameDuplicationException
         */
        public NavigableEffigy(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
                super(container, name);
        }

        /**
         * @return
         */
        public NavigationTreeModel getNavigationModel() {
                Nameable container = getContainer();
                if (container instanceof NavigableEffigy) {
                        return ((NavigableEffigy) container).getNavigationModel();
                } else {
                        if (_navigModel == null) {
                                _navigModel = new NavigationTreeModel(getModel());
                        }
                        return _navigModel;
                }
        }

        /**
         * @param model
         */
        public void setNavigModel(NavigationTreeModel model) {
                Nameable container = getContainer();
                if (!(container instanceof NavigableEffigy)) {
                        _navigModel = model;
                }
        }

        /* (non-Javadoc)
         * @see ptolemy.actor.gui.Effigy#numberOfOpenTableaux()
         */
        /*
        * this is to fix a TableauFrame bug in the _close() method
        * @see thales.vergil.navigable.NavigableActorGraphFrame#_close()
        */
        public int numberOfOpenTableaux() {
                if (_lieAtNumberOfOpendTableaux) {
                        _lieAtNumberOfOpendTableaux = false;
                        return 1;
                }
                return super.numberOfOpenTableaux();
        }

        /*
         * this is to fix a TableauFrame bug in the _close() method
         * @see thales.vergil.navigable.NavigableActorGraphFrame#_close()
         */
        public void lieAtNumberOfOpendTableaux() {
                _lieAtNumberOfOpendTableaux = true;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         inner classes                     ////

        /** A factory for creating new Ptolemy effigies.
         */
        public static class Factory extends EffigyFactory {

                protected String _tagToCheck;

                /** Create a factory with the given name and container.
                 *  @param container The container.
                 *  @param name The name.
                 *  @exception IllegalActionException If the container is incompatible
                 *   with this entity.
                 *  @exception NameDuplicationException If the name coincides with
                 *   an entity already in the container.
                 */
                public Factory(CompositeEntity container, String name)
                        throws IllegalActionException, NameDuplicationException {
                        super(container, name);
                        _tagToCheck = "_navigable";
                }

                ///////////////////////////////////////////////////////////////
                ////                     public methods                    ////

                /** Return true, indicating that this effigy factory is
                 *  capable of creating an effigy without a URL being specified.
                 *  @return True.
                 */
                public boolean canCreateBlankEffigy() {
                        return true;
                }

                /** Create a new effigy in the given container by reading the
                 *  <i>input</i> URL. If the <i>input</i> URL is null, then
                 *  create a blank effigy.
                 *  The blank effigy will have a new model associated with it.
                 *  If this effigy factory contains an entity named "blank", then
                 *  the new model will be a clone of that entity.  Otherwise,
                 *  it will be an instance of TypedCompositeActor.
                 *  If the URL does not end with extension ".xml" or ".moml", then
                 *  return null.  If the URL points to an XML file that is not
                 *  a MoML file, then also return null.
                 *  The specified base is used to expand any relative file references
                 *  within the URL.
                 *  @param container The container for the effigy.
                 *  @param base The base for relative file references, or null if
                 *   there are no relative file references.
                 *  @param input The input URL.
                 *  @return A new instance of PtolemyEffigy, or null if the URL
                 *   does not specify a Ptolemy II model.
                 *  @exception Exception If the URL cannot be read, or if the data
                 *   is malformed in some way.
                 */
                public Effigy createEffigy(
                        CompositeEntity container,
                        URL base,
                        URL input)
                        throws Exception {
                        if (input == null) {
                                // Create a blank effigy.
                                // Use the strategy pattern so derived classes can
                                // override this.
                                PtolemyEffigy effigy =
                                        _newEffigy(container, container.uniqueName("effigy"));

                                // If this factory contains an entity called "blank", then
                                // clone that.
                                NamedObj entity = getEntity("blank");
                                NamedObj newModel;
                                if (entity != null) {
                                        newModel = (NamedObj) entity.clone(new Workspace());
                                } else {
                                        newModel = new TypedCompositeActor(new Workspace());
                                }

                                // The model should have a parser associated with it
                                // so that undo works.

                                // Checking to see if there already is a _parser attribute
                                // might be overkill, but it is safer.
                                ParserAttribute parserAttribute =
                                        (ParserAttribute) newModel.getAttribute(
                                                "_parser",
                                                ParserAttribute.class);
                                if (parserAttribute == null) {
                                        parserAttribute = new ParserAttribute(newModel, "_parser");
                                        MoMLParser parser = new MoMLParser();
                                        // Make sure that the MoMLParser._modified flag is reset
                                        // If we don't call reset here, then the second time
                                        // the code generator is run, we will be prompted to
                                        // save the model because the first time we ran
                                        // the code generator the model was marked as modified.
                                        parser.reset();

                                        parserAttribute.setParser(parser);
                                }

                                // The name might be "blank" which is confusing.
                                // Set it to an empty string.  On Save As, this will
                                // be changed to match the file name.
                                newModel.setName("");
                                effigy.setModel(newModel);

                                // THALES MODIF
                                new Attribute(newModel, "_navigable");
                                if (!modelIsValid(newModel)) {
                                        effigy.setContainer(null);
                                        effigy = null;
                                }
                                //END THALES MODIF

                                return effigy;
                        } else {
                                String extension = getExtension(input);
                                if (!extension.equals("xml") && !extension.equals("moml")) {
                                        return null;
                                }

                                // THALES MODIF
                                if (checkFile(input)) {

                                        // Create a blank effigy.
                                        PtolemyEffigy effigy =
                                                _newEffigy(container, container.uniqueName("effigy"));

                                        MoMLParser parser = new MoMLParser();

                                        // Make sure that the MoMLParser._modified flag is reset
                                        // If we don't call reset here, then the second time
                                        // the code generator is run, we will be prompted to
                                        // save the model because the first time we ran
                                        // the code generator the model was marked as modified.
                                        parser.reset();

                                        NamedObj toplevel = null;
                                        try {
                                                try {
                                                        // If the following fails, we should remove the effigy.
                                                        try {
                                                                toplevel = parser.parse(base, input);
                                                        } catch (IOException io) {
                                                                // If we are running under Web Start, we
                                                                // might have a URL that refers to another
                                                                // jar file.
                                                                URL anotherURL =
                                                                        JNLPUtilities.jarURLEntryResource(
                                                                                input.toString());
                                                                if (anotherURL != null) {
                                                                        toplevel =
                                                                                parser.parse(
                                                                                        base,
                                                                                        anotherURL.openStream());
                                                                } else {
                                                                        throw io;
                                                                }
                                                        }
                                                        if (toplevel != null) {
                                                                effigy.setModel(toplevel);

                                                                // A MoMLFilter may have modified the model
                                                                // as it was being parsed.
                                                                effigy.setModified(MoMLParser.isModified());
                                                                // The effigy will handle saving the modified
                                                                // moml for us, so MoMLParser need
                                                                // not care anymore.
                                                                MoMLParser.setModified(false);

                                                                // Identify the URI from which the model was read
                                                                // by inserting an attribute into both the model
                                                                // and the effigy.
                                                                URIAttribute uriAttribute =
                                                                        new URIAttribute(toplevel, "_uri");
                                                                URI inputURI = null;
                                                                try {
                                                                        inputURI = new URI(input.toExternalForm());
                                                                } catch (java.net.URISyntaxException ex) {
                                                                        // This is annoying, if the input has a space
                                                                        // in it, then we cannot create a URI,
                                                                        // but we could create a URL.
                                                                        // If, under Windows, we call
                                                                        // File.createTempFile(), then we are likely
                                                                        // to get a pathname that has space.
                                                                        // FIXME: Note that jar urls will barf if there
                                                                        // is a %20 instead of a space.  This could
                                                                        // cause problems in Web Start
                                                                        String inputExternalFormFixed =
                                                                                StringUtilities.substitute(
                                                                                        input.toExternalForm(),
                                                                                        " ",
                                                                                        "%20");
                                                                        try {
                                                                                inputURI =
                                                                                        new URI(inputExternalFormFixed);
                                                                        } catch (Exception ex2) {
                                                                                throw new Exception(
                                                                                        "Failed to generate "
                                                                                                + "a URI from '"
                                                                                                + input.toExternalForm()
                                                                                                + "' and from '"
                                                                                                + inputExternalFormFixed
                                                                                                + "'",
                                                                                        ex);
                                                                        }
                                                                }
                                                                uriAttribute.setURI(inputURI);
                                                                // This is used by TableauFrame in its
                                                                //_save() method.
                                                                effigy.uri.setURI(inputURI);

                                                                return effigy;
                                                        } else {
                                                                effigy.setContainer(null);
                                                        }
                                                } catch (Throwable throwable) {
                                                        if (throwable instanceof StackOverflowError) {
                                                                Throwable newThrowable =
                                                                        new StackOverflowError(
                                                                                "StackOverflowError: "
                                                                                        + "Which often indicates that a class "
                                                                                        + "could not be found, but there was "
                                                                                        + "possibly a moml file with that same "
                                                                                        + "name in the directory that referred "
                                                                                        + "to the class, so we got into a loop."
                                                                                        + "For example: We had "
                                                                                        + "actor/lib/joystick/Joystick.java "
                                                                                        + "and "
                                                                                        + "actor/lib/joystick/joystick.xml, "
                                                                                        + "but "
                                                                                        + "the .class file would not load "
                                                                                        + "because of a classpath problem, "
                                                                                        + "so we kept "
                                                                                        + "loading joystick.xml which "
                                                                                        + "referred to Joystick and because "
                                                                                        + "of Windows "
                                                                                        + "filename case insensitivity, "
                                                                                        + "we found joystick.xml, which put "
                                                                                        + "us in a loop.");
                                                                newThrowable.initCause(throwable);
                                                                throwable = newThrowable;
                                                        }

                                                        throwable.printStackTrace();
                                                        // The finally clause below can result in the
                                                        // application exiting if there are no other
                                                        // effigies open.  We check for that condition,
                                                        // and report the error here.  Otherwise, we
                                                        // pass the error to the caller.
                                                        ModelDirectory dir =
                                                                (ModelDirectory) effigy
                                                                        .topEffigy()
                                                                        .getContainer();
                                                        List effigies = dir.entityList(Effigy.class);

                                                        // We might get to here if we are running a
                                                        // vergil with a model specified as a command
                                                        // line argument and the model has an invalid
                                                        // parameter.

                                                        // We might have three effigies here:
                                                        // 1) .configuration.directory.configuration
                                                        // 2) .configuration.directory.user library
                                                        // 3) .configuration.directory.effigy
                                                        // Note that one of the effigies is the configuration
                                                        // itself, which does not prevent exiting the app.
                                                        // Hence, we handle the error if there are 3 or fewer.

                                                        if (effigies.size() <= 3) {

                                                                // FIXME: This could cause problems with
                                                                // systems that do not load the user
                                                                // library.  Currently, VergilApplication
                                                                // loads the user library, but other
                                                                // applications like PtolemyApplication do
                                                                // not.  We could check to see if the
                                                                // names of two of the three Effigies were
                                                                // .configuration.directory.configuration
                                                                // and.configuration.directory.user
                                                                // library, but this seems like overkill.

                                                                String errorMessage = "Failed to read " + input;
                                                                System.err.println(errorMessage);
                                                                throwable.printStackTrace();
                                                                MessageHandler.error(errorMessage, throwable);
                                                        } else {
                                                                if (throwable instanceof Exception) {
                                                                        // Let the caller handle the error.
                                                                        throw (Exception) throwable;

                                                                } else {
                                                                        // If we have a parameter that has a backslash
                                                                        // then we might get a data.expr.TokenMgrError
                                                                        // which is an error, so we rethrow this
                                                                        // FIXME: createEffigy() should be
                                                                        // declared to throw Throwable, but that
                                                                        // results in lots of changes elsewhere.
                                                                        throw new Exception(throwable);
                                                                }
                                                        }
                                                }
                                        } finally {
                                                // If we failed to populate the effigy with a model,
                                                // then we remove the effigy from its container.
                                                if (toplevel == null) {
                                                        effigy.setContainer(null);
                                                }
                                        }
                                }
                                //END THALES MODIF
                                return null;
                        }
                }

                private boolean modelIsValid(NamedObj model) {
                        return model.getAttribute(_tagToCheck) != null;
                }

                private boolean checkFile(URL url) {
                        boolean answer = false;

                        BufferedReader reader;
                        try {
                                reader =
                                        new BufferedReader(new InputStreamReader(url.openStream()));
                                String aLine = "";
                                while ((aLine = reader.readLine()) != null) {
                                        if (aLine.indexOf("<property") != -1
                                                && (aLine.indexOf("name=\"" + _tagToCheck + "\"") != -1
                                                        || aLine.indexOf("name='" + _tagToCheck + "'")
                                                                != -1)) {
                                                answer = true;
                                                break;
                                        }
                                }
                        } catch (IOException e1) {
                                e1.printStackTrace();
                        }

                        return answer;
                }

                /** Create a new effigy.  We use the strategy pattern here
                 *  so that derived classes can easily override the exact class
                 *  that is created.
                 *  @param container The container for the effigy.
                 *  @param name The name.
                 *  @return A new effigy.
                 */
                protected NavigableEffigy _newEffigy(
                        CompositeEntity container,
                        String name)
                        throws IllegalActionException, NameDuplicationException {
                        return new NavigableEffigy(container, name);
                }
        }

        /** A factory for creating new Ptolemy effigies, but without the
         *  capability of creating a new blank effigy.  Use this factory
         *  in a configuration if you do not want the factory to appear
         *  in the File->New menu.
         */
        public static class FactoryWithoutNew extends Factory {

                /** Create a factory with the given name and container.
                 *  @param container The container.
                 *  @param name The name.
                 *  @exception IllegalActionException If the container is incompatible
                 *   with this entity.
                 *  @exception NameDuplicationException If the name coincides with
                 *   an entity already in the container.
                 */
                public FactoryWithoutNew(CompositeEntity container, String name)
                        throws IllegalActionException, NameDuplicationException {
                        super(container, name);
                }

                ///////////////////////////////////////////////////////////////
                ////                     public methods                    ////

                /** Return false, indicating that this effigy factory is not
                 *  capable of creating an effigy without a URL being specified.
                 *  @return False.
                 */
                public boolean canCreateBlankEffigy() {
                        return false;
                }
        }
}
