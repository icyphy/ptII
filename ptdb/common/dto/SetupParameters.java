/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.common.dto;

///////////////////////////////////////////////////////////////////
////SetupParameters

/**
 * Encapsulate the parameters required for creating an XML DB connection.
 *
 * <p>The Data Transfer Object pattern is used here.</p>
 *
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class SetupParameters {

    /**
     * Construct an instance with the given parameters.
     * @param url Url/Path for the location where the database
     * file is present.
     * @param containerName Name of the container
     * for the XML Database.
     * @param cacheContainerName Name of the cache container
     * for the XML Database.
     */
    public SetupParameters(String url, String containerName,
            String cacheContainerName) {
        this._url = url;
        this._containerName = containerName;
        this._cacheContainerName = cacheContainerName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the cache container name.
     * @return The cache container name.
     * @see #setCacheContainerName
     */
    public String getCacheContainerName() {
        return _cacheContainerName;
    }

    /**
     * Return the container name.
     * for the given XML database.
     * @return The container name.
     * @see #setContainerName
     */
    public String getContainerName() {
        return _containerName;
    }

    /**
     * Return the URL/path of the location where
     * the XML database file is present.
     * @return The database url.
     * @see #setUrl
     */
    public String getUrl() {
        return _url;
    }

    /**
     * Set the cache container name parameter to the given name.
     * @param cacheContainerName The cache container name.
     * @see #getCacheContainerName
     */
    public void setCacheContainerName(String cacheContainerName) {
        _cacheContainerName = cacheContainerName;
    }

    /**
     * Set the container name parameter to the given value.
     * @param containerName Name of the container
     * for the given XML database.
     * @see #getContainerName
     */
    public void setContainerName(String containerName) {
        _containerName = containerName;
    }

    /**
     * Set the URL parameter to the given value.
     * @param url Location where the database file is present.
     * @see #getUrl
     */
    public void setUrl(String url) {
        _url = url;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The main container name.*/
    private String _containerName;

    /** The cache container name.*/
    private String _cacheContainerName;

    /** The url to the xml db location.*/
    private String _url;
}
