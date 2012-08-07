package com.jadn.cc.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import com.jadn.cc.core.Subscription;

public class ExportOpml {

	public static void export(List<Subscription> subscriptions, FileOutputStream fileOutputStream) {
		PrintWriter pw = new PrintWriter(fileOutputStream);
		pw.println("<?xml version='1.0' encoding='ISO-8859-1'?>");
		pw.println("<opml version='2.0'>");
		pw.println("<head>");
		pw.println("<title>My CarCast Subscriptions</title>");
		pw.println("</head>");
		pw.println("<body>");
		for(Subscription subscription: subscriptions){
			pw.print("<outline title=\"");
			pw.print(subscription.name.replaceAll("\"", "&quote;").replaceAll("&", "&amp;"));
			pw.print("\" type='rss' version='RSS2' xmlUrl=\"");
			pw.print(subscription.url.replaceAll("\"", "&quote;").replaceAll("&", "&amp;"));
			pw.println("\"/>");
		}
		pw.println("</body>");
		pw.println("</opml>");
		pw.close();
	}

}
