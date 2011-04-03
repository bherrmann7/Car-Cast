package com.jadn.cc.ui;

import android.app.Activity;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.ContentServiceListener;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.services.ContentService;
import com.jadn.cc.services.PlayStatusListener;
import java.util.List;


public abstract class BaseActivity extends Activity implements ContentServiceListener, PlayStatusListener {
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
		if (contentService != null) {
			contentService.setPlayStatusListener(null);
		}
	    contentService = service;
	    if (service != null) {
	    	service.setPlayStatusListener(this);
            onContentService();
        }
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    getCarCastApplication().setContentServiceListener(this);
	}

    protected CarCastApplication getCarCastApplication() {
        return ((CarCastApplication)getApplication());
    }

	@Override
	public void playStateUpdated(boolean playing) {
		// default implementation does nothing
	}
}
