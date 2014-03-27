package org.ptolemy.machineLearning.particleFilter;
/**
 * The public particle class, currently used by the Optimizer.
 */
import java.util.LinkedList;
import java.util.List;

public class Particle{
        public Particle(int size){
            _particleValue = new LinkedList<Double>();
            _ssSize = size;
        }
        public Particle(Particle p){
            this._weight = p._weight;
            this._ssSize = p._ssSize;
            this._particleValue = new LinkedList<Double>();
            List temp = p.getValue();
            for(int i = 0; i < temp.size(); i++ ){
                this._particleValue.add((double)temp.get(i));
            }
        }
        public boolean adjustWeight(double w){
            // normalize weight
            if(w > 0.0){
                _weight = _weight/w;
            }else{
                return false;
            }
            return true;
        }
        public int getSize(){
            return _ssSize;
        }
        public List<Double> getValue(){
            List<Double> values = new LinkedList<Double>();
            for(int i = 0; i < _particleValue.size(); i++){
                values.add(_particleValue.get(i));
            }
            return values;
        }
        public double getWeight(){
            return _weight;
        }       
        public void setValue(LinkedList<Double> l){
            _particleValue = new LinkedList<Double>();

            for(int i = 0; i < l.size(); i++){
                _particleValue.add(l.get(i));
            }
        }
        public void setWeight(double weight){
            _weight = weight;
        }         
        private List<Double> _particleValue;
        private int _ssSize;
        private double _weight;
    }