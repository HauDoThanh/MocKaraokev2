package home.mockaraokev2.Actitivy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import home.mockaraokev2.R;

public class Test extends AppCompatActivity {

    TextView txtShow;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        txtShow = (TextView) findViewById(R.id.txtShow);


        //Declare the timer
        Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtShow.setText(String.valueOf(i++));
                    }
                });
            }
        }, 0, 1);

        if (i == 10000){
            t.cancel();
        }
    }
}
