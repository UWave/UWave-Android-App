package happinessdevelopment.u_wavealphaalpha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * This is our SplashScreen class, it's where we make the launcher screen when the app starts.
 *
 * @author H[App]iness Development
 * @version 1.0.0
 *          <p/>
 *          This app does a simple fade in and fade out of the U Wave logo before
 *          it switches to the main activity.
 */
public class SplashScreen extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is the default stuff that occurs when class is created.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Here we initialize and assign the U Wave logo to an instance of the ImageView class.
        final ImageView iv = (ImageView) findViewById(R.id.logo_image);

        /*
        Here we initialize and assign two different animations to the Animation class
        which allows us to make our logo fade in and out.
         */
        final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_in);
        final Animation an2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);

        // We start our first animation (fading in).
        iv.startAnimation(an);

        /*
        We create a listener that allows us to do a statement at the start or end of the animation.
         */
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Nothing is done at the start of the animation.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /*
                Here we create a handler that allows us to delay any action at the end of the
                 animation. This is done so we can view the logo longer before switching to
                 fadeout and moving to the main screen.
                 */
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // We begin the next animation (fade out).
                        iv.startAnimation(an2);
                        /*
                        After animation is complete, we call finish to end this activity
                        so that it does stay open anymore.
                         */
                        finish();
                        /*Create a new intention which is to go from this activity to the
                        main activity. But it doesn't act it out yet.*/
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        // This line acts out the intention.
                        startActivity(i);
                    }
                }, 1500); // How long it is delayed (1500 Millis)
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation doesn't repeat so nothing happens here.
            }
        });
    }
}