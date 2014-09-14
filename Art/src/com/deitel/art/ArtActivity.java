// ArtActivity.java
// Main Activity for the 3DArt app.
package com.deitel.art;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ArtActivity extends Activity 
{
   // int IDs for Shape dialog options
   public static final int CUBE = 0;
   public static final int PYRAMID = 1;
   public static final int PRISM = 2;
   
   // int IDs for color dialog options
   public static final int MULTI_COLOR = 0;
   public static final int RED = 1;
   public static final int GREEN = 2;
   public static final int BLUE = 3;
   
   private GLSurfaceView displayGLSurfaceView; // displays the shape
   private ShapeRenderer shapeRenderer; // draws Shape to GLSurfaceView

   private SeekBar sizeSeekBar;
   private SeekBar xSeekBar;
   private SeekBar ySeekBar;
   private SeekBar zSeekBar;
   
   // creates a new ArtActivity
   @Override
   protected void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main); 

      // get the GLSurfaceView from the layout
      displayGLSurfaceView = 
         (GLSurfaceView) findViewById(R.id.glSurfaceView);
      shapeRenderer = new ShapeRenderer(); // create Renderer
      displayGLSurfaceView.setRenderer(shapeRenderer);
      shapeRenderer.setShape(CUBE); // display cube to start
      
      // configure sizeSeekBar
      sizeSeekBar = (SeekBar) findViewById(
         R.id.sizeSeekBar); // get the Dialog's seekBar
      sizeSeekBar.setMax(shapeRenderer.getMaxSize());
      sizeSeekBar.setProgress(shapeRenderer.getSize());
      sizeSeekBar.setOnSeekBarChangeListener(sizeSeekBarChanged);      
      
      // configure shape selection RadioButtons
      RadioButton radioCube = (RadioButton) findViewById(
         R.id.radioCube); 
      radioCube.setOnClickListener(shapeRadioListener);
      RadioButton radioPyramid = (RadioButton) findViewById(
         R.id.radioPyramid); 
      radioPyramid.setOnClickListener(shapeRadioListener);
      RadioButton radioPrism = (RadioButton) findViewById(
         R.id.radioPrism); 
      radioPrism.setOnClickListener(shapeRadioListener);
      
      // configure shape selection RadioButtons
      RadioButton radioMulti = (RadioButton) findViewById(
         R.id.radioMulti); 
      radioMulti.setOnClickListener(colorRadioListener);
      RadioButton radioRed = (RadioButton) findViewById(
         R.id.radioRed); 
      radioRed.setOnClickListener(colorRadioListener);
      RadioButton radioBlue = (RadioButton) findViewById(
         R.id.radioBlue); 
      radioBlue.setOnClickListener(colorRadioListener);
	  RadioButton radioGreen = (RadioButton) findViewById(R.id.radioGreen);
	  radioGreen.setOnClickListener(colorRadioListener);
      
      // get and configure the rotation SeekBars 
      xSeekBar = ((SeekBar) findViewById(R.id.xSeekBar));
      xSeekBar.setOnSeekBarChangeListener(rotationSeekBarChanged);
      xSeekBar.setProgress(shapeRenderer.getXRotation());
      ySeekBar = ((SeekBar) findViewById(R.id.ySeekBar));
      ySeekBar.setOnSeekBarChangeListener(rotationSeekBarChanged);
      xSeekBar.setProgress(shapeRenderer.getYRotation());
      zSeekBar = ((SeekBar) findViewById(R.id.zSeekBar));
      zSeekBar.setOnSeekBarChangeListener(rotationSeekBarChanged);
      xSeekBar.setProgress(shapeRenderer.getZRotation());
   } //end method onCreate

   // pause the Activity and the GLSurfaceView
   @Override
   protected void onPause() 
   {
      super.onPause(); // pause the Activity
      displayGLSurfaceView.onPause(); // pause the GLSurfaceView
   } // end method onPause
   
   // when the Activity resumes focus
   @Override
   protected void onResume() 
   {
      super.onResume(); // resume the Activity
      displayGLSurfaceView.onResume(); // resume the GLSurfaceView
   } // end method onResume
    
   // listener for shape RadioButtons
   private View.OnClickListener shapeRadioListener = 
      new View.OnClickListener()
      {
         @Override 
         public void onClick(View v)
         {
            switch (v.getId())
            {
               case R.id.radioCube:
                  shapeRenderer.setShape(CUBE); // display cube
                  break;
               case R.id.radioPyramid:
                  shapeRenderer.setShape(PYRAMID); // display pyramid
                  break;
               case R.id.radioPrism:
                  shapeRenderer.setShape(PRISM); // display prism
                  break;
            } // end switch
            
            // update SeekBars
            sizeSeekBar.setMax(shapeRenderer.getMaxSize());
            sizeSeekBar.setProgress(shapeRenderer.getSize()); 
            xSeekBar.setProgress(shapeRenderer.getXRotation());
            ySeekBar.setProgress(shapeRenderer.getYRotation());
            zSeekBar.setProgress(shapeRenderer.getXRotation());
         } // end method onClick
      }; // end OnClickListener
   
      // listener for shape RadioButtons
      private View.OnClickListener colorRadioListener = 
         new View.OnClickListener()
         {
            @Override 
            public void onClick(View v)
            {
               switch (v.getId())
               {
                  case R.id.radioMulti:
                     shapeRenderer.setColor(MULTI_COLOR); // display cube
                     break;
                  case R.id.radioRed:
                     shapeRenderer.setColor(RED); // display red
                     break;
                  case R.id.radioBlue:
                     shapeRenderer.setColor(BLUE); // display blue
                     break;
                  case R.id.radioGreen:
                      shapeRenderer.setColor(GREEN); // display green
                      break;
               } // end switch
            } // end method onClick
         }; // end OnClickListener   

   // OnSeekBarChangeListener for all the "Rotation" Dialog's SeekBars
   OnSeekBarChangeListener rotationSeekBarChanged = 
      new OnSeekBarChangeListener()
   {
	  // called when the thumb position of a SeekBar is changed
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
         boolean fromUser) 
      {
         switch (seekBar.getId()) // get the id of the SeekBar
         {
            case R.id.xSeekBar: // if it is the x-axis SeekBar
               shapeRenderer.setXRotation(seekBar.getProgress());
               break;
            case R.id.ySeekBar: // if it is the x-axis SeekBar
               shapeRenderer.setYRotation(seekBar.getProgress());
               break;
            case R.id.zSeekBar: // if it is the x-axis SeekBar
               shapeRenderer.setZRotation(seekBar.getProgress());
               break;
         } // end switch         
      } // end method onProgressChanged

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) 
      {
      } // end method onStartTrackingTouch

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) 
      {
      } // end method onStopTrackingTouch
   }; // end OnSeekBarChangeListener
      
   // OnSeekBarChangeListener for the "Size" Dialog's SeekBars
   OnSeekBarChangeListener sizeSeekBarChanged = 
      new OnSeekBarChangeListener()
   {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
         boolean fromUser) 
      {
         shapeRenderer.setSize(progress);            
      } // end method onProgressChanged

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) 
      {
      } // end method onStartTrackingTouch

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) 
      {
      } // end method onStopTrackingTouch
   }; // end OnSeekBarChangeListener
} // end class ArtActivity

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
