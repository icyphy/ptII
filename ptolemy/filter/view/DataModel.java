// $Id$ %S%

package ptolemy.filter.view;

import java.util.*;

public class DataModel extends Observable {
    

   public void setData(int dataset, double datax[], double datay[], 
          boolean interact, int dfree, boolean conn, String xlab, String ylab,
          String dname){
         
         _datax[dataset]=new double[datax.length];
         _datay[dataset]=new double[datay.length];
         for (int ind=0;ind<datax.length;ind++){
             _datax[dataset][ind]=datax[ind];
             _datay[dataset][ind]=datay[ind];
         }
         _interact[dataset] = interact;
         _dfree[dataset] = dfree;
         _connect[dataset] = conn;
         _xlabel[dataset] = xlab;
         _ylabel[dataset] = ylab;
         _dataname[dataset] = dname;
      
   }

   public void setDataSet(int dataset){
         _dataset = dataset;
         _datax = new double[_dataset][];      
         _datay = new double[_dataset][];      
         _interact = new boolean[_dataset];
         _dfree = new int[_dataset];
         _connect = new boolean[_dataset];
         _xlabel = new String[_dataset];
         _ylabel = new String[_dataset];
         _dataname = new String[_dataset];
   }

   // called by view
   public void getAllData(){
        _dv.setNumSet(_dataset);
        for (int ind=0;ind<_dataset;ind++){
            _dv.dataUpdate(_dataname[ind], ind, _datax[ind], _datay[ind], 
                _interact[ind], _connect[ind], _xlabel[ind],
                _ylabel[ind], _dfree[ind], 0);
        } 
   }
  
   public void dataUpdate(int dataset, double [] xv, double [] yv){
        // only show the updateable ones
        if (_interact[dataset] == true){ 

System.out.println("======================="); 
System.out.println("updating dataset: "+dataset + " new data:"); 

             _datax[dataset] = new double[xv.length];
             _datay[dataset] = new double[yv.length];
             for (int ind=0;ind<xv.length;ind++){
                 _datax[dataset][ind] = xv[ind]; 
                 _datay[dataset][ind] = yv[ind]; 

System.out.println("x: "+_datax[dataset][ind]+" y: "+_datay[dataset][ind]); 
             }
        }
   }

   public void setView(DataView dv){
        addObserver(dv);
        _dv = dv;
   }

   protected DataView _dv;     
   protected int _dataset;
   protected boolean _interact[]; 
   protected boolean _connect[]; 
   protected String _xlabel[];
   protected String _ylabel[];
   protected int _dfree[]; 
   protected String _dataname[];
   protected double _datax[][];     
   protected double _datay[][];     

 
   public static void main(String[] args){ 
         double data1x[] = { 0.3, 0.5, 1, 1.5, 2.4, 3.6, 5.1, 8.3 };
         double data1y[] = { 3, 5, 1, 5, 4, 6, 1, 3 };
         double data2x[] = { 0.2, 0.4, 1, 1.3, 2.8, 3.0, 5.9, 6.3 };
         double data2y[] = { 8, 2, 9, 3, 7, 4, 4, 3 };
         double data3x[] = { 1.2, 2.4, 3, 3.3, 4.8, 6.0, 6.9, 8.3 };
         double data3y[] = { 9, 8, 7, 6, 5, 4, 3, 2, };
         double data4x[] = { 0.1, 2.0, 2.3, 3.4, 4.5, 5.6 };
         double data4y[] = { 1, 2, 3, 4, 5, 6 };
         
         DataModel dm = new DataModel();
         dm.setDataSet(4);
         dm.setData(0,data1x, data1y, false, 0, true, null, null, null);
         dm.setData(1,data2x, data2y, true, 3, true, "X2 ", "Y2 ", "Data2");
         dm.setData(2,data3x, data3y, true, 2, true, "X3 ", "Y3 ", "Data3");
         dm.setData(3,data4x, data4y, true, 3, false, "X4 ", "Y4 ", "Data4");

         DataView dataview = new DataView(dm, 0);
         dm.setView(dataview);
         dataview.start();
    } 
}


