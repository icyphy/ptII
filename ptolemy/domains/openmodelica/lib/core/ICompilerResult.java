package ptolemy.domains.openmodelica.lib.core;

/**
 *
 * @author Adrian Pop
 *
 */
public interface ICompilerResult {

    /**
     * @author Adrian Pop
     * @return the error resulted from calling sendExpression("command") to OMC
     */
    public String getError();

    /**
     * @author Adrian Pop
     * @return the first result of calling sendExpression("command") to OMC
     */
    public String getFirstResult();

    /**
     * @author Adrian Pop
     * @return the multiple result of calling sendExpression("command") to OMC
     */
    public String[] getResult();

    /**
     * @author Adrian Pop
     * trims the first result
     */
    public void trimFirstResult();

}
