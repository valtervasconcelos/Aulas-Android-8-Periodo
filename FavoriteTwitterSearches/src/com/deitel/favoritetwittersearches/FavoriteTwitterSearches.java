// FavoriteTwitterSearches.java
// Stores Twitter search queries and tags for easily opening them 
// in a browser.
package com.deitel.favoritetwittersearches;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

// main (and only) Activity class for the Favorite Twitter Searches app
public class FavoriteTwitterSearches extends Activity 
{
   private SharedPreferences savedSearches; // user's favorite searches
   private TableLayout queryTableLayout; // shows the search buttons
   private EditText queryEditText; // where the user enters queries
   private EditText tagEditText; // where the user enters a query's tag

   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState); // call the superclass version
      setContentView(R.layout.main); // set the layout
      
      // get the SharedPreferences that contains the user's saved searches 
      savedSearches = getSharedPreferences("searches", MODE_PRIVATE);

      // get a reference to the queryTableLayout
      queryTableLayout = 
         (TableLayout) findViewById(R.id.queryTableLayout);
      
      // get references to the two EditTexts  
      queryEditText = (EditText) findViewById(R.id.queryEditText);
      tagEditText = (EditText) findViewById(R.id.tagEditText);
      
      // register listeners for the Save and Clear Tags Buttons
      Button saveButton = (Button) findViewById(R.id.saveButton);
      saveButton.setOnClickListener(saveButtonListener);
      Button clearTagsButton = 
        (Button) findViewById(R.id.clearTagsButton);
      clearTagsButton.setOnClickListener(clearTagsButtonListener);
      
      refreshButtons(null); // add previously saved searches to GUI
   } // end method onCreate
   
   // recreate search tag and edit Buttons for all saved searches;
   // pass null to create all the tag and edit Buttons.
   private void refreshButtons(String newTag) 
   {
      // store saved tags in the tags array
      String[] tags = 
         savedSearches.getAll().keySet().toArray(new String[0]); 
      Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER); // sort by tag

      // if a new tag was added, insert in GUI at the appropriate location
      if (newTag != null)
      {
         makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
      } // end if
      else // display GUI for all tags
      {         
         // display all saved searches
         for (int index = 0; index < tags.length; ++index)
            makeTagGUI(tags[index], index);
      } // end else
   } // end method refreshButtons

   // add new search to the save file, then refresh all Buttons
   private void makeTag(String query, String tag)
   {
      // originalQuery will be null if we're modifying an existing search
      String originalQuery = savedSearches.getString(tag, null);

      // get a SharedPreferences.Editor to store new tag/query pair
      SharedPreferences.Editor preferencesEditor = savedSearches.edit();
      preferencesEditor.putString(tag, query); // store current search
      preferencesEditor.apply(); // store the updated preferences
            
      // if this is a new query, add its GUI
      if (originalQuery == null) 
         refreshButtons(tag); // adds a new button for this tag
   } // end method makeTag
   
   // add a new tag button and corresponding edit button to the GUI
   private void makeTagGUI(String tag, int index)
   {
      // get a reference to the LayoutInflater service
      LayoutInflater inflater = (LayoutInflater) getSystemService(
         Context.LAYOUT_INFLATER_SERVICE);

      // inflate new_tag_view.xml to create new tag and edit Buttons
      View newTagView = inflater.inflate(R.layout.new_tag_view, null);
       
      // get newTagButton, set its text and register its listener
      Button newTagButton = 
         (Button) newTagView.findViewById(R.id.newTagButton);
      newTagButton.setText(tag); 
      newTagButton.setOnClickListener(queryButtonListener); 

      // get newEditButton and register its listener
      Button newEditButton = 
         (Button) newTagView.findViewById(R.id.newEditButton); 
      newEditButton.setOnClickListener(editButtonListener);

      // add new tag and edit buttons to queryTableLayout
      queryTableLayout.addView(newTagView, index);
   } // end makeTagGUI
   
   // remove all saved search Buttons from the app
   private void clearButtons()
   {
      // remove all saved search Buttons
      queryTableLayout.removeAllViews();
   } // end method clearButtons
   
   // create a new Button and add it to the ScrollView
   public OnClickListener saveButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         // create tag if both queryEditText and tagEditText are not empty
         if (queryEditText.getText().length() > 0 &&
            tagEditText.getText().length() > 0)
         {
            makeTag(queryEditText.getText().toString(), 
               tagEditText.getText().toString());
            queryEditText.setText(""); // clear queryEditText
            tagEditText.setText(""); // clear tagEditText
            
            // hide the soft keyboard
            ((InputMethodManager) getSystemService(
               Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
               tagEditText.getWindowToken(), 0);  
         } // end if
         else // display message asking user to provide a query and a tag
         {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = 
               new AlertDialog.Builder(FavoriteTwitterSearches.this);

            builder.setTitle(R.string.missingTitle); // title bar string

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.OK, null); 
            
            // set the message to display
            builder.setMessage(R.string.missingMessage);
            
            // create AlertDialog from the AlertDialog.Builder
            AlertDialog errorDialog = builder.create();
            errorDialog.show(); // display the Dialog
         } // end else
      } // end method onClick
   }; // end OnClickListener anonymous inner class

   // clears all saved searches
   public OnClickListener clearTagsButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         // create a new AlertDialog Builder
         AlertDialog.Builder builder = 
            new AlertDialog.Builder(FavoriteTwitterSearches.this);

         builder.setTitle(R.string.confirmTitle); // title bar string

         // provide an OK button that simply dismisses the dialog
         builder.setPositiveButton(R.string.erase,
            new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog, int button)
               {
                  clearButtons(); // clear all saved searches from the map
                  
                  // get a SharedPreferences.Editor to clear searches
                  SharedPreferences.Editor preferencesEditor = 
                     savedSearches.edit();
                  
                  preferencesEditor.clear(); // remove all tag/query pairs
                  preferencesEditor.apply(); //  the changes
               } // end method onClick
            } // end anonymous inner class
         ); // end call to method setPositiveButton
         
         builder.setCancelable(true);
         builder.setNegativeButton(R.string.cancel, null);
         
         // set the message to display
         builder.setMessage(R.string.confirmMessage);
         
         // create AlertDialog from the AlertDialog.Builder
         AlertDialog confirmDialog = builder.create();
         confirmDialog.show(); // display the Dialog
      } // end method onClick
   }; // end OnClickListener anonymous inner class

   // load selected search in a web browser
   public OnClickListener queryButtonListener = new OnClickListener()
   {
      @Override
      public void onClick(View v) 
      {
         // get the query
         String buttonText = ((Button)v).getText().toString();
         String query = savedSearches.getString(buttonText, "");
         
         // create the URL corresponding to the touched Button's query
         String urlString = getString(R.string.searchURL) + query;

         // create an Intent to launch a web browser    
         Intent webIntent = new Intent(Intent.ACTION_VIEW, 
            Uri.parse(urlString));                      

         startActivity(webIntent); // execute the Intent
      } // end method onClick
   }; // end OnClickListener anonymous inner class
   
   // edit selected search
   public OnClickListener editButtonListener = new OnClickListener()
   {
      @Override
      public void onClick(View v) 
      {
         // get all necessary GUI components
         TableRow buttonTableRow = (TableRow) v.getParent();
         Button searchButton = 
            (Button) buttonTableRow.findViewById(R.id.newTagButton);
         
         String tag = searchButton.getText().toString();
         
         // set EditTexts to match the chosen tag and query
         tagEditText.setText(tag);
         queryEditText.setText(savedSearches.getString(tag, ""));
      } // end method onClick
   }; // end OnClickListener anonymous inner class
} // end class FavoriteTwitterSearches

   
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

