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

        Button offlineBtn = (Button)findViewById(R.id.offlineBtn);
        Button onlineBtn  = (Button)findViewById(R.id.onlineBtn);

        Button recordBtn  = (Button)findViewById(R.id.recordBtn);
        Button tempBtn    = (Button)findViewById(R.id.tempBtn);

        Button dbOpBtn    = (Button)findViewById(R.id.dbOperationsBtn);
        Button testingBtn = (Button)findViewById(R.id.testingBtn);


        offlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getApplicationContext(), OfflinePhase_Activity.class));
                Toast.makeText(getApplicationContext(), "Currently Disabled.", Toast.LENGTH_SHORT).show();
            }
        });

        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getApplicationContext(), FingerprintingHome_Activity.class));
                Toast.makeText(getApplicationContext(), "Currently Disabled.", Toast.LENGTH_SHORT).show();
            }
        });


        recordBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), Record_Activity.class));
            }
        });

        tempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Placeholder_Activity.class));
            }
        });


        dbOpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DBOperations_Activity.class));
            }
        });

        testingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getApplicationContext(), Testing_Activity.class));
                Toast.makeText(getApplicationContext(), "Currently Disabled.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
