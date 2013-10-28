package com.cdm.nearby.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 10:03 PM
 */
public abstract class BaseActivity extends Activity {
    protected LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutInflater = LayoutInflater.from(this);
    }
}
