package com.example.foodflowtfg;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CurrentPlanDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_plan_detail);

        ViewPager2 viewPager = findViewById(R.id.viewPagerCurrentPlan);
        TabLayout tabLayout = findViewById(R.id.tabLayoutCurrentPlan);

        CurrentPlanPagerAdapter adapter = new CurrentPlanPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Hoy");
            } else {
                tab.setText("Semana");
            }
        }).attach();
    }
}
