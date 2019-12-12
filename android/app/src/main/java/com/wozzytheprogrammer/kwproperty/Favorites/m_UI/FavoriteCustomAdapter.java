package com.wozzytheprogrammer.kwproperty.Favorites.m_UI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wozzytheprogrammer.kwproperty.R;
import com.wozzytheprogrammer.kwproperty.Favorites.m_FireBase.FavoritesDetailActivity;
import com.wozzytheprogrammer.kwproperty.Favorites.m_Model.Favorite;

import java.util.ArrayList;

/**
 * Created by Oclemy on 6/21/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 * 1. where WE INFLATE OUR MODEL LAYOUT INTO VIEW ITEM
 * 2. THEN BIND DATA
 */
public class FavoriteCustomAdapter extends BaseAdapter {
    Context c;
    ArrayList<Favorite> favorites;

    public FavoriteCustomAdapter(Context c, ArrayList<Favorite> favorites) {
        this.c = c;
        this.favorites = favorites;
    }

    @Override
    public int getCount() {
        return favorites.size();
    }

    @Override
    public Object getItem(int pos) {
        return favorites.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(convertView==null)
        {
            convertView= LayoutInflater.from(c).inflate(R.layout.favorites_model,viewGroup,false);
        }

        TextView nameTxt= (TextView) convertView.findViewById(R.id.nameTxt);
        TextView propTxt= (TextView) convertView.findViewById(R.id.propellantTxt);
        TextView descTxt= (TextView) convertView.findViewById(R.id.descTxt);

        final Favorite s= (Favorite) this.getItem(position);

        nameTxt.setText(s.getName());
        propTxt.setText(s.getPropellant());
        descTxt.setText(s.getDescription());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OPEN DETAIL
                openDetailActivity(s.getName(),s.getDescription(),s.getPropellant());
            }
        });

        return convertView;
    }
    //OPEN DETAIL ACTIVITY
    private void openDetailActivity(String...details)
    {
        Intent i=new Intent(c, FavoritesDetailActivity.class);
        i.putExtra("NAME_KEY",details[0]);
        i.putExtra("DESC_KEY",details[1]);
        i.putExtra("PROP_KEY",details[2]);

        c.startActivity(i);
    }
}














