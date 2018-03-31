package com.example.ankit.awareness;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AnalysisAdapter extends ArrayAdapter{

    private List appliance = new ArrayList<>();
    private List animation = new ArrayList<>();
    private String callingActivity;

    private String[] lookupName = {"heater", "dishwasher", "charger", "fridge"};

    private Context c;

    public AnalysisAdapter(Context context, int resource, String call)
    {
        super(context, resource);
        this.c = context;
        callingActivity = call;
    }

    public String getCallingActivity()
    {
        return this.callingActivity;
    }

    public void add(String newAppliance)
    {
        if(newAppliance.equals("All") || newAppliance.equals("No Connected Device"))
        {
            Log.d("Adapter", newAppliance + " added with position " + -1);

            appliance.add(newAppliance);
            animation.add(-1);

            super.add(newAppliance);
            //super.add(-1);
        }
        else
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
            //super.add(position);
        }
    }

    public boolean isAlreadyInList(String testAppliance) { return this.appliance.contains(testAppliance);}

    static class RowHolder
    {
        TextView APPLIANCE;
        ImageView ANIMATION;
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
            row = inflater.inflate(R.layout.analysis_item, parent, false);

            holder = new RowHolder();

            holder.APPLIANCE = (TextView) row.findViewById(R.id.analysis_item_text);
            holder.ANIMATION = (ImageView) row.findViewById(R.id.animation_item_image);

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

                    //Send to FriendInfoActivity with proper information when name is clicked
                    /*
                    Intent intent = new Intent(c, FriendInfoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("FRIENDKEY", (String) getId(currentPosition));
                    intent.putExtra("USERKEY", mAuth.getCurrentUser().getUid().toString());
                    intent.putExtra("FRIENDNAME", (String) getItem(currentPosition));
                    c.startActivity(intent);
                    */
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
            holder.ANIMATION.setBackgroundColor(Color.GREEN);
        else if (deviceID == 1) //dishwasher
            holder.ANIMATION.setBackgroundColor(Color.YELLOW);
        else if (deviceID == 2) //charger
            holder.ANIMATION.setBackgroundColor(Color.RED);
        else if (deviceID == 3)//fridge
            holder.ANIMATION.setBackgroundColor(Color.BLUE);

        return row;
    }
}