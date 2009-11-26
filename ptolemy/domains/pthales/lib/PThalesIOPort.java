package ptolemy.domains.pthales.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PThalesIOPort extends TypedIOPort {

    public PThalesIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);

        // Add parameters for PThales Domain
        _initialize();
    }

    public PThalesIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {

        base = new Parameter(this, "base");
        base.setExpression("");
        base.setTypeEquals(BaseType.STRING);

        pattern = new Parameter(this, "pattern");
        pattern.setExpression("");
        pattern.setTypeEquals(BaseType.STRING);

        tiling = new Parameter(this, "tiling");
        tiling.setExpression("");
        tiling.setTypeEquals(BaseType.STRING);

        size = new Parameter(this, "size");
        size.setExpression("");
        size.setTypeEquals(BaseType.STRING);

        dataType = new Parameter(this, "dataType");
        dataType.setExpression("");
        dataType.setTypeEquals(BaseType.STRING);

        dataTypeSize = new Parameter(this, "dataTypeSize");
        dataTypeSize.setExpression("");
        dataTypeSize.setTypeEquals(BaseType.INT);

        dimensionNames = new Parameter(this, "dimensionNames");
        dimensionNames.setExpression("");
        dimensionNames.setTypeEquals(BaseType.STRING);

    }

    /** Array base
     */
    public Parameter base;

    /** Array pattern
     */
    public Parameter pattern;

    /** Array tiling 
     */
    public Parameter tiling;

    /** Array size
     */
    public Parameter size;

    /** data type (for code generation only) 
     */
    public Parameter dataType;

    /** data type size(for code generation only) 
     */
    public Parameter dataTypeSize;

    /** data type size(for code generation only) 
     */
    public Parameter dimensionNames;

    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /** This is the value in parameter base.
     */
    protected String _base;

    /** This is the value in parameter pattern.
     */
    protected String _pattern;

    /** This is the value in parameter tiling.
     */
    protected String _tiling;

    /** This is the value in parameter size.
     */
    protected String _size;

    /** This is the data type (for code generation).
     */
    protected String _dataType;

    /** This is the data type size (for code generation).
     */
    protected int _dataTypeSize;

    /** This is the dimension names of the port.
     */
    protected String _dimensionNames;

}
