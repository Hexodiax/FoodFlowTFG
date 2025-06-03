package com.example.foodflowtfg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import java.util.List;

public class RecipesGridAdapter extends BaseAdapter {
    private Context context;
    private List<Recipe> recipes;

    public RecipesGridAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    @Override
    public int getCount() {
        return recipes.size();
    }

    @Override
    public Object getItem(int position) {
        return recipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageRecipe);
        Glide.with(context)
                .load(recipes.get(position).getImagenUrl())
                .placeholder(R.drawable.recipe_placeholder)
                .error(R.drawable.recipe_error)
                .into(imageView);

        return convertView;
    }
}
