package com.example.alias.ui.setup.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Team;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private final List<Team> teams;
    private final Context context;

    public TeamAdapter(Context context, List<Team> teams) {
        this.context = context;
        this.teams = teams;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_team_card, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        String formattedName = holder.itemView.getContext()
                .getString(R.string.team_name_format, position + 1, team.getName());
        holder.tvTeamName.setText(formattedName);

        setupRandomNameButton(holder, team, position);
        setupEditNameButton(holder, team, position);
    }


    private void setupRandomNameButton(@NonNull TeamViewHolder holder, Team team, int position) {
        holder.btnRandom.setOnClickListener(v -> {
            team.setName(generateRandomName());
            notifyItemChanged(position);
        });
    }

    private void setupEditNameButton(@NonNull TeamViewHolder holder, Team team, int position) {
        holder.btnEdit.setOnClickListener(v -> showEditDialog(holder.itemView.getContext(), team, position));
    }

    private void showEditDialog(Context context, Team team, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_team_name, null);
        EditText etTeamName = dialogView.findViewById(R.id.etTeamName);
        etTeamName.setText(team.getName());

        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CustomDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String updatedName = etTeamName.getText().toString().trim();
            if (!updatedName.isEmpty()) {
                team.setName(updatedName);
                notifyItemChanged(position);
            }
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 350, context.getResources().getDisplayMetrics()
                ),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private String generateRandomName() {
        String[] suggestions = {"Кмітливі", "Блискавки", "Ракети", "Словаки", "Синоніми", "Влучні"};
        return suggestions[new Random().nextInt(suggestions.length)];
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public void removeTeamAt(int position) {
        teams.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName;
        ImageButton btnEdit;
        ImageButton btnRandom;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnRandom = itemView.findViewById(R.id.btnRandom);
        }
    }
}