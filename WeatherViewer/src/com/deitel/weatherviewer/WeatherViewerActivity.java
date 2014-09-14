// WeatherViewerActivity.java
// Main Activity for the Weather Viewer app.
package com.deitel.weatherviewer;

import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.deitel.weatherviewer.AddCityDialogFragment.DialogFinishedListener;
import com.deitel.weatherviewer.CitiesFragment.CitiesListChangeListener;
import com.deitel.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class WeatherViewerActivity extends Activity implements 
   DialogFinishedListener
{
   public static final String WIDGET_UPDATE_BROADCAST_ACTION = 
      "com.deitel.weatherviewer.UPDATE_WIDGET";
   
   private static final int BROADCAST_DELAY = 10000;
   
   private static final int CURRENT_CONDITIONS_TAB = 0;
   
   public static final String PREFERRED_CITY_NAME_KEY = 
      "preferred_city_name";
   public static final String PREFERRED_CITY_ZIPCODE_KEY = 
      "preferred_city_zipcode";
   public static final String SHARED_PREFERENCES_NAME = 
      "weather_viewer_shared_preferences";
   private static final String CURRENT_TAB_KEY = "current_tab";
   private static final String LAST_SELECTED_KEY = "last_selected";
   
   private int currentTab; // position of the current selected tab
   private String lastSelectedCity; // last city selected from the list
   private SharedPreferences weatherSharedPreferences;
   
   // stores city names and the corresponding zipcodes
   private Map<String, String> favoriteCitiesMap;
   private CitiesFragment listCitiesFragment;
   private Handler weatherHandler;
   
   // initializes this Activity and inflates its layout from xml
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState); // pass given Bundle to super
      setContentView(R.layout.main); // inflate layout in main.xml
      
      // get the CitiesFragment
      listCitiesFragment = (CitiesFragment) 
         getFragmentManager().findFragmentById(R.id.cities);
      
      // set the CitiesListChangeListener
      listCitiesFragment.setCitiesListChangeListener(
         citiesListChangeListener);
      
      // create HashMap storing city names and corresponding zipcodes
      favoriteCitiesMap = new HashMap<String, String>();
      
      weatherHandler = new Handler();
      
      weatherSharedPreferences = getSharedPreferences(
         SHARED_PREFERENCES_NAME, MODE_PRIVATE);

      setupTabs(); // set up the ActionBar's navigation tabs
   } // end method onCreate
   
   // save this Activity's state
   @Override
   public void onSaveInstanceState(Bundle savedInstanceStateBundle) 
   {
      // save the currently selected tab
      savedInstanceStateBundle.putInt(CURRENT_TAB_KEY, currentTab);
      savedInstanceStateBundle.putString(LAST_SELECTED_KEY, 
         lastSelectedCity); // save the currently selected city
      super.onSaveInstanceState(savedInstanceStateBundle);
   } // end method onSaveInstanceState
   
   // restore the saved Activity state
   @Override
   public void onRestoreInstanceState(Bundle savedInstanceStateBundle) 
   {
      super.onRestoreInstanceState(savedInstanceStateBundle);
     
      // get the selected tab
      currentTab = savedInstanceStateBundle.getInt(CURRENT_TAB_KEY);
      lastSelectedCity = savedInstanceStateBundle.getString(
         LAST_SELECTED_KEY); // get the selected city
   } // end method onRestoreInstanceState
   
   // called when this Activity resumes
   @Override
   public void onResume() 
   {
      super.onResume();
      
      if (favoriteCitiesMap.isEmpty()) // if the city list is empty
      {
         loadSavedCities(); // load previously added cities
      } // end if
      
      // if there are no cities left
      if (favoriteCitiesMap.isEmpty()) 
      {
         addSampleCities(); // add sample cities
      } // end if
      
      // load previously selected forecast
      getActionBar().selectTab(getActionBar().getTabAt(currentTab));
      loadSelectedForecast();
   } // end method onResume
   
   // listens for changes to the CitiesFragment
   private CitiesListChangeListener citiesListChangeListener = 
      new CitiesListChangeListener()
   {
     // called when the selected city is changed
      @Override
     public void onSelectedCityChanged(String cityNameString) 
      {
         // show the given city's forecast
         selectForecast(cityNameString);     
     } // end method onSelectedCityChanged

      // called when the preferred city is changed
     @Override
     public void onPreferredCityChanged(String cityNameString) 
     {
        // save the new preferred city to the app's SharedPreferences
        setPreferred(cityNameString);   
     } // end method onPreferredCityChanged
   }; // end CitiesListChangeListener
   
   // load the previously selected forecast
   private void loadSelectedForecast()
   {
      // if there was a previously selected city
      if (lastSelectedCity != null) 
      {
         selectForecast(lastSelectedCity); // select last selected city
      } // end if
      else
      {
         // get the name of the preferred city
         String cityNameString = weatherSharedPreferences.getString(
            PREFERRED_CITY_NAME_KEY, getResources().getString(
            R.string.default_zipcode));
         selectForecast(cityNameString); // load preferred city's forecast
      } // end else
   } // end loadSelectedForecast
   
   // set the preferred city
   public void setPreferred(String cityNameString)
   {
      // get the give city's zipcode
      String cityZipcodeString = favoriteCitiesMap.get(cityNameString);
      Editor preferredCityEditor = weatherSharedPreferences.edit();
      preferredCityEditor.putString(PREFERRED_CITY_NAME_KEY, 
         cityNameString);
      preferredCityEditor.putString(PREFERRED_CITY_ZIPCODE_KEY, 
         cityZipcodeString);
      preferredCityEditor.apply(); // commit the changes
      lastSelectedCity = null; // remove the last selected forecast
      loadSelectedForecast(); // load the preferred city's forecast
      
      // update the app widget to display the new preferred city
      final Intent updateWidgetIntent = new Intent(
         WIDGET_UPDATE_BROADCAST_ACTION);
      
      // send broadcast after short delay
      weatherHandler.postDelayed(new Runnable()
      {
         @Override
         public void run() 
         {
            sendBroadcast(updateWidgetIntent); // broadcast the intent
         }
      }, BROADCAST_DELAY);
   } // end method setPreferred
   
   // reads previously saved city list from SharedPreferences
   private void loadSavedCities()
   {
      Map<String, ?> citiesMap = weatherSharedPreferences.getAll();

      for(String cityString : citiesMap.keySet())
      {
         // if this value is not the preferred city
         if (!(cityString.equals(PREFERRED_CITY_NAME_KEY) || 
            cityString.equals(PREFERRED_CITY_ZIPCODE_KEY)))
         {
            addCity(cityString, (String) citiesMap.get(cityString), false);
         } // end if
      } // end for
   } // end method loadSavedCities
   
   // add the sample cities 
   private void addSampleCities() 
   {
      // load the array of city names from resources
      String[] sampleCityNamesArray = getResources().getStringArray(
         R.array.default_city_names);
      
      // load the array of zipcodes from resources
      String[] sampleCityZipcodesArray = getResources().getStringArray(
         R.array.default_city_zipcodes);

      // for each sample city
      for (int i = 0; i < sampleCityNamesArray.length; i++)
      {
         // set the first sample city as the preferred city by default
         if (i == 0) 
         {
            setPreferred(sampleCityNamesArray[i]);
         } // end if
         
         // add city to the list
         addCity(sampleCityNamesArray[i], sampleCityZipcodesArray[i], 
            false);   
      } // end for
   } // end method addSampleCities
   
   // add a new city to the CitiesFragment ListFragment
   public void addCity(String city, String zipcode, boolean select) 
   {
      favoriteCitiesMap.put(city, zipcode); // add to HashMap of cities
      listCitiesFragment.addCity(city, select); // add city to Fragment
      Editor preferenceEditor = weatherSharedPreferences.edit();
      preferenceEditor.putString(city, zipcode);
      preferenceEditor.apply();
   } // end method addCity
   
   // display forecast information for the given city
   public void selectForecast(String name) 
   {      
      lastSelectedCity = name; // save the city name
      String zipcodeString = favoriteCitiesMap.get(name); 
      if (zipcodeString == null) // if the zipcode can't be found
      {
         return; // do not attempt to load a forecast
      } // end if
      
      // get the current visible ForecastFragment
      ForecastFragment currentForecastFragment = (ForecastFragment) 
         getFragmentManager().findFragmentById(R.id.forecast_replacer);
      
      if (currentForecastFragment == null || 
         !(currentForecastFragment.getZipcode().equals(zipcodeString) && 
         correctTab(currentForecastFragment)))
      {
         // if the selected current tab is "Current Conditions"
         if (currentTab == CURRENT_CONDITIONS_TAB)
         {
            // create a new ForecastFragment using the given zipcode
            currentForecastFragment = SingleForecastFragment.newInstance(
               zipcodeString);
         } // end if
         else
         {
            // create a new ForecastFragment using the given zipcode
            currentForecastFragment = FiveDayForecastFragment.newInstance(
               zipcodeString);
         } // end else
         
         // create a new FragmentTransaction
         FragmentTransaction forecastFragmentTransaction = 
            getFragmentManager().beginTransaction();
          
         // set transition animation to fade 
         forecastFragmentTransaction.setTransition(
            FragmentTransaction.TRANSIT_FRAGMENT_FADE);
          
         // replace the Fragment (or View) at the given id with our 
         // new Fragment
         forecastFragmentTransaction.replace(R.id.forecast_replacer, 
            currentForecastFragment);
         
         forecastFragmentTransaction.commit(); // begin the transition
      } // end if
   } // end method selectForecast
   
   // is this the proper ForecastFragment for the currently selected tab?
   private boolean correctTab(ForecastFragment forecastFragment)
   {
      // if the "Current Conditions" tab is selected
      if (currentTab == CURRENT_CONDITIONS_TAB)
      {
         // return true if the given ForecastFragment 
         // is a SingleForecastFragment
         return (forecastFragment instanceof SingleForecastFragment);
      } // end if
      else // the "Five Day Forecast" tab is selected
      {
         // return true if the given ForecastFragment 
         // is a FiveDayForecastFragment 
         return (forecastFragment instanceof FiveDayForecastFragment); 
      } // end else
   } // end method correctTab
   
   // select the tab at the given position
   private void selectTab(int position)
   {
      currentTab = position; // save the position tab
      loadSelectedForecast();
   } // end method selectTab
   
   // create this Activities Menu
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      super.onCreateOptionsMenu(menu); 
      MenuInflater inflater = getMenuInflater(); // global MenuInflator
      
      // inflate layout defined in actionmenu.xml
      inflater.inflate(R.menu.actionmenu, menu); 
      return true; // return true since the menu was created
   } // end method onCreateOptionsMenu
   
   // when one of the items was clicked
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
     // if the item selected was the "Add City" item
      if (item.getItemId() == R.id.add_city_item)
      {
         showAddCityDialog(); // show Dialog for user input
         return true; // return true since we handled the selection
      } // end if
      
      return false; // do not handle unexpected menu items
   } // end method onOptionsItemSelected
   
   // display FragmentDialog allowing the user to add a new city
   private void showAddCityDialog()
   {
     // create a new AddCityDialogFragment 
      AddCityDialogFragment newAddCityDialogFragment = 
         new AddCityDialogFragment();
      
      // get instance of the FragmentManager
      FragmentManager thisFragmentManager = getFragmentManager();
      
      // begin a FragmentTransaction
      FragmentTransaction addCityFragmentTransition = 
         thisFragmentManager.beginTransaction();
      
      // show the DialogFragment
      newAddCityDialogFragment.show(addCityFragmentTransition, "");
   } // end method showAddCityDialog

   // called when the FragmentDialog is dismissed 
   @Override
   public void onDialogFinished(String zipcodeString, boolean preferred) 
   {
     // convert zipcode to city
      getCityNameFromZipcode(zipcodeString, preferred);
   } // end method onDialogFinished
   
   // read city name from zipcode
   private void getCityNameFromZipcode(String zipcodeString, 
      boolean preferred)
   {
	  // if this zipcode is already added
      if (favoriteCitiesMap.containsValue(zipcodeString))
      {
    	 // create a Toast displaying error information
         Toast errorToast = Toast.makeText(WeatherViewerActivity.this, 
            WeatherViewerActivity.this.getResources().getString(
            R.string.duplicate_zipcode_error), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0);
         errorToast.show(); // show the Toast     
      } // end if
      else 
      {
         // load the location information in a background thread
         new ReadLocationTask(zipcodeString, this, 
            new CityNameLocationLoadedListener(zipcodeString, preferred)).
            execute();
      } // end else
   } // end method getCityNameFromZipcode
   
   // listens for city information loaded in background task
   private class CityNameLocationLoadedListener implements 
      LocationLoadedListener
   {
      private String zipcodeString; // zipcode to look up
      private boolean preferred;
        
      // create a new CityNameLocationLoadedListener
      public CityNameLocationLoadedListener(String zipcodeString, 
         boolean preferred)
      {
         this.zipcodeString = zipcodeString;
         this.preferred = preferred;
      } // end CityNameLocationLoadedListener
      
      @Override
      public void onLocationLoaded(String cityString, String stateString,
            String countryString) 
      {
          // if a city was found to match the given zipcode
         if (cityString != null)
         {
            addCity(cityString, zipcodeString, !preferred); // add new city
            
            if (preferred) // if this location is the preferred city
            {
               // save the preferred city to SharedPreferences
               setPreferred(cityString);
            } // end if
         } // end if
         else 
         {
            // display a text explaining that location information could
            // not be found
            Toast zipcodeToast = Toast.makeText(WeatherViewerActivity.this, 
               WeatherViewerActivity.this.getResources().getString(
               R.string.invalid_zipcode_error), Toast.LENGTH_LONG);
            zipcodeToast.setGravity(Gravity.CENTER, 0, 0);
            zipcodeToast.show(); // show the Toast
         } // end else
      } // end method onLocationLoaded
   } // end class CityNameLocationLoadedListener
   
   // set up the ActionBar's tabs
   private void setupTabs()
   {
      ActionBar weatherActionBar = getActionBar(); // get the ActionBar
      
      // set ActionBar's navigation mode to use tabs
      weatherActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      
      // create the "Current Conditions" Tab
      Tab currentConditionsTab = weatherActionBar.newTab();
      
      // set the Tab's title
      currentConditionsTab.setText(getResources().getString(
         R.string.current_conditions));
      
      // set the Tab's listener
      currentConditionsTab.setTabListener(weatherTabListener);
      weatherActionBar.addTab(currentConditionsTab); // add the Tab
      
      // create the "Five Day Forecast" tab
      Tab fiveDayForecastTab = weatherActionBar.newTab();
      fiveDayForecastTab.setText(getResources().getString(
         R.string.five_day_forecast));
      fiveDayForecastTab.setTabListener(weatherTabListener);
      weatherActionBar.addTab(fiveDayForecastTab);
      
      // select "Current Conditions" Tab by default
      currentTab = CURRENT_CONDITIONS_TAB; 
   } // end method setupTabs
   
   // listen for events generated by the ActionBar Tabs
   TabListener weatherTabListener = new TabListener()
   {
      // called when the selected Tab is re-selected
      @Override
      public void onTabReselected(Tab arg0, FragmentTransaction arg1) 
      {
      } // end method onTabReselected

      // called when a previously unselected Tab is selected
      @Override
      public void onTabSelected(Tab tab, FragmentTransaction arg1) 
      {
         // display the information corresponding to the selected Tab
         selectTab(tab.getPosition());
      } // end method onTabSelected

      // called when a tab is unselected
      @Override
      public void onTabUnselected(Tab arg0, FragmentTransaction arg1) 
      {
      } // end method onTabSelected
   }; // end WeatherTabListener
} // end Class WeatherViewerActivity