// FiveDayForecastFragment.java
// Displays the five day forecast for a single city.
package com.deitel.weatherviewer;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deitel.weatherviewer.ReadFiveDayForecastTask.FiveDayForecastLoadedListener;
import com.deitel.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class FiveDayForecastFragment extends ForecastFragment 
{
   // used to retrieve zipcode from saved Bundle
   private static final String ZIP_CODE_KEY = "id_key";
   private static final int NUMBER_DAILY_FORECASTS = 5;
   
   private String zipcodeString; // zipcode for this forecast
   private View[] dailyForecastViews = new View[NUMBER_DAILY_FORECASTS];
   
   private TextView locationTextView;
   
   // creates a new FiveDayForecastFragment for the given zipcode
   public static FiveDayForecastFragment newInstance(String zipcodeString)
   {
      // create new ForecastFragment
      FiveDayForecastFragment newFiveDayForecastFragment = 
         new FiveDayForecastFragment();
         
      Bundle argumentsBundle = new Bundle(); // create a new Bundle
         
      // save the given String in the Bundle
      argumentsBundle.putString(ZIP_CODE_KEY, zipcodeString);
         
      // set the Fragement's arguments
      newFiveDayForecastFragment.setArguments(argumentsBundle);
      return newFiveDayForecastFragment; // return the completed Fragment
   } // end method newInstance
   
   // create a FiveDayForecastFragment using the given Bundle
   public static FiveDayForecastFragment newInstance(
      Bundle argumentsBundle) 
   {
      // get the zipcode from the given Bundle
      String zipcodeString = argumentsBundle.getString(ZIP_CODE_KEY);
      return newInstance(zipcodeString); // create new Fragment
   } // end method newInstance
   
   // create the Fragment from the saved state Bundle
   @Override 
   public void onCreate(Bundle argumentsBundle) 
   {
      super.onCreate(argumentsBundle);
      
      // get the zipcode from the given Bundle
      this.zipcodeString = getArguments().getString(ZIP_CODE_KEY);
   } // end method onCreate
   
   // public access for the zipcode of this Fragments forecast information
   public String getZipcode() 
   {
      return zipcodeString; // return the zipcode String
   } // end method getZipcode
   
   // inflates this Fragement's layout from xml
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
      Bundle savedInstanceState) 
   {
      // inflate the five day forecast layout
      View rootView = inflater.inflate(R.layout.five_day_forecast_layout, 
         null);
      // get the TextView to display location information
      locationTextView = (TextView) rootView.findViewById(R.id.location);
    
      // get the ViewGroup to contain the daily forecast layouts
      LinearLayout containerLinearLayout = 
         (LinearLayout) rootView.findViewById(R.id.containerLinearLayout);
     
      int id; // int identifier for the daily forecast layout
    
      // if we are in landscape orientation
      if (container.getContext().getResources().getConfiguration().
         orientation == Configuration.ORIENTATION_LANDSCAPE)
      {
         id = R.layout.single_forecast_layout_landscape;
      } // end if
      else // portrait orientation
      {
         id = R.layout.single_forecast_layout_portrait; 
         containerLinearLayout.setOrientation(LinearLayout.VERTICAL);
      } // end else
    
      // load five daily forecasts
      View forecastView;
      for (int i = 0; i < NUMBER_DAILY_FORECASTS; i++)
      {
         forecastView = inflater.inflate(id, null); // inflate new View
        
         // add the new View to the container LinearLayout
         containerLinearLayout.addView(forecastView);
         dailyForecastViews[i] = forecastView; 
      } // end for
     
      // load the location information in a background thread
      new ReadLocationTask(zipcodeString, rootView.getContext(),
         new WeatherLocationLoadedListener(zipcodeString, 
         rootView.getContext())).execute();
     
      return rootView;
   } // end method onCreateView
   
   // receives location information from background task
   private class WeatherLocationLoadedListener implements 
      LocationLoadedListener
   {
      private String zipcodeString; // zipcode to look up
      private Context context;
      
      // create a new WeatherLocationLoadedListener
      public WeatherLocationLoadedListener(String zipcodeString, 
         Context context)
      {
         this.zipcodeString = zipcodeString;
         this.context = context;
      } // end WeatherLocationLoadedListener

      // called when the location information is loaded
      @Override
      public void onLocationLoaded(String cityString, String stateString,
         String countryString) 
      {
         if (cityString == null) // if there is no returned data
         {
        	// display error message
            Toast errorToast = Toast.makeText(context, 
               context.getResources().getString(R.string.null_data_toast), 
               Toast.LENGTH_LONG);
            errorToast.setGravity(Gravity.CENTER, 0, 0);
            errorToast.show(); // show the Toast
            return; // exit before updating the forecast
         } // end if
         
         // display the return information in a TextView
         locationTextView.setText(cityString + " " + stateString + ", " +
            zipcodeString + " " + countryString);
         
         // load the forecast in a background thread
         new ReadFiveDayForecastTask(zipcodeString, 
            weatherForecastListener, 
            locationTextView.getContext()).execute();
      } // end method onLocationLoaded
   } // end class WeatherLocationLoadedListener
   
   // receives weather information from AsyncTask
   FiveDayForecastLoadedListener weatherForecastListener = 
      new FiveDayForecastLoadedListener() 
   {
      // when the background task looking up location information finishes
      @Override
      public void onForecastLoaded(DailyForecast[] forecasts) 
      {
         // display five daily forecasts
         for (int i = 0; i < NUMBER_DAILY_FORECASTS; i++)
         {
            // display the forecast information 
            loadForecastIntoView(dailyForecastViews[i], forecasts[i]);    
         } // end for
      } // end method onForecastLoaded
   }; // end FiveDayForecastLoadedListener
   
   // display the given forecast information in the given View
   private void loadForecastIntoView(View view, 
      DailyForecast dailyForecast)
   {
      // if this Fragment was detached while the background process ran
      if (!FiveDayForecastFragment.this.isAdded()) 
      {
         return; // leave the method
      } // end if 
      // if there is no returned data
      else if (dailyForecast == null || 
         dailyForecast.getIconBitmap() == null) 
      {
         // display error message
         Toast errorToast = Toast.makeText(view.getContext(), 
            view.getContext().getResources().getString(
            R.string.null_data_toast), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0);
         errorToast.show(); // show the Toast
         return; // exit before updating the forecast
      } // end else if
       
      // get all the child Views
      ImageView forecastImageView = (ImageView) view.findViewById(
         R.id.daily_forecast_bitmap);
      TextView dayOfWeekTextView = (TextView) view.findViewById(
         R.id.day_of_week);
      TextView descriptionTextView = (TextView) view.findViewById(
         R.id.daily_forecast_description);
      TextView highTemperatureTextView = (TextView) view.findViewById(
         R.id.high_temperature);
      TextView lowTemperatureTextView = (TextView) view.findViewById(
         R.id.low_temperature);
      
      // display the forecast information in the retrieved Views
      forecastImageView.setImageBitmap(dailyForecast.getIconBitmap());
      dayOfWeekTextView.setText(dailyForecast.getDay());
      descriptionTextView.setText(dailyForecast.getDescription());
      highTemperatureTextView.setText(dailyForecast.getHighTemperature());
      lowTemperatureTextView.setText(dailyForecast.getLowTemperature());
   } // end method loadForecastIntoView
} // end class FiveDayForecastFragment
