/* Information for a web application running on a web server.

 Copyright (c) 2013-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.http;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Servlet;

import org.eclipse.jetty.util.resource.Resource;

import ptolemy.data.expr.FileParameter;

///////////////////////////////////////////////////////////////////
////WebApplicationInfo

/** Information for a web application running on a web server.  Used by
 * {@link org.ptolemy.ptango.lib.WebServerUtilities}
 *
 *   @author Elizabeth Latronico and Edward A. Lee
 *   @version $Id$
 *   @since Ptolemy II 10.0
 *   @Pt.ProposedRating Red (ltrnc)
 *   @Pt.AcceptedRating Red (ltrnc)
 */

public class WebApplicationInfo {

    /** Construct a new instance of this class with the given model name, an
     * application path, and a temporary file location.
     *
     * @param modelName  The full name of the model
     * @param applicationPath  The base path for the application
     * @param temporaryFileLocation  The directory where temporary files are
     * stored
     * @exception Exception If the model does not yet have a name (for example,
     *  a new unsaved model)
     */
    public WebApplicationInfo(String modelName, String applicationPath,
            FileParameter temporaryFileLocation) throws Exception {

        // Throw an exception if the model does not have a name (for example,
        // if a new unnamed model is not saved yet).
        // WebApplicationInfo is not a Nameable, so we can't use
        // IllegalActionException here.  The caller can catch this exception
        // and wrap it in an IllegalActionException
        if (modelName == null || modelName.isEmpty()) {
            throw new Exception("A model must have a name to create a web "
                    + "application.  Perhaps this is a new, unnamed model?  "
                    + "Please save the model with a name.");
        }

        _modelName = modelName;

        // Assign a default application path of "/" if none is given
        if (applicationPath == null || applicationPath.isEmpty()) {
            _applicationPath = new URI("/");
        } else {
            _applicationPath = new URI(applicationPath);
        }

        // The temporary file location may be null or empty.
        if (temporaryFileLocation == null) {
            _temporaryFileLocation = null;
        } else {
            _temporaryFileLocation = temporaryFileLocation;
        }

        // Initialize lists
        _servletInfo = new HashMap<URI, Servlet>();
        _resourceInfo = new HashMap<URI, HashSet<Resource>>();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new servlet path to the set of paths of this web application.
     * Null, empty or duplicate paths are not allowed.  Return true if the path
     * was added successfully, or false if the path was null, empty or a
     * duplicate
     *
     * @param servletPath  The new servlet path to add
     * @param servlet The servlet to associated with the servlet path
     * @exception Exception If the path has already been requested by another servlet
     */
    public void addServletInfo(URI servletPath, Servlet servlet)
            throws Exception {
        if (_servletInfo == null) {
            _servletInfo = new HashMap<URI, Servlet>();
        }

        // Check if this path is null, empty or has already been taken by
        // another controller or by a resource handler

        // It is generally OK if this path is a prefix of an existing path, or
        // an existing path is a prefix of the new path.  This is common in
        // RESTful web design.  The HTPP request will be matched to the most
        // specific path.  There might be a rare case where the designer
        // intended a less-specific path to handle all requests - in this
        // scenario, the new controller with the more-specific path will
        // erroneously handle some of the requests.
        if (servletPath == null || servletPath.toString().isEmpty()
                || _servletInfo.containsKey(servletPath)
                || _resourceInfo.keySet().contains(servletPath)) {
            throw new Exception("Duplicate path requested by a servlet, "
                    + servletPath
                    + " . Please check other servlet paths and resource "
                    + "paths for matches.");
        }

        _servletInfo.put(servletPath, servlet);
    }

    /** Add a new resource path and location set for this web application.
     * A null path is not allowed.  An empty path is OK (since apps may share a
     * resource handler, it can make sense to map the handler to the root path).
     * If the path is a duplicate of a path already assigned to a resource
     * handler, the resource location list for that path will be updated. The
     * path may not be a duplicate of a path already assigned to a context
     * handler.
     *
     * There should be at least one resource location.
     *
     * @param resourcePath  The new resource path to add.  May not be null.
     * @param resourceLocations The non-empty set of resource locations to add
     * @exception Exception If this path is a duplicate of a path already requested by
     * a servlet, or if the resource does not exist
     */
    public void addResourceInfo(URI resourcePath,
            Set<Resource> resourceLocations) throws Exception {
        if (resourcePath == null || _servletInfo.containsKey(resourcePath)
                || resourceLocations == null || resourceLocations.isEmpty()) {
            throw new Exception("Duplicate path requested by a resource, "
                    + resourcePath
                    + " . Please check servlet paths for matches.");
        }

        for (Resource resource : resourceLocations) {
            if (!resource.exists()) {
                throw new Exception("Resource " + resource.getName()
                        + " does not exist. Please check the path and "
                        + "read permission.");
            }
        }

        if (_resourceInfo.containsKey(resourcePath)) {
            _resourceInfo.get(resourcePath).addAll(resourceLocations);
        } else {
            _resourceInfo.put(resourcePath, new HashSet(resourceLocations));
        }
    }

    /** Get the base path of this web application.
     *
     * @return The base path of this web application
     * @see #setApplicationPath(URI)
     */
    public URI getApplicationPath() {
        return _applicationPath;
    }

    /** Get the set of servlet paths assigned to this web application and their
     * corresponding servlets. May be empty.
     *
     * @return  The set of servlet paths assigned to this web application and
     * their corresponding servlets.  May be empty.
     */
    public HashMap<URI, Servlet> getServletInfo() {
        return _servletInfo;
    }

    /** Get the full model name of the model that is running the web application.
     *
     * @return  The full model name of the model that is running the web
     * application
     * @see #setModelName(String)
     */
    public String getModelName() {
        return _modelName;
    }

    /** Get the set of resource paths and their associated locations.
     *
     * @return The set of resource paths and their associated locations
     */
    public HashMap<URI, HashSet<Resource>> getResourceInfo() {
        return _resourceInfo;
    }

    /** Get the location where temporary files should be stored.  May be empty
     * or null if the web application does not use temporary files.
     *
     * @return  The location where temporary files should be stored.  May be
     * empty or null if the web application does not use temporary files
     * @see #setTemporaryFileLocation(FileParameter)
     */
    public FileParameter getTemporaryFileLocation() {
        return _temporaryFileLocation;
    }

    /** Returns true if the web application has already mapped a handler to the
     * given URL; returns false otherwise.  The handler can be any handler -
     * for example, a ContextHandler or a ResourceHandler.  This check is
     * purposefully NOT case-sensitive.  While URLs are case-sensitive
     * (excluding the domain name), many users are not aware of this,
     * and it would be confusing to have two URLs that differ only in case.
     *
     * The application path should be included as part of this URL, e.g.
     * /webapp/start for service /start of application /webapp
     * @param path  The URL to check for existing mappings
     * @return True if the web application has already mapped a handler to the
     * given URL; false otherwise
     */
    public boolean hasPath(String path) {

        // Check for a null application path.  This is not allowed, but in case
        // it was just not set yet, use default "/"
        String appPath;
        if (_applicationPath == null) {
            appPath = "/";
        } else {
            appPath = _applicationPath.toString();
        }

        // Check for a match to the application path
        if (path.equalsIgnoreCase(appPath)) {
            return true;
        }

        // Check for a matching handler path
        for (URI servletPath : _servletInfo.keySet()) {
            if (servletPath != null
                    && path.equalsIgnoreCase(appPath + servletPath.toString())) {
                return true;
            }
        }

        // Check for a matching resource path
        for (URI resourcePath : _resourceInfo.keySet()) {
            if (resourcePath != null
                    && path.equalsIgnoreCase(appPath + resourcePath.toString())) {
                return true;
            }
        }

        return false;
    }

    /** Returns true if the web application has already mapped a handler to the
     * given URL; returns false otherwise.  See {@link #hasPath(String path)}
     *
     * @param path The URL to check for existing mappings
     * @return True if the web application has already mapped a handler to the
     * given URL; false otherwise
     */
    public boolean hasPath(URI path) {
        return hasPath(path.toString());
    }

    /** Returns true if the web application has already mapped a resource
     * handler to the given path.  Used by the web server to determine if a new
     * resource handler is needed or if an existing one can be re-used.
     *
     * @param path The URL to check for existing mappings to a resource handler
     * @return True if the web application has already mapped a resource handler
     * to the given URL
     */
    public boolean hasResourcePath(String path) {
        // Check for a null application path.  This is not allowed, but in case
        // it was just not set yet, use default "/"
        String appPath;
        if (_applicationPath == null) {
            appPath = "/";
        } else {
            appPath = _applicationPath.toString();
        }

        // Check for a matching resource path
        for (URI resourcePath : _resourceInfo.keySet()) {
            if (resourcePath != null
                    && path.equalsIgnoreCase(appPath + resourcePath.toString())) {
                return true;
            }
        }

        return false;
    }

    /** Set the base path of this application.
     *
     * @param applicationPath  The new base path to this application
     * @see #getApplicationPath()
     */
    public void setApplicationPath(URI applicationPath) {
        _applicationPath = applicationPath;
    }

    /** Record the full model name from the model that is running the web
     * application.
     *
     * @param modelName  The full model name of the model that is running the
     * web application
     * @see #getModelName()
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /** Set the location where temporary files should be stored.  May be null if
     * the web application does not use temporary files.
     *
     * @param temporaryFileLocation  The location where temporary files should
     * be stored.  May be null if the web application does not use temporary
     * files
     * @see #getTemporaryFileLocation()
     */
    public void setTemporaryFileLocation(FileParameter temporaryFileLocation) {
        _temporaryFileLocation = temporaryFileLocation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The base path of this application.  For example, an application with the
     *  path /modelName on the host localhost at port 8080 would be accessed at
     *  URL http://localhost:8080/modelName
     *  There should be one application path per model, since the application
     *  path is defined in {@link org.ptolemy.ptango.lib.WebServer} which may
     *  only have one instance per model.
     *
     *  See also comments in {@link org.ptolemy.ptango.lib.WebServer}
     */
    private URI _applicationPath;

    /** The full name of the Ptolemy model that this web app was created from.
     */
    private String _modelName;

    /** A map associating a resource path with one or more resource locations.
     *
     * A web application often hosts static content such as HTML pages and
     * images.  This content is server by a resource handler.  The resource
     * handler mapped to a URL, the resource path.  For example, the file
     * image.gif served by a resource handler with path /files in the
     * application with path /modelName on host localhost at port 8080 would be
     * accessed at URL http://localhost:8080/modelName/files/image.gif
     *
     * This full URL path must be unique on the server and may not be a prefix
     * of any other unique path.  This ensures that there is a unique handler
     * for each HTTP request.  One
     * {@link org.eclipse.jetty.server.handler.ResourceHandler}
     * will be allocated per resource path.  Models may share a ResourceHandler
     * by specifying the same resource path.
     *
     * A resource location is a directory where resources (e.g. files) are
     * stored.  A ResourceHandler may have one or more resource locations.
     * Upon receiving a request, the ResourceHandler will search the resource
     * locations for the file requested.
     *
     * Note that the order of resource location searching is unspecified by
     * Jetty.  Therefore, if two models share a ResourceHandler, and serve
     * files with the same name but that are in different resource locations,
     * an incorrect file may be returned.  In this scenario, the models should
     * use different ResourceHandlers by specifying different resource paths.
     *
     * May be an empty list, if the application does not serve any static
     * content or temporary files.
     *
     * See also comments in {@link org.ptolemy.ptango.lib.WebServer}
     */
    private HashMap<URI, HashSet<Resource>> _resourceInfo;

    /** A map associating servlet paths with servlets.
     *
     * A web application is typically composed of multiple servlets that
     * handle requests submitted to a specific sub-path prefix.  For example,
     * a servlet that handles requests at the path prefix /start on the host
     * localhost at port 8080 in the application at path /modelName would be
     * accessed at URL http://localhost:8080/modelName/start
     *
     * This full URL path must be unique on the server and may not be a prefix
     * of any other unique path.  This ensures that there is a unique handler
     * for each HTTP request.  One
     * {@link org.eclipse.jetty.server.handler.ContextHandler}
     * will be allocated per servlet path.  Since the servlet
     * functionality is different for each web service actor, two separate
     * actors may not specify the same servlet paths.
     *
     * May be an empty list, perhaps if the application only wants to serve
     * static content using a ResourceHandler.
     *
     * See also comments in {@link org.ptolemy.ptango.lib.WebServer}
     */
    private HashMap<URI, Servlet> _servletInfo;

    /** The location, typically the directory $TMPDIR, that temporary files are
     * stored in.  A temporary file is a file that should be deleted upon model
     * wrapup, for example, and image created by HTMLModelExporter.  All
     * ResourceHandlers for this model should also search here for resources.
     * There is one temporary file location per model, since the temporary
     * file location is defined in the {@link org.ptolemy.ptango.lib.WebServer},
     * which may only have one instance per model.
     *
     * May be null or empty if the model does not store temporary files.
     */
    private FileParameter _temporaryFileLocation;
}
