/**
 * An implementation of {@link ptolemy.matlab.impl.engine.AbstractMatlabEngine} to be used in the 
 * Expression actor, based on Matlab's (R) MEX API.
 * 
 * <p>Most, if not all of the non-trivial code under this package is a copy or a derivation of Zoltan Kemenczy's and Sean Simmons' 
 * implementation of the Matlab/Mex <-> Ptolemy II bridge. On the contrary, for most, if not for all of the bugs and inconveniences 
 * in this package's code David Guardado Barcia is to be blamed, as he is responsible for the latest refactoring and repackaging of 
 * the routines.</p>
 * 
 *  @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited.
 *  @author David Guardado Barcia
 *
 */
package ptolemy.matlab.impl.backends.mxapi;