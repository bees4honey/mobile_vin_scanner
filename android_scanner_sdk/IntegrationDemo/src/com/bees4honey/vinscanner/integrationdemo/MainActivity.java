package com.bees4honey.vinscanner.integrationdemo;

import android.app.Activity;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        Button scan = (Button)findViewById(R.id.btn_scan);

        EditText editText = (EditText) findViewById(R.id.et_scanned);
        Uri uri = getIntent().getData();
        if (uri != null) {
            String str = uri.getHost();
            editText.setText(str);
        }

        scan.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Uri uri = Uri.parse("b4hvinscan://scan?caller_name=FAST%20VIN&callback_url=b4hvindemo%3A%2F%2FB4HVINCODE");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
