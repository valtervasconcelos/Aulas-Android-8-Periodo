// VisualizerView.java
// Visualizer for the audio being recorded.
package com.deitel.voicerecorder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View 
{
   private static final int LINE_WIDTH = 1; // width of visualizer lines
   private static final int LINE_SCALE = 75; // scales visualizer lines
   private List<Float> amplitudes; // amplitudes for line lengths
   private int width; // width of this View
   private int height; // height of this View
   private Paint linePaint; // specifies line drawing characteristics
   
   // constructor
   public VisualizerView(Context context, AttributeSet attrs) 
   {
      super(context, attrs); // call superclass constructor
      linePaint = new Paint(); // create Paint for lines
      linePaint.setColor(Color.GREEN); // set color to green
      linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
   } // end VisualizerView constructor
   
   // called when the dimensions of the View change
   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh)
   {
      width = w; // new width of this View
      height = h; // new height of this View
      amplitudes = new ArrayList<Float>(width / LINE_WIDTH); 
   } // end method onSizeChanged
   
   // clear all amplitudes to prepare for a new visualization
   public void clear()
   {
      amplitudes.clear();
   } // end method clear
   
   // add the given amplitude to the amplitudes ArrayList
   public void addAmplitude(float amplitude)
   {
      amplitudes.add(amplitude); // add newest to the amplitudes ArrayList
      
      // if the power lines completely fill the VisualizerView
      if (amplitudes.size() * LINE_WIDTH >= width)
      {
         amplitudes.remove(0); // remove oldest power value 
      } // end if
   } // end method addAmplitude
   
   // draw the visualizer with scaled lines representing the amplitudes
   @Override
   public void onDraw(Canvas canvas) 
   {
      int middle = height / 2; // get the middle of the View
      float curX = 0; // start curX at zero

      // for each item in the amplitudes ArrayList
      for (float power : amplitudes)
      {
         float scaledHeight = power / LINE_SCALE; // scale the power      
         curX += LINE_WIDTH; // increase X by LINE_WIDTH
         
         // draw a line representing this item in the amplitudes ArrayList
         canvas.drawLine(curX, middle + scaledHeight / 2, curX,
            middle - scaledHeight / 2, linePaint);
      } // end for   
   } // end method onDraw
} // end class VisualizerView





/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/