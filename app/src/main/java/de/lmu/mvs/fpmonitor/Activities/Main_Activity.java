package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.lmu.mvs.fpmonitor.R;

/**
 * The Main Activity.
 * For navigating through the App.
 */
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
        Button btn4 = (Button)findViewById(R.id.toComeBtn);
        Button btn5 = (Button)findViewById(R.id.dbOperations);

        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //startActivity(new Intent(getApplicationContext(), Testing_Activity.class));
                Toast.makeText(getApplicationContext(),"Currently Disabled.",Toast.LENGTH_SHORT).show();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //startActivity(new Intent(getApplicationContext(), FingerprintingHome_Activity.class));
                Toast.makeText(getApplicationContext(),"Currently Disabled.",Toast.LENGTH_SHORT).show();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getApplicationContext(), OfflinePhase_Activity.class));
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Placeholder_Activity.class));
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DBOperations_Activity.class));
            }
        });
    }
}
