package com.ckt.yzf.bluetoothchat.assistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by yzf14 on 2016/3/15.
 */
public class ConversationListAdapter extends BaseAdapter {

    Context context = null;
    ArrayList<HashMap<String,Object>> chatList = null;
    int[] layout;
    String[] from;
    int[] to;
    public final static  int OTHER=1;
    public final static int ME=0;

    public ConversationListAdapter(Context context,ArrayList<HashMap<String,Object>> chatList,int[] layout,
    String[] form,int[] to)
    {
        super();
        this.context = context;
        this.chatList = chatList;
        this.layout = layout;
        this.from = form;
        this.to = to;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class  ViewHolder{
        public ImageView imageView = null;
        public TextView textView = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        int who=(Integer)chatList.get(position).get("person");

        convertView= LayoutInflater.from(context).inflate(
                layout[who==ME?0:1], null);
        holder=new ViewHolder();
        holder.imageView=(ImageView)convertView.findViewById(to[who*2+0]);
        holder.textView=(TextView)convertView.findViewById(to[who*2+1]);


        System.out.println(holder);
        System.out.println("WHYWHYWHYWHYW");
        System.out.println(holder.imageView);
        holder.imageView.setBackgroundResource((Integer)chatList.get(position).get(from[0]));
        holder.textView.setText(chatList.get(position).get(from[1]).toString());
        return convertView;

    }
}
