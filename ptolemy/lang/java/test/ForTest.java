package ptolemy.lang.java.test;

class ForTest {

    ForTest() {

      int j, k;

      for (int i = 0; i < 7; i++) {
          j = 3;
      }

      for (int i = 0, c = 3; c < 5; c++) {
          k = i + j;
      }

      for (j = 5, k = 6; j > 3; j--);
    }
