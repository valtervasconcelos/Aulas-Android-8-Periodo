// SavedRecordings.java
// Activity displaying and playing saved recordings.
package com.deitel.voicerecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SavedRecordings extends ListActivity 
{
   private static final String TAG = SavedRecordings.class.getName();
	
   // SavedRecordingsAdapter displays list of saved recordings in ListView
   private SavedRecordingsAdapter savedRecordingsAdapter;
	
   private MediaPlayer mediaPlayer; // plays saved recordings
   private SeekBar progressSeekBar; // controls audio playback
   private Handler handler; // updates the SeekBar thumb position
   private TextView nowPlayingTextView; // displays audio name
   private ToggleButton playPauseButton; // displays audio name

   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.saved_recordings);

      // get ListView and set its listeners and adapter 
      ListView listView = getListView();
      savedRecordingsAdapter = new SavedRecordingsAdapter(this, 
         new ArrayList<String>(
            Arrays.asList(getExternalFilesDir(null).list())));
      listView.setAdapter(savedRecordingsAdapter);
      
      handler = new Handler(); // updates SeekBar thumb position

      // get other GUI components and register listeners
      progressSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);
      progressSeekBar.setOnSeekBarChangeListener(
         progressChangeListener);
      playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
      playPauseButton.setOnCheckedChangeListener(playPauseButtonListener);
      nowPlayingTextView = 
         (TextView) findViewById(R.id.nowPlayingTextView);
   } // end method onCreate
   
   // create the MediaPlayer object
   @Override
   protected void onResume()
   {
      super.onResume();
      mediaPlayer = new MediaPlayer(); // plays recordings
   } // end method onResume

   // release the MediaPlayer object
   @Override
   protected void onPause()
   {
      super.onPause();
      
      if (mediaPlayer != null)
      {
         handler.removeCallbacks(updater); // stop updating GUI
         mediaPlayer.stop(); // stop audio playback
         mediaPlayer.release(); // release MediaPlayer resources
         mediaPlayer = null; 
      } // end if
   } // end method onPause

   // Class for implementing the view-holder pattern
   // for better ListView performance
   private static class ViewHolder
   {
      TextView nameTextView; 
      ImageView emailButton; 
      ImageView deleteButton; 
   } // end class ViewHolder
   
   // ArrayAdapter displaying recording names and delete buttons
   private class SavedRecordingsAdapter extends ArrayAdapter<String>
   {
      private List<String> items; // list of file names
      private LayoutInflater inflater;
      
      public SavedRecordingsAdapter(Context context, List<String> items)
      {
         super(context, -1, items); // -1 indicates we're customizing view
         Collections.sort(items, String.CASE_INSENSITIVE_ORDER);
         this.items = items;
         inflater = (LayoutInflater) 
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      } // end SavedRecordingsAdapter constructor

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
         ViewHolder viewHolder; // holds references to current item's GUI
         
         // if convertView is null, inflate GUI and create ViewHolder;
         // otherwise, get existing ViewHolder
         if (convertView == null)
         {
            convertView = 
               inflater.inflate(R.layout.saved_recordings_row, null);
            
            // set up ViewHolder for this ListView item
            viewHolder = new ViewHolder();
            viewHolder.nameTextView = 
               (TextView) convertView.findViewById(R.id.nameTextView);
            viewHolder.emailButton = 
               (ImageView) convertView.findViewById(R.id.emailButton);
            viewHolder.deleteButton = 
               (ImageView) convertView.findViewById(R.id.deleteButton);
            convertView.setTag(viewHolder); // store as View's tag
         } // end if
         else // get the ViewHolder from the convertView's tag
            viewHolder = (ViewHolder) convertView.getTag();
         
         // get and display name of recording file 
         String item = items.get(position);
         viewHolder.nameTextView.setText(item); 

         // configure listeners for email and delete "buttons"
         viewHolder.emailButton.setTag(item);
         viewHolder.emailButton.setOnClickListener(emailButtonListener);
         viewHolder.deleteButton.setTag(item);
         viewHolder.deleteButton.setOnClickListener(deleteButtonListener);

         return convertView;
      } // end method getView
   } // end class SavedRecordingsAdapter
   
   // sends specified recording as e-mail attachment
   OnClickListener emailButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // get Uri to the recording's location on disk
         Uri data = Uri.fromFile(
            new File(getExternalFilesDir(null), (String) v.getTag()));
         
         // create Intent to send e-mail
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("text/plain");
         intent.putExtra(Intent.EXTRA_STREAM, data);
         startActivity(Intent.createChooser(intent,
            getResources().getString(R.string.emailPickerTitle)));
      } // end method onClick
   }; // end OnClickListener

   // deletes the specified recording
   OnClickListener deleteButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // create an input dialog to get recording name from user
         AlertDialog.Builder confirmDialog = 
            new AlertDialog.Builder(SavedRecordings.this);
         confirmDialog.setTitle(R.string.dialog_confirm_title); 
         confirmDialog.setMessage(R.string.dialog_confirm_message); 

         confirmDialog.setPositiveButton(R.string.button_delete, 
            new DialogInterface.OnClickListener()
            { 
               public void onClick(DialogInterface dialog, int which) 
               {
                  File fileToDelete = new File(getExternalFilesDir(null)+
                     File.separator + (String) v.getTag()); 
                  fileToDelete.delete();
                  savedRecordingsAdapter.remove((String) v.getTag());
               } // end method onClick 
            } // end anonymous inner class
         ); // end call to setPositiveButton
         
         confirmDialog.setNegativeButton(R.string.button_cancel, null);
         confirmDialog.show();         
      } // end method onClick
   }; // end OnClickListener

   @Override
   protected void onListItemClick(ListView l, View v, int position, 
      long id) 
   {
      super.onListItemClick(l, v, position, id);
      playPauseButton.setChecked(true); // checked state
      handler.removeCallbacks(updater); // stop updating progressSeekBar
      
      // get the item that was clicked 
      TextView nameTextView = 
         ((TextView) v.findViewById(R.id.nameTextView));
      String name = nameTextView.getText().toString(); 
      
      // get path to file
      String filePath = getExternalFilesDir(null).getAbsolutePath() + 
         File.separator + name;
      
      // set nowPlayingTextView's text
      nowPlayingTextView.setText(getResources().getString(
         R.string.now_playing_prefix) + " " + name);
    
      try 
      {
         // set the MediaPlayer to play the file at filePath
         mediaPlayer.reset(); // reset the MediaPlayer
         mediaPlayer.setDataSource(filePath);
         mediaPlayer.prepare(); // prepare the MediaPlayer
         progressSeekBar.setMax(mediaPlayer.getDuration());
         progressSeekBar.setProgress(0);
         mediaPlayer.setOnCompletionListener(
            new OnCompletionListener()
            {
               @Override
               public void onCompletion(MediaPlayer mp)
               {
                  playPauseButton.setChecked(false); // unchecked state
                  mp.seekTo(0);
               } // end method onCompletion
            } // end OnCompletionListener
         ); // end call to setOnCompletionListener
         mediaPlayer.start();
         updater.run(); // start updating progressSeekBar
      } // end try
      catch (Exception e) 
      {
         Log.e(TAG, e.toString()); // log exceptions
      } // end catch
   } // end method onListItemClick   
   
   // reacts to events created when the Seekbar's thumb is moved
   OnSeekBarChangeListener progressChangeListener = 
      new OnSeekBarChangeListener() 
      {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) 
         {
            if (fromUser)
               mediaPlayer.seekTo(seekBar.getProgress());
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

   // updates the SeekBar every second
   Runnable updater = new Runnable() 
   {
      @Override
      public void run() 
      {
         if (mediaPlayer.isPlaying())
         {
            // update the SeekBar's position
            progressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this, 100);
         } // end if
      } // end method run
   }; // end Runnable

   // called when the user touches the "Play" Button
   OnCheckedChangeListener playPauseButtonListener = 
      new OnCheckedChangeListener() 
      {
         // toggle play/pause
         @Override
         public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked)
         {
            if (isChecked)
            {
               mediaPlayer.start(); // start the MediaPlayer
               updater.run(); // start updating progress SeekBar
            }
            else
               mediaPlayer.pause(); // pause the MediaPlayer
         } // end method onCheckedChanged
      }; // end OnCheckedChangedListener
} // end class SavedRecordings


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