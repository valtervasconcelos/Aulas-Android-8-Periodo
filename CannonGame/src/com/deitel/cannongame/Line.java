// Line.java
// Class Line represents a line with two endpoints.
package com.deitel.cannongame;

import android.graphics.Point;

public class Line
{
   public Point start; // starting Point
   public Point end; // ending Point

   // default constructor initializes Points to (0, 0)
   public Line()
   {
      start = new Point(0, 0); // start Point
      end = new Point(0, 0); // end Point
   } // end method Line
} // end class Line

/*********************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and * Pearson Education, *
 * Inc. All Rights Reserved. * * DISCLAIMER: The authors and publisher of this   *
 * book have used their * best efforts in preparing the book. These efforts      *
 * include the * development, research, and testing of the theories and programs *
 * * to determine their effectiveness. The authors and publisher make * no       *
 * warranty of any kind, expressed or implied, with regard to these * programs   *
 * or to the documentation contained in these books. The authors * and publisher *
 * shall not be liable in any event for incidental or * consequential damages in *
 * connection with, or arising out of, the * furnishing, performance, or use of  *
 * these programs.                                                               *
 *********************************************************************************/