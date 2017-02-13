package com.example.skedtext.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.skedtext.Data.Messages;
import com.example.skedtext.R;

import java.util.List;

/**
 * Created by solomon on 2/7/17.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {
    private final List<Messages> messagesList;
    private final OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Messages item);
    }

    private Context context;

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView txtcontact;
        TextView txtmessages;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            txtcontact = (TextView)itemView.findViewById(R.id.txtcontacts);
            txtmessages = (TextView)itemView.findViewById(R.id.txtmessages);
        }

        public void bind(final Messages item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public RVAdapter(List<Messages> messagesList, Context context, OnItemClickListener listener){
        this.context = context;
        this.messagesList = messagesList;
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_row, parent, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder holder, int position) {
        holder.txtcontact.setText(messagesList.get(position).contact);
        holder.txtmessages.setText(messagesList.get(position).message);
        holder.bind(messagesList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }
}
