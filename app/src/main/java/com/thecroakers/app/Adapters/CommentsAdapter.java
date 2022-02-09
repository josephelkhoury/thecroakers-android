package com.thecroakers.app.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.thecroakers.app.Constants;
import com.thecroakers.app.Models.CommentModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;

import java.util.ArrayList;

/**
 * Created by thecroakers on 3/20/2018.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CustomViewHolder> {

    public Context context;
    public CommentsAdapter.OnItemClickListener listener;
    public CommentsAdapter.onReplyItemCLickListener onReplyItemCLickListener;
    LinkClickListener linkClickListener;
    private ArrayList<CommentModel> dataList;
    public Comments_Reply_Adapter commentsReplyAdapter;

    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item

    public interface LinkClickListener {
        void onLinkClicked(SocialView view, String matchedText);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, CommentModel item, View view);
        void onItemLongClick(int position, CommentModel item, View view);
    }

    public interface onReplyItemCLickListener {
        void onItemClick(ArrayList<CommentModel> arrayList, int position, View view);
        void onItemLongClick(ArrayList<CommentModel> arrayList, int position, View view);
    }

    public interface onLongPressListener {
        void onItemClick(int position, CommentModel item, View view);
    }

    public CommentsAdapter(Context context, ArrayList<CommentModel> dataList, CommentsAdapter.OnItemClickListener listener, CommentsAdapter.onReplyItemCLickListener onReplyItemCLickListener, LinkClickListener linkClickListener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.linkClickListener = linkClickListener;
        this.onReplyItemCLickListener = onReplyItemCLickListener;
    }

    @Override
    public CommentsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        CommentsAdapter.CustomViewHolder viewHolder = new CommentsAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.CustomViewHolder holder, final int i) {
        final CommentModel item = dataList.get(i);

        holder.setIsRecyclable(true);
        holder.username.setText(item.user_name);

        if (item.profile_pic != null && !item.profile_pic.equals("")) {
            holder.userPic.setController(Functions.frescoImageLoad(item.profile_pic,holder.userPic,false));
        }

        if (item.liked != null && !item.equals("")) {
            if (item.liked.equals("1")) {
                holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
            } else {
                holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like));
            }
        }

        holder.likeTxt.setText(Functions.getSuffix(item.like_count));

        holder.message.setText(item.comments);
        if (item.isExpand) {
            holder.lessLayout.setVisibility(View.VISIBLE);
            holder.replyCount.setVisibility(View.GONE);
        }

        if (item.arrayList != null && item.arrayList.size() > 0) {
            holder.replyCount.setText(context.getString(R.string.view_replies)+" (" + item.arrayList.size() + ")");
        } else {
            holder.replyCount.setVisibility(View.GONE);
        }

        holder.replyCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpand = true;
                holder.lessLayout.setVisibility(View.VISIBLE);
                holder.replyCount.setVisibility(View.GONE);
            }
        });

        holder.showLessTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpand = false;
                holder.lessLayout.setVisibility(View.GONE);
                holder.replyCount.setVisibility(View.VISIBLE);
            }
        });

        commentsReplyAdapter = new Comments_Reply_Adapter(context, item.arrayList);
        holder.replyRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.replyRecyclerView.setAdapter(commentsReplyAdapter);
        holder.replyRecyclerView.setHasFixedSize(false);
        holder.bind(i, item, listener);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username, message, replyCount, likeTxt, showLessTxt;
        SimpleDraweeView userPic;
        ImageView likeImage;
        LinearLayout messageLayout, lessLayout, likeLayout;
        RecyclerView replyRecyclerView;

        public CustomViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.username);
            userPic = view.findViewById(R.id.user_pic);
            message = view.findViewById(R.id.message);
            replyCount = view.findViewById(R.id.reply_count);
            likeImage = view.findViewById(R.id.like_image);
            messageLayout = view.findViewById(R.id.message_layout);
            likeTxt = view.findViewById(R.id.like_txt);
            replyRecyclerView = view.findViewById(R.id.reply_recycler_view);
            lessLayout = view.findViewById(R.id.less_layout);
            showLessTxt = view.findViewById(R.id.show_less_txt);
            likeLayout = view.findViewById(R.id.like_layout);
        }

        public void bind(final int position, final CommentModel item, final CommentsAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(position, item, v);
            });

            userPic.setOnClickListener(v -> {
                listener.onItemClick(position, item, v);
            });

            messageLayout.setOnClickListener(v -> {
                listener.onItemClick(position, item, v);
            });

            messageLayout.setOnLongClickListener(v -> {
                listener.onItemLongClick(position, item, v);
                return true;
            });

            likeLayout.setOnClickListener(v -> {
                listener.onItemClick(position, item, v);
            });
        }
    }

    public class Comments_Reply_Adapter extends RecyclerView.Adapter<Comments_Reply_Adapter.CustomViewHolder> {

        public Context context;
        private ArrayList<CommentModel> dataList;

        public Comments_Reply_Adapter(Context context, ArrayList<CommentModel> dataList) {
            this.context = context;
            this.dataList = dataList;
        }

        @Override
        public Comments_Reply_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_reply_layout, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            Comments_Reply_Adapter.CustomViewHolder viewHolder = new Comments_Reply_Adapter.CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public void onBindViewHolder(final Comments_Reply_Adapter.CustomViewHolder holder, final int i) {
            final CommentModel item = dataList.get(i);
            holder.setIsRecyclable(true);
            holder.username.setText(item.user_name);

            if (item.profile_pic != null && !item.profile_pic.equals("")) {
                holder.user_pic.setController(Functions.frescoImageLoad(Constants.BASE_URL + item.profile_pic, holder.user_pic,false));
            }

            holder.message.setText(item.comments);

            if (item.liked != null && !item.liked.equals("")) {
                if (item.liked.equals("1")) {
                    holder.reply_like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
                } else {
                    holder.reply_like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like));
                }
            }

            holder.like_txt.setText(Functions.getSuffix(item.like_count));

            holder.message.setOnMentionClickListener(new SocialView.OnClickListener() {
                @Override
                public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                    linkClickListener.onLinkClicked(view, text.toString());
                }
            });

            holder.bind(i, dataList, onReplyItemCLickListener);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {

            TextView username, like_txt;
            SocialTextView message;
            SimpleDraweeView user_pic;
            ImageView reply_like_image;
            LinearLayout reply_layout, like_layout;

            public CustomViewHolder(View view) {
                super(view);

                username = view.findViewById(R.id.username);
                user_pic = view.findViewById(R.id.user_pic);
                message = view.findViewById(R.id.message);
                reply_layout = view.findViewById(R.id.reply_layout);
                reply_like_image = view.findViewById(R.id.reply_like_image);
                like_layout = view.findViewById(R.id.like_layout);
                like_txt = view.findViewById(R.id.like_txt);
            }

            public void bind(final int position, ArrayList<CommentModel> datalist, final CommentsAdapter.onReplyItemCLickListener listener) {

                itemView.setOnClickListener(v -> {
                    CommentsAdapter.this.onReplyItemCLickListener.onItemClick(datalist, position, v);
                });

                user_pic.setOnClickListener(v -> {
                    CommentsAdapter.this.onReplyItemCLickListener.onItemClick(datalist, position, v);
                });

                reply_layout.setOnClickListener(v -> {
                    CommentsAdapter.this.onReplyItemCLickListener.onItemClick(datalist, position, v);
                });

                message.setOnLongClickListener(v -> {
                    listener.onItemLongClick(datalist, position, v);
                    return true;
                });

                like_layout.setOnClickListener(v -> {
                    CommentsAdapter.this.onReplyItemCLickListener.onItemClick(datalist, position, v);
                });
            }
        }
    }
}