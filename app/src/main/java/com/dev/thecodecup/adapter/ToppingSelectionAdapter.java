package com.dev.thecodecup.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.ToppingSelection;
import com.dev.thecodecup.model.network.dto.AdminProductDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for selecting toppings with custom prices in a dialog
 */
public class ToppingSelectionAdapter extends RecyclerView.Adapter<ToppingSelectionAdapter.ViewHolder> {
    
    private final List<AdminProductDto> allToppings;
    private final Map<String, ToppingSelection> selectedToppings; // toppingId -> ToppingSelection
    
    public ToppingSelectionAdapter(List<AdminProductDto> allToppings, List<ToppingSelection> preSelected) {
        this.allToppings = allToppings != null ? allToppings : new ArrayList<>();
        this.selectedToppings = new HashMap<>();
        
        // Pre-populate with existing selections
        if (preSelected != null) {
            for (ToppingSelection selection : preSelected) {
                selectedToppings.put(selection.toppingId, selection);
            }
        }
    }
    
    public List<ToppingSelection> getSelectedToppings() {
        return new ArrayList<>(selectedToppings.values());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topping_selection, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminProductDto topping = allToppings.get(position);
        String toppingId = topping.getProductId();
        String toppingName = topping.getProductName();
        
        holder.tvToppingName.setText(toppingName != null ? toppingName : "Unknown");
        
        // Check if this topping is already selected
        ToppingSelection existingSelection = selectedToppings.get(toppingId);
        boolean isSelected = existingSelection != null;
        
        holder.cbTopping.setChecked(isSelected);
        holder.etExtraPrice.setEnabled(isSelected);
        
        if (isSelected) {
            holder.etExtraPrice.setText(existingSelection.extraPrice);
        } else {
            holder.etExtraPrice.setText("");
        }
        
        // Handle checkbox changes
        holder.cbTopping.setOnCheckedChangeListener((buttonView, isChecked) -> {
            holder.etExtraPrice.setEnabled(isChecked);
            
            if (isChecked) {
                String price = holder.etExtraPrice.getText().toString().trim();
                if (price.isEmpty()) {
                    price = "0";
                    holder.etExtraPrice.setText(price);
                }
                selectedToppings.put(toppingId, new ToppingSelection(toppingId, toppingName, price));
            } else {
                selectedToppings.remove(toppingId);
                holder.etExtraPrice.setText("");
            }
        });
        
        // Handle price changes
        holder.etExtraPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (holder.cbTopping.isChecked()) {
                    String price = s.toString().trim();
                    if (!price.isEmpty()) {
                        selectedToppings.put(toppingId, new ToppingSelection(toppingId, toppingName, price));
                    }
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return allToppings.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTopping;
        TextView tvToppingName;
        EditText etExtraPrice;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTopping = itemView.findViewById(R.id.cbTopping);
            tvToppingName = itemView.findViewById(R.id.tvToppingName);
            etExtraPrice = itemView.findViewById(R.id.etExtraPrice);
        }
    }
}
