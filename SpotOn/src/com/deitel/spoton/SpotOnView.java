// SpotOnView.java
// View that displays and manages the game
package com.deitel.spoton;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpotOnView extends View 
{
   // constant for accessing the high score in SharedPreference
   private static final String HIGH_SCORE = "HIGH_SCORE";
   private SharedPreferences preferences; // stores the high score

   // variables for managing the game
   private int spotsTouched; // number of spots touched
   private int score; // current score
   private int level; // current level
   private int viewWidth; // stores the width of this View
   private int viewHeight; // stores the height of this view
   private long animationTime; // how long each spot remains on the screen
   private boolean gameOver; // whether the game has ended
   private boolean gamePaused; // whether the game has ended
   private boolean dialogDisplayed; // whether the game has ended
   private int highScore; // the game's all time high score
   
   // collections of spots (ImageViews) and Animators 
   private final Queue<ImageView> spots = 
      new ConcurrentLinkedQueue<ImageView>(); 
   private final Queue<Animator> animators = 
      new ConcurrentLinkedQueue<Animator>(); 
   
   private TextView highScoreTextView; // displays high score
   private TextView currentScoreTextView; // displays current score
   private TextView levelTextView; // displays current level
   private LinearLayout livesLinearLayout; // displays lives remaining
   private RelativeLayout relativeLayout; // displays spots
   private Resources resources; // used to load resources
   private LayoutInflater layoutInflater; // used to inflate GUIs

   // time in milliseconds for spot and touched spot animations
   private static final int INITIAL_ANIMATION_DURATION = 6000;
   private static final Random random = new Random(); // for random coords
   private static final int SPOT_DIAMETER = 100; // initial spot size
   private static final float SCALE_X = 0.25f; // end animation x scale
   private static final float SCALE_Y = 0.25f; // end animation y scale
   private static final int INITIAL_SPOTS = 5; // initial # of spots
   private static final int SPOT_DELAY = 500; // delay in milliseconds
   private static final int LIVES = 3; // start with 3 lives   
   private static final int MAX_LIVES = 7; // maximum # of total lives
   private static final int NEW_LEVEL = 10; // spots to reach new level
   private Handler spotHandler; // adds new spots to the game 

   // sound IDs, constants and variables for the game's sounds
   private static final int HIT_SOUND_ID = 1;
   private static final int MISS_SOUND_ID = 2;
   private static final int DISAPPEAR_SOUND_ID = 3;
   private static final int SOUND_PRIORITY = 1; 
   private static final int SOUND_QUALITY = 100;
   private static final int MAX_STREAMS = 4;
   private SoundPool soundPool; // plays sound effects
   private int volume; // sound effect volume
   private Map<Integer, Integer> soundMap; // maps ID to soundpool
   
   // constructs a new SpotOnView
   public SpotOnView(Context context, SharedPreferences sharedPreferences,
      RelativeLayout parentLayout)
   {
      super(context);
      
      // load the high score
      preferences = sharedPreferences;
      highScore = preferences.getInt(HIGH_SCORE, 0);

      // save Resources for loading external values
      resources = context.getResources();

      // save LayoutInflater
      layoutInflater = (LayoutInflater) context.getSystemService(
         Context.LAYOUT_INFLATER_SERVICE);

      // get references to various GUI components
      relativeLayout = parentLayout;
      livesLinearLayout = (LinearLayout) relativeLayout.findViewById(
         R.id.lifeLinearLayout); 
      highScoreTextView = (TextView) relativeLayout.findViewById(
         R.id.highScoreTextView);
      currentScoreTextView = (TextView) relativeLayout.findViewById(
         R.id.scoreTextView);
      levelTextView = (TextView) relativeLayout.findViewById(
         R.id.levelTextView);

      spotHandler = new Handler(); // used to add spots when game starts
   } // end SpotOnView constructor

   // store SpotOnView's width/height
   @Override
   protected void onSizeChanged(int width, int height, int oldw, int oldh)
   {
      viewWidth = width; // save the new width
      viewHeight = height; // save the new height
   } // end method onSizeChanged

   // called by the SpotOn Activity when it receives a call to onPause
   public void pause()
   {
      gamePaused = true;
      soundPool.release(); // release audio resources
      soundPool = null;
      cancelAnimations(); // cancel all outstanding animations
   } // end method pause

   // cancel animations and remove ImageViews representing spots
   private void cancelAnimations()
   {
      // cancel remaining animations
      for (Animator animator : animators)
         animator.cancel();

      // remove remaining spots from the screen
      for (ImageView view : spots)
         relativeLayout.removeView(view);

      spotHandler.removeCallbacks(addSpotRunnable);
      animators.clear();
      spots.clear();
   } // end method cancelAnimations
   
   // called by the SpotOn Activity when it receives a call to onResume
   public void resume(Context context)
   {
      gamePaused = false;
      initializeSoundEffects(context); // initialize app's SoundPool

      if (!dialogDisplayed)
         resetGame(); // start the game
   } // end method resume

   // start a new game
   public void resetGame()
   {
      spots.clear(); // empty the List of spots
      animators.clear(); // empty the List of Animators
      livesLinearLayout.removeAllViews(); // clear old lives from screen
      
      animationTime = INITIAL_ANIMATION_DURATION; // init animation length
      spotsTouched = 0; // reset the number of spots touched
      score = 0; // reset the score
      level = 1; // reset the level
      gameOver = false; // the game is not over
      displayScores(); // display scores and level
      
      // add lives 
      for (int i = 0; i < LIVES; i++)
      {
         // add life indicator to screen
         livesLinearLayout.addView(
            (ImageView) layoutInflater.inflate(R.layout.life, null));
      } // end for

      // add INITIAL_SPOTS new spots at SPOT_DELAY time intervals in ms
      for (int i = 1; i <= INITIAL_SPOTS; ++i)
         spotHandler.postDelayed(addSpotRunnable, i * SPOT_DELAY);
   } // end method resetGame

   // create the app's SoundPool for playing game audio
   private void initializeSoundEffects(Context context)
   {
      // initialize SoundPool to play the app's three sound effects
      soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,
         SOUND_QUALITY);

      // set sound effect volume
      AudioManager manager =
         (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);

      // create sound map
      soundMap = new HashMap<Integer, Integer>(); // create new HashMap

      // add each sound effect to the SoundPool
      soundMap.put(HIT_SOUND_ID,
         soundPool.load(context, R.raw.hit, SOUND_PRIORITY));
      soundMap.put(MISS_SOUND_ID,
         soundPool.load(context, R.raw.miss, SOUND_PRIORITY));
      soundMap.put(DISAPPEAR_SOUND_ID,
         soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));
   } // end method initializeSoundEffect

   // display scores and level
   private void displayScores()
   {
      // display the high score, current score and level
      highScoreTextView.setText(
         resources.getString(R.string.high_score) + " " + highScore);
      currentScoreTextView.setText(
         resources.getString(R.string.score) + " " + score);
      levelTextView.setText(
         resources.getString(R.string.level) + " " + level);
   } // end function displayScores

   // Runnable used to add new spots to the game at the start
   private Runnable addSpotRunnable = new Runnable()
   {
      public void run()
      {
         addNewSpot(); // add a new spot to the game
      } // end method run
   }; // end Runnable

   // adds a new spot at a random location and starts its animation
   public void addNewSpot()
   {
      // choose two random coordinates for the starting and ending points
      int x = random.nextInt(viewWidth - SPOT_DIAMETER);
      int y = random.nextInt(viewHeight - SPOT_DIAMETER);
      int x2 = random.nextInt(viewWidth - SPOT_DIAMETER);
      int y2 = random.nextInt(viewHeight - SPOT_DIAMETER);

      // create new spot
      final ImageView spot =
         (ImageView) layoutInflater.inflate(R.layout.untouched, null);
      spots.add(spot); // add the new spot to our list of spots
      spot.setLayoutParams(new RelativeLayout.LayoutParams(
         SPOT_DIAMETER, SPOT_DIAMETER));
      spot.setImageResource(random.nextInt(2) == 0 ?
         R.drawable.green_spot : R.drawable.red_spot);
      spot.setX(x); // set spot's starting x location
      spot.setY(y); // set spot's starting y location
      spot.setOnClickListener( // listens for spot being clicked
         new OnClickListener()
         {            
            public void onClick(View v)
            {
               touchedSpot(spot); // handle touched spot
            } // end method onClick
         } // end OnClickListener 
      ); // end call to setOnClickListener 
      relativeLayout.addView(spot); // add spot to the screen

      // configure and start spot's animation
      spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y)
         .setDuration(animationTime).setListener(
            new AnimatorListenerAdapter()
            {
               @Override
               public void onAnimationStart(Animator animation)
               {
                  animators.add(animation); // save for possible cancel
               } // end method onAnimationStart

               public void onAnimationEnd(Animator animation)
               {
                  animators.remove(animation); // animation done, remove
                  
                  if (!gamePaused && spots.contains(spot)) // not touched
                  {
                     missedSpot(spot); // lose a life
                  } // end if
               } // end method onAnimationEnd
            } // end AnimatorListenerAdapter
         ); // end call to setListener
   } // end addNewSpot method

   // called when the user touches the screen, but not a spot
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
      // play the missed sound
      if (soundPool != null)
         soundPool.play(MISS_SOUND_ID, volume, volume, 
            SOUND_PRIORITY, 0, 1f);
      
      score -= 15 * level; // remove some points
      score = Math.max(score, 0); // do not let the score go below zero
      displayScores(); // update scores/level on screen
      return true;
   } // end method onTouchEvent

   // called when a spot is touched
   private void touchedSpot(ImageView spot)
   {
      relativeLayout.removeView(spot); // remove touched spot from screen 
      spots.remove(spot); // remove old spot from list               

      ++spotsTouched; // increment the number of spots touched
      score += 10 * level; // increment the score

      // play the hit sounds
      if (soundPool != null)
         soundPool.play(HIT_SOUND_ID, volume, volume, 
            SOUND_PRIORITY, 0, 1f);

      // increment level if player touched 10 spots in the current level
      if (spotsTouched % NEW_LEVEL == 0)
      {
         ++level; // increment the level
         animationTime *= 0.95; // make game 5% faster than prior level

         // if the maximum number of lives has not been reached
         if (livesLinearLayout.getChildCount() < MAX_LIVES)
         {
            ImageView life = 
               (ImageView) layoutInflater.inflate(R.layout.life, null);
            livesLinearLayout.addView(life); // add life to screen
         } // end if
      } // end if
 
      displayScores(); // update score/level on the screen
      
      if (!gameOver)
         addNewSpot(); // add another untouched spot
   } // end method touchedSpot

   // called when a spot finishes its animation without being touched
   public void missedSpot(ImageView spot)
   {      
      spots.remove(spot); // remove spot from spots List
      relativeLayout.removeView(spot); // remove spot from screen
      
      if (gameOver) // if the game is already over, exit
         return;

      // play the disappear sound effect
      if (soundPool != null)
         soundPool.play(DISAPPEAR_SOUND_ID, volume, volume, 
            SOUND_PRIORITY, 0, 1f);

      // if the game has been lost
      if (livesLinearLayout.getChildCount() == 0)
      {
         gameOver = true; // the game is over

         // if the last game's score is greater than the high score
         if (score > highScore)
         {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(HIGH_SCORE, score);
            editor.commit(); // store the new high score
            highScore = score;
         } // end if

         cancelAnimations();
            
         // display a high score dialog
         Builder dialogBuilder = new AlertDialog.Builder(getContext());
         dialogBuilder.setTitle(R.string.game_over);
         dialogBuilder.setMessage(resources.getString(R.string.score) +
            " " + score);
         dialogBuilder.setPositiveButton(R.string.reset_game,
            new DialogInterface.OnClickListener()
            {
               public void onClick(DialogInterface dialog, int which)
               { 
                  displayScores(); // ensure that score is up to date
                  dialogDisplayed = false;
                  resetGame(); // start a new game
               } // end method onClick
            } // end DialogInterface
         ); // end call to dialogBuilder.setPositiveButton
         dialogDisplayed = true;
         dialogBuilder.show(); // display the reset game dialog
      } // end if
      else // remove one life   
      {
         livesLinearLayout.removeViewAt( // remove life from screen
            livesLinearLayout.getChildCount() - 1); 
         addNewSpot(); // add another spot to game
      } // end else
   } // end method missedSpot
} // end class SpotOnView



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
