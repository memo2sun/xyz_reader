package com.example.xyzreader.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;

import java.util.List;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.TextViewHolder> {

    private List<String> mArticleBody;


    @NonNull
    @Override
    public TextAdapter.TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.text_item, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextAdapter.TextViewHolder holder, int position) {
        holder.tvArticleBody.setText(Html.fromHtml(mArticleBody.get(position).replace("\\r\\n", " ")));
    }

    @Override
    public int getItemCount() {
        return mArticleBody == null ? 0 : mArticleBody.size();
    }

    public void setTextData(List articleBody) {
        mArticleBody = articleBody;
        notifyDataSetChanged();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tvArticleBody;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArticleBody = itemView.findViewById(R.id.article_body);
        }
    }
}
