// ReadLocationTask.java
// Reads location information in a background thread.
package com.deitel.weatherviewer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

//converts zipcode to city name in a background thread
class ReadLocationTask extends AsyncTask<Object, Object, String>
{
   private static final String TAG = "ReadLocatonTask.java";   
   
   private String zipcodeString; // the zipcode for the location 
   private Context context; // launching Activity's Context
   private Resources resources; // used to look up String from xml
   
   // Strings for each type of data retrieved
   private String cityString;
   private String stateString;
   private String countryString;
   
   // listener for retrieved information
   private LocationLoadedListener weatherLocationLoadedListener; 
   
   // interface for receiver of location information
   public interface LocationLoadedListener 
   {
      public void onLocationLoaded(String cityString, String stateString, 
         String countryString);
   } // end interface LocationLoadedListener
   
   // public constructor
   public ReadLocationTask(String zipCodeString, Context context, 
      LocationLoadedListener listener)
   {
      this.zipcodeString = zipCodeString;
      this.context = context;
      this.resources = context.getResources();
      this.weatherLocationLoadedListener = listener;
   } // end constructor ReadLocationTask
   
   // load city name in background thread
   @Override
   protected String doInBackground(Object... params) 
   {
      try 
      {
         // construct Weatherbug API URL
         URL url = new URL(resources.getString(
            R.string.location_url_pre_zipcode) + zipcodeString + 
            "&api_key=YOUR_API_KEY");
         
         // create an InputStreamReader using the URL
         Reader forecastReader = new InputStreamReader(
            url.openStream());
         
         // create a JsonReader from the Reader
         JsonReader forecastJsonReader = new JsonReader(forecastReader);
         forecastJsonReader.beginObject(); // read the first Object
         
         // get the next name
         String name = forecastJsonReader.nextName();

         // if the name indicates that the next item describes the 
         // zipcode's location
         if (name.equals(resources.getString(R.string.location)))
         {
            // start reading the next JSON Object
            forecastJsonReader.beginObject();  
            
            String nextNameString;
            
            // while there is more information to be read
            while (forecastJsonReader.hasNext()) 
            {
               nextNameString = forecastJsonReader.nextName();
               // if the name indicates that the next item describes the 
               // zipcode's corresponding city name
               if ((nextNameString).equals(
                  resources.getString(R.string.city))) 
               {
                  // read the city name
                  cityString = forecastJsonReader.nextString();
               } // end if
               else if ((nextNameString).equals(resources.
                  getString(R.string.state))) 
               {
                  stateString = forecastJsonReader.nextString();
               } // end else if
               else if ((nextNameString).equals(resources.
                  getString(R.string.country))) 
               {
                  countryString = forecastJsonReader.nextString();
               } // end else if
               else 
               {
                  forecastJsonReader.skipValue(); // skip unexpected value
               } // end else
            } // end while
            
            forecastJsonReader.close(); // close the JsonReader
         } // end if
      } // end try
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString()); // print the exception to the LogCat
      } // end catch
      catch (IOException e) 
      {
         Log.v(TAG, e.toString()); // print the exception to the LogCat
      } // end catch
     
      return null; // return null if the city name couldn't be found
   } // end method doInBackground

   // executed back on the UI thread after the city name loads
   protected void onPostExecute(String nameString)
   {
      // if a city was found to match the given zipcode
      if (cityString != null)
      {
         // pass the information back to the LocationLoadedListener
         weatherLocationLoadedListener.onLocationLoaded(cityString, 
            stateString, countryString);
      } // end if
      else 
      {
         // display Toast informing that location information 
         // couldn't be found
         Toast errorToast = Toast.makeText(context, resources.getString(
            R.string.invalid_zipcode_error), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0); // center the Toast
         errorToast.show(); // show the Toast
      } // end else
   } // end method onPostExecute
} // end class ReadLocationTask