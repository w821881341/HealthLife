package com.healthslife.healthtest;

import com.healthslife.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class EcgFragment extends Fragment {
	private Button ecgTestBtn;
	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_ecg, container, false);
		ecgTestBtn = (Button) root.findViewById(R.id.ecg_test_btn);
		ecgTestBtn.setOnClickListener(mOnClickListener);
		return root;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EcgFragment.this.getActivity(),
					EcgTakePicActivity.class);
			startActivityForResult(intent, 100);
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if(20==resultCode)  
	        {  
	            String resultString=data.getExtras().getString("analysis_result");  
	            Toast.makeText(getActivity(), resultString, Toast.LENGTH_SHORT).show();;
	        }  
	        super.onActivityResult(requestCode, resultCode, data);  
	};
}
