package com.jadn.cc.services;

import com.jadn.cc.trace.TraceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ItunesSearchHelper extends SearchHelper {

	public ItunesSearchHelper(String search) {
        super(search);
	}

	@Override
	public void run() {
		try {
			URL url = new URL("https://itunes.apple.com/search?media=podcast&limit=50&term="
					+ URLEncoder.encode(search));

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.connect();
			if (con.getResponseCode() != 200) {
				done = true;
				return;
			}
			StringBuilder sb = new StringBuilder();
			InputStream is = con.getInputStream();
			byte[] buf = new byte[2048];
			int amt = 0;
			while ((amt = is.read(buf)) > 0) {
				sb.append(new String(buf, 0, amt));
			}
			is.close();

            StringBuilder res = new StringBuilder();
            JSONObject jObject = new JSONObject(sb.toString());
            JSONArray results =  jObject.getJSONArray("results");
            System.out.println(results.get(0));
            for(int i=0;i<results.length();i++){
                JSONObject result = (JSONObject) results.get(i);
                res.append(result.get("trackName"));
                res.append("=");
                res.append(result.get("feedUrl"));
                res.append("\n");
            }

			this.results = res.toString();
		} catch (Throwable e) {
			TraceUtil.report(e);
			//e.printStackTrace();
		} finally {		
			done = true;
		}
	}

}
