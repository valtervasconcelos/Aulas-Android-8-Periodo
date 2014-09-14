// Pizza.java
// Main Activity for the Pizza App.
package com.deitel.pizza;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

public class Pizza extends Activity  
{
   private String phoneNumber; // phone number to which order is sent
   
   // identifying String for sent SMS message broadcast Intent
   private static final String BROADCAST_STRING = 
      "com.deitel.pizza.sent_sms";
   
   // SMS message broadcast Intent
   private BroadcastReceiver textMessageStatusBroadcastReceiver;

   // 0-based index of each pizza question
   private static final int PIZZA_SIZE_INDEX = 1;
   private static final int PEPPERONI_INDEX = 2;
   private static final int MUSHROOM_INDEX = 3;
   private static final int ORDER_SUMMARY_INDEX = 4;
   
   // message IDs to differentiate between a 
   // regular message and the final message
   private final static int UPDATE_TEXT_ID = 15;
   private final static int FINAL_UPDATE_TEXT_ID = 16;
   private final static int DISPLAY_TOAST_ID = 17;
   
   // String identifiers for restoring instance state
   private final static String INDEX_ID = "index";
   private final static String ORDER_ID = "order";
   private final static String LISTENING_ID = "listening";
      
   private TextToSpeech textToSpeech; // converts text to speech
   private int currentMessageIndex; // index of the current message
   
   private boolean waitingForResponse; // waiting for user response?
   private boolean listening; // waiting for Activity result?
   private TextView messageText; // used to display the current message
   private String order; // the pizza order
   
   private String[] audioMessages; // messages spoken by the app
   private String[] displayMessages; // messages displayed by the app
   
   private String errorMessageString; // message for unexpected response
   private String finalMessageString; // message when app sends order 

   // possible choices for each of the five order options
   private String[][] choices = new String[6][];
   
   private String positiveResponseString; // "Yes"
   private String negativeResponseString; // "No"
   
   private Resources resources; // used to access the app's Resources 
   private boolean quitInProgress; 
   
   private HashMap<String, String> ttsParams; // TextToSpeech parameters
   
   // Called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main); // set the Activity's layout
      
      // initialize TextToSpeech engine and register its OnInitListener
      textToSpeech = new TextToSpeech(this, 
         new OnInitListener()
         {
            // called when the TextToSpeech is initialized
            @Override
            public void onInit(int status) 
            {
               // speak U.S. English
               textToSpeech.setLanguage(Locale.US); 
               
               // set listener that responds to events generated
               // when messages are completed
               textToSpeech.setOnUtteranceCompletedListener(
                  new OnUtteranceCompletedListener()
                  {
                     @Override
                     public void onUtteranceCompleted(String id) 
                     {
                        utteranceCompleted();
                     } // end method onUtteranceCompleted      
                  } // end anonymous inner class                  
               ); // end call to setOnUtteranceCompletedListener
         
               playFirstMessage();
            } // end method onInit
         } // end anonymous inner class that implements OnInitListener         
      ); // end call to TextToSpeech constructor
      
      // used in calls to TextToSpeech's speak method to ensure that 
      // OnUtteranceCompletedListener is notified when speech completes
      ttsParams = new HashMap<String, String>();
      ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");
      
      currentMessageIndex = 1; // start at the first message
      waitingForResponse = false; // not waiting for user response
      
      // get the Activity's TextView
      messageText = (TextView) findViewById(R.id.mainText);
      
      loadResources(); // load String resources from xml
   } // end method onCreate

   // called when this Activity is resumed
   @Override
   public void onResume()
   {
      super.onResume();
      
      // create BoradcastReceiver to receive SMS message status broadcast
      textMessageStatusBroadcastReceiver = new BroadcastReceiver()
      {
         @Override
         public void onReceive(Context context, Intent intent) 
         {
            // if the message was not sent
            if (getResultCode() != Activity.RESULT_OK)
            {
                viewUpdateHandler.sendMessage(
                   viewUpdateHandler.obtainMessage(Pizza.DISPLAY_TOAST_ID,
                      R.string.text_error_message, 0, null));
            } // end if
         } // end method onReceive
      }; // end BroadcastReceiver anonymous inner class
     
     // register the receiver
     registerReceiver(textMessageStatusBroadcastReceiver, 
        new IntentFilter(Pizza.BROADCAST_STRING));
   } // end method onResume
   
   // called when this Activity is paused
   @Override
   public void onPause()
   {
      super.onPause();
      
      // if the BroadcastReceiver is not null, unregister it
      if (textMessageStatusBroadcastReceiver != null)
         unregisterReceiver(textMessageStatusBroadcastReceiver);
      
      textMessageStatusBroadcastReceiver = null;
   } // end method onPause
   
   // load String resources from XML
   private void loadResources()
   {
      resources = getResources(); // get the app's resources
      phoneNumber = resources.getString(
         R.string.phone_number); // load audio messages
      audioMessages = resources.getStringArray(
         R.array.audio_messages); // load audio messages
      displayMessages = resources.getStringArray(
         R.array.display_messages); // load the display messages
      errorMessageString = resources.getString(
         R.string.error_message); // error message
      finalMessageString = resources.getString(
         R.string.final_message); // final message
      positiveResponseString = resources.getString(
         R.string.positive_response); // "Yes"
      negativeResponseString = resources.getString(
         R.string.negative_response); // "No"
      
      // initialize the pizza order
      order = resources.getString(R.string.initial_order); 
      
      // load the valid user responses
      String[] binaryChoices = 
         resources.getStringArray(R.array.binary_choices);
      choices[PIZZA_SIZE_INDEX] = 
         resources.getStringArray(R.array.size_choices); 
      choices[PEPPERONI_INDEX] = binaryChoices;
      choices[MUSHROOM_INDEX] = binaryChoices;
      choices[ORDER_SUMMARY_INDEX] = binaryChoices;
   } // end method loadResources

   // speak the first message 
   private void playFirstMessage()
   {
      // speak the first message
      textToSpeech.speak(
         audioMessages[0], TextToSpeech.QUEUE_FLUSH, ttsParams);
   } // end method playFirstMessage

   // utility method called when speech completes and 
   // when it's time to move to the next message
   private void utteranceCompleted()
   {
      // if the TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID 
      // contains "quit" terminate the app
      String quit = 
         ttsParams.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);

      if (quit.equals("quit")) // check whether user wishes to quit
      {
         textToSpeech.shutdown(); // shut down the TextToSpeech
         finish();
      } // end if
      
      // allow user to quit
      if (currentMessageIndex >= displayMessages.length && 
         !quitInProgress) 
      {
         allowUserToQuit();
      } // end if      
      else if (!waitingForResponse) // if we're not waiting for a response 
      {
         // update the TextView
         viewUpdateHandler.sendMessage(
            viewUpdateHandler.obtainMessage(UPDATE_TEXT_ID)); 
         
         String words = ""; 
         
         // summarize the order
         if (currentMessageIndex == ORDER_SUMMARY_INDEX) 
         {
            words = resources.getString(R.string.order_summary_prefix);
            words += order.substring(order.indexOf(':') + 1);
         } // end if
         
         words += audioMessages[currentMessageIndex]; // next message
         words = words.replace(resources.getString(R.string.pepperoni), 
            resources.getString(R.string.pepperoni_speech));
         words = words.replace(resources.getString(R.string.pizza), 
            resources.getString(R.string.pizza_speech));

          // speak the next message
         textToSpeech.speak(words, TextToSpeech.QUEUE_FLUSH, ttsParams);
         waitingForResponse = true; // we are waiting for a response
      } // end if
      else if (!listening && currentMessageIndex > 0)
      {
         listen(); // capture the user's response
      } // end else if
   } // end method utteranceCompleted
   
   // listens for a user response
   private void listen() 
   {
      listening = true; // we are now listening
     
      // create Intent for speech recognition Activity
      Intent speechRecognitionIntent = 
         new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      
      // try to launch speech recognition Activity
      try
      {
         startActivityForResult(speechRecognitionIntent, 0);
      } // end try
      catch (ActivityNotFoundException exception) 
      {
         viewUpdateHandler.sendMessage(viewUpdateHandler.obtainMessage(
            Pizza.DISPLAY_TOAST_ID, R.string.no_speech_message, 0, null));
      } // end catch
   } // end method listen

   // called when the speech recognition Activity returns
   @Override
   protected void onActivityResult(int requestCode, int resultCode, 
      Intent dataIntent) 
   {
      listening = false;
      
      // if there was no error
      if (requestCode == 0 && resultCode == RESULT_OK) 
      {
         // get list of possible matches to user's speech
         ArrayList<String> possibleMatches = 
            dataIntent.getStringArrayListExtra(
               RecognizerIntent.EXTRA_RESULTS);

         // get current list of possible valid choices
         String[] validResponses;
         
         if (!quitInProgress) 
            validResponses = choices[currentMessageIndex];
         else 
            validResponses = 
               resources.getStringArray(R.array.binary_choices);

         if (validResponses == null) 
            return;
         
         // for each possible valid choice, compare to the user's speech 
         // to determine whether the user spoke one of those choices
         String result = null;

         checkForMatch: 
         for (String validResponse : validResponses)
         {
            for (String match : possibleMatches) 
            {
               if (validResponse.compareToIgnoreCase(match) == 0) 
               {
                  result = validResponse; // store the user response
                  break checkForMatch; // stop checking possible responses
               } // end if
            } // end for
         } // end for

         if (result == null) // there was no match
            playError(); // ask the user to repeat the response
         else if (quitInProgress) 
         {
            quitInProgress = false;
            
            // the user said to quit
            if (result.equalsIgnoreCase(positiveResponseString)) 
            {
               if (currentMessageIndex >= displayMessages.length) 
               {
                  reset(); // reset the order
                  return; // return
               } // end if
               else 
               {
                  ttsParams.put(
                     TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "quit");
                  
                  // speak the final message
                  textToSpeech.speak(
                     resources.getString(R.string.quit_message), 
                     TextToSpeech.QUEUE_FLUSH, ttsParams);
               } // end else
            } // end if
            else // the user wants to return
            {
               if (currentMessageIndex >= displayMessages.length) 
               {
                  ttsParams.put(
                     TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "quit");
                   
                  // speak the final message
                  textToSpeech.speak(
                     resources.getString(R.string.leave_message), 
                     TextToSpeech.QUEUE_FLUSH, ttsParams);
               } // end if
               else 
               {
                  listen(); 
               } // end else
            } // end else
         } // end else if
         // there was a match and it is on the last message
         else if (currentMessageIndex == displayMessages.length - 1) 
         {
            // the user said to send the order
            if (result.equalsIgnoreCase(positiveResponseString)) 
            {
               waitingForResponse = false; 
               ++currentMessageIndex;
               sendMessage(); // send the order as a text message
            } // end if
            else // the user canceled the order
            {
               reset(); // reset the order
               return; // return
            } // end else
         } // end else if
         else // there was a match and it is not the last message
         {
            // the user responded positively
            if (result.equalsIgnoreCase(positiveResponseString)) 
            {
               // if previous question asked if the user wants pepperoni
               if (currentMessageIndex == PEPPERONI_INDEX)
               {
                  // add pepperoni to the pizza order
                  order += resources.getString(R.string.pepperoni);
               } // end if
               else if (currentMessageIndex == MUSHROOM_INDEX)
               {
                  // add mushrooms to the pizza order
                  order += resources.getString(R.string.mushrooms);
               } // else if
            } // end if
            else if (!result.equalsIgnoreCase(negativeResponseString))
               order += ", " + result; // update the order              
             
            waitingForResponse = false;
            ++currentMessageIndex; // move to the next question
            
            utteranceCompleted(); // move to next message
         } // end else
      } // end if
      else if ((currentMessageIndex > 0 && !listening) || 
         resultCode == Activity.RESULT_CANCELED)
      {
         allowUserToQuit(); // listen for user input
      } // end else
      
      // call super method
      super.onActivityResult(requestCode, resultCode, dataIntent);
   } // end method onActivityResult
 
   // called when the user says an unexpected response
   private void playError() 
   {
      textToSpeech.speak(errorMessageString, // play error message
         TextToSpeech.QUEUE_FLUSH, ttsParams); 
   } // end method playError

   // start a new order
   private void reset() 
   {
      // reset the instance variables associated with taking an order
      currentMessageIndex = 1; 
      order = resources.getString(R.string.initial_order);
      waitingForResponse = false;
      listening = false;
   
      playFirstMessage(); 
   } // end method reset
   
   // save the order state
   @Override
   public void onSaveInstanceState(Bundle savedStateBundle)
   {
      // store the currentMessageIndex, order and listening values
      savedStateBundle.putInt(INDEX_ID, currentMessageIndex); 
      savedStateBundle.putString(ORDER_ID, order); 
      savedStateBundle.putBoolean(LISTENING_ID, listening); 
      
      super.onSaveInstanceState(savedStateBundle);
   } // end method onSaveInstanceState
   
   // restore the order state
   @Override
   public void onRestoreInstanceState(Bundle savedStateBundle)
   {
      // retrieve the currentMessageIndex, order and listening values
      currentMessageIndex = savedStateBundle.getInt(INDEX_ID); 
      order = savedStateBundle.getString(ORDER_ID); 
      listening = savedStateBundle.getBoolean(LISTENING_ID);
      super.onRestoreInstanceState(savedStateBundle);
   } // end method onRestoreInstanceState
   
   // send order as a text message
   private void sendMessage() 
   {
      Intent broadcastIntent = new Intent(Pizza.BROADCAST_STRING);
      PendingIntent messageSentPendingIntent = 
         PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);
      
      // get the default SMSManager
      SmsManager smsManager = SmsManager.getDefault();

      // send the order to PHONE_NUMBER
      smsManager.sendTextMessage(phoneNumber, null, order, 
         messageSentPendingIntent, null);

      // display the final message
      viewUpdateHandler.sendMessage(
         viewUpdateHandler.obtainMessage(FINAL_UPDATE_TEXT_ID)); 
   } // end method sendMessage

   // updates the UI
   private Handler viewUpdateHandler = new Handler() 
   {
      // displays the given next message
      public void handleMessage(Message receivedMessage) 
      {
         // process Message based on the ID stored in receivedMessage.what
         switch (receivedMessage.what) 
         {
            case Pizza.UPDATE_TEXT_ID: // if it is not the last message 
               // display the message
               String text = "";
               
               // if next message is the last one 
               if (currentMessageIndex == displayMessages.length - 1)
                  text = order;
              
               text += displayMessages[currentMessageIndex];
               messageText.setText(text);
               break;
            case Pizza.FINAL_UPDATE_TEXT_ID: // if order is complete
               // display and play the final message
               messageText.setText(finalMessageString);
              
               // speak the final message
               textToSpeech.speak(finalMessageString, 
                  TextToSpeech.QUEUE_FLUSH, ttsParams);
               break;
            case DISPLAY_TOAST_ID:
               // if speech recognition is not available on this device 
               // inform the user using a Toast
               Toast.makeText(Pizza.this, receivedMessage.arg1, 
                  Toast.LENGTH_LONG).show();
         } // end switch statement
      } // end method handleMessage
   }; // end Handler
   
   // allow the user to exit the app
   private void allowUserToQuit() 
   {
      quitInProgress = true;
      waitingForResponse = true;
      
      // if the order is complete, ask whether to quit or start new order
      if (currentMessageIndex >= displayMessages.length) 
      {
         textToSpeech.speak(
            resources.getString(R.string.leave_question), 
            TextToSpeech.QUEUE_FLUSH, ttsParams); 
      } // end if
      else // ask whether to quit or continue order
      {
         textToSpeech.speak(
            resources.getString(R.string.quit_question), 
            TextToSpeech.QUEUE_FLUSH, ttsParams);
      } // end else
   } // end method allowUserToQuit

   // when the app is shut down
   @Override
   public void onDestroy() 
   {
      super.onDestroy(); // call super method
      textToSpeech.shutdown(); // shut down the TextToSpeech
   } // end method onDestroy
} // end class Pizza



/*************************************************************************
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
*************************************************************************/
