//$Id$ %S%

package ptolemy.filter.view;

import java.util.*;

public class HistoModel {
    
   
   public static void main(String[] args){ 
         double data1x[] = { 1,2,3,5,6,8 };
         double data1y[] = { 3,5,1,5,4,6 };
         double data2x[] = { 1,2,3,4,7,8 };
         double data2y[] = { 9,3,2,8,12,6 };
         double data3x[] = { 1,3,4,5,6,7,9 };
         double data3y[] = { 2,8,10,14,12,6,8 };
         
         DataModel dm = new DataModel();
         dm.setDataSet(3);
         dm.setData(0,data1x, data1y, false, 0, false, null, null, null);
         dm.setData(1,data2x, data2y, true, 0, false, "X2 ", "Y2 ", "Year1");
         dm.setData(2,data3x, data3y, true, 0, false, "X3 ", "Y3 ", "Year2");

         DataView dataview = new DataView(dm, 0);
         dataview.setBars(0.5, 0.3);
         dm.setView(dataview);
         dataview.start();
    } 
}


