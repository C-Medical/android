package jp.sakuramochi702.colorrecognize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //タイトルバーを消す
		setContentView(R.layout.activity_result);
		
		//今回のスコア
		Intent i = this.getIntent();
		int score = i.getIntExtra("TOTAL_SCORE", 0);
		TextView tvScore = (TextView) findViewById(R.id.tvEachScore);
		tvScore.setText(String.valueOf(score));
		
		//今までのハイスコア
		int highScore = getHighScoreFromFile();
		TextView tvhighScore = (TextView) findViewById(R.id.tvBestScore);
		tvhighScore.setText(String.valueOf(highScore));
		
		//ハイスコア更新の場合はNewRecord!を出す
		TextView tvNewRec = (TextView) findViewById(R.id.tvNewRec);
		if (score > highScore) {
			tvNewRec.setVisibility(View.VISIBLE);
			//更新の場合はファイル保存もしておく
			saveHighScore(score);
		} else {
			tvNewRec.setVisibility(View.INVISIBLE);
		}
		
	}

	/**
	 * ファイルからハイスコアを読み込み
	 */
	private int getHighScoreFromFile() {
		int res = 0;
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(openFileInput("highscore.dat")));
			while (br.ready()) {
				res = Integer.parseInt(br.readLine());
			}
			br.close();
			return res;
		} catch (FileNotFoundException fnfe) {
			return 0;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * ハイスコアをファイルに保存
	 */
	private void saveHighScore(int score) {
		try {
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(openFileOutput("highscore.dat", Context.MODE_PRIVATE)));
			bw.write(String.valueOf(score));
			bw.close();
		} catch (FileNotFoundException fnfe) {
			//
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
	 * トップ画面に戻る
	 */
	public void btnBackOnClick(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		//遷移先のActivityより上のActivityを消す
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//遷移するとActivityを作り直す
		//intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	
	/**
	 * ツイート
	 */
	public void onClickTweet(View v) {
		if(appInstalledOrNot("com.twitter.android")){
			TextView tvScore = (TextView) findViewById(R.id.tvEachScore);
			String tweetStr = "I scored " + tvScore.getText().toString() + " points at SHAPE! ";
			//String tweetStr = "test: " + tvScore.getText().toString() + " score ";
            String url = "twitter://post?message="+tweetStr+"https://play.google.com/store/apps/details?id=jp.sakuramochi702.colorrecognize";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
		} else {
			Toast.makeText(this, "Twitter App is not found.", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 特定のアプリがインストールされているかどうかを調べるメソッド
	 * @param uri
	 * @return
	 */
	private boolean appInstalledOrNot(String uri){
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try{
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }catch (Exception e){
        }
        return app_installed ;
    }
	
}
