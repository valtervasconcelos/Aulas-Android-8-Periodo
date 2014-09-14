// FavoriteTwitterSearchs.js
// Storing and retrieving key-value pairs using 
// HTML5 localStorage and sessionStorage
var tags; // array of tags for queries

// register event handlers then load searches
function start()
{
   var saveButton = document.getElementById( "saveButton" );
   saveButton.addEventListener( "click", saveSearch, false );
   var clearButton = document.getElementById( "clearButton" );
   clearButton.addEventListener( "click", clearAllSearches, false );
   loadSearches(); // load the previously saved searches
} // end function start

// loads previously saved searches and displays them in the page
function loadSearches() 
{
   if ( !window.sessionStorage.getItem( "herePreviously" ) )
   {
      sessionStorage.setItem( "herePreviously", "true" );
      document.getElementById( "welcomeMessage" ).innerHTML = 
         "Welcome to the Favorite Twitter Searches App";
   } // end if

   var length = localStorage.length; // number of key-value pairs
   tags = []; // create empty array

   // load all keys
   for (var i = 0; i < length; ++i) 
   {
      tags[i] = localStorage.key(i);
   } // end for

   tags.sort(
      function( string1, string2 )
      {
         var comparisonResult = 0;
         string1 = string1.toUpperCase();
         string2 = string2.toUpperCase();

         if ( string1 > string2 ) 
            comparisonResult = 1;

         if ( string1 < string2 )
            comparisonResult = -1;
         
         return comparisonResult;
      } // end function
   ); // sort the keys

   var markup = "<ul>"; // used to store search link markup
   var url = "http://search.twitter.com/search?q=";

   // build list of links
   for (var tag in tags) 
   {
      var query = url + localStorage.getItem(tags[tag]);
      markup += "<li><a href = '" + query + "'>" + tags[tag] + "</a>" +
         "<img id = '" + tags[tag] + "' src = 'edit.png' alt = '" + 
            "Edit search: " + tags[tag] + "' onclick = 'editTag(id)'>" +
         "<img id = '" + tags[tag] + "' src = 'delete.png' alt = '" + 
            "Delete search: " + tags[tag] + "' onclick = 'deleteTag(id)'>";
   } // end for

   markup += "</ul>";
   document.getElementById("searches").innerHTML = markup;
} // end function loadSearches

// deletes all key-value pairs from localStorage
function clearAllSearches() 
{
   if ( confirm("Are you sure?") )
   {
      localStorage.clear();
      loadSearches(); // reload searches
   }
} // end function clearAllSearches

// saves a newly tagged search into localStorage
function saveSearch() 
{
   var query = document.getElementById("query");
   var tag = document.getElementById("tag");
   localStorage.setItem(tag.value, query.value); 
   tag.value = ""; // clear tag input
   query.value = ""; // clear query input
   loadSearches(); // reload searches
} // end function saveSearch

// deletes a specific key-value pair from localStorage
function deleteTag( tag ) 
{
   localStorage.removeItem( tag );
   loadSearches(); // reload searches
} // end function deleteTag

// display existing tagged query for editing
function editTag( tag )
{
   document.getElementById("query").value = localStorage[ tag ];
   document.getElementById("tag").value = tag;   
   loadSearches(); // reload searches
} // end function editTag

window.addEventListener( "load", start, false );

/*************************************************************************
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