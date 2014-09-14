// DeviceChooser.java
// Activity for choosing a connecting device.
package com.deitel.addressbook;

import java.util.Set;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class DeviceChooser extends ListActivity 
{
   // length of a MAC address in characters
   private static final int MAC_ADDRESS_LENGTH = 17;

   // key for storing selected device's MAC address as an Intent extra
   public static final String DEVICE_ADDRESS = "device_address";

   private BluetoothAdapter bluetoothAdapter; // the Bluetooth Adapter
   private ArrayAdapter<String> foundDevicesAdapter; // ListView data
   private ListView newDevicesListView; // ListView that shows devices

   // called when this Activity is first created
   @Override
   protected void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);

      // shows a progess bar while activity loads
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

      // set the Activity's layout
      setContentView(R.layout.device_chooser_layout);

      // set the result code to return to the previous Activity
      // if the user touches the 'Cancel' Button
      setResult(Activity.RESULT_CANCELED);

      // create Button to start the discovery
      Button scanButton = (Button) findViewById(R.id.scanButton);
      scanButton.setOnClickListener(
         new OnClickListener() 
         {
            // called when the scanButton is clicked
            public void onClick(View v) 
            {
               startDiscovery(); // begin searching for devices
            } // end method onClick
         } // end OnClickListener
      ); // end call to setOnClickListener
      
      // initialize the list adapter for list of found devices
      foundDevicesAdapter = 
         new ArrayAdapter<String>(this, R.layout.device_layout);

      // initialize the ListView that will display newly found devices
      newDevicesListView = getListView();
      newDevicesListView.setAdapter(foundDevicesAdapter);
      newDevicesListView.setOnItemClickListener(
         deviceListItemClickListener);

      // listen for broadcast Intents alerting that a Bluetooth device 
      // was found nearby
      IntentFilter filter = 
         new IntentFilter(BluetoothDevice.ACTION_FOUND);
      registerReceiver(deviceChooserReceiver, filter);

      // listen for broadcast Intents alerting that the search for
      // nearby devices is completed
      filter = 
         new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
      registerReceiver(deviceChooserReceiver, filter);

      // get the local BluetoothAdapter
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      
      // get a Set of all devices we are already connected to
      Set<BluetoothDevice> pairedDevices = 
         bluetoothAdapter.getBondedDevices();

      // add each connected device's name to our ListView
      for (BluetoothDevice device : pairedDevices) 
      {
         foundDevicesAdapter.add(device.getName() + "\n" + 
            device.getAddress());
      } // end for
   } // end method onCreate

   // called before this Activity is destroyed
   @Override
   protected void onDestroy() 
   {
      super.onDestroy();

      // end Bluetooth discovery
      if (bluetoothAdapter != null) 
      {
         bluetoothAdapter.cancelDiscovery();
      } // end if

      // unregister the deviceChooserReceiver BroadcastReceiver
      unregisterReceiver(deviceChooserReceiver);
   } // end method onDestroy

   // start the discovery
   private void startDiscovery() 
   {
	   // check if Bluetooth is still enabled
      if (!bluetoothAdapter.isEnabled()) 
      {
    	   Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_LONG);
    	   return;
      } // end if
	   
	   // end existing discovery if needed
      if (bluetoothAdapter.isDiscovering()) 
      {
         bluetoothAdapter.cancelDiscovery();
      } // end if

      // show the progress bar
      setProgressBarIndeterminateVisibility(true);
      
      // begin searching for other devices
      bluetoothAdapter.startDiscovery();
   } // end method startDiscovery

   // listens for events generated when the user clicks ListView item
   private OnItemClickListener deviceListItemClickListener = 
      new OnItemClickListener() 
      {
         public void onItemClick(AdapterView<?> parent, View view,  
            int position, long id) 
         {
            // cancel the discovery before attempting to connect
            bluetoothAdapter.cancelDiscovery();
   
            // get the MAC address of the selected device
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 
               MAC_ADDRESS_LENGTH);
   
            // create an Intent to return to the calling Activity
            Intent intent = new Intent();
            
            // include the device's MAC address in the return Intent
            intent.putExtra(DEVICE_ADDRESS, address);
   
            // set our Intent as the successful return value and finish
            setResult(Activity.RESULT_OK, intent);
            finish();
         } // end method onItemClick
      }; // end OnItemClickListener

   // listens for broadcast Intents announcing when a 
   // discovery finishes and when new devices are detected
   private final BroadcastReceiver deviceChooserReceiver = 
      new BroadcastReceiver() 
      {
         // called when a broadcast is received
         public void onReceive(Context context, Intent intent) 
         {
            // get the calling Intent's action
            String action = intent.getAction(); 
   
            // a new device was detected
            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
            {
               // get the BluetoothDevice from the broadcast Intent
               BluetoothDevice device = 
                  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
               
               // if the device is not already connected, add its name
               // to the ListView
               if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
               {
                  foundDevicesAdapter.add(device.getName() + "\n" + 
                     device.getAddress());
               } // end if
            } // end if
            // a search for new devices has ended 
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(
               action)) 
            {
               // hide the progress bar
               setProgressBarIndeterminateVisibility(false);
               
               // set the title of the Activity
               setTitle(getResources().getString(R.string.choose_device)); 
               
               // if there were no devices in range, display a message
               if (foundDevicesAdapter.getCount() == 0) 
               {
                  // disable list item clicks
                  newDevicesListView.setOnItemClickListener(null);
                  foundDevicesAdapter.add(getResources().getString(
                     R.string.no_devices));
               } // end if
            } // end else if
         } // end method onReceive
      }; // end BroadcastReceiver
} // end class DeviceChooser


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
