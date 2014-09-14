// ShapeRenderer.java
// Draws a single Shape.
package com.deitel.art;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

class ShapeRenderer implements GLSurfaceView.Renderer 
{
   private Shape currentShape; // Shape currently being displayed
   private float currentAngle; // angle of the current Shape
   private int currentColorID; // id of the current Shape's color
   private int currentShapeTypeID; // id of the current Shape's type
   
   private int xRotation = 1; // if 1, rotating in the x-axis
   private float xMultiplier= 2; // angle multiplier in the x-axis
   
   private int yRotation = 1; // if 1, rotating in the y-axis
   private float yMultiplier= .05f; // angle multiplier in the y-axis
   
   private int zRotation = 0; // if 1, rotating in the z-axis
   private float zMultiplier= 1; // angle multiplier in the z-axis     
   
   // create a new ShapeRenderer
   public ShapeRenderer() 
   {
      currentShape = new RectangularPrism(true); // create a new Cube
   } // end public ShapeRenderer constructor
   
   // draw the GLSurfaceView
   public void onDrawFrame(GL10 gl) 
   {  
     // clear the screen
     gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

     gl.glMatrixMode(GL10.GL_MODELVIEW);
     gl.glLoadIdentity(); // load the identity matrix
     gl.glTranslatef(0, 0, -3.0f); // move three units "into" the screen
      
     if (xRotation > 0) // if we are rotating along the x-axis
     {
       gl.glRotatef(currentAngle * xMultiplier,  xRotation, 0, 0);
     } // end if
     
     if (yRotation > 0) // if we are rotating along the y-axis
     {
       gl.glRotatef(currentAngle*yMultiplier, 0, yRotation, 0);
     } // end if
     
     if (zRotation > 0) // if we are rotating along the z-axis 
     {
       gl.glRotatef(currentAngle*zMultiplier,  0, 0, zRotation);
     } // end if
      
     gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
     gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

     currentShape.draw(gl); // draw the cube

     currentAngle += .8f; // increase the angle for the next frame      
   } // end method onDrawFrame

   // set the Shape's rotation in the x-axis
   public void setXRotation(int progress) 
   {
      if (progress > 0) // if the SeekBar is not all the way left
      {
         xRotation = 1; // enable rotation along the x-axis
      } // end if
      else 
      {
    	  xRotation = 0; // disable rotation along the y-axis
      } // end else
      
      xMultiplier = progress; // set rotation multiplier to match progress
   } // end method setXRotation
   
   // set the Shape's rotation in the x-axis
   public void setYRotation(int progress) 
   {
     if (progress > 0) // if the SeekBar is not all the way left
     {
       yRotation = 1; // enable rotation along the y-axis
     } // end if
     else 
     {
       yRotation = 0; // disable rotation along the y-axis
     } // end else
      
     yMultiplier = progress; // set rotation multiplier to match progress
   } // end method setYRotation
   
   public void setZRotation(int progress) 
   {
      if (progress > 0) // if the SeekBar is not all the way left
      {
        zRotation = 1; // enable rotation along the z-axis
      } // end if
      else 
      {
        zRotation = 0; // disable rotation along the z-axis
      } // end else
      
      zMultiplier = progress; // set rotation multiplier to match progress
   } // end method setZRotation
   
   // get the current rotation around the x-axis
   public int getXRotation()
   {
      return (int) xMultiplier;
   } // end getXRotation
   
   // get the current rotation around the y-axis
   public int getYRotation()
   {
      return (int) yMultiplier;
   } // end getYRotation
   
   // get the current rotation around the z-axis
   public int getZRotation()
   {
      return (int)zMultiplier;
   } // end getZRotation
   
   public void setSize(int size)
   {
      currentShape.setSize(size);
   } // end method setSize
   
   public int getSize()
   {
      return currentShape.getSize();
   } // end method getSize
   
   // set the Shape color
   public void setColor(int color)
   {
      currentColorID = color;
      currentShape.setColor(color);
   } // end method setColor
   
   // get the current Shape's color
   public int getColor()
   {
      return currentColorID;
   } // end method getColor
   
   // set new Shape to display
   public void setShape(int shapeID)
   {
	  currentShapeTypeID = shapeID; // save new Shape's type ID
      switch(shapeID)
      {
         case ArtActivity.CUBE:
            currentShape = new RectangularPrism(true); // create new Cube
            break;
         case ArtActivity.PYRAMID:
            currentShape = new Pyramid(); // create a new Pyramid
            break;
         case ArtActivity.PRISM: // create a new RectangularPrism
            currentShape = new RectangularPrism(false);
            break;
      } // end switch
      currentShape.init(); // intialize the new Shape's buffers
   } // end method setShape
   
   // get the current Shape's type ID
   public int getShapeType()
   {
	   return currentShapeTypeID; // return the current Shape's type ID
   } // end method getShapeType
   
   // get the current Shape's maximum size
   public int getMaxSize()
   {
      return currentShape.getMaxSize();
   } // end method getMaxSize
    
   // called when the surface is created
   @Override
   public void onSurfaceCreated(GL10 gl, EGLConfig config) 
   {
      gl.glClearColor(1,1,1,1); // set white background
      gl.glShadeModel(GL10.GL_SMOOTH); // use smooth shading technique
      gl.glEnable(GL10.GL_DEPTH_TEST); // enable depth testing
   } // end method onSurfaceCreated  
   
   // called when the surface size changes
   @Override
   public void onSurfaceChanged(GL10 gl, int width, int height) 
   {
	  // set the viewport's size and origin
      gl.glViewport(0, 0, width, height); 

      float ratio = (float) width / height; // screen width/height ratio
      gl.glMatrixMode(GL10.GL_PROJECTION); // use projection matrix stack
      gl.glLoadIdentity(); // load the identity matrix
      
      // define the perspective matrix
      gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10); 
   }  // end method onSurfaceChanged
} // end class ShapeRenderer



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
