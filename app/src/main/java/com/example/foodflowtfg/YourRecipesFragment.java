package com.example.foodflowtfg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;

public class YourRecipesFragment extends Fragment {

    public YourRecipesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_recipes, container, false);
        GridView gridView = view.findViewById(R.id.gridViewRecipes);

        // Lista provisional para las recetas propias
        List<Integer> yourRecipesImages = Arrays.asList(
                R.drawable.recipe2,
                R.drawable.recipe2,
                R.drawable.recipe2
        );

        // Pasar la lista al adaptador
        RecipesGridAdapter adapter = new RecipesGridAdapter(requireContext(), yourRecipesImages);
        gridView.setAdapter(adapter);

        return view;
    }
}
