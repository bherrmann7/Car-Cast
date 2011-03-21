package com.jadn.cc.ui;
 
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
 
/**
 * A preference type that allows a user to choose a time
 * 
 * Based loosely on http://www.ebessette.com/d/TimePickerPreference
 */
public class TimePickerPreference extends DialogPreference implements TimePicker.OnTimeChangedListener {
 
	private int mHour, mMinute;
 
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
 
	public TimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
 
	private void initialize() {
		setPersistent(true);
		mHour = 2;
		mMinute = 0;
	}
 
	@Override
	protected View onCreateDialogView() {
 
		TimePicker tp = new TimePicker(getContext());
		tp.setOnTimeChangedListener(this);

		String time = getPersistedString(mHour + ":" + mMinute);
		tp.setCurrentHour(Integer.valueOf(time.split(":")[0]));
		tp.setCurrentMinute(Integer.valueOf(time.split(":")[1]));
 
		return tp;
	}
 
	@Override
	public void onTimeChanged(TimePicker view, int hour, int minute) {
 
		mHour = hour;
		mMinute = minute;
	}
	
	@Override
	public void onDialogClosed(boolean positiveResult) {
		
		if (positiveResult && isPersistent()) {
			persistString(mHour + ":" + mMinute);
		}
	}
}