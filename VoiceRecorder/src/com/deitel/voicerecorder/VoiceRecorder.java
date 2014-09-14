// VoiceRecorder.java
// Main Activity for the VoiceRecorder class.
package com.deitel.voicerecorder;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VoiceRecorder extends Activity 
{
   private static final String TAG = VoiceRecorder.class.getName();	
   private MediaRecorder recorder; // used to record audio
   private Handler handler; // Handler for updating the visualizer
   private boolean recording; // are we currently recording?
   
   // variables for GUI
   private VisualizerView visualizer; 
   private ToggleButton recordButton;
   private Button saveButton;
   private Button deleteButton;
   private Button viewSavedRecordingsButton;
   
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main); // set the Activity's layout
      
      // get the Activity's Buttons and VisualizerView
      recordButton = 
         (ToggleButton) findViewById(R.id.recordButton);
      saveButton = (Button) findViewById(R.id.saveButton);
      saveButton.setEnabled(false); // disable saveButton initially
      deleteButton = (Button) findViewById(R.id.deleteButton);
      deleteButton.setEnabled(false); // disable deleteButton initially
      viewSavedRecordingsButton = 
         (Button) findViewById(R.id.viewSavedRecordingsButton);
      visualizer = (VisualizerView) findViewById(R.id.visualizerView);
      
      // register listeners
      saveButton.setOnClickListener(saveButtonListener);
      deleteButton.setOnClickListener(deleteButtonListener);
      viewSavedRecordingsButton.setOnClickListener(
         viewSavedRecordingsListener);
            
      handler = new Handler(); // create the Handler for visualizer update
   } // end method onCreate
   
   // create the MediaRecorder
   @Override
   protected void onResume()
   {
      super.onResume();
      
      // register recordButton's listener
      recordButton.setOnCheckedChangeListener(recordButtonListener);
   } // end method onResume
   
   // release the MediaRecorder
   @Override
   protected void onPause()
   {
      super.onPause();
      recordButton.setOnCheckedChangeListener(null); // remove listener
      
      if (recorder != null)
      {
         handler.removeCallbacks(updateVisualizer); // stop updating GUI
         visualizer.clear(); // clear visualizer for next recording
         recordButton.setChecked(false); // reset recordButton
         viewSavedRecordingsButton.setEnabled(true); // enable
         recorder.release(); // release MediaRecorder resources
         recording = false; // we are no longer recording
         recorder = null; 
         ((File) deleteButton.getTag()).delete(); // delete the temp file
      } // end if
   } // end method onPause

   // starts/stops a recording
   OnCheckedChangeListener recordButtonListener = 
      new OnCheckedChangeListener() 
      {
         @Override
         public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked)
         {
            if (isChecked)
            {
               visualizer.clear(); // clear visualizer for next recording
               saveButton.setEnabled(false); // disable saveButton
               deleteButton.setEnabled(false); // disable deleteButton
               viewSavedRecordingsButton.setEnabled(false); // disable 

               // create MediaRecorder and configure recording options
               if (recorder == null)
                  recorder = new MediaRecorder(); // create MediaRecorder 
               recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
               recorder.setOutputFormat(
                  MediaRecorder.OutputFormat.THREE_GPP);
               recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
               recorder.setAudioEncodingBitRate(16); 
               recorder.setAudioSamplingRate(44100);
               
               try 
               {
                  // create temporary file to store recording
                  File tempFile = File.createTempFile(
                     "VoiceRecorder", ".3gp", getExternalFilesDir(null));
                  
                  // store File as tag for saveButton and deleteButton 
                  saveButton.setTag(tempFile);
                  deleteButton.setTag(tempFile);
                  
                  // set the MediaRecorder's output file
                  recorder.setOutputFile(tempFile.getAbsolutePath());
                  recorder.prepare(); // prepare to record   
                  recorder.start(); // start recording
                  recording = true; // we are currently recording
                  handler.post(updateVisualizer); // start updating view
               } // end try
               catch (IllegalStateException e) 
               {
                  Log.e(TAG, e.toString());
               } // end catch 
               catch (IOException e) 
               {
                  Log.e(TAG, e.toString());
               } // end catch               
            } // end if
            else
            {
               recorder.stop(); // stop recording
               recorder.reset(); // reset the MediaRecorder
               recording = false; // we are no longer recording
               saveButton.setEnabled(true); // enable saveButton
               deleteButton.setEnabled(true); // enable deleteButton
               recordButton.setEnabled(false); // disable recordButton
            } // end else
         } // end method onCheckedChanged
      }; // end OnCheckedChangedListener

   // updates the visualizer every 50 milliseconds
   Runnable updateVisualizer = new Runnable() 
   {
      @Override
      public void run() 
      {
         if (recording) // if we are already recording
         {
            // get the current amplitude
            int x = recorder.getMaxAmplitude();
            visualizer.addAmplitude(x); // update the VisualizeView
            visualizer.invalidate(); // refresh the VisualizerView
            handler.postDelayed(this, 50); // update in 50 milliseconds
         } // end if
      } // end method run
   }; // end Runnable

   // saves a recording
   OnClickListener saveButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // get a reference to the LayoutInflater service
         LayoutInflater inflater = (LayoutInflater) getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
   
         // inflate name_edittext.xml to create an EditText
         View view = inflater.inflate(R.layout.name_edittext, null);
         final EditText nameEditText = 
            (EditText) view.findViewById(R.id.nameEditText);       
            
         // create an input dialog to get recording name from user
         AlertDialog.Builder inputDialog = 
            new AlertDialog.Builder(VoiceRecorder.this);
         inputDialog.setView(view); // set the dialog's custom View
         inputDialog.setTitle(R.string.dialog_set_name_title); 
         inputDialog.setPositiveButton(R.string.button_save, 
            new DialogInterface.OnClickListener()
            { 
               public void onClick(DialogInterface dialog, int which) 
               {
                  // create a SlideshowInfo for a new slideshow
                  String name = nameEditText.getText().toString().trim();
                  
                  if (name.length() != 0)
                  {
                     // create Files for temp file and new file name
                     File tempFile = (File) v.getTag();
                     File newFile = new File(
                        getExternalFilesDir(null).getAbsolutePath() + 
                           File.separator + 
                           name + ".3gp");
                     tempFile.renameTo(newFile); // rename the file
                     saveButton.setEnabled(false); // disable 
                     deleteButton.setEnabled(false); // disable 
                     recordButton.setEnabled(true); // enable 
                     viewSavedRecordingsButton.setEnabled(true); // enable 
                  } // end if
                  else
                  {
                     // display message that slideshow must have a name
                     Toast message = Toast.makeText(VoiceRecorder.this, 
                        R.string.message_name, Toast.LENGTH_SHORT);
                     message.setGravity(Gravity.CENTER, 
                        message.getXOffset() / 2, 
                        message.getYOffset() / 2);
                     message.show(); // display the Toast
                  } // end else
               } // end method onClick 
            } // end anonymous inner class
         ); // end call to setPositiveButton
         
         inputDialog.setNegativeButton(R.string.button_cancel, null);
         inputDialog.show();
      } // end method onClick
   }; // end OnClickListener
   
   // deletes the temporary recording
   OnClickListener deleteButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // create an input dialog to get recording name from user
         AlertDialog.Builder confirmDialog = 
            new AlertDialog.Builder(VoiceRecorder.this);
         confirmDialog.setTitle(R.string.dialog_confirm_title); 
         confirmDialog.setMessage(R.string.dialog_confirm_message); 

         confirmDialog.setPositiveButton(R.string.button_delete, 
            new DialogInterface.OnClickListener()
            { 
               public void onClick(DialogInterface dialog, int which) 
               {
                  ((File) v.getTag()).delete(); // delete the temp file
                  saveButton.setEnabled(false); // disable
                  deleteButton.setEnabled(false); // disable
                  recordButton.setEnabled(true); // enable
                  viewSavedRecordingsButton.setEnabled(true); // enable 
               } // end method onClick 
            } // end anonymous inner class
         ); // end call to setPositiveButton
         
         confirmDialog.setNegativeButton(R.string.button_cancel, null);
         confirmDialog.show();         
         recordButton.setEnabled(true); // enable recordButton
      } // end method onClick
   }; // end OnClickListener

   // launch Activity to view saved recordings
   OnClickListener viewSavedRecordingsListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         // launch the SaveRecordings Activity
         Intent intent = 
            new Intent(VoiceRecorder.this, SavedRecordings.class);
         startActivity(intent);
      } // end method onClick
   }; // end OnClickListener
} // end class VoiceRecorder




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