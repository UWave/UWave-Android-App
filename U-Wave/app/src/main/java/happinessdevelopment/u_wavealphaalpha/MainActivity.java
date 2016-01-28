package happinessdevelopment.u_wavealphaalpha;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import happinessdevelopment.u_wavealphaalpha.MusicService.LocalBinder;

/**
 * This is our main class that is displayed after the launcher screen.
 * It is where the user interacts with the application.
 *
 * @author H[App]iness Development
 * @version 1.0.0
 *          <p/>
 *          This is where we set up our buttons, start our music service and
 *          handle other tasks like making an icon notification.
 */
public class MainActivity extends FragmentActivity {
    // This is used for identifying our notification.
    private static final int NOTIFICATION_ID = 1;
    // Used for accessing music service/creating our own instance here to use.
    MusicService musicService;
    // Used for toggling play and pause button.
    boolean volume = false;

    // This is used for monitoring the state of our music service.
    ServiceConnection mConnection = new ServiceConnection() {
        /**
         * This method is for what happens when we disconnect from our service.
         *
         * @param name This is the name of the service that was disconnected from.
         */
        public void onServiceDisconnected(ComponentName name) {
            // If we were to disconnect, we set our music player to null.
            musicService = null;
        }

        /**
         * This is the method used if we were able to connect to our music service.
         *
         * @param name    This is the name of the service.
         * @param service This is an instance of a bind we want to bind to.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*
             Here we make an instance of our local binder which we assign to the
             binder passed through the parameter.
              */
            LocalBinder mLocalBinder = (LocalBinder) service;
            /*
             Once our local binder is set, we get the instance of the music service from
             the local binder so we can assign it to our instance of the music player so
             that we can access it and manipulate it.
             */
            musicService = mLocalBinder.getServerInstance();
        }
    };

    /*
     This notification manager allows us to inform the user
      that this app is running in the background
      */
    private NotificationManager mNotifyMgr;

    // Assigning our buttons
    private ImageButton playButton; // Play/Pause button
    private TextView songName;  // Text box for song name
    private TextView albumName; // Text box for album name
    private TextView artistName; // Text box for artist name

    /**
     * What occurs when the application is started/created.
     *
     * @param savedInstanceState Bundle object that is passed through
     *                           into the method of every activity.
     *                           <p/>
     *                           Here we also set the view of the application to the main screen, add our icon
     *                           in the notification bar, get our track information, and set our play and pause
     *                           buttons to do their actions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default Stuff that happens when app created.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Setting our view to the main screen.

        // Create our notification icon
        makeNotificationIcon();

        // Initializing our buttons and text views to their respective icons on the screen.
        playButton = (ImageButton) findViewById(R.id.play_button);
        songName = (TextView) findViewById(R.id.song_name);
        albumName = (TextView) findViewById(R.id.album_name);
        artistName = (TextView) findViewById(R.id.artist_name);


        //Setting up our play/paused button listener for when we press the button.
        playButton.setOnClickListener(new View.OnClickListener() {
            //When play/pause button is clicked.
            @Override
            public void onClick(View v) {
                /*
                 First we check if the music player is still buffering. If it is,
                  then we inform the user that the stream is still connecting.
                  */
                if (musicService.isBuffering()) {
                    //Simply letting the user know that we are still buffering.
                    Toast.makeText(MainActivity.this, "Connecting", Toast.LENGTH_SHORT).show();
                } else {
                    /*
                    Otherwise if the it is done buffering then we check what the volume
                    is set to. If its at 0 (mute) then we set it to un-mute and change
                    the play button icon to a pause button icon and vise versa.
                     */
                    if (!volume) {// If mute
                        musicService.unMute(); // Unmute player
                        volume = true; // Change the volume since its playing now.
                        playButton.setImageResource(R.drawable.stop_icon); // Changing icon image.
                    } else if (volume) {// If not mute
                        musicService.mute(); // Mute player
                        volume = false; // Change the volume since its not playing now.
                        playButton.setImageResource(R.drawable.play_icon); // Changing icon image.
                    }
                }
            }
        });

        // We now display the song information.
        displaySongInfo();

    }

    /**
     * This method is used for releaseing the application when we are done.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Getting rid of the notification icon on notification bar.
        mNotifyMgr.cancel(NOTIFICATION_ID);
    }

    /**
     * This method is called when this class begins.
     * <p/>
     * It is where we start our music player service and start the
     * connection from that service to this class.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // New intent from this class to the music player service class.
        Intent mIntent = new Intent(this, MusicService.class);
        // Binding our intent and also check the connection between the two.
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    /**
     * This method is what we use for getting the song information.
     * <p/>
     * We parse a json from a url containing the song name, the album, and the artist.
     */
    private void displaySongInfo() {
        /*
        Create a new thread so this can happen in the background since it
        will continue to update to keep the information up-to-date.
         */
        Thread t = new Thread() {
            /**
             * This is what will be running in the background.
             */
            @Override
            public void run() {
                try {
                    // Checking if our receiver has been interrupted.
                    while (!isInterrupted()) {
                        /*
                        This line pause this thread for 1000 millis so
                        that it updates our information every second.
                         */
                        Thread.sleep(1000);
                        /*
                        This is similar to our run above that happens but its for our UI,
                        if there is something queued on our UI then this will enter the
                        queue. Otherwise it will happen immediately.
                         */
                        runOnUiThread(new Runnable() {
                            /**
                             * This is what runs on the UI
                             */
                            @Override
                            public void run() {
                                /*
                                Makes a new instance of our JSONTask class
                                (created within the MainActivity class) and parses
                                the given url for get the song, album, and artist.
                                 */
                                new JSONTask().execute("https://uwave.fm/listen/now-playing.json");
                            }
                        });
                    }
                } catch (Exception e) {
                    // If any errors occurred during the thread, the stackTrace will be printed.
                    e.printStackTrace();
                }
            }
        };

        t.start(); // Start our thread
    }

    /**
     * This is where we create our notification icon.
     */
    private void makeNotificationIcon() {
        // This is how we control our notification as well as construct our notification layout.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_image_view).setContentTitle("U Wave")
                .setContentText("Return to player");
        // Creating an intent from this notification to our main activity.
        Intent resultIntent = new Intent(this, MainActivity.class);
        /*
         Similar to a regular intent except it includes target actions to perform.
         For this case it simply tales you back to the application main screen when performed.
          */
        PendingIntent resultPendingIntent = PendingIntent.getActivity
                (this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // When the notification is clicked it will perform the pending intent described above.
        mBuilder.setContentIntent(resultPendingIntent);
        //This creates the notification which the user will be able to see based on what we created.
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        // This tells our notification manager whay type of notification it is.
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // This notifies the user of our notification that the app is running.
        mNotifyMgr.notify(NOTIFICATION_ID, notification);
    }

    /**
     * This is our separate class that we use for parsing our JSON text from the given url.
     * <p/>
     * Since the audio changes, the JSON track information has to be updated every
     * second to see if there is a change.
     */
    public class JSONTask extends AsyncTask<String, String, String> {

        /**
         * This is what happens in the background when getting the JSON text and parsing it.
         *
         * @param params This is an array of strings containing any useful
         *               information. Like the url of what JSON we are parsing.
         * @return We return a string containing the information we need from the JSON text.
         */
        @Override
        protected String doInBackground(String... params) {
            // This is our url we are getting our track info from.
            HttpURLConnection connection = null;
            // This is the reader used when parsing the JSON text.
            BufferedReader reader = null;

            // Use a try-catch to test if we can parse from the url.
            try {
                URL url = new URL(params[0]); // Assign our url.
                // Attempt to connect to the url so we can pull information.
                connection = (HttpURLConnection) url.openConnection();
                // If it works we connect.
                connection.connect();

                // Create an input stream from the connection we formed to the url.
                InputStream stream = connection.getInputStream();
                // Assign our reader to the stream.
                reader = new BufferedReader(new InputStreamReader(stream));

                /*
                 Create a string buffer that takes a squence of characters and
                 creates strings from it.
                  */
                StringBuffer buffer = new StringBuffer();
                // Used for storing the buffered string.
                String line = "";

                /*
                 While loop that will continue until the string line (which was set to
                 the reader) equals null.
                  */
                while ((line = reader.readLine()) != null) {
                    buffer.append(line); // Our buffer then appends the line.
                }

                // Next we create a string that is equal to our buffer in string format.
                String finalJson = buffer.toString();

                /*
                Here we being the parsing of the information we want.
                We first create a JSONObject which is everything with a JSON's brackets.
                We create it from our string that we got from the url.
                 */
                JSONObject parentObject = new JSONObject(finalJson);
                // Get the song name by looking for a string with the tag title.
                String songName = parentObject.getString("title");
                // Get the artist name by looking for a string with the tag artist.
                String artistName = parentObject.getString("artist");
                // Get the artist name by looking for a string with the tag album.
                String albumName = parentObject.getString("album");

                // Returning a string containing the information we want.
                return (songName + "\n" + albumName + "\n" + artistName);

            } catch (Exception e) {
                e.printStackTrace(); // Print stack if error occurred during connecting and parsing.
            } finally {
                // Once we are done we check if connection is null, is so then we can disconnect.
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    /*
                    If our reader is empty then we can also close the stream
                     since its no longer needed.
                     */
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Print any errors if we couldn't close the reader.
                }
            }
            // In case it doesn't work we return null.
            return null;
        }

        /**
         * What occurs once we are done parsing the JSON
         *
         * @param result This is the result that was return after it finished parsing.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Check if results is null
            if (result != null) {
                // Create a scanner from the result string to get individual items.
                Scanner sc = new Scanner(result);
                String[] songInfo = new String[3]; // For song, album, artist.
                for (int i = 0; i < 3; i++) {
                    // If it does contain content the we assign the data.
                    if (sc.hasNextLine()) {
                        songInfo[i] = sc.nextLine();
                    } else {
                        // Otherwise we just leave it blank.
                        songInfo[i] = "";
                    }
                }
                // We finally change the text in the text boxes for each of the three components.
                songName.setText(songInfo[0]);
                albumName.setText(songInfo[1]);
                artistName.setText("By " + songInfo[2]);
            }
        }
    }
}