package com.dev.thecodecup.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.ToppingSelection;

import java.util.List;

public class SelectedToppingAdapter extends RecyclerView.Adapter<SelectedToppingAdapter.ViewHolder> {

    private final List<ToppingSelection> toppings;
    private final OnRemoveListener onRemoveListener;

    public interface OnRemoveListener {
        void onRemove(ToppingSelection topping);
    }

    public SelectedToppingAdapter(List<ToppingSelection> toppings, OnRemoveListener listener) {
        this.toppings = toppings;
        this.onRemoveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_topping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToppingSelection topping = toppings.get(position);
        holder.tvToppingName.setText(topping.toppingName.isEmpty() ? "Topping " + (position + 1) : topping.toppingName);
        holder.etExtraPrice.setText(topping.extraPrice);
        
        holder.etExtraPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                topping.extraPrice = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRemoveListener != null) {
                    onRemoveListener.onRemove(topping);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return toppings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvToppingName;
        EditText etExtraPrice;
        ImageButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            tvToppingName = itemView.findViewById(R.id.tvToppingName);
            etExtraPrice = itemView.findViewById(R.id.etExtraPrice);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
