/*
 *
 */
package ptdb.common.dto;

import ptolemy.data.expr.Variable;

///////////////////////////////////////////////////////////////
//// PTDBSearchAttribute

/**
 * The attribute to wrap the attributes information to be searched in the
 * database. It indicates that this attribute is wrapped from the DB Search
 * frame.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
 *
 */
public class PTDBSearchAttribute extends Variable {

    /**
     * Default constructor of PTDBSearchAttribute.
     */
    public PTDBSearchAttribute() {
        super();
    }

}
