// PictureTaker.java
// Activity for taking a picture with the device's camera
package com.deitel.enhancedslideshow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;  
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class PictureTaker extends Activity
{
   private static final String TAG = "PICTURE_TAKER"; // for logging errors

   private SurfaceView surfaceView; // used to display camera preview     
   private SurfaceHolder surfaceHolder; // manages the SurfaceView changes
   private boolean isPreviewing; // is the preview running?

   private Camera camera; // used to capture image data
   private List<String> effects; // supported color effects for camera
   private List<Camera.Size> sizes; // supported preview sizes for camera
   private String effect = Camera.Parameters.EFFECT_NONE; // default effect
   
   // called when the activity is first created
   @Override
   public void onCreate(Bundle bundle) 
   {
      super.onCreate(bundle); 
      setContentView(R.layout.camera_preview); // set the layout
      
      // initialize the surfaceView and set its touch listener
      surfaceView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
      surfaceView.setOnTouchListener(touchListener);                   
      
      // initialize surfaceHolder and set object to handles its callbacks
      surfaceHolder = surfaceView.getHolder();   
      surfaceHolder.addCallback(surfaceCallback);

      // required before Android 3.0 for camera preview
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
   } // end method onCreate

   // create the Activity's menu from list of supported color effects
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      super.onCreateOptionsMenu(menu);

      // create menu items for each supported effect
      for (String effect : effects)
         menu.add(effect); 
      
      return true;
   } // end method onCreateOptionsMenu
   
   // handle choice from options menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      Camera.Parameters p = camera.getParameters(); // get parameters  
      p.setColorEffect(item.getTitle().toString()); // set color effect
      camera.setParameters(p); // apply the new parameters             
      return true;
   } // end method onOptionsItemSelected

   // handles SurfaceHolder.Callback events
   private SurfaceHolder.Callback surfaceCallback =
      new SurfaceHolder.Callback()                 
      {
         // release resources after the SurfaceView is destroyed
         @Override
         public void surfaceDestroyed(SurfaceHolder arg0)
         {
            camera.stopPreview(); // stop the Camera preview
            isPreviewing = false;
            camera.release(); // release the Camera's Object resources
         } // end method surfaceDestroyed
         
         // initialize the camera when the SurfaceView is created
         @Override
         public void surfaceCreated(SurfaceHolder arg0)
         {
            // get camera and its supported color effects/preview sizes
            camera = Camera.open(); // defaults to back facing camera   
            effects = camera.getParameters().getSupportedColorEffects();
            sizes = camera.getParameters().getSupportedPreviewSizes();  
         } // end method surfaceCreated
         
         @Override
         public void surfaceChanged(SurfaceHolder holder, int format,
            int width, int height)                                   
         {
            if (isPreviewing) // if there's already a preview running
               camera.stopPreview(); // stop the preview
      
            // configure and set the camera parameters
            Camera.Parameters p = camera.getParameters();               
            p.setPreviewSize(sizes.get(0).width, sizes.get(0).height);  
            p.setColorEffect(effect); // use the current selected effect
            camera.setParameters(p); // apply the new parameters        
      
            try 
            {
               camera.setPreviewDisplay(holder); // display using holder
            } // end try
            catch (IOException e) 
            {
               Log.v(TAG, e.toString());
            } // end catch
      
            camera.startPreview(); // begin the preview
            isPreviewing = true;
         } // end method surfaceChanged
      }; // end SurfaceHolder.Callback

   // handles Camera callbacks
   Camera.PictureCallback pictureCallback = new Camera.PictureCallback()
   {
      // called when the user takes a picture
      public void onPictureTaken(byte[] imageData, Camera c)
      {
         // use "Slideshow_" + current time in ms as new image file name
         String fileName = "Slideshow_" + System.currentTimeMillis();
   
         // create a ContentValues and configure new image's data
         ContentValues values = new ContentValues();
         values.put(Images.Media.TITLE, fileName);
         values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
         values.put(Images.Media.MIME_TYPE, "image/jpg");
   
         // get a Uri for the location to save the file
         Uri uri = getContentResolver().insert(
            Images.Media.EXTERNAL_CONTENT_URI, values);
   
         try 
         {
            // get an OutputStream to uri
            OutputStream outStream = 
               getContentResolver().openOutputStream(uri);
            outStream.write(imageData); // output the image
            outStream.flush(); // empty the buffer
            outStream.close(); // close the stream
            
            // Intent for returning data to SlideshowEditor
            Intent returnIntent = new Intent();
            returnIntent.setData(uri); // return Uri to SlideshowEditor 
            setResult(RESULT_OK, returnIntent); // took pic successfully
   
            // display a message indicating that the image was saved
            Toast message = Toast.makeText(PictureTaker.this, 
               R.string.message_saved, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, 
               message.getYOffset() / 2);
            message.show(); // display the Toast
         
            finish(); // finish and return to SlideshowEditor
         } // end try
         catch (IOException ex) 
         {
            setResult(RESULT_CANCELED); // error taking picture

            // display a message indicating that the image was saved
            Toast message = Toast.makeText(PictureTaker.this, 
               R.string.message_error_saving, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, 
               message.getYOffset() / 2);
            message.show(); // display the Toast
         } // end catch
      } // end method onPictureTaken
   }; // end pictureCallback

   // takes picture when user touches the screen
   private OnTouchListener touchListener = new OnTouchListener()
   {
      @Override
      public boolean onTouch(View v, MotionEvent event) 
      {
         // take a picture
         camera.takePicture(null, null, pictureCallback);
         return false;
      } // end method onTouch
   }; // end touchListener
} // end class PictureTaker




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