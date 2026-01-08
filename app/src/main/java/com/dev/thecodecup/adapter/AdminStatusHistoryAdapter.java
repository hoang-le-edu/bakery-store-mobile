package com.dev.thecodecup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.AdminStatusHistoryDto;

import java.util.ArrayList;
import java.util.List;

public class AdminStatusHistoryAdapter extends RecyclerView.Adapter<AdminStatusHistoryAdapter.StatusHistoryViewHolder> {

    private final List<AdminStatusHistoryDto> items = new ArrayList<>();

    public void setItems(List<AdminStatusHistoryDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatusHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_status_history, parent, false);
        return new StatusHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusHistoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StatusHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus;
        TextView tvChangedAt;
        TextView tvChangedBy;
        TextView tvNote;

        public StatusHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvChangedAt = itemView.findViewById(R.id.tvChangedAt);
            tvChangedBy = itemView.findViewById(R.id.tvChangedBy);
            tvNote = itemView.findViewById(R.id.tvNote);
        }

        void bind(AdminStatusHistoryDto item) {
            tvStatus.setText(item.getStatus() != null ? item.getStatus() : "--");
            tvChangedAt.setText(formatTimestamp(item.getChangedAt()));

            // Changed by
            String changedByText = "By system";
            if (item.getChangedBy() != null) {
                String name = item.getChangedBy().getName();
                String email = item.getChangedBy().getEmail();
                String id = item.getChangedBy().getId();
                
                if (name != null && !name.isEmpty()) {
                    changedByText = "By " + name;
                } else if (email != null && !email.isEmpty()) {
                    changedByText = "By " + email;
                } else if (id != null && !id.isEmpty()) {
                    changedByText = "By " + id;
                }
            }
            tvChangedBy.setText(changedByText);

            // Note
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText(item.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }

        /** Trim fractional seconds, remove trailing Z, and replace T with space for compact display. */
        private String formatTimestamp(String raw) {
            if (raw == null || raw.isEmpty()) return "--";
            String cleaned = raw.replace('T', ' ');
            int dotIndex = cleaned.indexOf('.');
            String trimmed = dotIndex > 0 ? cleaned.substring(0, dotIndex) : cleaned;
            if (trimmed.endsWith("Z")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            return trimmed;
        }
    }
}
