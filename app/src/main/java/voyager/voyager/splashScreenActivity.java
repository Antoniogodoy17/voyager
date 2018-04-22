package voyager.voyager;

import android.app.ActionBar;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class splashScreenActivity extends AppCompatActivity {
    private LinearLayout l1;
    Animation alphaAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);



        l1 = findViewById(R.id.l1);
        alphaAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_transition);
        l1.setAnimation(alphaAnim);

        Thread timer = new Thread(){
            @Override
            public void run(){
                try{
                    sleep(2500);
                    Intent login = new Intent(getApplicationContext(),LogInActivity.class);
                    startActivity(login);
                    finish();
                    super.run();
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        timer.start();
//        View decorView = getWindow().getDecorView();
//// Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//// Remember that you should never show the action bar if the
//// status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();
    }
}