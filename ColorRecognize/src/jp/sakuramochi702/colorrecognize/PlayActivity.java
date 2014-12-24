package jp.sakuramochi702.colorrecognize;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.sakuramochi702.colorrecognize.common.EachResultDialogFragment;
import jp.sakuramochi702.colorrecognize.common.ShapeArray;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends ActionBarActivity {
	
	private int correctIdx = 0;
	private int selectIdx = 0;
	private boolean isHardMode = false; 
	
	private TextView tvCountDown;
	private TextView tvTimeLimit;
	
	private ImageView ivQ1;
	private ImageView ivQ2;
	
	private Timer timer1;
	private Timer timer2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //�^�C�g���o�[������
		setContentView(R.layout.activity_play);
		
		
		Intent i = this.getIntent();
		isHardMode = i.getBooleanExtra("HARD_MODE_FLG", false);
		
		//��萶��
		ivQ1 = (ImageView)this.findViewById(R.id.imgQ1);
		ivQ2 = (ImageView)this.findViewById(R.id.imgQ2);
		prepareQuestion();
		
		//�J�E���g�_�E��
		tvCountDown = (TextView) findViewById(R.id.tvCountDown);
		tvCountDown.setText("4");
		//View����}�[�W�����擾
		//MarginLayoutParams lp = (MarginLayoutParams)tvCountDown.getLayoutParams();
		//�ړ��������������ɕύX
		//RelativeLayout rl = (RelativeLayout) findViewById(R.id.layoutQ);
		//lp.topMargin = - (rl.getHeight() / 2) - 24;
		//lp.leftMargin = -270;
		//tvCountDown.setLayoutParams(lp);
		tvCountDown.setTextSize(60);
		doCountDown();
	}

	/**
	 * ���̐���
	 */
	private void prepareQuestion() {
		//�F�ƌ`�������_����2�g����
		int colorQ1 = (int)(Math.random()*5);
		int colorQ2;
		while (true) {
			colorQ2 = (int)(Math.random()*5);
			if (colorQ1 != colorQ2) {
				break;
			}
		}
		
		int shapeQ1 = (int)(Math.random()*5);
		int shapeQ2;
		while (true) {
			shapeQ2 = (int)(Math.random()*5);
			if (shapeQ1 != shapeQ2) {
				break;
			}
		}
		//���ɃZ�b�g
		ImageView iv1 = (ImageView)this.findViewById(R.id.imgQ1);
		iv1.setImageResource(ShapeArray.getRId(colorQ1, shapeQ1));
		ImageView iv2 = (ImageView)this.findViewById(R.id.imgQ2);
		iv2.setImageResource(ShapeArray.getRId(colorQ2, shapeQ2));
		
		//���̓J�E���g�_�E����܂Ō����Ȃ����Ă���
		ivQ1.setVisibility(View.INVISIBLE);
		ivQ2.setVisibility(View.INVISIBLE);
		
		//�I��������
		List<Integer> restColor = new ArrayList<Integer>();
		restColor.add(ShapeArray.RED);
		restColor.add(ShapeArray.BLUE);
		restColor.add(ShapeArray.GREEN);
		restColor.add(ShapeArray.YELLOW);
		restColor.add(ShapeArray.PURPLE);
		
		List<Integer> restShape = new ArrayList<Integer>();
		restShape.add(ShapeArray.CIRCLE);
		restShape.add(ShapeArray.MOON);
		restShape.add(ShapeArray.STAR);
		restShape.add(ShapeArray.FLOWER);
		restShape.add(ShapeArray.HEART);
		
		List<Integer> restIndex = new ArrayList<Integer>();
		restIndex.add(1);
		restIndex.add(2);
		restIndex.add(3);
		restIndex.add(4);
		restIndex.add(5);
		
		//���[�h����
		boolean isHardFlg = false;
		if (isHardMode) {
			if ((int)(Math.random()*3) % 3 == 0) {
				isHardFlg = true;
			}
		}
		
		//�����𐶐�
		int corrColor;
		int corrShape;
		if (isHardFlg) {
			//�n�[�h�F�e�[�}�Ɠ����}�`������
			if ((int)(Math.random()*2) == 0) {
				corrColor = colorQ1;
				corrShape = shapeQ1;
			} else {
				corrColor = colorQ2;
				corrShape = shapeQ2;
			}
		} else {
			//�ʏ�F�e�[�}�ƈقȂ�}�`������
			while (true) {
				corrColor = (int)(Math.random()*5);
				if ((corrColor != colorQ1) && (corrColor != colorQ2)) {
					break;
				}
			}
			while (true) {
				corrShape = (int)(Math.random()*5);
				if ((corrShape != shapeQ1) && (corrShape != shapeQ2)) {
					break;
				}
			}
		}
		
		correctIdx = (int)(Math.random()*5) + 1;
		ImageView ivCorr = getImageView(correctIdx);
		ivCorr.setImageResource(ShapeArray.getRId(corrColor, corrShape));
		restColor.remove(Integer.valueOf(corrColor));
		restShape.remove(Integer.valueOf(corrShape));
		restIndex.remove(Integer.valueOf(correctIdx));
		
		//�c��𐶐�
		if (isHardFlg) {
			if (corrColor == colorQ1) {
				//color2�ƈ�v�ishape1,2�Ƃ͈�v���Ȃ��j
				setRestSelectionForColor(colorQ2, restColor, restShape, restIndex, shapeQ1, shapeQ2);
				//shape2�ƈ�v�icolor1,2�Ƃ͈�v���Ȃ��j
				setRestSelectionForShape(shapeQ2, restColor, restShape, restIndex, colorQ1, colorQ2);
			} else if (corrColor == colorQ2) {
				//color1�ƈ�v�ishape1,2�Ƃ͈�v���Ȃ��j
				setRestSelectionForColor(colorQ1, restColor, restShape, restIndex, shapeQ1, shapeQ2);
				//shape1�ƈ�v�icolor1,2�Ƃ͈�v���Ȃ��j
				setRestSelectionForShape(shapeQ1, restColor, restShape, restIndex, colorQ1, colorQ2);
			}
			setRestSelection(restColor, restShape, restIndex);
			setRestSelection(restColor, restShape, restIndex);
		} else {
			//color1�ƈ�v�ishape1,2�Ƃ͈�v���Ȃ��j
			setRestSelectionForColor(colorQ1, restColor, restShape, restIndex, shapeQ1, shapeQ2);
			//color2�ƈ�v�ishape1,2�Ƃ͈�v���Ȃ��j
			setRestSelectionForColor(colorQ2, restColor, restShape, restIndex, shapeQ1, shapeQ2);
			//shape1�ƈ�v�icolor1,2�Ƃ͈�v���Ȃ��j
			setRestSelectionForShape(shapeQ1, restColor, restShape, restIndex, colorQ1, colorQ2);
			//shape2�ƈ�v�icolor1,2�Ƃ͈�v���Ȃ��j
			setRestSelectionForShape(shapeQ2, restColor, restShape, restIndex, colorQ1, colorQ2);
		}
		
		//������
		selectIdx = 0;
	}
	
	private ImageView getImageView(int idx) {
		switch (idx) {
		case 1:
			return (ImageView)this.findViewById(R.id.imgA1);
		case 2:
			return (ImageView)this.findViewById(R.id.imgA2);
		case 3:
			return (ImageView)this.findViewById(R.id.imgA3);
		case 4:
			return (ImageView)this.findViewById(R.id.imgA4);
		case 5:
			return (ImageView)this.findViewById(R.id.imgA5);
		}
		return null;
	}
	
	/**
	 * �F���w��
	 */
	private void setRestSelectionForColor(int color, List<Integer> restColor, 
			List<Integer> restShape, List<Integer> restIndex, int shape1, int shape2) {
		int tmpColor = color;
		int tmpShape;
		while (true) {
			tmpShape = restShape.get((int)(Math.random()*restShape.size()));
			if ((tmpShape != shape1) && (tmpShape != shape2)) {
				break;
			} 
		}
		int tmpIndex = restIndex.get((int)(Math.random()*restIndex.size()));
		ImageView tmpIv = getImageView(tmpIndex);
		tmpIv.setImageResource(ShapeArray.getRId(tmpColor, tmpShape));
		restColor.remove(Integer.valueOf(tmpColor));
		restShape.remove(Integer.valueOf(tmpShape));
		restIndex.remove(Integer.valueOf(tmpIndex));
	}
	
	/**
	 * �`���w��
	 */
	private void setRestSelectionForShape(int shape, List<Integer> restColor, 
			List<Integer> restShape, List<Integer> restIndex, int color1, int color2) {
		int tmpShape = shape;
		int tmpColor;
		while (true) {
			tmpColor = restColor.get((int)(Math.random()*restColor.size()));
			if ((tmpColor != color1) && (tmpColor != color2)) {
				break;
			} 
		}
		int tmpIndex = restIndex.get((int)(Math.random()*restIndex.size()));
		ImageView tmpIv = getImageView(tmpIndex);
		tmpIv.setImageResource(ShapeArray.getRId(tmpColor, tmpShape));
		restColor.remove(Integer.valueOf(tmpColor));
		restShape.remove(Integer.valueOf(tmpShape));
		restIndex.remove(Integer.valueOf(tmpIndex));
	}
	
	/**
	 * �w��Ȃ�
	 */
	private void setRestSelection(List<Integer> restColor, 
			List<Integer> restShape, List<Integer> restIndex) {
		int tmpColor = restColor.get((int)(Math.random()*restColor.size()));
		int tmpShape = restShape.get((int)(Math.random()*restShape.size()));
		int tmpIndex = restIndex.get((int)(Math.random()*restIndex.size()));
		ImageView tmpIv = getImageView(tmpIndex);
		tmpIv.setImageResource(ShapeArray.getRId(tmpColor, tmpShape));
		restColor.remove(Integer.valueOf(tmpColor));
		restShape.remove(Integer.valueOf(tmpShape));
		restIndex.remove(Integer.valueOf(tmpIndex));
	}
	
	/**
	 * ���J�n�܂ł̃J�E���g�_�E��
	 */
	private void doCountDown() {
		timer1 = new Timer(true);
	    final android.os.Handler handler = new android.os.Handler();
	    timer1.schedule(
	    	new TimerTask() {
	    		@Override
	    		public void run() {
	    			handler.post( new Runnable(){
	    				public void run(){
	    					int cnt = Integer.valueOf(tvCountDown.getText().toString());
	    					//TextView�̕\����؂�ւ���
	    					cnt--;
	    					tvCountDown.setText(String.valueOf(cnt));
	    					if (cnt == 0) {
	    						//����������悤�ɂ���Timer�̃J�E���g�_�E�����J�n
	    						tvCountDown.setVisibility(View.INVISIBLE);
	    						ivQ1.setVisibility(View.VISIBLE);
	    						ivQ2.setVisibility(View.VISIBLE);
	    						if (timer1 != null) {
	    							timer1.cancel();
	    							timer1 = null;
	    			            }
	    						tvTimeLimit = (TextView) findViewById(R.id.tvTimeLimit);
	    						tvTimeLimit.setText("3000");
	    						startCountDown();
	    					}
	    				}
	    			});
	    		}
	    	}
	    	, 0, 1000   //�J�n�x��(���~���b��ɊJ�n���邩)�ƁA����(���~���b���ƂɎ��s���邩)
	    );
	}
	
	/**
	 * �^�C�����~�b�g�܂ł̃J�E���g�_�E��
	 */
	private void startCountDown() {
		timer2 = new Timer(true);
	    final android.os.Handler handler = new android.os.Handler();
	    timer2.schedule(
	    	new TimerTask() {
	    		@Override
	    		public void run() {
	    			handler.post( new Runnable(){
	    				public void run(){
	    					int remain = Integer.valueOf(tvTimeLimit.getText().toString());
	    					if (remain <= 0) {
	    						return;
	    					}
	    					//TextView�̕\����؂�ւ���
	    					remain = remain - 10;
	    					String remStr = null;
	    					if (remain < 10) {
	    						remStr = "000" + String.valueOf(remain);
	    					} else if (remain < 100) {
	    						remStr = "00" + String.valueOf(remain);
	    					} else if (remain < 1000) {
	    						remStr = "0" + String.valueOf(remain);
	    					} else {
	    						remStr = String.valueOf(remain);
	    					}
	    					tvTimeLimit.setText(remStr);
	    					if (remain == 0) {
	    						//���Ԑ؂�
	    						if (timer2 != null) {
	    							timer2.cancel();
	    							timer2 = null;
	    			            }
	    						//���Ԑ؂�͕s��������
	    						divergeByCorrectOrNot(-1);
	    					}
	    				}
	    			});
	    		}
	    	}
	    	, 0, 10   //�J�n�x��(���~���b��ɊJ�n���邩)�ƁA����(���~���b���ƂɎ��s���邩)
	    );
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * A1�I��
	 */
	public void onClickA1(View v) {
		divergeByCorrectOrNot(1);
	}
	
	/**
	 * A2�I��
	 */
	public void onClickA2(View v) {
		divergeByCorrectOrNot(2);
	}
	
	/**
	 * A3�I��
	 */
	public void onClickA3(View v) {
		divergeByCorrectOrNot(3);
	}
	
	/**
	 * A4�I��
	 */
	public void onClickA4(View v) {
		divergeByCorrectOrNot(4);
	}
	
	/**
	 * A5�I��
	 */
	public void onClickA5(View v) {
		divergeByCorrectOrNot(5);
	}
	
	/**
	 * �I�������}�`���������ǂ����Ɋ�Â��ĕ���
	 */
	private void divergeByCorrectOrNot(int selectted) {
		//�܂���肪�J�n���Ă��Ȃ����͉������Ȃ�
		if (tvCountDown.getVisibility() == View.VISIBLE) {
			return;
		}
		
		//�^�C�}�[�X�g�b�v
		if (timer2 != null) {
			timer2.cancel();
			timer2 = null;
        }
		
		//field��selectIdx���Z�b�g(��x����)
		if (selectIdx == 0) {
			selectIdx = selectted;
		}
		if (isLastAnswer()) {
			//�Ō�̉񓚂̏ꍇ�͍ŏI���ʉ�ʂ�
			Intent i = new Intent(this, jp.sakuramochi702.colorrecognize.ResultActivity.class);
			if (selectIdx == correctIdx) {
				//����̓��_
				int score = Integer.valueOf(tvTimeLimit.getText().toString());
				//�݌v���_
				TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
				int tscore = Integer.valueOf(tvTS.getText().toString()) + score;
				//�c�~�X�������Z
				TextView tvMiss = (TextView) findViewById(R.id.tvMiss);
				int miss = Integer.valueOf(tvMiss.getText().toString().substring(0, 1));
				tscore += (3 - miss) * 2000;
				//�n�[�h���[�h�̏ꍇ�͓��_2�{
				if (isHardMode) {
					tscore = tscore*2;
				}
				
				i.putExtra("TOTAL_SCORE", tscore);
			} else {
				TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
				int tscore = Integer.valueOf(tvTS.getText().toString());
				i.putExtra("TOTAL_SCORE", tscore);
			}
			startActivity(i);
			return;
		}
		
		if (selectIdx == correctIdx) {
			//����
			EachResultDialogFragment erdf = new EachResultDialogFragment();
			erdf.setCorrect(true);
			//����̓��_
			int score = Integer.valueOf(tvTimeLimit.getText().toString());
			erdf.setScore(score);
			//�݌v���_
			TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
			int tscore = Integer.valueOf(tvTS.getText().toString()) + score;
			erdf.setTotalScore(tscore);
			//�\��
		    erdf.show(getSupportFragmentManager(),"dialog");
		} else {
			//�s����
			EachResultDialogFragment erdf = new EachResultDialogFragment();
			erdf.setCorrect(false);
			//����̓��_
			erdf.setScore(0);
			//�݌v���_
			TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
			erdf.setTotalScore(Integer.valueOf(tvTS.getText().toString()));
			//�\��
		    erdf.show(getSupportFragmentManager(),"dialog");
		}
	}
	
	/**
	 * �Ō�̉񓚂��ǂ���
	 */
	private boolean isLastAnswer() {
		TextView tvRest = (TextView) findViewById(R.id.tvRest);
		int rest = Integer.valueOf(tvRest.getText().toString().substring(0, 2));
		if (rest >= 2) {
			return false;
		}
		
		if (selectIdx == correctIdx) {
			//�c�肪1�Ő����̏ꍇ�͍Ō�
			return true;
		} else {
			TextView tvMiss = (TextView) findViewById(R.id.tvMiss);
			int miss = Integer.valueOf(tvMiss.getText().toString().substring(0, 1));
			if (miss == 3) {
				//�c�肪1�ŁA�~�X������3�񂠂�ꍇ���Ō�
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ����Dlg���uNEXT�v�ŕ���ꂽ�ꍇ
	 */
	public void onReturnNext(boolean isCorrect, int totalScore) {
		//Score
		TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
		tvTS.setText(String.valueOf(totalScore));
		
		int miss = -1;
		if (!isCorrect) {
			//Miss
			TextView tvMiss = (TextView) findViewById(R.id.tvMiss);
			miss = Integer.valueOf(tvMiss.getText().toString().substring(0, 1)) + 1;
			if (miss <= 3) {
				tvMiss.setText(String.valueOf(miss) + "/3");
			}
		}
		
		if (isCorrect || (miss > 3)) {
			//Rest
			TextView tvRest = (TextView) findViewById(R.id.tvRest);
			int rest = Integer.valueOf(tvRest.getText().toString().substring(0, 2)) - 1;
			if (rest <= 9) {
				tvRest.setText("0" + String.valueOf(rest) + "/10");
			} else {
				tvRest.setText(String.valueOf(rest) + "/10");
			}
		}
		
		//���̖��𐶐�
		prepareQuestion();
		
		//�ēx�J�E���g�_�E��
		tvCountDown.setVisibility(View.VISIBLE);
		tvCountDown.setText("4");
		tvTimeLimit.setText("3000");
		doCountDown();
	}
	
	/**
	 * ����Dlg���uRETIRE�v�ŕ���ꂽ�ꍇ
	 */
	public void onReturnRetire() {
		//���^�C�A�̏ꍇ��TOP��ʂɖ߂�
		finish();
	}
	
	/**
	 * ���^�C�A
	 */
	public void onClickRetire(View v) {
		//���^�C�A�̏ꍇ��TOP��ʂɖ߂�
		finish();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer1 != null) {
			timer1.cancel();
			timer1 = null;
		}
		if (timer2 != null) {
			timer2.cancel();
			timer2 = null;
		}
	}

	

}
