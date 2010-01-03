package com.jadn.cc.ui; import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jadn.cc.R;

public class Delete extends BaseActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete_choices);	
		
		((Button)findViewById(R.id.deleteCurrent)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					contentService.deleteCurrentPodcast();
				} catch (RemoteException e) {
					Log.e("","",e);
				}
				finish();
			}
			
		});
		((Button)findViewById(R.id.deleteToCurrent)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					contentService.purgeToCurrent();
				} catch (RemoteException e) {
					Log.e("","",e);
				}
				finish();
			}			
		});
		((Button)findViewById(R.id.deleteAllPodcasts)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					contentService.purgeAll();
				} catch (RemoteException e) {
					Log.e("","",e);
				}
				finish();
			}
			
		});
		((Button)findViewById(R.id.eraseHistory)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					contentService.eraseHistory();
				} catch (RemoteException e) {
					Log.e("","",e);
				}
				finish();
			}
			
		});
		((Button)findViewById(R.id.deleteAllSubscriptions)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					contentService.deleteAllSubscriptions();
				} catch (RemoteException e) {
					Log.e("","",e);
				}
				finish();
			}			
		});
		((Button)findViewById(R.id.resetToDemoSubscriptions)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					contentService.resetToDemoSubscriptions();
				} catch (RemoteException e) {
					Log.e("","",e);
				}
				finish();
			}			
		});
	}

	@Override
	void onContentService() throws RemoteException {	
	}
//		contentService.deleteCurrentPodcast();
//	
//	  			if (item.getItemId() == R.id.purgeToCurrent) {
//				esay("purgeToCurrent");
//				contentService.purgeToCurrent();
//				return true;
//			}
//			if (item.getItemId() == R.id.eraseHistory) {
//				esay("eraseHistory");
//				contentService.eraseHistory();
//				return true;
//			}
}
