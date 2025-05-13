package com.example.foodflowtfg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;

public class RecipeBookFragment extends Fragment {

    public RecipeBookFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_book, container, false);

        GridView gridView = view.findViewById(R.id.gridViewRecipes);

        // Lista provisional para las recetas
        List<Integer> recipeBookImages = Arrays.asList(
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1,
                R.drawable.recipe1
        );

        // Pasar la lista al adaptador
        RecipesGridAdapter adapter = new RecipesGridAdapter(requireContext(), recipeBookImages);
        gridView.setAdapter(adapter);

        return view;
    }
}
