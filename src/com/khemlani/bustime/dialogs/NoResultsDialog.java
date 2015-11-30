package com.khemlani.bustime.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.khemlani.bustime.TaskListener;

public class NoResultsDialog extends DialogFragment {

	String response;
	TaskListener listener;
	List<String> routeNames = new ArrayList<String>();
	List<String> routeLinks = new ArrayList<String>();
	boolean containsRoutes = false;
	boolean containsLocations = false;


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
		.setMessage("No matching stop or route could be found.")
		.setPositiveButton(android.R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
	}

}
