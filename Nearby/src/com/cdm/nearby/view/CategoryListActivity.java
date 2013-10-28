package com.cdm.nearby.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.cdm.nearby.R;
import com.cdm.nearby.common.L;
import com.cdm.nearby.modal.Category;
import com.cdm.nearby.modal.CategoryManager;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 11:51 PM
 */
public class CategoryListActivity extends BaseActivity {
    private ImageButton backButton;
    private TextView titleTextView;
    private ListView categoryListView;
    private CategoryListAdapter categoryListAdapter;
    private Category parentCategory;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.category_list);

        backButton = (ImageButton)findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        titleTextView = (TextView)findViewById(R.id.titleTextView);

        categoryListView = (ListView) findViewById(R.id.categoryListView);


        Intent data = getIntent();
        String code = data.getStringExtra("code");
        CategoryManager categoryManager = new CategoryManager(this);
        try {
            parentCategory = categoryManager.getByCode(code);
        } catch (Exception e) {
           L.e(e.getMessage(), e);
        }


        categoryListAdapter = new CategoryListAdapter(this, parentCategory.getChildren());
        categoryListView.setAdapter(categoryListAdapter);

        titleTextView.setText(parentCategory.getName());
    }
}