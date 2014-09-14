// SingleForecastFragment.java
// Displays forecast information for a single city.
package com.deitel.weatherviewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deitel.weatherviewer.ReadForecastTask.ForecastListener;
import com.deitel.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class SingleForecastFragment extends ForecastFragment 
{
   private String zipcodeString; // zipcode for this forecast
   
   // lookup keys for the Fragment's saved state
   private static final String LOCATION_KEY = "location";
   private static final String TEMPERATURE_KEY = "temperature";
   private static final String FEELS_LIKE_KEY = "feels_like";
   private static final String HUMIDITY_KEY = "humidity";
   private static final String PRECIPITATION_KEY = "chance_precipitation";
   private static final String IMAGE_KEY = "image";
   
   // used to retrieve zipcode from saved Bundle
   private static final String ZIP_CODE_KEY = "id_key";
   
   private View forecastView; // contains all forecast Views
   private TextView temperatureTextView; // displays actual temperature
   private TextView feelsLikeTextView; // displays "feels like" temperature
   private TextView humidityTextView; // displays humidity
   
   private TextView locationTextView;
   
   // displays the percentage chance of precipitation
   private TextView chanceOfPrecipitationTextView; 
   private ImageView conditionImageView; // image of current sky condition
   private TextView loadingTextView;
   private Context context;
   private Bitmap conditionBitmap;
   
   // creates a new ForecastFragment for the given zipcode
   public static SingleForecastFragment newInstance(String zipcodeString)
   { 
      // create new ForecastFragment
      SingleForecastFragment newForecastFragment = 
         new SingleForecastFragment();
      
      Bundle argumentsBundle = new Bundle(); // create a new Bundle
      
      // save the given String in the Bundle
      argumentsBundle.putString(ZIP_CODE_KEY, zipcodeString);
      
      // set the Fragement's arguments
      newForecastFragment.setArguments(argumentsBundle);
      return newForecastFragment; // return the completed ForecastFragment
   } // end method newInstance
   
   // create a ForecastFragment using the given Bundle
   public static SingleForecastFragment newInstance(Bundle argumentsBundle) 
   {
      // get the zipcode from the given Bundle
      String zipcodeString = argumentsBundle.getString(ZIP_CODE_KEY);
      return newInstance(zipcodeString); // create new ForecastFragment
   } // end method newInstance
   
   // create the Fragment from the saved state Bundle
   @Override 
   public void onCreate(Bundle argumentsBundle) 
   {
      super.onCreate(argumentsBundle);
      
      // get the zipcode from the given Bundle
      this.zipcodeString = getArguments().getString(ZIP_CODE_KEY);
   } // end method onCreate
   
   // save the Fragment's state
   @Override
   public void onSaveInstanceState(Bundle savedInstanceStateBundle) 
   {
      super.onSaveInstanceState(savedInstanceStateBundle);
      
      // store the View's contents into the Bundle
      savedInstanceStateBundle.putString(LOCATION_KEY, 
         locationTextView.getText().toString());
      savedInstanceStateBundle.putString(TEMPERATURE_KEY, 
         temperatureTextView.getText().toString());
      savedInstanceStateBundle.putString(FEELS_LIKE_KEY, 
         feelsLikeTextView.getText().toString());
      savedInstanceStateBundle.putString(HUMIDITY_KEY, 
         humidityTextView.getText().toString());
      savedInstanceStateBundle.putString(PRECIPITATION_KEY, 
         chanceOfPrecipitationTextView.getText().toString());
      savedInstanceStateBundle.putParcelable(IMAGE_KEY, conditionBitmap);
   } // end method onSaveInstanceState
   
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
      // use the given LayoutInflator to inflate layout stored in
      // forecast_fragment_layout.xml
      View rootView = inflater.inflate(R.layout.forecast_fragment_layout, 
         null);
      
      // get the TextView in the Fragment's layout hierarchy
      forecastView = rootView.findViewById(R.id.forecast_layout);
      loadingTextView = (TextView) rootView.findViewById(
         R.id.loading_message);
      locationTextView = (TextView) rootView.findViewById(R.id.location);
      temperatureTextView = (TextView) rootView.findViewById(
         R.id.temperature);
      feelsLikeTextView = (TextView) rootView.findViewById(
         R.id.feels_like);
      humidityTextView = (TextView) rootView.findViewById(
         R.id.humidity);
      chanceOfPrecipitationTextView = (TextView) rootView.findViewById(
         R.id.chance_of_precipitation);
      conditionImageView = (ImageView) rootView.findViewById(
         R.id.forecast_image);

      context = rootView.getContext(); // save the Context
      
      return rootView; // return the inflated View
   } // end method onCreateView
   
   // called when the parent Activity is created
   @Override
   public void onActivityCreated(Bundle savedInstanceStateBundle) 
   {
      super.onActivityCreated(savedInstanceStateBundle);
       
      // if there is no saved information
      if (savedInstanceStateBundle == null) 
      {
         // hide the forecast and show the loading message
         forecastView.setVisibility(View.GONE); 
         loadingTextView.setVisibility(View.VISIBLE); 
         
         // load the location information in a background thread
         new ReadLocationTask(zipcodeString, context,
            new WeatherLocationLoadedListener(zipcodeString)).execute();
      } // end if
      else 
      {
         // display information in the saved state Bundle using the 
         // Fragment's Views
         conditionImageView.setImageBitmap(
            (Bitmap) savedInstanceStateBundle.getParcelable(IMAGE_KEY));
         locationTextView.setText(savedInstanceStateBundle.getString(
            LOCATION_KEY));
         temperatureTextView.setText(savedInstanceStateBundle.getString(
            TEMPERATURE_KEY));
         feelsLikeTextView.setText(savedInstanceStateBundle.getString(
            FEELS_LIKE_KEY));
         humidityTextView.setText(savedInstanceStateBundle.getString(
            HUMIDITY_KEY));
         chanceOfPrecipitationTextView.setText(
            savedInstanceStateBundle.getString(PRECIPITATION_KEY));
      } // end else
   } // end method onActivityCreated
   
   // receives weather information from AsyncTask
   ForecastListener weatherForecastListener = new ForecastListener() 
   {
      // displays the forecast information
      @Override
      public void onForecastLoaded(Bitmap imageBitmap, 
         String temperatureString, String feelsLikeString, 
         String humidityString, String precipitationString) 
      {
         // if this Fragment was detached while the background process ran
         if (!SingleForecastFragment.this.isAdded()) 
         {
            return; // leave the method
         } // end if
         else if (imageBitmap == null) 
         {
            Toast errorToast = Toast.makeText(context, 
               context.getResources().getString(
               R.string.null_data_toast), Toast.LENGTH_LONG);
            errorToast.setGravity(Gravity.CENTER, 0, 0);
            errorToast.show(); // show the Toast
            return; // exit before updating the forecast
         } // end if
        
         Resources resources = SingleForecastFragment.this.getResources();
         
         // display the loaded information
         conditionImageView.setImageBitmap(imageBitmap);
         conditionBitmap = imageBitmap;
         temperatureTextView.setText(temperatureString + (char)0x00B0 + 
            resources.getString(R.string.temperature_unit));
         feelsLikeTextView.setText(feelsLikeString + (char)0x00B0 + 
            resources.getString(R.string.temperature_unit));
         humidityTextView.setText(humidityString + (char)0x0025);
         chanceOfPrecipitationTextView.setText(precipitationString +
            (char)0x0025);
         loadingTextView.setVisibility(View.GONE); // hide loading message
         forecastView.setVisibility(View.VISIBLE); // show the forecast
      } // end method onForecastLoaded
   }; // end weatherForecastListener
   
   // receives location information from background task
   private class WeatherLocationLoadedListener implements 
      LocationLoadedListener
   {
      private String zipcodeString; // zipcode to look up
     
      // create a new WeatherLocationLoadedListener
      public WeatherLocationLoadedListener(String zipcodeString)
      {
         this.zipcodeString = zipcodeString;
      } // end WeatherLocationLoadedListener

      // called when the location information is loaded
      @Override
      public void onLocationLoaded(String cityString, String stateString,
         String countryString) 
      {
         if (cityString == null) // if there is no returned data
         {
        	// display the error message
            Toast errorToast = Toast.makeText(
               context, context.getResources().getString(
               R.string.null_data_toast), Toast.LENGTH_LONG);
            errorToast.setGravity(Gravity.CENTER, 0, 0); 
            errorToast.show(); // show the Toast
            return; // exit before updating the forecast
         } // end if
         // display the return information in a TextView
         locationTextView.setText(cityString + " " + stateString + ", " +
            zipcodeString + " " + countryString);
         // load the forecast in a background thread
         new ReadForecastTask(zipcodeString, weatherForecastListener, 
            locationTextView.getContext()).execute(); 
      } // end method onLocationLoaded
   } // end class LocationLoadedListener
} // end class SingleForecastFragment