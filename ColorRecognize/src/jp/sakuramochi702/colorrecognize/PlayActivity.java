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
		requestWindowFeature(Window.FEATURE_NO_TITLE); //タイトルバーを消す
		setContentView(R.layout.activity_play);
		
		
		Intent i = this.getIntent();
		isHardMode = i.getBooleanExtra("HARD_MODE_FLG", false);
		
		//問題生成
		ivQ1 = (ImageView)this.findViewById(R.id.imgQ1);
		ivQ2 = (ImageView)this.findViewById(R.id.imgQ2);
		prepareQuestion();
		
		//カウントダウン
		tvCountDown = (TextView) findViewById(R.id.tvCountDown);
		tvCountDown.setText("4");
		//Viewからマージンを取得
		//MarginLayoutParams lp = (MarginLayoutParams)tvCountDown.getLayoutParams();
		//移動させたい距離に変更
		//RelativeLayout rl = (RelativeLayout) findViewById(R.id.layoutQ);
		//lp.topMargin = - (rl.getHeight() / 2) - 24;
		//lp.leftMargin = -270;
		//tvCountDown.setLayoutParams(lp);
		tvCountDown.setTextSize(60);
		doCountDown();
	}

	/**
	 * 問題の生成
	 */
	private void prepareQuestion() {
		//色と形をランダムで2組生成
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
		//問題にセット
		ImageView iv1 = (ImageView)this.findViewById(R.id.imgQ1);
		iv1.setImageResource(ShapeArray.getRId(colorQ1, shapeQ1));
		ImageView iv2 = (ImageView)this.findViewById(R.id.imgQ2);
		iv2.setImageResource(ShapeArray.getRId(colorQ2, shapeQ2));
		
		//問題はカウントダウン後まで見えなくしておく
		ivQ1.setVisibility(View.INVISIBLE);
		ivQ2.setVisibility(View.INVISIBLE);
		
		//選択肢生成
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
		
		//モード判定
		boolean isHardFlg = false;
		if (isHardMode) {
			if ((int)(Math.random()*3) % 3 == 0) {
				isHardFlg = true;
			}
		}
		
		//正解を生成
		int corrColor;
		int corrShape;
		if (isHardFlg) {
			//ハード：テーマと同じ図形が正解
			if ((int)(Math.random()*2) == 0) {
				corrColor = colorQ1;
				corrShape = shapeQ1;
			} else {
				corrColor = colorQ2;
				corrShape = shapeQ2;
			}
		} else {
			//通常：テーマと異なる図形が正解
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
		
		//残りを生成
		if (isHardFlg) {
			if (corrColor == colorQ1) {
				//color2と一致（shape1,2とは一致しない）
				setRestSelectionForColor(colorQ2, restColor, restShape, restIndex, shapeQ1, shapeQ2);
				//shape2と一致（color1,2とは一致しない）
				setRestSelectionForShape(shapeQ2, restColor, restShape, restIndex, colorQ1, colorQ2);
			} else if (corrColor == colorQ2) {
				//color1と一致（shape1,2とは一致しない）
				setRestSelectionForColor(colorQ1, restColor, restShape, restIndex, shapeQ1, shapeQ2);
				//shape1と一致（color1,2とは一致しない）
				setRestSelectionForShape(shapeQ1, restColor, restShape, restIndex, colorQ1, colorQ2);
			}
			setRestSelection(restColor, restShape, restIndex);
			setRestSelection(restColor, restShape, restIndex);
		} else {
			//color1と一致（shape1,2とは一致しない）
			setRestSelectionForColor(colorQ1, restColor, restShape, restIndex, shapeQ1, shapeQ2);
			//color2と一致（shape1,2とは一致しない）
			setRestSelectionForColor(colorQ2, restColor, restShape, restIndex, shapeQ1, shapeQ2);
			//shape1と一致（color1,2とは一致しない）
			setRestSelectionForShape(shapeQ1, restColor, restShape, restIndex, colorQ1, colorQ2);
			//shape2と一致（color1,2とは一致しない）
			setRestSelectionForShape(shapeQ2, restColor, restShape, restIndex, colorQ1, colorQ2);
		}
		
		//初期化
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
	 * 色を指定
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
	 * 形を指定
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
	 * 指定なし
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
	 * 問題開始までのカウントダウン
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
	    					//TextViewの表示を切り替える
	    					cnt--;
	    					tvCountDown.setText(String.valueOf(cnt));
	    					if (cnt == 0) {
	    						//問題を見えるようにしてTimerのカウントダウンを開始
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
	    	, 0, 1000   //開始遅延(何ミリ秒後に開始するか)と、周期(何ミリ秒ごとに実行するか)
	    );
	}
	
	/**
	 * タイムリミットまでのカウントダウン
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
	    					//TextViewの表示を切り替える
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
	    						//時間切れ
	    						if (timer2 != null) {
	    							timer2.cancel();
	    							timer2 = null;
	    			            }
	    						//時間切れは不正解扱い
	    						divergeByCorrectOrNot(-1);
	    					}
	    				}
	    			});
	    		}
	    	}
	    	, 0, 10   //開始遅延(何ミリ秒後に開始するか)と、周期(何ミリ秒ごとに実行するか)
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
	 * A1選択
	 */
	public void onClickA1(View v) {
		divergeByCorrectOrNot(1);
	}
	
	/**
	 * A2選択
	 */
	public void onClickA2(View v) {
		divergeByCorrectOrNot(2);
	}
	
	/**
	 * A3選択
	 */
	public void onClickA3(View v) {
		divergeByCorrectOrNot(3);
	}
	
	/**
	 * A4選択
	 */
	public void onClickA4(View v) {
		divergeByCorrectOrNot(4);
	}
	
	/**
	 * A5選択
	 */
	public void onClickA5(View v) {
		divergeByCorrectOrNot(5);
	}
	
	/**
	 * 選択した図形が正解かどうかに基づいて分岐
	 */
	private void divergeByCorrectOrNot(int selectted) {
		//まだ問題が開始していない内は何もしない
		if (tvCountDown.getVisibility() == View.VISIBLE) {
			return;
		}
		
		//タイマーストップ
		if (timer2 != null) {
			timer2.cancel();
			timer2 = null;
        }
		
		//fieldのselectIdxをセット(一度だけ)
		if (selectIdx == 0) {
			selectIdx = selectted;
		}
		if (isLastAnswer()) {
			//最後の回答の場合は最終結果画面へ
			Intent i = new Intent(this, jp.sakuramochi702.colorrecognize.ResultActivity.class);
			if (selectIdx == correctIdx) {
				//今回の得点
				int score = Integer.valueOf(tvTimeLimit.getText().toString());
				//累計得点
				TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
				int tscore = Integer.valueOf(tvTS.getText().toString()) + score;
				//残ミス数を加算
				TextView tvMiss = (TextView) findViewById(R.id.tvMiss);
				int miss = Integer.valueOf(tvMiss.getText().toString().substring(0, 1));
				tscore += (3 - miss) * 2000;
				//ハードモードの場合は得点2倍
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
			//正解
			EachResultDialogFragment erdf = new EachResultDialogFragment();
			erdf.setCorrect(true);
			//今回の得点
			int score = Integer.valueOf(tvTimeLimit.getText().toString());
			erdf.setScore(score);
			//累計得点
			TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
			int tscore = Integer.valueOf(tvTS.getText().toString()) + score;
			erdf.setTotalScore(tscore);
			//表示
		    erdf.show(getSupportFragmentManager(),"dialog");
		} else {
			//不正解
			EachResultDialogFragment erdf = new EachResultDialogFragment();
			erdf.setCorrect(false);
			//今回の得点
			erdf.setScore(0);
			//累計得点
			TextView tvTS = (TextView) findViewById(R.id.tvScoreTotal);
			erdf.setTotalScore(Integer.valueOf(tvTS.getText().toString()));
			//表示
		    erdf.show(getSupportFragmentManager(),"dialog");
		}
	}
	
	/**
	 * 最後の回答かどうか
	 */
	private boolean isLastAnswer() {
		TextView tvRest = (TextView) findViewById(R.id.tvRest);
		int rest = Integer.valueOf(tvRest.getText().toString().substring(0, 2));
		if (rest >= 2) {
			return false;
		}
		
		if (selectIdx == correctIdx) {
			//残りが1で正解の場合は最後
			return true;
		} else {
			TextView tvMiss = (TextView) findViewById(R.id.tvMiss);
			int miss = Integer.valueOf(tvMiss.getText().toString().substring(0, 1));
			if (miss == 3) {
				//残りが1で、ミスが既に3回ある場合も最後
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 結果Dlgが「NEXT」で閉じられた場合
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
		
		//次の問題を生成
		prepareQuestion();
		
		//再度カウントダウン
		tvCountDown.setVisibility(View.VISIBLE);
		tvCountDown.setText("4");
		tvTimeLimit.setText("3000");
		doCountDown();
	}
	
	/**
	 * 結果Dlgが「RETIRE」で閉じられた場合
	 */
	public void onReturnRetire() {
		//リタイアの場合はTOP画面に戻る
		finish();
	}
	
	/**
	 * リタイア
	 */
	public void onClickRetire(View v) {
		//リタイアの場合はTOP画面に戻る
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
