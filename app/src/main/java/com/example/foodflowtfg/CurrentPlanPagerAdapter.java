package com.example.foodflowtfg;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CurrentPlanPagerAdapter extends FragmentStateAdapter {
    private final String nombrePlan;
    private String diaPlan;

    public CurrentPlanPagerAdapter(FragmentActivity fa, String nombrePlan, String diaPlan) {
        super(fa);
        this.nombrePlan = nombrePlan;
        this.diaPlan = diaPlan;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putString("nombrePlan", nombrePlan);
        args.putString("diaPlan", diaPlan);

        if (position == 0) {
            TodayPlanFragment todayFragment = new TodayPlanFragment();
            todayFragment.setArguments(args);
            return todayFragment;
        } else {
            WeekPlanFragment weekFragment = new WeekPlanFragment();
            weekFragment.setArguments(args);
            return weekFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    // Nuevo método para actualizar el día y refrescar el fragmento de Hoy
    public void actualizarDiaPlan(String nuevoDiaPlan) {
        if (!nuevoDiaPlan.equals(diaPlan)) {
            diaPlan = nuevoDiaPlan;
            // Fuerza recarga del fragmento Hoy:
            notifyItemChanged(0);
        }
    }
}
