package com.healthslife.fragments;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.google.android.gms.internal.mb;
import com.healthslife.BuildConfig;
import com.healthslife.R;
import com.healthslife.activitys.BNavigatorActivity;
import com.healthslife.pedometer.main.Database;
import com.healthslife.pedometer.main.Fragment_Settings;
import com.healthslife.pedometer.tools.ControlTools;
import com.healthslife.pedometer.util.Logger;
import com.healthslife.pedometer.util.Util;
import com.healthslife.widget.eazegraph.charts.BarChart;
import com.healthslife.widget.eazegraph.charts.PieChart;
import com.healthslife.widget.eazegraph.models.BarModel;
import com.healthslife.widget.eazegraph.models.PieModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Fragment_Overview extends Fragment implements SensorEventListener {

    private TextView stepsView, totalView, averageView;

    private PieModel sliceGoal, sliceCurrent;
    private PieChart pg;
    private ImageButton mSearchBtn;
    
    private int todayOffset, total_start, goal, since_boot, total_days;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
    private boolean showSteps = true;
    private final static String ACCESS_KEY = "	g7roxmwdB8dUQwTrdOpuLcgF";
    private boolean mIsEngineInitSuccess = false;
    private ControlTools controlTools;
    private boolean isLightOpen = false;
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        controlTools = new ControlTools();
        controlTools.init(getActivity());
        BaiduNaviManager.getInstance().initEngine(getActivity(),
				getSdcardDir(), mNaviEngineInitListener, ACCESS_KEY, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_overview, null);
        stepsView = (TextView) v.findViewById(R.id.steps);
        totalView = (TextView) v.findViewById(R.id.total);
        averageView = (TextView) v.findViewById(R.id.average);
        mSearchBtn = (ImageButton)v.findViewById(R.id.ensureSearch_button);
        
        pg = (PieChart) v.findViewById(R.id.graph);

        // slice for the steps taken today
        sliceCurrent = new PieModel("", 0, Color.parseColor("#f77171"));
        pg.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        sliceGoal = new PieModel("", Fragment_Settings.DEFAULT_GOAL, Color.parseColor("#ffa7a4"));
        pg.addPieSlice(sliceGoal);

        pg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        });

        pg.setDrawValueInPie(false);
        pg.setUsePieRotation(true);
        pg.startAnimation();
        
        
        mSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BaiduNaviManager.getInstance().launchNavigator(getActivity(),
						40.05087, 116.30142, "百度大厦", 39.90882, 116.39750,
						"北京天安门", NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
						true, // 真实导航
						BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
						new OnStartNavigationListener() { // 跳转监听

							@Override
							public void onJumpToNavigator(Bundle configParams) {
								Intent intent = new Intent(getActivity(),
										BNavigatorActivity.class);
								intent.putExtras(configParams);
								startActivity(intent);
							}

							@Override
							public void onJumpToDownloader() {
							}
						});
			}
		});
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
       

        Database db = Database.getInstance(getActivity());

     
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);

        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
//        if (!prefs.contains("pauseCount")) {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_FASTEST, 0);
//        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

        stepsDistanceChanged();
    }
    
    


    /**
     * Call this method if the Fragment should update the "steps"/"km" text in
     * the pie graph as well as the pie and the bars graphs.
     */
    private void stepsDistanceChanged() {
        if (showSteps) {
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
        } else {
            String unit =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS)
                            .getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT);
            if (unit.equals("cm")) {
                unit = "km";
            } else {
                unit = "mi";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
        }

        updatePie();
//        updateBars();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

//    @Override
//    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
//        inflater.inflate(R.menu.main, menu);
//        MenuItem pause = menu.getItem(0);
//        Drawable d;
//        if (getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS)
//                .contains("pauseCount")) { // currently paused
//            pause.setTitle(R.string.resume);
//            d = getResources().getDrawable(R.drawable.ic_resume);
//        } else {
//            pause.setTitle(R.string.pause);
//            d = getResources().getDrawable(R.drawable.ic_pause);
//        }
//        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        pause.setIcon(d);
//    }

//    @Override
//    public boolean onOptionsItemSelected(final MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_split_count:
//                Dialog_Split.getDialog(getActivity(),
//                        total_start + Math.max(todayOffset + since_boot, 0)).show();
//                return true;
//            case R.id.action_pause:
//                SensorManager sm =
//                        (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
//                Drawable d;
//                if (getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS)
//                        .contains("pauseCount")) { // currently paused -> now resumed
//                    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
//                            SensorManager.SENSOR_DELAY_UI, 0);
//                    item.setTitle(R.string.pause);
//                    d = getResources().getDrawable(R.drawable.ic_pause);
//                } else {
//                    sm.unregisterListener(this);
//                    item.setTitle(R.string.resume);
//                    d = getResources().getDrawable(R.drawable.ic_resume);
//                }
//                d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//                item.setIcon(d);
//                getActivity().startService(new Intent(getActivity(), SensorListener.class)
//                        .putExtra("action", SensorListener.ACTION_PAUSE));
//                return true;
//            default:
//                return ((Activity_Main) getActivity()).optionsItemSelected(item);
//        }
//    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // won't happen
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (BuildConfig.DEBUG)
            Logger.log("UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                    event.values[0]);
        if (event.values[0] == 0) return;
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            Database db = Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(), (int) event.values[0]);
            db.close();
        }
        since_boot = (int) event.values[0];
        Log.e("Lei","light sensor");
        if (isLightOpen){
            controlTools.lightControl(1);
            isLightOpen = false;
        }else{
            Log.e("Lei","lightControl1");
            controlTools.lightControl(1);
            isLightOpen = true;
        }
        updatePie();
    }

    /**
     * Updates the pie graph to show todays steps/distance as well as the
     * yesterday and total values. Should be called when switching from step
     * count to distance.
     */
    private void updatePie() {
        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);
        sliceCurrent.setValue(steps_today);
        if (goal - steps_today > 0) {
            // goal not reached yet
            if (pg.getData().size() == 1) {
                // can happen if the goal value was changed: old goal value was
                // reached but now there are some steps missing for the new goal
                pg.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(goal - steps_today);
        } else {
            // goal reached
            pg.clearChart();
            pg.addPieSlice(sliceCurrent);
        }
        pg.update();
        if (showSteps) {
            stepsView.setText(formatter.format(steps_today));
            totalView.setText(formatter.format(total_start + steps_today));
            averageView.setText(formatter.format((total_start + steps_today) / total_days));
        } else {
            // update only every 10 steps when displaying distance
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);
            float stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
            float distance_today = steps_today * stepsize;
            float distance_total = (total_start + steps_today) * stepsize;
            if (prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
                    .equals("cm")) {
                distance_today /= 100000;
                distance_total /= 100000;
            } else {
                distance_today /= 5280;
                distance_total /= 5280;
            }
            stepsView.setText(formatter.format(distance_today));
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
    }

    /**
     * Updates the bar graph to show the steps/distance of the last week. Should
     * be called when switching from step count to distance.
     */
//    private void updateBars() {
//        Database db = Database.getInstance(getActivity());
//        Calendar yesterday = Calendar.getInstance();
//        yesterday.setTimeInMillis(Util.getToday());
//        yesterday.add(Calendar.DAY_OF_YEAR, -1);
//        BarChart barChart = (BarChart) getView().findViewById(R.id.bargraph);
//        if (barChart.getData().size() > 0) barChart.clearChart();
//        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
//        yesterday.add(Calendar.DAY_OF_YEAR, -6);
//        int steps;
//        float distance, stepsize = Fragment_Settings.DEFAULT_STEP_SIZE;
//        boolean stepsize_cm = true;
//        if (!showSteps) {
//            // load some more settings if distance is needed
//            SharedPreferences prefs =
//                    getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);
//            stepsize = prefs.getFloat("stepsize_value", Fragment_Settings.DEFAULT_STEP_SIZE);
//            stepsize_cm = prefs.getString("stepsize_unit", Fragment_Settings.DEFAULT_STEP_UNIT)
//                    .equals("cm");
//        }
//        barChart.setShowDecimal(!showSteps); // show decimal in distance view only
//        BarModel bm;
//        for (int i = 0; i < 7; i++) {
//            steps = db.getSteps(yesterday.getTimeInMillis());
//            if (steps > 0) {
//                bm = new BarModel(df.format(new Date(yesterday.getTimeInMillis())), 0,
//                        steps > goal ? Color.parseColor("#99CC00") : Color.parseColor("#0099cc"));
//                if (showSteps) {
//                    bm.setValue(steps);
//                } else {
//                    distance = steps * stepsize;
//                    if (stepsize_cm) {
//                        distance /= 100000;
//                    } else {
//                        distance /= 5280;
//                    }
//                    distance = Math.round(distance * 1000) / 1000f; // 3 decimals
//                    bm.setValue(distance);
//                }
//                barChart.addBar(bm);
//            }
//            yesterday.add(Calendar.DAY_OF_YEAR, 1);
//        }
//        if (barChart.getData().size() > 0) {
//            barChart.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Dialog_Statistics.getDialog(getActivity(), since_boot).show();
//                }
//            });
//            barChart.startAnimation();
//        } else {
//            barChart.setVisibility(View.GONE);
//        }
//        db.close();
//    }
    
	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			// 导航初始化是异步的，需要一小段时间，以这个标志来识别引擎是否初始化成功，为true时候才能发起导航
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

}
