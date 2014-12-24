package jp.sakuramochi702.colorrecognize.common;

import jp.sakuramochi702.colorrecognize.PlayActivity;
import jp.sakuramochi702.colorrecognize.R;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 
 * @author sakuramochi702
 *
 */
public class EachResultDialogFragment extends DialogFragment {
	
	private boolean isCorrect = false;
	private int score = 0;
	private int totalScore = 0;
	
	/**
	 * setters
	 */
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	
	private TextView tvResSimbol;
	private TextView tvScore;
	private TextView tvTotal;

	/**
	 * ����
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(getActivity());        
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		dialog.setContentView(R.layout.dialog_each_result);       
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		//�R���|�[�l���g�擾
		tvResSimbol = (TextView)(dialog.findViewById(R.id.tvResSimbol));
		tvScore = (TextView)(dialog.findViewById(R.id.tvEachScore));
		tvTotal = (TextView)(dialog.findViewById(R.id.tvTotal));
		
		//�R���|�[�l���g�ɒl���Z�b�g
		if (isCorrect) {
			tvResSimbol.setText("��");
			//tvResSimbol.setTextColor(Color.BLUE);
		} else {
			tvResSimbol.setText("�~");
			//tvResSimbol.setTextColor(Color.RED);
		}
		tvScore.setText(String.valueOf(score));
		tvTotal.setText(String.valueOf(totalScore));
		
		/*
		 * ���փ{�^������������
		 */
		dialog.findViewById(R.id.btnNext).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayActivity callingActivity = (PlayActivity) getActivity();
				callingActivity.onReturnNext(isCorrect, totalScore);
				dismiss(); 
			}
		});
		
		/*
		 * ���^�C�A�{�^������������
		 */
		/*dialog.findViewById(R.id.btnRetire).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayActivity callingActivity = (PlayActivity) getActivity();
				callingActivity.onReturnRetire();
				dismiss(); 
			}
		})*/;
		
		return dialog;
	}
	
	/**
	 * �T�C�Y�ύX�͂����ōs��
	 */
	@Override  
    public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState);  
          
        Dialog dialog = getDialog();  
          
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
          
        DisplayMetrics metrics = getResources().getDisplayMetrics();  
        int dialogWidth = (int) (metrics.widthPixels * 0.8);  
        int dialogHeight = (int) (metrics.heightPixels * 0.8);  
          
        lp.width = dialogWidth;  
        lp.height = dialogHeight;  
        dialog.getWindow().setAttributes(lp);  
    }  
}
