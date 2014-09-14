// RectangularPrism.java
package com.deitel.art;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class RectangularPrism extends Shape
{
   private static final int MAXIMUM_SIZE = 0x10000;
   private static final int NUMBER_OF_VERTICES = 36;
	
   private IntBuffer vertexIntBuffer; // buffer for vertices
   private IntBuffer colorIntBuffer; // buffer for color values
   private ByteBuffer indexByteBuffer; // buffer for vertex indices   
   
   int multiplier = 1; // ratio between sides, if 1 then its a cube
   
   // create a new rectangle
   public RectangularPrism(boolean cube)
   {
	   size = 0x9500; // set the RectangularPrism's initial size
	   
      init(); // initialize the vertice, color and indice buffers
      
      if (!cube) // if its not a cube
      {
         multiplier = 2; // increase the multiplier
      } // end if
   } // end constructor RectangularPrism
   
   // initialize the Shape's buffers
   public void init() 
   {
	   // x, y and z values for each of the Cube's eight vertices
      int vertices[] = 
      {
         -size, -size, -size * multiplier,
         size, -size, -size * multiplier,
         size, size, -size * multiplier,
         -size, size, -size * multiplier,
         -size, -size, size * multiplier,
         size, -size, size * multiplier,
         size, size, size * multiplier,
         -size, size, size * multiplier
      }; // end int vertices[]
      
      // color values for vertices making up this RectangularPrism
      int colors[] = 
      {
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red
      }; // end int colors[]

      // preset multicolored array of jumbled colors
      int multiColors[] = 
      {
         size,   0,   0,  size,
         0,  size,  size,  size,
         0,   0,   0,  size,
         size,  size,   0,  size,
         size,  size,  size,  size,
         0,  size,   0,  size,
         size,   0,  size,  size,
         0,   0,  size,  size
      }; // end int multiColors[]

      // indices describing the 12 triangles comprising a
      // RectangularPrism's six faces
      byte indices[] = 
      {
         0, 4, 5,   0, 5, 1,
         1, 5, 6,   1, 6, 2,
         2, 6, 7,   2, 7, 3,
         3, 7, 4,   3, 4, 0,
         4, 7, 6,   4, 6, 5,
         3, 0, 1,   3, 1, 2
      }; // end byte indices[]
      
      // directly allocate a new ByteBuffer
      ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(
         vertices.length * Shape.BYTE_BUFFER_MULTIPLIER);
      vertexByteBuffer.order(ByteOrder.nativeOrder()); // set byte order
      
      // convert the ByteBuffer to an IntBuffer
      vertexIntBuffer = vertexByteBuffer.asIntBuffer();
      vertexIntBuffer.put(vertices); // add the vertices array values
      vertexIntBuffer.position(0); // position the buffer at its start

      // directly allocate a new ByteBuffer
      ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(
         colors.length * Shape.BYTE_BUFFER_MULTIPLIER);
      colorByteBuffer.order(ByteOrder.nativeOrder()); // set byte order
      
      // convert the ByteBuffer to an IntBuffer
      colorIntBuffer = colorByteBuffer.asIntBuffer();
      
      if (multiColor) // if the 
      {
         colorIntBuffer.put(multiColors); // add values from multiColors
      } // end if
      else 
      {
    	  colorIntBuffer.put(colors); // add values from colors array
      } // end else
      
      colorIntBuffer.position(0); // position the buffer at the start

      // directly allocate a new ByteBuffer
      indexByteBuffer = ByteBuffer.allocateDirect(indices.length * 
         Shape.BYTE_BUFFER_MULTIPLIER);
      indexByteBuffer.put(indices); // add values from indices array
      indexByteBuffer.position(0); // position the buffer at the start
   } // end method init
   
   // draw the RectangularPrism
   public void draw(GL10 gl)
   {
	  // use clockwise winding window coordinates
      gl.glFrontFace(GL10.GL_CW);
      
      // define the RectangularPrism's vertex coordinates
      gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexIntBuffer);
      
      // define array of color components to use while rendering
      gl.glColorPointer(4, GL10.GL_FIXED, 0, colorIntBuffer);
      gl.glDrawElements(GL10.GL_TRIANGLES, NUMBER_OF_VERTICES, 
         GL10.GL_UNSIGNED_BYTE, indexByteBuffer);
   } // end method draw
   
   // get the maximum size of this Shape
   public int getMaxSize() 
   {
      return MAXIMUM_SIZE;
   } // end method getMaxSize
} // end class RectangularPrism

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
