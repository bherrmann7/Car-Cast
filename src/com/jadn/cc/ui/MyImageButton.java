package com.jadn.cc.ui; import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

import com.jadn.cc.R;

public class MyImageButton extends ImageButton {

	public MyImageButton(Context context) {
		super(context);		
	}

	public MyImageButton(Context context, AttributeSet as){
		super(context,as);		
	}

	public MyImageButton(Context context, AttributeSet as, int defStyle){
		super(context,as,defStyle);		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean x = super.onTouchEvent(event);
		if(event.getAction()==MotionEvent.ACTION_DOWN) {
			setImageResource(R.drawable.prevo);
		} else {
			setImageResource(R.drawable.prev);
		}
		return x;
	}
}
