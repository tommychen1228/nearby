package com.cdm.nearby.view;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.cdm.nearby.R;
import com.cdm.nearby.common.L;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/17/13
 * Time: 3:27 PM
 */
public class SettingActivity extends BaseActivity {
    private ImageButton backButton;
    private TextView versionTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        versionTextView = (TextView)findViewById(R.id.versionTextView);
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            String versionName = pinfo.versionName;
            int versionCode = pinfo.versionCode;

            versionTextView.setText("软件版本:" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            L.e(e.getMessage(), e);
        }


    }
}