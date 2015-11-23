package happinessdevelopment.u_wavealphaalpha;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;

public class MainActivity extends FragmentActivity {

    private MediaPlayer mediaPlayer; // Creates our media player.
    // Where we will be streaming from. Keep it as final since it shouldn't chance.
    final String streamURL = "http://live.uwave.fm:8000/listen-128.mp3";
    // Assigning our buttons
    private ImageButton playButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Default Stuff that happens when app created.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing our buttons
        playButton = (ImageButton) findViewById(R.id.play_button);

        // This is a listener used to keep track if the play button has been pressed.
        try{
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(streamURL);
            mediaPlayer.prepare();
        }catch(IOException e){
            e.printStackTrace();
        }

        playButton.setOnClickListener(new View.OnClickListener() {

            //When Play button is clicked
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.stop_icon);
                } else if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    playButton.setImageResource(R.drawable.play_icon);
                    mediaPlayer.prepareAsync();
                }
            }

        });
    }
}