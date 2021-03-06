package com.healthslife.fragments;

import com.healthslife.R;
import com.healthslife.activitys.newMainActivity;
import com.healthslife.pedometer.main.Database;
import com.healthslife.pedometer.util.Util;
import com.healthslife.setting.AppSetting;
import com.healthslife.widget.CircleProgress;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class RunFragment extends Fragment {
	// 3 button at bottom
	ImageButton mImageRun;
	ImageButton mImageGolf;
	ImageButton mImagePushUp;
	// Aim
	TextView mTextViewAim;
	// Calorie already burnt;
	TextView mTextViewBurnt;
	// Circle
	CircleProgress mCircleProgress;
	// ImageView of Calorie == 1碗米饭
	ImageView mImageViewTip;
	private float aim_calories;
	private AppSetting setting;
	private int num1;
	private int num2;
	private int num3;
	// layout
	View view;

	public static RunFragment self;

	public static RunFragment getInstance() {
		if (self == null) {
			return self = new RunFragment();
		}
		return self;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		view = inflater.inflate(R.layout.fragment_run_lei, null, false);
		findView();
		onClick();
		CompleteListener();
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setting = new AppSetting(getActivity());
		int calorie = setting.getRun();
		//mTextViewAim.setText(calorie);
	 
	}

	private void onClick() {
		mImageRun.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager manager = getFragmentManager();
				manager.beginTransaction()
						.replace(R.id.main_content,
								new Fragment_Overview()
//								NewDistanceFragment.getInstance()
								).commit();
			}
		});
		mImagePushUp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager manager = getFragmentManager();
				manager.beginTransaction()
						.replace(R.id.main_content,
								PushUpFragment.getInstance()).commit();
			}
		});
	}
	@Override
	public void onResume() {
		super.onResume();
		Database db = Database.getInstance(getActivity());
		int i  = db.getCurrentSteps();
		setCircleProgress(i/1000f);
		mTextViewBurnt.setText(""+ (int)(i/1000f*196));
	}

	/**
	 * sliding percent(%) of Circle with animation
	 * 
	 * @param percent
	 */
	public void setCircleProgress(float percent) {
		mCircleProgress.slideToProgress((int) (percent * mCircleProgress
				.getMax()));
	}

	private void CompleteListener() {
		mCircleProgress
				.setCompleteListener(new CircleProgress.CompleteListener() {
					@Override
					public void complete() {

					}
				});
	}

	/**
	 * change the Image of Calorie == 1碗 米饭 1，2，3，4
	 */
	private void changeImage(int num) {
		if (mImageViewTip != null) {
			switch (num) {
			case 1:
				mImageViewTip.setImageDrawable(getResources().getDrawable(
						R.drawable.tips1));
				break;
			case 2:
				mImageViewTip.setImageDrawable(getResources().getDrawable(
						R.drawable.tips2));
				break;
			case 3:
				mImageViewTip.setImageDrawable(getResources().getDrawable(
						R.drawable.tips3));
				break;
			case 4:
				mImageViewTip.setImageDrawable(getResources().getDrawable(
						R.drawable.tips4));
				break;
			default:
				break;
			}
		}
	}

	private void findView() {
		mImageRun = (ImageButton) view.findViewById(R.id.run_button);
		mImageGolf = (ImageButton) view.findViewById(R.id.golf_button);
		mImagePushUp = (ImageButton) view.findViewById(R.id.push_up_button);
		mTextViewAim = (TextView) view.findViewById(R.id.aim_run_calorie);
		mTextViewBurnt = (TextView) view.findViewById(R.id.run_calorie_txt);
		mCircleProgress = (CircleProgress) view
				.findViewById(R.id.fragment_run_porbar);
		mImageViewTip = (ImageView) view.findViewById(R.id.image_view_tip);
		TextView t = (TextView) view.findViewById(R.id.image_today_aim);
		getMainActivity().setFONT(t, "S");
		getMainActivity().setFONT(mTextViewBurnt, "H");
		getMainActivity().setFONT(mTextViewAim, "S");
		TextView AIM = (TextView) view.findViewById(R.id.text_view_hyden);
		getMainActivity().setFONT(AIM, "S");
		getMainActivity().setFONT(mTextViewAim, "S");
	}
	
	public newMainActivity getMainActivity(){
		return (newMainActivity)getActivity();
	}
}
