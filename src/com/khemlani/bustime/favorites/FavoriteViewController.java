package com.steelhawks.hawkscout.favorites;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.khemlani.bustime.R;
import com.khemlani.bustime.data.BusStopViewController;
import com.khemlani.bustime.data.Route;

public class FavoriteViewController extends BusStopViewController {

	private static int count = 0;

	protected boolean showAnimation = true;
	private Context c;
	private Route r;
	private LinearLayout parentView;
	private AnimationListener animListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {}

		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
		public void onAnimationEnd(Animation arg0) {
			System.out.println("Removing Item");
			favPrefs.removeFavoriteStop(preferenceString);
			isStopFavorite = !isStopFavorite;
		}
	};

	@SuppressWarnings("deprecation")
	public FavoriteViewController(Context c, Route r, String preferenceString) {
		super(c, r, preferenceString);
		this.c = c;
		this.r = r;
		LinearLayout rootView = (LinearLayout) super.getView();
		rootView.setTag(this);
		rootView.getChildAt(0).setPadding(PX(16), PX(0), PX(16), PX(16)); 
		rootView.getChildAt(0)
			.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.stop_card_background_middle_blue));
		((TextView) rootView.findViewById(R.id.direction_label)).setText(busStop.getName());
		rootView.findViewById(R.id.upcoming_buses)
			.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.stop_card_background_middle));
		((View) rootView.findViewById(R.id.favorite_button).getParent())
			.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.stop_card_background_bottom));
		rootView.addView(getRouteNameView(),0);
		
		LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lP.leftMargin = lP.rightMargin = PX(24);
		rootView.setLayoutParams(lP);
		
		parentView = new LinearLayout(c);
		parentView.addView(rootView);
		
		updateOnClickListener();
	}

	public View getView(boolean last) {
		parentView.setPadding(0, PX(16), 0, last ? PX(16) : 0);
		return parentView;
	}

	@SuppressWarnings("deprecation")
	private View getRouteNameView() {
		TextView t = new TextView(c, null, android.R.style.TextAppearance_Large);
		t.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		t.setText(r.getRouteName().trim());
		t.setTextSize(30);
		t.setTypeface(null, Typeface.BOLD);
		t.setGravity(Gravity.CENTER);
		t.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.stop_card_background_top));
		t.setTextColor(c.getResources().getColor(R.color.light_green));
		t.setPadding(0, PX(8), 0, 0);
		return t;
	}

	private void updateOnClickListener() {
		OnClickListener toggleFavoriteListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("CLICKED------------------------------");
				if (isStopFavorite) {
					if (showAnimation) collapseView();
					else animListener.onAnimationEnd(null);
				} else {
					favPrefs.addFavoriteStop(preferenceString);
					isStopFavorite = !isStopFavorite;
					defineFavoriteView();
				}
			}
		};
		System.out.println("SETTING ON CLICK LISTENER " + FavoriteViewController.count++);
		super.setOnClickListener(toggleFavoriteListener);
	}

	private void collapseView() {
		final int initialHeight = parentView.getMeasuredHeight();

		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					parentView.setVisibility(View.GONE);
				}
				else {
					parentView.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					parentView.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		anim.setAnimationListener(animListener);
		anim.setDuration(400);
		parentView.startAnimation(anim);
	}

	private int PX (int dp) {
		final float scale = c.getResources().getDisplayMetrics().density;
		int px = (int) (dp*scale+0.5f);
		return px;
	}

}
