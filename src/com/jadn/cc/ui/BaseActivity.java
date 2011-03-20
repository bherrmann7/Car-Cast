package com.jadn.cc.ui;

import android.app.Activity;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.ContentServiceListener;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.services.ContentService;
import java.util.List;


public abstract class BaseActivity extends Activity implements ContentServiceListener {
	ContentService contentService;

	public ContentService getContentService() {
		return contentService;
	}

	protected List<Subscription> getSubscriptions() {
		return contentService.getSubscriptions();
	}

	protected void onContentService() { // TODO rename
	    // does nothing by default
	}

	@Override
	public void onContentServiceChanged(ContentService service) {
	    contentService = service;
	    if (service != null) {
            onContentService();
        }
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    getCarCastApplication().addContentServiceListener(this);
	}

    protected CarCastApplication getCarCastApplication() {
        return ((CarCastApplication)getApplication());
    }

	@Override protected void onPause() {
	    super.onPause();
	    getCarCastApplication().removeContentServiceListner(this);
	}
}
