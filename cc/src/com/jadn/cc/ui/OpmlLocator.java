package com.jadn.cc.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.Subscription;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpmlLocator extends BaseActivity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opml_locator);

        final EditText pathEditText = (EditText) findViewById(R.id.path);
        final Button button = (Button) findViewById(R.id.import_oiml_button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                pathEditText.setEnabled(false);
                pathEditText.setInputType(InputType.TYPE_NULL);

                try {
                    String text = pathEditText.getText().toString();
                    if (!text.startsWith("/")){
                        if (!text.startsWith("http://") ||
                                !text.startsWith("https://") ){
                            text = "http://"+text;
                            pathEditText.setText(text);
                        }
                    } else {
                        File file = new File(text);
                        if (!file.exists()) {
                            sorry("That file does not exist.");
                        } else if (!file.canRead()) {
                            sorry("That file cannot be read.");
                        }else{
                            Intent intent = new Intent(getApplicationContext(), OpmlImport.class);
                            intent.setData(Uri.fromFile(file));
                            startActivity(intent);
                        }
                    }
                } catch(Throwable t){
                    sorry(t.getMessage());
                }

            }
        });
    }

    private void sorry(String message) {
        final EditText pathEditText = (EditText) findViewById(R.id.path);
        final Button button = (Button) findViewById(R.id.import_oiml_button);

        button.setEnabled(true);
        pathEditText.setEnabled(true);
        pathEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        Toast.makeText(getApplicationContext(), "SORRY!\n\n"+message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void run() {
        final EditText pathEditText = (EditText) findViewById(R.id.path);
        String text = pathEditText.getText().toString();

        try {

            Intent intent = new Intent(getApplicationContext(), OpmlImport.class);
            intent.setData(Uri.parse(text));
            startActivity(intent);

        } catch(Throwable t){
            // on UI thread?
            sorry(t.getMessage());
        }
    }
}
