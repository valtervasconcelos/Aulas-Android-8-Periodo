// DailyForecase.java
// Represents a single day's forecast.
package com.deitel.weatherviewer;

import android.graphics.Bitmap;

public class DailyForecast 
{
   // indexes for all the forecast information
   public static final int DAY_INDEX = 0;
   public static final int PREDICTION_INDEX = 1;
   public static final int HIGH_TEMP_INDEX = 2;
   public static final int LOW_TEMP_INDEX = 3;
   
   final private String[] forecast; // array of all forecast information
   final private Bitmap iconBitmap; // image representation of forecast
   
   // create a new DailyForecast
   public DailyForecast(String[] forecast, Bitmap iconBitmap)
   {
      this.forecast = forecast;
      this.iconBitmap = iconBitmap;
   } // end DailyForecast constructor
   
   // get this forecast's image
   public Bitmap getIconBitmap()
   {
      return iconBitmap;
   } // end method getIconBitmap
   
   // get this forecast's day of the week
   public String getDay()
   {
      return forecast[DAY_INDEX];      
   } // end method getDay
   
   // get short description of this forecast
   public String getDescription()
   {
      return forecast[PREDICTION_INDEX];      
   } // end method getDescription
   
   // return this forecast's high temperature
   public String getHighTemperature()
   {
      return forecast[HIGH_TEMP_INDEX];      
   } // end method getHighTemperature
   
   // return this forecast's low temperature
   public String getLowTemperature()
   {
      return forecast[LOW_TEMP_INDEX];      
   } // end method getLowTemperature
} // end class DailyForecast