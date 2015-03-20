package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.lmu.mvs.fpmonitor.R;


public class Main_Activity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button)findViewById(R.id.testingBtn);
        Button btn2 = (Button)findViewById(R.id.fpHomeBtn);
        Button btn3 = (Button)findViewById(R.id.recordHomeBtn);

        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getApplicationContext(), Testing_Activity.class));
            }
        });

        btn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getApplicationContext(), FingerprintingHome_Activity.class));
            }
        });

        btn3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getApplicationContext(), RadioMapHome_Activity.class));
            }
        });
    }
}
