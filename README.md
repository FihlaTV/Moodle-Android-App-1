# Moodle-Android-App
=======================================
ANDROID APP:  MiniMoodle
=======================================

----------------------------------------
Requirement: User must be connected to IIT-Bombay network.
----------------------------------------

MiniMoodle app shows you all the posts, from the News Feed in your moodle, which have been made after the date and time you mentioned.

1. Firstly, user need to login with his/her moodle ID and PASSWORD then it will redirected to next Activity(SubjectListActivity).
2. In SubjectListActivity, there are two buttons for selecting date and time after which user wants to see the posts.
   After selecting the date nd time, user will need to select the ssubject from the list, the post of which will be shown on the next Activity.
3. NewsFeedActivity shows all the posts from the user's news feed which have been made after the date and time as selected by the user.
4. There is a Logout option in the MenuOptions in the ActionBar in SubjectListActivity and NewsFeedActivty.
----------------------------------------

MiniMoodle uses the 'Jsoup' to connect to the moodle server and then parses the required content from different pages.

Different possibilities which have been taken care are:
1. If user leaves one of the ID/PASSWORD field empty, a error message is displayed.
2. If the phone is not connected to internet, a proper message is displayed.
3. If phone is using its own network instead of IIT-B network, a proper message is displayed.
4. If a user Log's out, then on pressing back button, user does not goes back to previous activities, this has been handled by managing the 
   Activities Stack.
4. If user has not logged his account out and closes the app, then on again reopening the application, he remains logged in. This has been implemented using the 'SharedPreferences' which is a kind of local database but just stores the values in the form of key-value pair.
---------------------------------------



