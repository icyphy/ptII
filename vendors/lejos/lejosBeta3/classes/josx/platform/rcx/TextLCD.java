package josx.platform.rcx;

/* Translated from C to Java by Ryan VanderBijl
 *  Copying portion from the legOS project, version 0.2.4.
 *  Pretty much all data values are borrowed.
 *  Original code by Markus L. Noga
 *  Released under the MPL 1.0
 */ 

 /* API:
  * TextLCD.print(char[] text); displays the first 5 characters
  *      onto the LCD. As best it can.
  * TextLCD.printChar(char c, int pos) displays c on the pos
  *      position counting from the RIGHT.
  */

/**
 * Display text on the LCD screen.
 *
 * @author <a href="mailto:rvbijl39<at>calvin<dot>edu">Ryan VanderBijl</a> 
 */
public class TextLCD 
{
 
  // keeping in with TinyVM, i dont allow any instances to be made.
  //  They are all final/static anyways, so it doesnt matter.
  private TextLCD()
  {
  }

  /**
   * Prints a string on the LCD. 
   * No need to refresh.
   */
  public static final void print(String str)
  {
    // This is leaking
    print (str.toCharArray());
  }

  /**
   * Print up to the first 5 characters of a char array to the LCD.
   * There is no need to refresh the LCD.
   * Space is displayed if less than five characters.
   * Characters are approximations. There is only so much you can
   * do with this lcd screen!
   */
  public static final void print(char[] text) 
  {
     int i = 0;

     for(; (i < text.length) && (i <= 4); i++)
	printChar( text[i] , 4 - i); 

     while(i <= 4) printChar((char)0, 4 - i++); // print blanks
     LCD.refresh();
  }

  /**
   * Prints a character to a given position.  Input character is
   * assumed to be an ascii character < 127. Position is between
   * zero and four, counting from the RIGHT.
   * Character will not be shown until <code>LCD.refresh</code> is called.
   */
  public static final void printChar(char the_char, int pos)
  {
     if (pos == 0)
	 native_print_pos_0(ascii_display_codes[(int)the_char]);
     else if (pos == 1)
	 native_print_pos_1(ascii_display_codes[(int)the_char]);
     else if (pos == 2)
	 native_print_pos_2(ascii_display_codes[(int)the_char]);
     else if (pos == 3)
	 native_print_pos_3(ascii_display_codes[(int)the_char]);
     else if (pos == 4)
	 native_print_pos_4(ascii_display_codes[(int)the_char]);
  }

  private static final void native_print_pos_0(byte mask)
  {
     Native.setMemoryBit(0xef44, 0x4, (mask & 1<<0x2));
     Native.setMemoryBit(0xef44, 0x5, (mask & 1<<0x0));
     Native.setMemoryBit(0xef44, 0x7, (mask & 1<<0x5));
     Native.setMemoryBit(0xef45, 0x5, (mask & 1<<0x1));
     Native.setMemoryBit(0xef45, 0x7, (mask & 1<<0x6));
     Native.setMemoryBit(0xef47, 0x5, (mask & 1<<0x3));
     Native.setMemoryBit(0xef47, 0x7, (mask & 1<<0x4));
  }

  private static final void native_print_pos_1(byte mask)
  {
     Native.setMemoryBit(0xef43, 0x4, (mask & 1<<0x2));
     Native.setMemoryBit(0xef43, 0x5, (mask & 1<<0x0));
     Native.setMemoryBit(0xef43, 0x7, (mask & 1<<0x5));
     Native.setMemoryBit(0xef47, 0x1, (mask & 1<<0x1));
     Native.setMemoryBit(0xef47, 0x3, (mask & 1<<0x6));
     Native.setMemoryBit(0xef48, 0x5, (mask & 1<<0x3));
     Native.setMemoryBit(0xef48, 0x7, (mask & 1<<0x4));
  }
  
  private static final void native_print_pos_2(byte mask)
  {
     Native.setMemoryBit(0xef44, 0x0, (mask & 1<<0x2));
     Native.setMemoryBit(0xef44, 0x1, (mask & 1<<0x0));
     Native.setMemoryBit(0xef44, 0x3, (mask & 1<<0x5));
     Native.setMemoryBit(0xef48, 0x1, (mask & 1<<0x1));
     Native.setMemoryBit(0xef48, 0x3, (mask & 1<<0x6));
     Native.setMemoryBit(0xef49, 0x5, (mask & 1<<0x3));
     Native.setMemoryBit(0xef49, 0x7, (mask & 1<<0x4));
  }
  
  private static final void native_print_pos_3(byte mask)
  {
     Native.setMemoryBit(0xef46, 0x0, (mask & 1<<0x2));
     Native.setMemoryBit(0xef46, 0x1, (mask & 1<<0x0));
     Native.setMemoryBit(0xef46, 0x3, (mask & 1<<0x5));
     Native.setMemoryBit(0xef4b, 0x1, (mask & 1<<0x1));
     Native.setMemoryBit(0xef4b, 0x3, (mask & 1<<0x6));
     Native.setMemoryBit(0xef4b, 0x5, (mask & 1<<0x3));
     Native.setMemoryBit(0xef4b, 0x7, (mask & 1<<0x4));
  }
  
  private static final void native_print_pos_4(byte mask)
  {
     Native.setMemoryBit(0xef46, 0x4, (mask & 1<<0x2));
     Native.setMemoryBit(0xef46, 0x5, (mask & 1<<0x0));
     Native.setMemoryBit(0xef46, 0x7, (mask & 1<<0x5));
     Native.setMemoryBit(0xef4a, 0x1, (mask & 1<<0x1));
     Native.setMemoryBit(0xef4a, 0x3, (mask & 1<<0x6));
     Native.setMemoryBit(0xef4a, 0x5, (mask & 1<<0x3));
     Native.setMemoryBit(0xef4a, 0x7, (mask & 1<<0x4));
  }

// Some documentation, leached from legOS.
//- // LCD segment control byte and bit locations
//- // 0xNNNN,0xM => Mth bit (value 1<<M) of byte 0xNNNN
//- // overall memory range: 0xef43-0xef4b (9 bytes)


//- private final static byte hex_display_codes[] =
//- {
//-    0x7e,			// 0
//-    0x42,			// 1
//-    0x37,			// 2
//-    0x67,			// 3
//-    0x4b,			// 4
//-    0x6d,			// 5
//-    0x7d,			// 6
//-    0x46,			// 7
//-    0x7f,			// 8
//-    0x6f,			// 9
//-    0x5f,			// A 
//-    0x79,			// b 
//-    0x31,			// c
//-    0x73,			// d 
//-    0x3d,			// E
//-    0x1d,			// F
//- };



    //! ASCII display codes
    /*! This is a 7-bit ASCII table only.  */
    // Leached from legOS:
    private static final byte ascii_display_codes[] =
    {
       0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,	//non-printables
       0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,	// -> underscore
       0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,	// except 0x00.
       0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,

       0x00,			// 32 ' '
       0x42,			// 33 ! 1
       0x0a,			// 34 "
       0x7b,			// 35 #
       0x6d,			// 36 $ 5 S Z
       0x13,			// 37 % /
       0x7d,			// 38 & 6
       0x08,			// 39 '  alt: ` 
       0x3c,			// 40 ( C [ {
       0x66,			// 41 ) ] }
       0x5b,			// 42 * H K X
       0x43,			// 43 +  alt: 0x19 (worse)
       0x10,			// 44 , .  alt: 0x40
       0x01,			// 45 -
       0x10,			// 46 . alt: 0x40
       0x13,			// 47 /

       0x7e,			// 48 0 0
       0x42,			// 49 1
       0x37,			// 50 2
       0x67,			// 51 3
       0x4b,			// 52 4
       0x6d,			// 53 5
       0x7d,			// 54 6
       0x46,			// 55 7
       0x7f,			// 56 8
       0x6f,			// 57 9

       0x21,			// 58 : ; = alt:0x5 (worse)
       0x21,			// 59 ;
       0x31,			// 60 < c alt:0xd (worse)       
       0x21,			// 61 =
       0x61,			// 62 >   alt: 0x7 (worse)
       0x17,			// 63 ?
       0x3f,			// 64 @ alt: e

       0x5f,			// 65 A
       0x79,			// 66 b 
       0x3c,			// 67 C
       0x73,			// 68 d
       0x3d,			// 69 E
       0x1d,			// 70 F
       0x7c,			// 71 G
       0x5b,			// 72 H
       0x42,			// 73 I 1
       0x62,			// 74 J
       0x5b,			// 75 K
       0x38,			// 76 L
       0x5e,			// 77 M N
       0x5e,			// 78 N
       0x7e,			// 79 O alt: o
       0x1f,			// 80 P
       0x4f,			// 81 Q 
       0x11,			// 82 r
       0x6d,			// 83 S
       0x46,			// 84 T alt: t
       0x7a,			// 85 U V W
       0x7a,			// 86 V
       0x7a,			// 87 W
       0x5b,			// 88 X
       0x6b,			// 89 Y
       0x37,			// 90 Z

       0x3c,			// 91 [
       0x49,			// 92 '\\'
       0x66,			// 93 ]
       0x0e,			// 94 ^ ~
       0x20,			// 95 _
       0x02,			// 96 ` alt: '

       0x5f,			// 97 A R
       0x79,			// 98 b 
       0x31,			// 99 c
       0x73,			// 100 d 
       0x3d,			// 101 E
       0x1d,			// 102 F
       0x7c,			// 103 G
       0x59,			// 104 h
       0x42,			// 105 I 1
       0x62,			// 106 J alt 0x60 (worse)
       0x5b,			// 107 K alt h (worse?)
       0x38,			// 108 L
       0x51,			// 109 m n
       0x51,			// 110 n
       0x71,			// 111 o
       0x1f,			// 112 P
       0x4f,			// 113 q        
       0x11,			// 114 r
       0x6d,			// 115 S
       0x39,			// 116 t
       0x70,			// 117 u v w
       0x70,			// 118 v
       0x70,			// 119 w
       0x5b,			// 120 X
       0x6b,			// 121 Y
       0x37,			// 122 Z

       0x3c,			// 123 {
       0x18,			// 124 | (left) alt: 1 (worse)
       0x66,			// 125 }
       0x0e,			// 126 ~
       0x00				// 127 "" 127 empty
    };


}
