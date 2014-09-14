// MediaItem.java
// Represents an image or video in a slideshow.
package com.deitel.enhancedslideshow;

import java.io.Serializable;

public class MediaItem implements Serializable
{
   private static final long serialVersionUID = 1L; // class's version #

   // constants for media types
   public static enum MediaType { IMAGE, VIDEO } 
   
   private MediaType type; // this MediaItem is an IMAGE or VIDEO
   private String path; // location of this MediaItem
   
   // constructor
   public MediaItem(MediaType mediaType, String location)
   {
      type = mediaType;
      path = location;
   } // end constructor
   
   // get the MediaType of this image or video
   public MediaType getType()
   {
      return type;
   } // end method MediaType
   
   // return the description of this image or video
   public String getPath()
   {
      return path;
   } // end method getDescription
} // end class MediaItem




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