// ViewContact.java
// Activity for viewing a single contact.
package com.deitel.addressbook;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ViewContact extends Activity 
{
   private static final String TAG = ViewContact.class.getName();
   
   // Intent request code used to start an Activity that returns a result
   private static final int REQUEST_CONNECT_DEVICE = 1;

   private BluetoothAdapter bluetoothAdapter = null; // Bluetooth adapter
   private Handler handler; // for displaying Toasts in the GUI thread
   
   private long rowID; // selected contact's row ID in the database
   private TextView nameTextView; // displays contact's name 
   private TextView phoneTextView; // displays contact's phone
   private TextView emailTextView; // displays contact's e-mail
   private TextView streetTextView; // displays contact's street
   private TextView cityTextView; // displays contact's city/state/zip      
   
   // called when the Activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.view_contact); // inflate GUI

      // get the EditTexts
      nameTextView = (TextView) findViewById(R.id.nameTextView);
      phoneTextView = (TextView) findViewById(R.id.phoneTextView);
      emailTextView = (TextView) findViewById(R.id.emailTextView);
      streetTextView = (TextView) findViewById(R.id.streetTextView);
      cityTextView = (TextView) findViewById(R.id.cityTextView);
      
      // get the selected contact's row ID
      Bundle extras = getIntent().getExtras();
      rowID = extras.getLong(AddressBook.ROW_ID); 
      
      // get the default Bluetooth adapter
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
     
      handler = new Handler(); // create the Handler
   } // end method onCreate

   // called when the activity is first created
   @Override
   protected void onResume()
   {
      super.onResume();
      
      // create new LoadContactTask and execute it 
      new LoadContactTask().execute(rowID);
   } // end method onResume
   
   // performs database query outside GUI thread
   private class LoadContactTask extends AsyncTask<Long, Object, Cursor> 
   {
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(ViewContact.this);

      // perform the database access
      @Override
      protected Cursor doInBackground(Long... params)
      {
         databaseConnector.open();
         
         // get a cursor containing all data on given entry
         return databaseConnector.getOneContact(params[0]);
      } // end method doInBackground

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result)
      {
         super.onPostExecute(result);
   
         result.moveToFirst(); // move to the first item 
   
         // get the column index for each data item
         int nameIndex = result.getColumnIndex("name");
         int phoneIndex = result.getColumnIndex("phone");
         int emailIndex = result.getColumnIndex("email");
         int streetIndex = result.getColumnIndex("street");
         int cityIndex = result.getColumnIndex("city");
   
         // fill TextViews with the retrieved data
         nameTextView.setText(result.getString(nameIndex));
         phoneTextView.setText(result.getString(phoneIndex));
         emailTextView.setText(result.getString(emailIndex));
         streetTextView.setText(result.getString(streetIndex));
         cityTextView.setText(result.getString(cityIndex));
   
         result.close(); // close the result cursor
         databaseConnector.close(); // close database connection
      } // end method onPostExecute
   } // end class LoadContactTask
      
   // create the Activity's menu from a menu resource XML file
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.view_contact_menu, menu);
      return true;
   } // end method onCreateOptionsMenu
   
   // handle choice from options menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      switch (item.getItemId()) // switch based on selected MenuItem's ID
      {
         case R.id.editItem: // selected Edit Contact menu item
            // create an Intent to launch the AddEditContact Activity
            Intent addEditContact =
               new Intent(this, AddEditContact.class);
            
            // pass the selected contact's data as extras with the Intent
            addEditContact.putExtra(AddressBook.ROW_ID, rowID);
            addEditContact.putExtra("name", nameTextView.getText());
            addEditContact.putExtra("phone", phoneTextView.getText());
            addEditContact.putExtra("email", emailTextView.getText());
            addEditContact.putExtra("street", streetTextView.getText());
            addEditContact.putExtra("city", cityTextView.getText());
            startActivity(addEditContact); // start the Activity
            break;
         case R.id.deleteItem: // selected Delete Contact menu item
            deleteContact(); // delete the displayed contact
            break;
         case R.id.transferItem: // selected Transfer Contact menu item
            // if we are not already connected
            if (bluetoothAdapter.isEnabled()) 
            {
               // launch DeviceChooser so user can pick a nearby device
               Intent serverIntent = 
                  new Intent(this, DeviceChooser.class);
               startActivityForResult(
                  serverIntent, REQUEST_CONNECT_DEVICE);
            } // end if    
            else // indicate that Bluetooth is not enabled
            {
               Toast.makeText(this, 
                  R.string.no_bluetooth, Toast.LENGTH_LONG).show();
            } // end else
            break;
      } // end switch
      
      return super.onOptionsItemSelected(item);
   } // end method onOptionsItemSelected
   
   // delete a contact
   private void deleteContact()
   {
      // create a new AlertDialog Builder
      AlertDialog.Builder builder = 
         new AlertDialog.Builder(ViewContact.this);

      builder.setTitle(R.string.confirmTitle); // title bar string
      builder.setMessage(R.string.confirmMessage); // message to display

      // provide an OK button that simply dismisses the dialog
      builder.setPositiveButton(R.string.button_delete,
         new DialogInterface.OnClickListener()
         {
            @Override
            public void onClick(DialogInterface dialog, int button)
            {
               final DatabaseConnector databaseConnector = 
                  new DatabaseConnector(ViewContact.this);

               // create an AsyncTask that deletes the contact in another 
               // thread, then calls finish after the deletion
               AsyncTask<Long, Object, Object> deleteTask =
                  new AsyncTask<Long, Object, Object>()
                  {
                     @Override
                     protected Object doInBackground(Long... params)
                     {
                        databaseConnector.deleteContact(params[0]); 
                        return null;
                     } // end method doInBackground

                     @Override
                     protected void onPostExecute(Object result)
                     {
                        finish(); // return to the AddressBook Activity
                     } // end method onPostExecute
                  }; // end new AsyncTask

               // execute the AsyncTask to delete contact at rowID
               deleteTask.execute(new Long[] { rowID });               
            } // end method onClick
         } // end anonymous inner class
      ); // end call to method setPositiveButton
      
      builder.setNegativeButton(R.string.button_cancel, null);
      builder.show(); // display the Dialog
   } // end method deleteContact
  
   // called when an Activity launched from this one using 
   // startActivityForResult finishes
   public void onActivityResult(int requestCode, int resultCode, 
      Intent data) 
   {
      // if the connection was established
      if (resultCode == Activity.RESULT_OK) 
      {
         // get the remote device's MAC address and pass it to 
         // SendContactTask's execute method
         new SendContactTask().execute(new String[] { 
            data.getExtras().getString(DeviceChooser.DEVICE_ADDRESS)});
      } // end if
      else // there was a connection error
      {
     	   // display connection error Toast
         Toast.makeText(this, 
            R.string.connection_error, Toast.LENGTH_LONG);
      } // end else
   } // end method onActivityResult
   
   // Task for sending a contact in a background thread
   private class SendContactTask extends AsyncTask<String, Object, Object>
   {
      // get the BluetoothDevice for the specified address, 
      // connect to the device and send the contact
      @Override
      protected Object doInBackground(String... params)
      {
         // get a BluetoothDevice object representing the remote device
         BluetoothDevice device =
            bluetoothAdapter.getRemoteDevice(params[0]);
            
         BluetoothSocket bluetoothSocket = null; // for sending contact
         
         // make connection to remote device and send contact
         try 
         {
            AddressBook.displayToastViaHandler(ViewContact.this, handler, 
               R.string.sending_contact);

            // get BluetoothSocket, then connect to the other device
            bluetoothSocket = device.createRfcommSocketToServiceRecord(
               AddressBook.MY_UUID);
            bluetoothSocket.connect(); // establish connection
            
            // get streams for communicating via BluetoothSocket
            OutputStream outputStream = bluetoothSocket.getOutputStream();

            // create JSONObject representing the contact
            final JSONObject contact = new JSONObject();
            contact.put("name", nameTextView.getText().toString());
            contact.put("phone", phoneTextView.getText().toString());
            contact.put("email", emailTextView.getText().toString());
            contact.put("street", streetTextView.getText().toString());
            contact.put("city", cityTextView.getText().toString());
            
            // send a byte array containing the contact's information
            outputStream.write(contact.toString().getBytes()); 
            outputStream.flush();
            AddressBook.displayToastViaHandler(ViewContact.this, handler, 
               R.string.contact_sent);
         } // end try
         catch (IOException e) // problem sending contact
         {
            AddressBook.displayToastViaHandler(ViewContact.this, handler, 
               R.string.transfer_failed);
            Log.e(TAG, e.toString());
         } // end catch
         catch (JSONException e) // problem with JSON data formatting
         {
            AddressBook.displayToastViaHandler(ViewContact.this, handler, 
               R.string.transfer_failed);
            Log.e(TAG, e.toString());
         } // end catch
         finally // ensure that BluetoothSocket is closed
         {
            try
            {
               bluetoothSocket.close(); // closes BluetoothSocket
            } // end try
            catch (IOException e) // problem closing BluetoothSocket
            {
               Log.e(TAG, e.toString());
            } // end catch
   
            bluetoothSocket = null;
         } // end finally

         return null;
      } // end method doInBackground
   } // end class SendContactTask 
} // end class ViewContact


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
