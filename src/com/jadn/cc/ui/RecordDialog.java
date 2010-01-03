package com.jadn.cc.ui; import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jadn.cc.R;

public class RecordDialog extends Dialog {

	public RecordDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("TestApp", "Dialog created");
		setContentView(R.layout.record_dialog);
	}
}