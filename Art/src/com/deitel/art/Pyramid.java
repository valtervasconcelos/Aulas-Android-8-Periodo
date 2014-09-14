package com.deitel.art;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
  
public class Pyramid extends Shape 
{
   private static final int MAXIMUM_SIZE = 100;	
	
   private FloatBuffer vertexFloatBuffer; // buffer for Pyramid's vertices  
   private FloatBuffer colorFloatBuffer; // Pyramid's color values buffer  
   private ByteBuffer indexByteBuffer; // Pyramid's indices buffer  

   // constructs a new Pyramid
   public Pyramid() 
   {
	   size = MAXIMUM_SIZE; // set the Pyramid's initial size
      init(); // construct the Pyramid's buffers
   } // end constructor Pyramid

   // initialize the Pyramid's buffers
   @Override
   void init() 
   {
      // scale the size to a float between 0.0 and 1.0
      float scaledSize = ((float)size) / MAXIMUM_SIZE; 
        
      // x, y and z values for each of the Pyramid's eight vertices
      float[] vertices = 
      { 
         -scaledSize, -scaledSize, -scaledSize,  
         scaledSize,  -scaledSize, -scaledSize,  
         scaledSize,  -scaledSize, scaledSize, 
         -scaledSize, -scaledSize, scaledSize,
         0.0f,  scaledSize,  0.0f 
      }; // end float[] vertices
           
      // color values for vertices making up this Pyramid
      float[] colors = 
      {  
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,
         red,  green,  blue,  red,  
      }; // end float[] multiColors
      
      // preset multicolored array of jumbled colors
      float[] multiColors = 
      {  
    	   0.0f, scaledSize, 0.0f, scaledSize,
         0.0f, 0.0f, scaledSize, scaledSize,
         0.0f, 0.0f, scaledSize, scaledSize,
         scaledSize, 0.0f, 0.0f, scaledSize,
         0.0f, scaledSize, 0.0f, scaledSize,  
      }; // end float[] multiColors
           
      // indices describing the 4 triangles comprising the Pyramid
      byte[] indices = 
      { 
         2, 4, 3,  
         1, 4, 2,   
         0, 4, 1,   
         4, 0, 3   
      }; // end byte[] indices

      // directly allocate a new ByteBuffer
      ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(
         vertices.length * Shape.BYTE_BUFFER_MULTIPLIER);
      vertexByteBuffer.order(ByteOrder.nativeOrder()); // set byte order
      
      // convert the ByteBuffer to a FloatBuffer
      vertexFloatBuffer = vertexByteBuffer.asFloatBuffer(); 
      vertexFloatBuffer.put(vertices); // add values from vertices array     
      vertexFloatBuffer.position(0); // position the buffer at its start           

      // directly allocate a new ByteBuffer
      ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(
         colors.length * Shape.BYTE_BUFFER_MULTIPLIER);
      colorByteBuffer.order(ByteOrder.nativeOrder()); // set byte order
      
      // convert the ByteBuffer to a FloatBuffer
      colorFloatBuffer = colorByteBuffer.asFloatBuffer();
      
      if (multiColor) // if this Pyramid is multicolored
      {
         colorFloatBuffer.put(multiColors); // add muliColors array values
      } // end if
      else 
      {
         colorFloatBuffer.put(colors); // add colors array values
      } // end else
      colorFloatBuffer.position(0); // position the buffer at its start

      // directly allocate a new ByteBuffer
      indexByteBuffer = ByteBuffer.allocateDirect(indices.length);
      indexByteBuffer.put(indices); // add indice array values
      indexByteBuffer.position(0); // position the buffer at its start
   } // end method init
   
   // draw the Pyramid
   public void draw(GL10 gl) 
   {
	  // use clockwise winding window coordinates
      gl.glFrontFace(GL10.GL_CCW); 
  
      // define the Pyramid's vertex coordinates
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexFloatBuffer);

      // define array of color components to use while rendering
      gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorFloatBuffer);
      
      gl.glDrawElements(GL10.GL_TRIANGLES, 12, GL10.GL_UNSIGNED_BYTE,
         indexByteBuffer);
   } // end method draw
   
   // get this Shape's maximum size
   public int getMaxSize()
   {
      return MAXIMUM_SIZE;
   } // end method getMaxSize
} // end class Pyramid


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
