// Shape.java
// Abstract class for all 3D Shapes.
package com.deitel.art;

import javax.microedition.khronos.opengles.GL10;

public abstract class Shape 
{
   // used to allocate correct sized byte buffers
   public static final int BYTE_BUFFER_MULTIPLIER = 4; 
	
   protected int size; // the size of the Shape
   int red = 0; // red component of the Shape's color
   int blue = 0; // blue component of the Shape's color
   int green = 0; // green component of the Shape's color
   protected boolean multiColor = true; // is the Shape multicolored?
   
   // set the Shape's size
   public void setSize(int size) 
   {
      this.size = size;
      init(); // re-initialize the Shape
   } // end method setSize
   
   // returns the Shape's size
   public int getSize()
   {
      return size;
   } // end method getSize
   
   // sets the Shape's color
   public void setColor(int colorID)
   {
	   clearColor();
      switch(colorID) // switch the given int identifier
      {
         case ArtActivity.MULTI_COLOR: // make the Shape multicolored
            multiColor = true; 
            break;
         case ArtActivity.GREEN: // make the Shape green
            green = size; 
            break;
         case ArtActivity.BLUE: // make the Shape blue
            blue = size; 
            break;
         case ArtActivity.RED: // make the Shape red
            red = size; 
            break;
      } // end switch
         
      init(); // re-initialize the shape
   } // end method setColor
   
   // reset the Shape's color
   private void clearColor()
   {
      red = 0;
      blue = 0; 
      green = 0; 
      multiColor = false;
   } // end method clearColor
   
   abstract void init(); // re-initialize the Shape
   abstract void draw(GL10 gl); // draw the Shape to the SurfaceView
   abstract int getMaxSize(); // get the Shape's maximum size
} // end class Shape


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
