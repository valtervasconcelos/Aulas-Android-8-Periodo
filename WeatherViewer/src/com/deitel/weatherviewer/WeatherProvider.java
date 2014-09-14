// WeatherProvider.java 
// Updates the Weather app widget
package com.deitel.weatherviewer;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.deitel.weatherviewer.ReadForecastTask.ForecastListener;
import com.deitel.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class WeatherProvider extends AppWidgetProvider 
{
   // sample size for the forecast image Bitmap   
   private static final int BITMAP_SAMPLE_SIZE = 4;   
   
   // updates all installed Weather App Widgets
   @Override
   public void onUpdate(Context context, 
      AppWidgetManager appWidgetManager, int[] appWidgetIds) 
   {
      startUpdateService(context); // start new WeatherService
   } // end method onUpdate
   
   // gets the saved zipcode for this app widget
   private String getZipcode(Context context)
   {
      // get the app's SharedPreferences
      SharedPreferences preferredCitySharedPreferences = 
         context.getSharedPreferences(
         WeatherViewerActivity.SHARED_PREFERENCES_NAME, 
         Context.MODE_PRIVATE);
      
      // get the zipcode of the preferred city from SharedPreferences
      String zipcodeString = preferredCitySharedPreferences.getString(
         WeatherViewerActivity.PREFERRED_CITY_ZIPCODE_KEY, 
         context.getResources().getString(R.string.default_zipcode));
      return zipcodeString; // return the zipcode string
   } // end method getZipcode
   
   // called when this AppWidgetProvider receives a broadcast Intent
   @Override
   public void onReceive(Context context, Intent intent)
   {
      // if the preferred city was changed in the app      
      if (intent.getAction().equals(
         WeatherViewerActivity.WIDGET_UPDATE_BROADCAST_ACTION))
      {
         startUpdateService(context); // display the new city's forecast
      } // end if
      super.onReceive(context, intent);
   } // end method onReceive
   
   // start new WeatherService to update app widget's forecast information
   private void startUpdateService(Context context)
   {
      // create a new Intent to start the WeatherService
      Intent startServiceIntent;
      startServiceIntent = new Intent(context, WeatherService.class);

      // include the zipcode as an Intent extra
      startServiceIntent.putExtra(context.getResources().getString(
         R.string.zipcode_extra), getZipcode(context));
      context.startService(startServiceIntent);
   } // end method startUpdateService
   
   // updates the Weather AppWidget
   public static class WeatherService extends IntentService implements 
      ForecastListener
   {
      public WeatherService() 
      {
         super(WeatherService.class.toString());
      } // end WeatherService constructor

      private Resources resources; // the app's Resources
      private String zipcodeString; // the preferred city's zipcode
      private String locationString; // the preferred city's location text

      @Override
      protected void onHandleIntent(Intent intent) 
      {
         resources = getApplicationContext().getResources();
        
         zipcodeString = intent.getStringExtra(resources.getString(
            R.string.zipcode_extra));
        
         // load the location information in a background thread
         new ReadLocationTask(zipcodeString, this,
            new WeatherServiceLocationLoadedListener(
            zipcodeString)).execute();
      } // end method onHandleIntent
      
      // receives weather information from the ReadForecastTask
      @Override
      public void onForecastLoaded(Bitmap image, String temperature,
         String feelsLike, String humidity, String precipitation) 
      {
         Context context = getApplicationContext();
         
         if (image == null) // if there is no returned data
         {
            Toast.makeText(context, context.getResources().getString(
               R.string.null_data_toast), Toast.LENGTH_LONG);
            return; // exit before updating the forecast
         } // end if
         
         // create PendingIntent to launch WeatherViewerActivity
         Intent intent = new Intent(context, WeatherViewerActivity.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(
            getBaseContext(), 0, intent, 0);
         
         // get the App Widget's RemoteViews
         RemoteViews remoteView = new RemoteViews(getPackageName(), 
            R.layout.weather_app_widget_layout);
         
         // set the PendingIntent to launch when the app widget is clicked
         remoteView.setOnClickPendingIntent(R.id.containerLinearLayout, 
            pendingIntent);

         // display the location information
         remoteView.setTextViewText(R.id.location, locationString);
         
         // display the temperature
         remoteView.setTextViewText(R.id.temperatureTextView, 
            temperature + (char)0x00B0 + resources.getString(
            R.string.temperature_unit));
         
         // display the "feels like" temperature
         remoteView.setTextViewText(R.id.feels_likeTextView, feelsLike + 
            (char)0x00B0 + resources.getString(R.string.temperature_unit));
         
         // display the humidity
         remoteView.setTextViewText(R.id.humidityTextView, humidity + 
            (char)0x0025);
         
         // display the chance of precipitation
         remoteView.setTextViewText(R.id.precipitationTextView, 
            precipitation + (char)0x0025);
         
         // display the forecast image
         remoteView.setImageViewBitmap(R.id.weatherImageView, image);

         // get the Component Name to identify the widget to update
         ComponentName widgetComponentName = new ComponentName(this, 
            WeatherProvider.class);
         
         // get the global AppWidgetManager
         AppWidgetManager manager = AppWidgetManager.getInstance(this);
         
         // update the Weather AppWdiget 
         manager.updateAppWidget(widgetComponentName, remoteView);
      } // end method onForecastLoaded
        
      // receives location information from background task
      private class WeatherServiceLocationLoadedListener implements 
         LocationLoadedListener
      {
         private String zipcodeString; // zipcode to look up
          
         // create a new WeatherLocationLoadedListener
         public WeatherServiceLocationLoadedListener(String zipcodeString)
         {
            this.zipcodeString = zipcodeString;
         } // end WeatherLocationLoadedListener

         // called when the location information is loaded
         @Override
         public void onLocationLoaded(String cityString, 
            String stateString, String countryString) 
         {
            Context context = getApplicationContext();
            
            if (cityString == null) // if there is no returned data
            {
               Toast.makeText(context, context.getResources().getString(
                  R.string.null_data_toast), Toast.LENGTH_LONG);
               return; // exit before updating the forecast
            } // end if
            // display the return information in a TextView
            locationString = cityString + " " + stateString + ", " +
               zipcodeString + " " + countryString;

            // launch a new ReadForecastTask
            ReadForecastTask readForecastTask = new ReadForecastTask(
               zipcodeString, (ForecastListener) WeatherService.this, 
               WeatherService.this);
             
            // limit the size of the Bitmap
            readForecastTask.setSampleSize(BITMAP_SAMPLE_SIZE);
            readForecastTask.execute();
         } // end method onLocationLoaded
      } // end class WeatherServiceLocationLoadedListener
   } // end class WeatherService
} // end WeatherProvider