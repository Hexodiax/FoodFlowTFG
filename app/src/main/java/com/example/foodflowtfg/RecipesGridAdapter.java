package com.example.foodflowtfg;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;

import java.util.List;

public class RecipesGridAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> imageIds;

    public RecipesGridAdapter(Context context, List<Integer> imageIds) {
        this.context = context;
        this.imageIds = imageIds;
    }

    @Override
    public int getCount() {
        return imageIds.size();
    }

    @Override
    public Object getItem(int i) {
        return imageIds.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        }

        ImageView image = view.findViewById(R.id.recipeImage);
        image.setImageResource(imageIds.get(i));
        return view;
    }
}
