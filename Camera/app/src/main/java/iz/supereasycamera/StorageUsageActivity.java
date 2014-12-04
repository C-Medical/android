package iz.supereasycamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import iz.supereasycamera.dto.StorageUsage;
import iz.supereasycamera.service.ContentsService;
import iz.supereasycamera.utils.Misc;


public class StorageUsageActivity extends Activity {
    private final ContentsService contentsService = new ContentsService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_storage_usage);

        final StorageUsage usage = contentsService.getStorageUsage(getApplicationContext());

        ((TextView) findViewById(R.id.txtNumDir)).setText(String.valueOf(usage.numberOfDirs));
        ((TextView) findViewById(R.id.txtNumPic)).setText(String.valueOf(usage.numberOfPics));
        ((TextView) findViewById(R.id.txtTotalByte)).setText(Misc.toMB(usage.totalBytes));
    }

}
