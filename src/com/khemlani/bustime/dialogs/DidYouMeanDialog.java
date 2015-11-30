package com.khemlani.bustime.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.khemlani.bustime.R;
import com.khemlani.bustime.TaskListener;
import com.khemlani.bustime.route.RouteTask;

public class DidYouMeanDialog extends DialogFragment implements OnItemClickListener, OnClickListener {

	String response;
	TaskListener listener;
	List<String> routeNames = new ArrayList<String>();
	List<String> routeLinks = new ArrayList<String>();
	boolean containsRoutes = false;
	boolean containsLocations = false;


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		parseHTML(response);
		LinearLayout parentLayout = 
				(LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_did_you_mean, null, false);

		ListView list = (ListView) parentLayout.findViewById(R.id.suggestion_list);
		list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item, routeNames));
		list.setOnItemClickListener(this);
		if(routeNames.size() > 6){
			View item = list.getAdapter().getView(0, null, list);
			item.measure(0, 0);         
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (6.5 * item.getMeasuredHeight()));
			list.setLayoutParams(params);
		}
		
		TextView msg = (TextView) parentLayout.findViewById(R.id.message);

		((Button) parentLayout.findViewById(R.id.cancel)).setOnClickListener(this);
		if (containsRoutes && !containsLocations)
			msg.setText("Select a route:");
		else if (containsLocations && !containsRoutes)
			msg.setText("Select a bus stop:");
		else msg.setText("<b>Did you mean:");

		return new AlertDialog.Builder(getActivity())
		.setView(parentLayout)
		.show();
	}

	public DidYouMeanDialog newInstance(TaskListener listener, String response) {
		this.listener = listener;
		this.response = response;
		return this;
	}

	private void parseHTML(String html) {
		Document doc = Jsoup.parse(html);
		Element routeList = doc.getElementsByClass("routeList").size() > 0 ? doc.getElementsByClass("routeList").get(0) : null;
		Element ambigLocations = doc.getElementsByClass("ambiguousLocations").size() > 0 ? doc.getElementsByClass("ambiguousLocations").get(0) : null;
		if (containsRoutes = (routeList != null)) {
			Elements routes = routeList.getElementsByTag("a");
			for (Element route : routes) {
				if (route.hasAttr("href")) {
					routeNames.add(route.text());
					routeLinks.add(route.attr("href").replaceAll("(/m/index;)(\\S)*(q=)", ""));
				}
			}
		}
		if (containsLocations = (ambigLocations != null)) {
			Elements locations = ambigLocations.getElementsByTag("p");
			for (Element location : locations) {
				Element aTag = location.getElementsByTag("a").size() > 0 ? location.getElementsByTag("a").get(0) : null;
				if (location.toString().contains("href") && aTag != null) {
					routeNames.add(location.text());
					System.out.println(aTag.attr("href").replaceAll("(/m/index;)(\\S)*(q=)", ""));
					routeLinks.add(aTag.attr("href").replaceAll("(/m/index;)(\\S)*(q=)", ""));
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		new RouteTask().newInstance(listener, routeLinks.get(arg2)).execute();
		dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		listener.onTaskFinished("");
	}

	@Override
	public void onClick(View v) {
		dismiss();
		listener.onTaskFinished("");
	}

}
