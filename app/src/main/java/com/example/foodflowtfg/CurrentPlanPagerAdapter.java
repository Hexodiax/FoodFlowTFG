package com.example.foodflowtfg;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CurrentPlanPagerAdapter extends FragmentStateAdapter {
    public CurrentPlanPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new TodayPlanFragment();   // Fragmento de "Hoy"
        } else {
            return new WeekPlanFragment();    // Fragmento de "Semana"
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
