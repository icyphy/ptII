/* Black-box optimizer class - to be modified

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package org.ptolemy.machineLearning.particleFilter;
/**
Black-box optimizer that uses JCobyla as the solver

<p> Performs mutual information-based optimization using 

<b>References</b>
 <p>[1]
B. Charrow, V. Kumar, and N. Michael <i>Approximate Representations for Multi-Robot 
  Control Policies that Maximize Mutual Information</i>, In Proc. Robotics: Science and Systems Conference (RSS), 2013.
@see org.ptolemy.machineLearning.particleFilter.ParticleFilter
@see com.cureos.numerics

@author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating Red (ilgea)

*/


import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.math.DoubleArrayMath;
import ptolemy.math.DoubleMatrixMath;

public class MutualInformationCalculator extends TypedAtomicActor {

    public MutualInformationCalculator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
        
        optIndex = new Parameter(this, "optIndex");
        optIndex.setExpression("-1");
        optIndex.setTypeEquals(BaseType.INT);
        _optIndex = -1;
        
        timeHorizon = new Parameter(this, "timeHorizon");
        timeHorizon.setExpression("1");
        timeHorizon.setTypeEquals(BaseType.INT);
        _timeHorizon = 1;
        
        
        xValue = new TypedIOPort(this,"xValue", true, false);
        xValue.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        
        particles = new TypedIOPort(this, "particles", true, false); 
        ArrayToken names = new ArrayToken("{\"x\",\"y\"}"); // 
        String stateName;
        _labels = new String[names.length() + 1];
        _types = new Type[names.length() + 1];
        try{
            for(int i = 0; i < names.length(); i++){
                stateName = ((StringToken)names.getElement(i)).stringValue();
                if(this.getAttribute(stateName) == null && stateName.length()!= 0){
                    Parameter y = new Parameter(this, stateName);
                    y.setExpression("0.0");
                    y.setVisibility(Settable.EXPERT);
                }
                _labels[i] = stateName;
                _types[i]  = BaseType.DOUBLE; // preset to be double
            }
            _labels[names.length()] = "weight";
            _types[names.length()] = BaseType.DOUBLE;
            particles.setTypeEquals(new ArrayType(new RecordType(_labels,_types)));
        }catch(NameDuplicationException e){
            // should not happen
            System.err.println("Duplicate field in " + this.getName());
        }
        _particles = new Particle[0];
        
        // an array of robot locations
        locations = new TypedIOPort(this, "locations", true, false);
        
        _robotLocations = new LinkedList<RecordToken>();
        
         
        output = new TypedIOPort(this,"output",false,true);
        output.setTypeEquals(BaseType.DOUBLE);
        
        _covariance = 2.0;
        
        
        
    }
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException { 
        if (attribute == timeHorizon){
            int th = ((IntToken)timeHorizon.getToken()).intValue();
            if(th > 0){
                _timeHorizon = th;
            }else{
                throw new IllegalActionException(this,"Time horizon must be a positive integer");
            }
        }else if( attribute == optIndex)
        {
            _optIndex = ((IntToken)optIndex.getToken()).intValue();
        }else{
            super.attributeChanged(attribute);
        }
    }
    public void fire() throws IllegalActionException{
         
        super.fire();
        
        
        
        if(particles.hasToken(0)){
            ArrayToken incoming = (ArrayToken)particles.get(0);
            N = incoming.length();
            LinkedList<Double> particleValue = new LinkedList<Double>();
            // copy the input particles to a local array ( this is expected to be subsampled)
            _particles = new Particle[incoming.length()]; // initialize particle array
            _weights   = new double[incoming.length()];
            
            _px = new double [N];
            _py = new double [N];
            
            for( int i = 0 ; i < incoming.length(); i++){
                RecordToken token = (RecordToken)incoming.getElement(i);
                Particle p1 = new Particle(2);
                
                for(int k = 0; k < _labels.length; k++){
                    if(_labels[k].equals("weight")){
                        p1.setWeight(((DoubleToken)token.get(_labels[k])).doubleValue());
                        _weights[i] = p1.getWeight();
                    }else{
                        particleValue.add(((DoubleToken)token.get(_labels[k])).doubleValue());
                    }
                }
                p1.setValue( particleValue);
                _px[i] = particleValue.get(0);
                _py[i] = particleValue.get(1);
                particleValue.clear();
                _particles[i] = p1;
            }
            
            double wsum = 0;
            for(int i = 0; i < N; i++){
                wsum += _weights[i];
            }
            for(int i = 0; i < N; i++){
                _weights[i]/=wsum;
            }
        }
        
        if(locations.hasToken(0)){
            ArrayToken robotLocations = (ArrayToken)locations.get(0);
            _nRobots = robotLocations.length();
            _robotLocations.clear();
            for(int i = 0; i < _nRobots; i++){
                RecordToken robotLocation = (RecordToken)robotLocations.getElement(i);
                _robotLocations.add(robotLocation);
            } 
        }
        if(xValue.hasToken(0)){
            ArrayToken xArray = ((ArrayToken)xValue.get(0));
            _xValue = new double[xArray.length()];
            for(int i = 0; i < xArray.length(); i++){
                _xValue[i] = (double)((DoubleToken)xArray.getElement(i)).doubleValue();
            }
        }
        
        output.send(0, new DoubleToken(Hz(_xValue))); 
        
        
    }
//    public boolean postfire() throws IllegalActionException {
//        super.postfire();
//        _robotLocations.clear();
//        return true;
//    }
    /**
     * f(x)
     */
    public TypedIOPort output;
    /**
     * Particles input that accepts an array of record tokens. One field of the record must be labeled as "weight".
     * Other fields will be resolved to state variables.
     */
    public TypedIOPort particles;
    public TypedIOPort locations;
    public Parameter covariance;
    public Parameter timeHorizon;
    public Parameter optIndex;
    
    public TypedIOPort xValue;
    // code for computing the mutual information between particle sets and measurements
    private double Hz(double[] x){
        // zeroth order approximation of the measurement entropy 
        double[][] Sigma = DoubleMatrixMath.identity(_nRobots);
        for(int i = 0; i < Sigma.length; i++){
            for(int j = 0; j < Sigma[0].length; j++){
                Sigma[i][j]*= _covariance;
            }
        }
        double[][] gaussianMeans = new double[N][_nRobots];
        double robotX = 0;
        double robotY = 0;
        
        
        for(int i = 0; i < N; i++){
            // var: process noise
            double[][] var = new double[2][2];
            var[0][0] = 1.0;
            var[1][1] = 1.0;
            Token[] mu = new Token[2];
            mu[0] = new DoubleToken(0.0);
            mu[1] = new DoubleToken(0.0);

            if( _optIndex < 0 ){
                for(int j = 0; j<_nRobots; j++){
                    RecordToken robotJ = _robotLocations.get(j);
                    robotX = ((DoubleToken)robotJ.get("x")).doubleValue() + x[2*j];
                    robotY = ((DoubleToken)robotJ.get("y")).doubleValue() + x[2*j+1];
                    gaussianMeans[i][j] = Math.sqrt(Math.pow(_px[i]-robotX,2)+Math.pow(_py[i]-robotY,2));
                }
            }else{
                // optimize over single robot
                for(int j = 0; j<_nRobots; j++){
                    RecordToken robotJ = _robotLocations.get(j);
                    robotX = ((DoubleToken)robotJ.get("x")).doubleValue();
                    robotY = ((DoubleToken)robotJ.get("y")).doubleValue();
                    if( j == _optIndex){
                        robotX += x[0];
                        robotY += x[1];
                    }
                    gaussianMeans[i][j] = Math.sqrt(Math.pow(_px[i]-robotX,2)+Math.pow(_py[i]-robotY,2));
                }
            }
        } 
        double Hz = 0;
        double logSum = 0;
        for(int k = 0 ; k < N; k++){
            logSum = 0;
            for( int j = 0; j < N; j++){
                logSum += _weights[j]*mvnpdf(gaussianMeans[k],gaussianMeans[j],Sigma);
            }
            Hz += _weights[k]*Math.log(logSum);
        }
        
        return -Hz;
    }
    // compute the multivariate PDF value at x.
    private double mvnpdf(double[] x, double[] mu, double[][] Sigma){
        int k = x.length;
        double multiplier = Math.sqrt(1.0/(Math.pow(Math.PI*2, k)*DoubleMatrixMath.determinant(Sigma)));
        double[] x_mu = new double[x.length];
        for(int i = 0; i < x.length; i++){
            x_mu[i] = x[i] - mu[i];
        }
        double exponent = DoubleArrayMath.dotProduct(
                DoubleMatrixMath.multiply(x_mu, 
                        DoubleMatrixMath.inverse(Sigma)),x_mu);
        
        return multiplier*Math.exp(-0.5*exponent);
    }
    private Particle[] _particles;
    private double[] _weights;
    private double[] _px;
    private double[] _py;
    private List<RecordToken> _robotLocations;
    private double _covariance; 
    private int _optIndex;
    private int _nRobots;
    private double[] _xValue;
    private String[] _labels;
    private Type[] _types; 
    private int _timeHorizon;
    private int N;  
    

}
