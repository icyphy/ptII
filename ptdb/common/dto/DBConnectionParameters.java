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
//// DBConnectionParameters
/**
 * Encapsulate the parameters required for creating an XML DB connection.
 *
 * <p>The Data Transfer Object pattern is used here.</p>
 *
 * @author Ashwini Bijwe
 *
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 *
 */
public class DBConnectionParameters {

    /**
     * Construct an instance with the given parameters.
     * @param url Url/Path for the location where the database
     * file is present
     * @param containerName Name of the container
     * for the XML Database.
     *
     * @param isTransactionRequired To specify
     * whether the transaction management is required for the
     * connection that is created.
     */
    public DBConnectionParameters(String url, String containerName,
            boolean isTransactionRequired) {
        this._url = url;
        this._containerName = containerName;
        this._isTransactionRequired = isTransactionRequired;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return the container name
     * for the given XML database.
     * @see #setContainerName
     * @return The container name.
     */
    public String getContainerName() {
        return _containerName;
    }

    /**
     * Return the URL/path of the location where
     * the XML database file is present.
     * @see #setUrl
     * @return The database url.
     */
    public String getUrl() {
        return _url;
    }

    /**
     * Return true if the connection is to be
     * created with transaction
     * and false if it is to be created without transaction.
     *
     * @return True if transaction is required, false otherwise.
     */
    public boolean isTransactionRequired() {
        return _isTransactionRequired;
    }

    /**
     * Set the container name parameter to the given value.
     * @see #getContainerName
     * @param containerName Name of the container
     * for the given XML database.
     */
    public void setContainerName(String containerName) {
        _containerName = containerName;
    }

    /**
     * Set the transaction required parameter to the given value.
     * @param isTransactionRequired To specify whether the transaction
     * management is required for the connection that is created.
     */
    public void setIsTransactionRequired(boolean isTransactionRequired) {
        _isTransactionRequired = isTransactionRequired;
    }

    /**
     * Set the URL parameter to the given value.
     * @see #getUrl
     * @param url Location where the database file is present.
     */
    public void setUrl(String url) {
        _url = url;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _containerName;
    private boolean _isTransactionRequired;
    private String _url;
    /*
    private String _userName;
    private String _password;
     */

}
