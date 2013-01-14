/**
 * Author: Johny Urgiles
 * Date: Dec 10, 2012
 * Org: RightRides
 */
package com.rightrides;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class RightRidesSplashScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(RightRidesSplashScreen.this, MainActivity.class);
                RightRidesSplashScreen.this.startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        }, 2500);

    }

}