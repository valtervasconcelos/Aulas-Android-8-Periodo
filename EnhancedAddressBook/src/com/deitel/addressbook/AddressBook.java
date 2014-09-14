// AddressBook.java
// Main activity for the Address Book app.
package com.deitel.addressbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class AddressBook extends ListActivity 
{
   private static String TAG = AddressBook.class.getName();
   
   // unique app UUID generated at http://www.guidgenerator.com/
   public static final UUID MY_UUID = 
      UUID.fromString("6acc0a73-afc3-4483-a3a8-94be2c0dfc52");
   
   // name of this service for service discovery
   private static final String NAME = "AddressBookBluetooth";

   // constants passed to startActivityForResult 
   private static final int ENABLE_BLUETOOTH = 1; 
   private static final int REQUEST_DISCOVERABILITY = 2; 

   // BluetoothAdapter provides access to Bluetooth capabilities
   private BluetoothAdapter bluetoothAdapter = null; 
   private boolean userAllowedBluetooth = true;
   private Handler handler; // for displaying Toasts from non-GUI threads
   
   public static final String ROW_ID = "row_id"; // Intent extra key
   private ListView contactListView; // the ListActivity's ListView
   private CursorAdapter contactAdapter; // adapter for ListView
      
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState); // call super's onCreate
      contactListView = getListView(); // get the built-in ListView
      contactListView.setOnItemClickListener(viewContactListener);      

      // map each contact's name to a TextView in the ListView layout
      String[] from = new String[] { "name" };
      int[] to = new int[] { R.id.contactTextView };
      contactAdapter = new SimpleCursorAdapter(
         AddressBook.this, R.layout.contact_list_item, null, from, to);
      setListAdapter(contactAdapter); // set contactView's adapter
      
      // get the default Bluetooth adapter
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      
      handler = new Handler(); // for displaying Toasts in GUI thread
   } // end method onCreate
   
   // called when this Activity returns from the background
   @Override
   protected void onResume() 
   {
      super.onResume(); // call super's onResume method

      // request that Bluetooth be enabled if it isn't already
      if (!bluetoothAdapter.isEnabled() && userAllowedBluetooth) 
      {
         // create and start Intent to ask user to enable Bluetooth
         Intent enableBluetoothIntent = new Intent(
            BluetoothAdapter.ACTION_REQUEST_ENABLE);
         startActivityForResult(enableBluetoothIntent, 
            ENABLE_BLUETOOTH);
      } // end if
      
      // create new GetContactsTask and execute it 
      new GetContactsTask().execute((Object[]) null);
   } // end method onResume
   
   // when this Activity is stopped, deactivate Cursor for ListView
   @Override
   protected void onStop() 
   {
      Cursor cursor = contactAdapter.getCursor(); // get current Cursor
      
      if (cursor != null) 
         cursor.deactivate(); // deactivate it
      
      contactAdapter.changeCursor(null); // adapted now has no Cursor
      super.onStop();
   } // end method onStop

   // performs database query outside GUI thread
   private class GetContactsTask extends AsyncTask<Object, Object, Cursor> 
   {
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(AddressBook.this);

      // perform the database access
      @Override
      protected Cursor doInBackground(Object... params)
      {
         databaseConnector.open();

         // get a cursor containing call contacts
         return databaseConnector.getAllContacts(); 
      } // end method doInBackground

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result)
      {
         contactAdapter.changeCursor(result); // set the adapter's Cursor
         databaseConnector.close();
      } // end method onPostExecute
   } // end class GetContactsTask
      
   // create the Activity's menu from a menu resource XML file
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.addressbook_menu, menu);
      return true;
   } // end method onCreateOptionsMenu
   
   // handle choice from options menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      switch (item.getItemId())
      {
         case R.id.addContactItem: 
            // create a new Intent to launch the AddEditContact Activity
            Intent addNewContact = 
               new Intent(AddressBook.this, AddEditContact.class);
            startActivity(addNewContact); // start AddEditContact Activity
            break;
         case R.id.receiveContactItem:
            if (bluetoothAdapter.isEnabled())
            {
               // launch Intent to request discoverability for 120 seconds
               Intent requestDiscoverabilityIntent = new Intent(
                  BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
               startActivityForResult(requestDiscoverabilityIntent, 
                  REQUEST_DISCOVERABILITY);
            } // end if
            else // user did not allow Bluetooth adapter to be enabled
            {
               Toast.makeText(this, 
                  R.string.no_bluetooth,
                  Toast.LENGTH_LONG).show();
            } // end else
            break;
      } // end switch
      
      return super.onOptionsItemSelected(item); // call super's method
   } // end method onOptionsItemSelected
   
   // event listener that responds to the user 
   // touching a contact's name in the ListView
   OnItemClickListener viewContactListener = new OnItemClickListener() 
   {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int id,
         long position) 
      {
         // create an Intent to launch the ViewContact Activity
         Intent viewContact = 
            new Intent(AddressBook.this, ViewContact.class);
         
         // pass the selected contact's row ID as an extra with the Intent
         viewContact.putExtra(ROW_ID, position);
         startActivity(viewContact); // start the ViewContact Activity
      } // end method onItemClick
   }; // end viewContactListener
   
   // called with result of startActivityForResult
   @Override
   protected void onActivityResult(int requestCode, int resultCode,
      Intent data)
   {
      super.onActivityResult(requestCode, resultCode, data);

      switch (requestCode) // process result based on the requestCode
      {
         case ENABLE_BLUETOOTH: // attempted to enable Bluetooth
            if (resultCode == RESULT_OK) // bluetooth was enabled
            {
               Toast.makeText(this, 
                  R.string.bluetooth_enabled,
                  Toast.LENGTH_LONG).show();               
            } // end if 
            else // Bluetooth was not enabled
            {
               userAllowedBluetooth = false;
               Toast.makeText(this, R.string.no_bluetooth,
                  Toast.LENGTH_LONG).show();               
            } // end else
            break;
         // attempted to make the device discoverable 
         case REQUEST_DISCOVERABILITY: 
            if (resultCode != RESULT_CANCELED) // user gave permission
            {
               listenForContact(); // start listening for a connection
            } // end if
            else // user did not allow discoverability
            {
               Toast.makeText(this, 
                  R.string.no_discoverability,
                  Toast.LENGTH_LONG).show();               
            } // end else
            break;
      } // end switch
   } // end method onActivityResult

   // start listening for a contact sent from another device
   private void listenForContact()
   {
      // start background task to wait for connection 
      // and receive a contact
      ReceiveContactTask task = new ReceiveContactTask();
      task.execute((Object[]) null);
   } // end method listenForContact
   
   // thread that listens for incoming connection requests
   private class ReceiveContactTask 
      extends AsyncTask<Object, Object, Object> 
   {
      private BluetoothServerSocket serverSocket; // awaits connection
      private BluetoothSocket socket; // used to process connection
      
      // await connection, receive contact and update contacts list
      @Override
      protected Object doInBackground(Object... params)
      {
         try
         {
            // get BluetoothServerSocket from bluetoothAdapter
            serverSocket = 
               bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                  NAME, MY_UUID);

            displayToastViaHandler(AddressBook.this, handler, 
               R.string.waiting_for_contact);

            // wait for connection
            BluetoothSocket socket = serverSocket.accept(); 
            
            // get InputStream for receiving contact
            InputStream inputStream = socket.getInputStream();
            
            // create a byte array to hold incoming contact information
            byte[] buffer = new byte[1024];
            int bytes; // number of bytes read
   
            // read from the InputStream and store data in buffer
            bytes = inputStream.read(buffer);
            
            if (bytes != -1) // a contact was received
            {
               DatabaseConnector databaseConnector = null;
               
               // convert readMessage to JSONObject
               try
               {
                  // create JSONObject from read bytes
                  JSONObject contact = 
                     new JSONObject(new String(buffer, 0, buffer.length));
                  
                  // create new DatabaseConnector
                  databaseConnector = 
                     new DatabaseConnector(getBaseContext());
         
                  // open the database and add the contact to the database
                  databaseConnector.open(); // connect to the database
                  
                  databaseConnector.insertContact( // add the contact
                     contact.getString("name"), 
                     contact.getString("email"), 
                     contact.getString("phone"), 
                     contact.getString("street"), 
                     contact.getString("city"));
      
                  // update the contacts list
                  new GetContactsTask().execute((Object[]) null);
                  displayToastViaHandler(AddressBook.this, handler, 
                     R.string.contact_received);
               } // end try
               catch (JSONException e) // problem with the JSON formatting
               {        
                  displayToastViaHandler(AddressBook.this, handler, 
                     R.string.contact_not_received);
                  Log.e(TAG, e.toString());
               } // end catch
               finally // ensure that the database connection is closed
               {
                  if (databaseConnector != null)
                     databaseConnector.close(); // close connection
               } // end finally
            } // end if
         } // end try
         catch (IOException e) 
         {            
            Log.e(TAG, e.toString());
         } // end catch
         finally // ensure BluetoothServerSocket & BluetoothSocket closed
         {
            try 
            {
               // if the BluetoothServerSocket is not null, close it
               if (serverSocket != null)
                  serverSocket.close();
               
               // if the BluetoothSocket is not null, close it
               if (socket != null)
                  socket.close();
            } // end try
            catch (IOException e) // problem closing a socket
            {
               Log.e(TAG, e.toString());
            } // end catch
         } // end finally
         
         return null;
      } // end method doInBackround
   } // end nested class ReceiveContactTask 
   
   // use handler to display a Toast in GUI thread with specified message
   public static void displayToastViaHandler(final Context context, 
      Handler handler, final int stringID)
   {
      handler.post(
         new Runnable()
         {
            public void run()
            {
               Toast.makeText(context, stringID,
                  Toast.LENGTH_SHORT).show();
            } // end method run
         } // end Runnable
      ); // end call to handler's post method
   } // end method displayToastViaHandler
} // end class AddressBook


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
