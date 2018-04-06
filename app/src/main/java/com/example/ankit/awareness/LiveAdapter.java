package com.example.ankit.awareness;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ankit.awareness.AnalysisActivity;
import com.example.ankit.awareness.R;

import java.util.ArrayList;
import java.util.List;

public class LiveAdapter extends ArrayAdapter{

    private List appliance = new ArrayList<>();
    private List animation = new ArrayList<>();
    private String callingActivity;
    private int listSize;

    private AnimationDrawable batteryani;
    private AnimationDrawable kettleani;
    private AnimationDrawable monitorani;

    private String[] lookupName = {"kettle", "laptop charger", "mixer", "panini press"};

    private Context c;

    public LiveAdapter(Context context, int resource, String call)
    {
        super(context, resource);
        this.c = context;
        callingActivity = call;
        listSize = 0;
    }

    public String getCallingActivity()
    {
        return this.callingActivity;
    }

    public void add(String newAppliance)
    {

            int position = 0;

            for (int i = 0; i < lookupName.length; i++)
            {
                if (lookupName[i].equals(newAppliance))
                {
                    position = i;
                    i = lookupName.length;
                }
            }

            //Log.d("Adapter", newAppliance + " added with position " + position);

            appliance.add(newAppliance);
            animation.add(position);

            super.add(newAppliance);

            listSize++;
            //super.add(position);
    }


    public boolean isAlreadyInList(String testAppliance) { return this.appliance.contains(testAppliance);}

    static class RowHolder
    {
        ImageView ANIMATION;
        TextView APPLIANCE;
    }

    @Override
    public int getCount()
    {
        return this.appliance.size();
    }

    @Override
    public Object getItem(int position)
    {
        return this.appliance.get(position);
    }

    public void remove(String removeAppliance)
    {
        int removePosition = appliance.indexOf(removeAppliance);
        appliance.remove(removeAppliance);
        animation.remove(removePosition);
        listSize--;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        row = convertView;
        RowHolder holder;

        final int currentPosition = position;

        //Inflate row
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.live_item, parent, false);

            holder = new RowHolder();

            holder.APPLIANCE = (TextView) row.findViewById(R.id.live_item_text);
            holder.ANIMATION = (ImageView) row.findViewById(R.id.live_item_image);

            holder.APPLIANCE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(int i = 0; i < appliance.size(); i ++)
                        Log.d("Adapter", "All appliance in order: " + appliance.get(i));

                    int index = (int) v.getTag();

                    Log.d("Adapter", appliance.get(index) + " send to analysis with position " + index);
                    Intent intent= new Intent(c, AnalysisActivity.class);
                    intent.putExtra("DEVICENAME", (String) appliance.get(index));
                    intent.putExtra("STARTINGACTIVITY", callingActivity);
                    c.startActivity(intent);
                }
            });

            row.setTag(holder);
        } else
            holder = (RowHolder) row.getTag();

        holder.APPLIANCE.setTag(position);

        //Display name
        String EM = (String) getItem(position);

        Log.d("Adapter", "String " + getItem(position) + " sent to holder");

        holder.APPLIANCE.setText(EM);


        //Display image
        //////////holder.ANIMATION.setImage();

        int deviceID = (int) animation.get(position);


        if (deviceID == 0) //heater
        {
            //ImageView imageView = (ImageView)row.findViewById(R.id.animation_item_image);
            //imageView.setBackgroundResource(R.drawable.batteryanimation);
            //batteryani = (AnimationDrawable) imageView.getBackground();
            //batteryani.start();

            holder.ANIMATION.setBackgroundResource(R.drawable.batteryanimation);
            batteryani = (AnimationDrawable) holder.ANIMATION.getBackground();
            batteryani.start();

        }
        else if (deviceID == 1) //dishwasher
        {
            holder.ANIMATION.setBackgroundResource(R.drawable.monitoranimation);
            monitorani = (AnimationDrawable) holder.ANIMATION.getBackground();
            monitorani.start();
        }
        else if (deviceID == 2) //charger
        {
            holder.ANIMATION.setBackgroundResource(R.drawable.kettleanimation);
            kettleani = (AnimationDrawable) holder.ANIMATION.getBackground();
            kettleani.start();
        }
        else if (deviceID == 3)//fridge
        {
            holder.ANIMATION.setBackgroundResource(R.drawable.kettleanimation);
            kettleani = (AnimationDrawable) holder.ANIMATION.getBackground();
            kettleani.start();
        }

        return row;
    }
}