package ptolemy.lang.java.test;

class Switch {

  public int ugh() {
    int h = 3;
    int y = 0;

    switch (h) {
     case 1:
     case 2:
     y = 1;

     case 3:

     y = 3;
     break;

     default:
   //  break;

    }
    return y;
  }

}