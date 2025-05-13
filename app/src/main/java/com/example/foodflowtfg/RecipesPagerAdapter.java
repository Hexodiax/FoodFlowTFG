package com.example.foodflowtfg;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RecipesPagerAdapter extends FragmentStateAdapter {

    public RecipesPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new RecipeBookFragment() : new YourRecipesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}



