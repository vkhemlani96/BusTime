package com.khemlani.bustime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

import com.khemlani.bustime.data.BusStopViewController;
import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Route;
import com.khemlani.bustime.route.RouteFragment;
import com.khemlani.bustime.route.RouteTask;
import com.steelhawks.hawkscout.favorites.Preferences;

public class RouteActivity extends FragmentActivity implements TaskListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	String routeNumber;
	MenuItem refreshItem;

	private static final String HTML_RESPONSE_KEY = "com.khemlani.bustime.RouteActivity.HTML_RESPONSE_KEY";
	private static final String HTML_ROUTE_KEY = "com.khemlani.bustime.RouteActivity.HTML_ROUTE_KEY";
	private static final String HTML_STOP_KEY = "com.khemlani.bustime.RouteActivity.HTML_STOP_KEY";

	private boolean isViewShown;
	float startY;
	float endY;
	float startX;
	float endX;
	int initialY;
	boolean stop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_route);
		isViewShown = !new Preferences(this).getViewedRoute();
		//TODO fix help view
//		if (!isViewShown) findViewById(R.id.help_view).setVisibility(View.GONE);
//		new Handler().postDelayed(new Runnable() {
//			public void run() {
//				dismissHelpView();
//			}
//		}, 5000);
		String searchQuery = getIntent().getStringExtra(HTML_RESPONSE_KEY).toUpperCase(Locale.US);
		parseHTML(searchQuery);
	}

	//	@Override
	//	public boolean dispatchTouchEvent(MotionEvent ev) {
	//		if (stop)
	//			return super.dispatchTouchEvent(ev);
	//		switch (ev.getAction()) {
	//		case MotionEvent.ACTION_DOWN:
	//			startY = ev.getY();
	//			startX = ev.getX();
	//			break;
	//		case MotionEvent.ACTION_MOVE:
	//			endY = ev.getY();
	//			endX = ev.getX();
	//			float deltaY = startY-endY;
	//			float deltaX = startX-endX;
	//			if (Math.abs(deltaX) > Math.abs(deltaY)) return true;
	//			if (deltaY < 0) return false;
	//			float newY = (mViewPager.getY() - deltaY > 0) ? (mViewPager.getY() - deltaY) : 0;
	//			stop = newY == 0;
	//			mViewPager.setY(newY);
	//			findViewById(R.id.help_view).setY(newY-PX(48));
	//			mViewPager.getLayoutParams().height = (int) (mViewPager.getLayoutParams().height + deltaY);
	//			findViewById(R.id.help_view).getLayoutParams().height = (int) (findViewById(R.id.help_view).getLayoutParams().height - deltaY);
	//			startY = endY;
	////			deltaY = startY - ev.getY();
	////			System.out.println(deltaY);
	//////			findViewById(R.id.help_view).setY(-deltaY);
	//////			findViewById(R.id.help_view).getLayoutParams().height = (int) ((int) initialHideHeight - deltaY);
	////			mViewPager.setY(initialHideHeight-deltaY);
	////			mViewPager.getLayoutParams().height += initialHideHeight-deltaY;
	//		}
	//		return false;
	//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.favorites, menu);
		refreshItem = menu.findItem(R.id.action_refresh)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						refreshItem.setVisible(false);
						setProgressBarIndeterminateVisibility(true);
						new RouteTask().newInstance(RouteActivity.this, routeNumber).execute();
						return false;
					}
				});
		return true;
	}

	@Override
	public void onTaskFinished(String response) {
		refreshItem.setVisible(true);
		setProgressBarIndeterminateVisibility(false);
		parseHTML(response);
	}

	public void onClick(View v) {
		BusStopViewController viewController = (BusStopViewController) ((View) v.getParent()).getTag();
		viewController.click();
	}

//	private void dismissHelpView() {
//		final View helpView = findViewById(R.id.help_view);
//		final int initialHeight = helpView.getMeasuredHeight();
//		
//		CountDownTimer c = new CountDownTimer(400, 4) {
//			int update = 0;
//			@Override
//			public void onTick(long millisUntilFinished) {
//				update++;
//				helpView.getLayoutParams().height = (int)(initialHeight * (millisUntilFinished/400.0));
//				helpView.requestLayout();
//			}
//			
//			@Override
//			public void onFinish() {
//				System.out.println(update);
//				helpView.setVisibility(View.GONE);
//				
//			}
//		};
////		c.start();
//		helpView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink_favorite));
//
////		Animation anim = new Animation() {
////			@Override
////			protected void applyTransformation(float interpolatedTime, Transformation t) {
////				System.out.println("Interpolated Time: " + interpolatedTime);
////				if (interpolatedTime == 1) {
////					helpView.setVisibility(View.GONE);
////				}
////				else {
////					helpView.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
////					helpView.requestLayout();
////				}
////			}
////
////			@Override
////			public boolean willChangeBounds() {
////				return true;
////			}
////		};
////		anim.setDuration(200);
////		helpView.startAnimation(anim);
//	}

	private void setupViewPager(Route route, int requestedDirectionIndex) {
		Calendar c = Calendar.getInstance();
		String refreshString = "Refreshed at " + c.get(Calendar.HOUR) + ":" + 
				(c.get(Calendar.MINUTE)<10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + 
				" " + (c.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setIcon(R.drawable.ic_logo);
		actionBar.setSubtitle(refreshString);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(), route);

		if (mViewPager == null) {
			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);

			// When swiping between different sections, select the corresponding
			// tab. We can also use ActionBar.Tab#select() to do this if we have
			// a reference to the Tab.
			mViewPager
			.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);
				}
			});

			// For each of the sections in the app, add a tab to the action bar.
			for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
				// Create a tab with text corresponding to the page title defined by
				// the adapter. Also specify this Activity object, which implements
				// the TabListener interface, as the callback (listener) for when
				// this tab is selected.
				actionBar.addTab(actionBar.newTab()
						.setText(mSectionsPagerAdapter.getPageTitle(i))
						.setTabListener(new TabListener(mViewPager)));
			}
		} else {
			mViewPager.setAdapter(null);
			mViewPager.setAdapter(mSectionsPagerAdapter);
		}
		System.out.println("Setting Adapter");

		if (requestedDirectionIndex != -1) {
			mViewPager.setCurrentItem(requestedDirectionIndex);
			//try in case there is a problem with stop key.
			try {
				((RouteFragment)((SectionsPagerAdapter) mViewPager.getAdapter()).getItem(requestedDirectionIndex))
				.setStopToClick(getIntent().getStringExtra(HTML_STOP_KEY));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void parseHTML(String response) {
		String requestedDirection = getIntent().getStringExtra(HTML_ROUTE_KEY);

		Document doc = Jsoup.parse(response);
		routeNumber = doc.title().replaceAll(".+(?=(( \\w+)$))", "").trim();
		setTitle(routeNumber);
		Elements directions = doc.getElementsByClass("direction-link");
		Elements stopsOnRoute = doc.getElementsByClass("stopsOnRoute");
		Direction[] directionsArray = new Direction[directions.size()];
		int requestedDirectionIndex = -1;
		int highestFoundCount = 0;
		for (int x=0; x<directions.size(); x++) {
			directionsArray[x] = new Direction(directions.get(x).text(), stopsOnRoute.get(x));
			if (requestedDirection != null) {
				int foundCount = compareStrings(requestedDirection, directions.get(x).text());
				System.out.println(foundCount);
				if (foundCount > highestFoundCount) requestedDirectionIndex = x;
				//TODO what to do if foundCount == highestFoundCount
			}
		}
		setupViewPager(new Route(routeNumber, directionsArray), requestedDirectionIndex);
	}

	private int compareStrings(String searchString, String controlString) {
		String[] searchArray = searchString.trim().split(" ");
		List<String> controlArray = Arrays.asList(controlString.trim().split(" "));
		int foundCount = 0;
		for (int x=0; x<searchArray.length; x++) {
			if (controlArray.contains(searchArray[x]))
				foundCount++;
		}
		return foundCount;
	}

	public static void start(Activity a, String responseString) {
		Intent i = new Intent(a.getBaseContext(), RouteActivity.class);
		i.putExtra(HTML_RESPONSE_KEY, responseString);
		a.startActivity(i);
	}

	public static void start(Activity a, String responseString, String routeName, String stopName) {
		Intent i = new Intent(a.getBaseContext(), RouteActivity.class);
		i.putExtra(HTML_RESPONSE_KEY, responseString);
		i.putExtra(HTML_ROUTE_KEY, routeName);
		i.putExtra(HTML_STOP_KEY, stopName);
		a.startActivity(i);
	}

	private int PX (int dp) {
		final float scale = this.getResources().getDisplayMetrics().density;
		int px = (int) (dp*scale+0.5f);
		return px;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		Route route;
		RouteFragment[] frags;

		public SectionsPagerAdapter(FragmentManager fm, Route r) {
			super(fm);
			this.route = r;
			frags = new RouteFragment[r.getDirections().length];
			for (int x = 0; x < frags.length; x++)
				frags[x] = new RouteFragment().newInstance(getApplicationContext(), r, x);
		}

		@Override
		public Fragment getItem(int position) {
			return frags[position];
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return route.getDirections().length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return route.getDirections()[position].getTitle();
		}
	}

}
