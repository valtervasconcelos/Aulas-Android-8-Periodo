// AddCityDialogFragment.java
// DialogFragment allowing the user to enter a new city's zipcode.
package com.deitel.weatherviewer;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddCityDialogFragment extends DialogFragment 
   implements OnClickListener 
{
   // listens for results from the AddCityDialog
   public interface DialogFinishedListener 
   {
      // called when the AddCityDialog is dismissed
      void onDialogFinished(String zipcodeString, boolean preferred);
   } // end interface DialogFinishedListener   
   
   EditText addCityEditText; // the DialogFragment's EditText
   CheckBox addCityCheckBox; // the DialogFragment's CheckBox
   
   // initializes a new DialogFragment
   @Override
   public void onCreate(Bundle bundle)
   {
      super.onCreate(bundle);
      
      // allow the user to exit using the back key
      this.setCancelable(true); 
   } // end method onCreate
   
   // inflates the DialogFragment's layout
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
      Bundle argumentsBundle)
   {
      // inflate the layout defined in add_city_dialog.xml 
      View rootView = inflater.inflate(R.layout.add_city_dialog, container, 
         false);
      
      // get the EditText
      addCityEditText = (EditText) rootView.findViewById(
         R.id.add_city_edit_text);
      
      // get the CheckBox
      addCityCheckBox = (CheckBox) rootView.findViewById(
         R.id.add_city_checkbox);
      
      if (argumentsBundle != null) // if the arguments Bundle isn't empty
      {
         addCityEditText.setText(argumentsBundle.getString(
            getResources().getString(
               R.string.add_city_dialog_bundle_key)));
      } // end if
      
      // set the DialogFragment's title
      getDialog().setTitle(R.string.add_city_dialog_title); 
      
      // initialize the positive Button
      Button okButton = (Button) rootView.findViewById(
         R.id.add_city_button);
      okButton.setOnClickListener(this);
      return rootView; // return the Fragment's root View
   } // end method onCreateView
   
   // save this DialogFragment's state
   @Override
   public void onSaveInstanceState(Bundle argumentsBundle)
   {
      // add the EditText's text to the arguments Bundle
      argumentsBundle.putCharSequence(getResources().getString(
         R.string.add_city_dialog_bundle_key), 
         addCityEditText.getText().toString());
      super.onSaveInstanceState(argumentsBundle);
   } // end method onSaveInstanceState

   // called when the Add City Button is clicked
   @Override
   public void onClick(View clickedView) 
   {
      if (clickedView.getId() == R.id.add_city_button)
      {
         DialogFinishedListener listener = 
            (DialogFinishedListener) getActivity();
         listener.onDialogFinished(addCityEditText.getText().toString(), 
            addCityCheckBox.isChecked() );
         dismiss(); // dismiss the DialogFragment
      } // end if
   } // end method onClick
} // end class AddCityDialogFragment