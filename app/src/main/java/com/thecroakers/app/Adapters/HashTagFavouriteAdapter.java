package com.thecroakers.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.thecroakers.app.Models.HashTagModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;

import java.util.ArrayList;

public class HashTagFavouriteAdapter extends RecyclerView.Adapter<HashTagFavouriteAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<HashTagModel> datalist;
    AdapterClickListener adapterClickListener;

    public HashTagFavouriteAdapter(Context context, ArrayList<HashTagModel> arrayList, AdapterClickListener adapterClickListener) {
        this.context = context;
        datalist = arrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_hashtag_favourite_list, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(true);
        HashTagModel item = datalist.get(i);
        holder.nameTxt.setText(item.name);
        holder.viewsTxt.setText(Functions.getSuffix(item.videos_count)+" "+(holder.itemView.getContext().getString(R.string.views)).toLowerCase());
        holder.hashtagImg.setController(Functions.frescoImageLoad(item.image, holder.hashtagImg,false));

        holder.bind(i, item, adapterClickListener);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView nameTxt, viewsTxt;
        SimpleDraweeView hashtagImg;

        public CustomViewHolder(View view) {
            super(view);

            nameTxt = view.findViewById(R.id.name_txt);
            viewsTxt = view.findViewById(R.id.views_txt);
            hashtagImg = view.findViewById(R.id.hashtag_icon);
        }

        public void bind(final int pos, final Object item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });
        }
    }
}

