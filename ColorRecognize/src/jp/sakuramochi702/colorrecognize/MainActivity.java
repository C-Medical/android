package jp.sakuramochi702.colorrecognize;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private int hScore = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //タイトルバーを消す
		setContentView(R.layout.activity_main);
		
		//ハイスコア読込
		hScore = getHighScoreFromFile();
		TextView tvHScore = (TextView) findViewById(R.id.tvHighScore);
		tvHScore.setText(String.valueOf(hScore));
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
	 * STARTボタン
	 */
	public void btnStartOnClick(View v) {
		Intent i = new Intent(this, jp.sakuramochi702.colorrecognize.PlayActivity.class);
		startActivity(i);
	}
	
	/**
	 * HELPボタン
	 */
	public void btnHelpOnClick(View v) {
		Intent i = new Intent(this, jp.sakuramochi702.colorrecognize.HelpActivity1.class);
		startActivity(i);
	}
	
	/**
	 * HARD MODEボタン
	 */
	public void btnHardModeOnClick(View v) {
		if (hScore < 25000) {
			Toast.makeText(this, "Unlocked by getting 25000 score!", Toast.LENGTH_LONG).show();
			return;
		}
		Intent i = new Intent(this, jp.sakuramochi702.colorrecognize.PlayActivity.class);
		i.putExtra("HARD_MODE_FLG", true);
		startActivity(i);
	}
}
