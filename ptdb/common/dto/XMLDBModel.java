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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * The Model that need to be stored or retrieved from the database.
 *
 * <p>
 * It is used as a data transfer object and hold 4 information about the model:
 * <br>- Model name.
 * <br>- Model content in xml format.
 * <br>- Does the model exist in the database or it is a new model.
 * <br>- List of parents for the current model.
 * <br>Each of the 4 information has its own getter and setter methods.
 * </p>
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */
public class XMLDBModel implements Comparable {

    /** String for DBModelId. */
    public static final String DB_MODEL_ID_ATTR = "DBModelId";
    /** String for DBReference. */
    public static final String DB_REFERENCE_ATTR = "DBReference";
    /** String for model name. */
    public static final String DB_MODEL_NAME = "name";

    /**
     * Construct a XMLDBModel instance
     * with the given model name.
     *
     * @param modelName the name for the given model.
     */
    public XMLDBModel(String modelName) {
        this._modelName = modelName;
    }

    /**
     * Construct a XMLDBModel instance
     * with the given model name.
     *
     * @param modelName Name for the given model.
     * @param modelId Id for the given model.
     */
    public XMLDBModel(String modelName, String modelId) {
        this._modelName = modelName;
        this._modelId = modelId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the given parent list to the model's
     * parent list.
     * @param list a List of parents to be added.
     */
    public void addParentList(List<XMLDBModel> list) {
        if (_listParents == null) {
            _listParents = new ArrayList<List<XMLDBModel>>();
            _parentsMap = new HashMap<String, Integer>();
        }

        String parentsMapKey = _createParentHierarchyString(list);
        if (_parentsMap.containsKey(parentsMapKey)) {
            int count = _parentsMap.get(parentsMapKey);
            count++;
            _parentsMap.put(parentsMapKey, count);
        } else {
            _listParents.add(list);
            _parentsMap.put(parentsMapKey, 1);
        }

    }

    /**
     * Add the given child entity to the model's
     * referenced children list.
     * @param modelId Child entity to be added to the referenced children list.
     */
    public void addReferencedChild(String modelId) {
        if (_listReferencedChildren == null) {
            _listReferencedChildren = new ArrayList<String>();
        }

        _listReferencedChildren.add(modelId);
    }

    /**
     * Compare this model with another given model, and the model is compared
     * according to their name.
     *
     * @param otherModel The other model to be compared with this model.
     * @return The value to indicate the result of comparison. If the returned
     * value is larger than 0, it means the otherModel is smaller.  If the
     * returned value is less than 0, it means the otherModel is larger. If 0
     * is returned, it means these two models are equal.
     */

    @Override
    public int compareTo(Object otherModel) {

        return _modelName.compareToIgnoreCase(((XMLDBModel) otherModel)
                .getModelName());
    }

    /** Return true if this XMLDBModel has the same
     *  name as the argument.
     *  @param xmldbModel The XMLDBModel object to be compared.
     *  @return True if the two XMLDBModels have the same name.
     */
    @Override
    public boolean equals(Object xmldbModel) {
        // See http://www.technofundo.com/tech/java/equalhash.html

        // Findbugs says:
        // "Eq: Class defines compareTo(...) and uses Object.equals()
        // (EQ_COMPARETO_USE_OBJECT_EQUALS)"

        // "This class defines a compareTo(...) method but
        // inherits its equals() method from
        // java.lang.Object. Generally, the value of compareTo
        // should return zero if and only if equals returns
        // true. If this is violated, weird and unpredictable
        // failures will occur in classes such as
        // PriorityQueue. In Java 5 the PriorityQueue.remove
        // method uses the compareTo method, while in Java 6 it
        // uses the equals method.

        // "From the JavaDoc for the compareTo method in the Comparable
        // interface:"

        // "It is strongly recommended, but not strictly required that
        // (x.compareTo(y)==0) == (x.equals(y)). Generally speaking,
        // any class that implements the Comparable interface and
        // violates this condition should clearly indicate this
        // fact. The recommended language is "Note: this class has a
        // natural ordering that is inconsistent with equals." "
        if (xmldbModel == this) {
            return true;
        }
        if (xmldbModel == null || xmldbModel.getClass() != getClass()) {
            return false;
        } else {
            return compareTo(xmldbModel) == 0;
        }
    }

    /** Return the hash code for the XMLDBModel object.
     *  @return The hash code for this XMLDBModel object.
     */
    @Override
    public int hashCode() {
        // See http://www.technofundo.com/tech/java/equalhash.html
        int hashCode = 31;
        if (_isNew) {
            hashCode = 31 * hashCode + 1;
        }
        if (_listParents != null) {
            hashCode = 31 * hashCode + _listParents.hashCode();
        }
        if (_parentsMap != null) {
            hashCode = 31 * hashCode + _parentsMap.hashCode();
        }
        if (_listReferencedChildren != null) {
            hashCode = 31 * hashCode + _listReferencedChildren.hashCode();
        }
        if (_modelContent != null) {
            hashCode = 31 * hashCode + _modelContent.hashCode();
        }
        if (_modelName != null) {
            hashCode = 31 * hashCode + _modelName.hashCode();
        }
        if (_modelId != null) {
            hashCode = 31 * hashCode + _modelId.hashCode();
        }
        return hashCode;
    }

    /**
     * Return True or false based on if the model is new or it exists in the database.
     * @return True or false based on if the model is new or it exists in the database.
     *
     * @see #setIsNew
     */
    public boolean getIsNew() {
        return _isNew;
    }

    /**
     * Return the model content.
     * @return A string representation of the model content.
     *
     * @see #setModel
     */
    public String getModel() {
        return _modelContent;
    }

    /**
     * Return the model id.
     * @return The model id.
     *
     * @see #setModelId
     */
    public String getModelId() {
        return _modelId;
    }

    /**
     * Return the model name.
     * @return The model name.
     *
     * @see #setModelName
     */
    public String getModelName() {
        return _modelName;
    }

    /**
     * Return the number of times the model is referenced in the
     * given hierarchy.
     *
     * @param list List of models in the parent hierarchy.
     * @return the number of times the model is referenced in the given
     * hierarchy.
     */
    public int getReferenceCount(List<XMLDBModel> list) {
        String parentsMapKey = _createParentHierarchyString(list);
        int count = 0;
        if (_parentsMap.containsKey(parentsMapKey)) {
            count = _parentsMap.get(parentsMapKey);
        }
        return count;
    }

    /**
     * Return the parents for the current model.
     * @return List of parents models for the current model.
     *
     * @see #setParents
     */
    public List<List<XMLDBModel>> getParents() {
        return _listParents;
    }

    /**
     * Return the first level referenced children entities
     * for the current model.
     *
     * @return List of first level referenced children entities
     * for the current model.
     *
     * @see #setReferencedChildren
     */
    public List<String> getReferencedChildren() {
        return _listReferencedChildren;
    }

    /**
     * Set the isNew variable which indicates if the model is in the database or
     * it is new model.
     * @param isNew a boolean True or false value to set the isNew member variable.
     *
     * @see #getIsNew
     */
    public void setIsNew(boolean isNew) {
        _isNew = isNew;
    }

    /**
     * Set the model content.
     * @param modelContent The model content in xml format.
     *
     * @see #getModel
     */
    public void setModel(String modelContent) {
        _modelContent = modelContent;
    }

    /**
     * Set the model identifier.
     * @param modelId The model identifier.
     *
     * @see #getModelId
     */
    public void setModelId(String modelId) {
        _modelId = modelId;
    }

    /**
     * Set the model name.
     * @param modelName The model name.
     *
     * @see #getModelName
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /**
     * Set the parents for the current model.
     * @param listParents List of parents for this model.
     *
     * @see #getParents
     */
    public void setParents(List<List<XMLDBModel>> listParents) {
        _listParents = listParents;
    }

    /**
     * Set the first level referenced children entities  for the current model.
     * @param listChildren List of first level referenced children entities
     * for this model.
     *
     * @see #getReferencedChildren
     */
    public void setReferencedChildren(List<String> listChildren) {
        _listReferencedChildren = listChildren;
    }

    /**
     * Get the String representation of this model.
     *
     * @return The String representation of this model.
     */

    @Override
    public String toString() {

        return super.toString() + "@ModelName:" + _modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _createParentHierarchyString(List<XMLDBModel> list) {
        StringBuilder hierarchyStringBuilder = new StringBuilder();

        for (XMLDBModel model : list) {
            hierarchyStringBuilder.append(model.getModelName()).append(">");
        }

        return hierarchyStringBuilder.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * True or false value to indicate whether the model is in the database or
     * a new model.
     */
    private boolean _isNew;

    /** List of all the parents for the current model. */
    private List<List<XMLDBModel>> _listParents;
    /** List of unique hierarchies and their counts */
    private HashMap<String, Integer> _parentsMap;
    /** List of all the first level referenced child entities for
     * the current model.
     */
    private List<String> _listReferencedChildren;

    /** The content of the model in a string. */
    private String _modelContent;

    /** Model name. */
    private String _modelName;

    /** Model id. */
    private String _modelId;

}
