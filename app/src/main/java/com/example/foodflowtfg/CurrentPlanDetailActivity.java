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
public class CurrentPlanDetailActivity extends AppCompatActivity implements WeekPlanFragment.OnDiaSeleccionadoListener {

    private CurrentPlanPagerAdapter adapter;
    private ViewPager2 viewPager;
    private String nombrePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_plan_detail);

        nombrePlan = getIntent().getStringExtra("Plan_actual");
        String diaPlan = getIntent().getStringExtra("plan_day");

        viewPager = findViewById(R.id.viewPagerCurrentPlan);
        TabLayout tabLayout = findViewById(R.id.tabLayoutCurrentPlan);

        adapter = new CurrentPlanPagerAdapter(this, nombrePlan, diaPlan);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Hoy" : "Semana");
        }).attach();
    }

    @Override
    public void onDiaSeleccionado(String dia) {
        // Cambiar a la pestaña "Hoy"
        viewPager.setCurrentItem(0, true);

        // Actualizar el fragmento TodayPlanFragment con el nuevo día
        adapter.actualizarDiaPlan(dia);
    }
}
