// ReadForecastTask.java
// Reads weather information off the main thread.
package com.deitel.weatherviewer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

class ReadForecastTask extends AsyncTask<Object, Object, String> 
{
   private String zipcodeString; // the zipcode of the forecast's city
   private Resources resources;
   
   // receives weather information
   private ForecastListener weatherForecastListener; 
   private static final String TAG = "ReadForecastTask.java";
   
   private String temperatureString; // the temperature
   private String feelsLikeString; // the "feels like" temperature
   private String humidityString; // the humidity
   private String chanceOfPrecipitationString; // chance of precipitation
   private Bitmap iconBitmap; // image of the sky condition
   
   private int bitmapSampleSize = -1;
     
   // interface for receiver of weather information
   public interface ForecastListener 
   {
      public void onForecastLoaded(Bitmap image, String temperature, 
         String feelsLike, String humidity, String precipitation);
   } // end interface ForecastListener

   // creates a new ReadForecastTask
   public ReadForecastTask(String zipcodeString, 
      ForecastListener listener, Context context)
   {
      this.zipcodeString = zipcodeString;
      this.weatherForecastListener = listener;
      this.resources = context.getResources();
   } // end constructor ReadForecastTask
   
   // set the sample size for the forecast's Bitmap
   public void setSampleSize(int sampleSize)
   {
      this.bitmapSampleSize = sampleSize;
   } // end method setSampleSize
     
   // load the forecast in a background thread
   protected String doInBackground(Object... args) 
   {
      try 
      {
         // the url for the WeatherBug JSON service
         URL webServiceURL = new URL(resources.getString(
            R.string.pre_zipcode_url) + zipcodeString  + "&ht=t&ht=i&"
            + "ht=cp&ht=fl&ht=h&api_key=YOUR_API_KEY");
         
         // create a stream Reader from the WeatherBug url
         Reader forecastReader = new InputStreamReader(
            webServiceURL.openStream());
          
         // create a JsonReader from the Reader
         JsonReader forecastJsonReader = new JsonReader(forecastReader);
          
         forecastJsonReader.beginObject(); // read the first Object

         // get the next name
         String name = forecastJsonReader.nextName();
         
         // if its the name expected for hourly forecast information
         if (name.equals(resources.getString(R.string.hourly_forecast))) 
         {
            readForecast(forecastJsonReader); // read the forecast
         } // end if
           
         forecastJsonReader.close(); // close the JsonReader
      } // end try
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString());
      } // end catch
      catch (IOException e) 
      {
         Log.v(TAG, e.toString());
      } // end catch
      catch (IllegalStateException e)
      {
        Log.v(TAG, e.toString() + zipcodeString);
      } // end catch
      return null;  
   } // end method doInBackground

   // update the UI back on the main thread
   protected void onPostExecute(String forecastString) 
   {
      // pass the information to the ForecastListener
      weatherForecastListener.onForecastLoaded(iconBitmap, 
         temperatureString, feelsLikeString, humidityString, 
         chanceOfPrecipitationString);
   } // end method onPostExecute
    
   // get the sky condition image Bitmap
   public static Bitmap getIconBitmap(String conditionString, 
      Resources resources, int bitmapSampleSize) 
   {
      Bitmap iconBitmap = null; // create the Bitmap
      try 
      {
         // create a URL pointing to the image on WeatherBug's site
         URL weatherURL = new URL(resources.getString(
            R.string.pre_condition_url) + conditionString + 
            resources.getString(R.string.post_condition_url));
         
         BitmapFactory.Options options = new BitmapFactory.Options();
         if (bitmapSampleSize != -1) 
         { 
            options.inSampleSize = bitmapSampleSize;
         } // end if
         
         // save the image as a Bitmap 
         iconBitmap = BitmapFactory.decodeStream(weatherURL.
            openStream(), null, options);
      } // end try
      catch (MalformedURLException e) 
      {
         Log.e(TAG, e.toString());
      } // end catch
      catch (IOException e) 
      {
         Log.e(TAG, e.toString());
      } // end catch
      
      return iconBitmap; // return the image
   } // end method getIconBitmap
    
   // read the forecast information using the given JsonReader
   private String readForecast(JsonReader reader)
   {
      try 
      {
         reader.beginArray(); // start reading the next array
         reader.beginObject(); // start reading the next object

         // while there is a next element in the current object
         while (reader.hasNext()) 
         {
            String name = reader.nextName(); // read the next name
            
            // if this element is the temperature
            if (name.equals(resources.getString(R.string.temperature)))
            {
              // read the temperature
               temperatureString = reader.nextString(); 
            } // end if
            // if this element is the "feels-like" temperature
            else if (name.equals(resources.getString(R.string.feels_like)))  
            {
              // read the "feels-like" temperature
               feelsLikeString = reader.nextString(); 
            } // end else if
            // if this element is the humidity
            else if (name.equals(resources.getString(R.string.humidity))) 
            {
               humidityString = reader.nextString(); // read the humidity
            } // end else if 
            // if this next element is the chance of precipitation
            else if (name.equals(resources.getString(
               R.string.chance_of_precipitation))) 
            {
               // read the chance of precipitation  
               chanceOfPrecipitationString = reader.nextString();
            } // end else if
            // if the next item is the icon name
            else if (name.equals(resources.getString(R.string.icon))) 
            {
               // read the icon name
                iconBitmap = getIconBitmap(reader.nextString(), resources, 
                   bitmapSampleSize);
            } // end else if
            else // there is an unexpected element
            {
               reader.skipValue(); // skip the next element
            } // end else
         } // end while
      } // end try
      catch (IOException e) 
      {
         Log.e(TAG, e.toString());
      } // end catch
      return null;
   } // end method readForecast
} // end ReadForecastTask
