package com.jadn.cc.ui; import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;

public class Splash extends Activity {
	
	 @Override
     public void onCreate(Bundle icicle) {
          super.onCreate(icicle);
          setContentView(R.layout.splash);
          
          setTitle(CarCastApplication.getAppTitle()+"");
          
          ImageView imageView = (ImageView)findViewById(R.id.splashscreen);
          imageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();				
			}});
	 }

}
