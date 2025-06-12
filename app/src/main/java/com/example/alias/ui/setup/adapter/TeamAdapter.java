package com.example.alias.ui.setup.adapter;

import android.app.Activity;
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

import java.util.ArrayList;
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
            String name = generateUniqueRandomName();
            team.setName(name);

            notifyItemChanged(position);
        });
    }

    private void setupEditNameButton(@NonNull TeamViewHolder holder, Team team, int position) {
        holder.btnEdit.setOnClickListener(v -> showEditDialog(holder.itemView.getContext(), team, position));
    }

    private void showEditDialog(Context context, Team team, int position) {
        ViewGroup root = ((Activity) context).findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_team_name, root, false);
        EditText etTeamName = dialogView.findViewById(R.id.etTeamName);
        etTeamName.setText(team.getName());

        AlertDialog dialog = buildDialog(context, dialogView);

        setupSaveButton(dialogView, etTeamName, dialog, team, position);
        setupCancelButton(dialogView, dialog);

        dialog.show();
        setDialogWidth(dialog, context, 350);
    }

    private AlertDialog buildDialog(Context context, View dialogView) {
        return new AlertDialog.Builder(context, R.style.CustomDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();
    }

    private void setDialogWidth(AlertDialog dialog, Context context, int dpWidth) {
        Objects.requireNonNull(dialog.getWindow()).setLayout(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpWidth, context.getResources().getDisplayMetrics()
                ),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void setupSaveButton(View dialogView, EditText etTeamName, AlertDialog dialog, Team team, int position) {
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String updatedName = etTeamName.getText().toString().trim();

            if (updatedName.length() > 20) {
                etTeamName.setError("Назва команди не може бути більша за 20 символів");
            } else if (!isValidName(updatedName, team)) {
                etTeamName.setError("Ця назва вже існує або недійсна");
            } else {
                team.setName(updatedName);
                notifyItemChanged(position);
                dialog.dismiss();
            }
        });
    }

    private void setupCancelButton(View dialogView, AlertDialog dialog) {
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
    }

    private boolean isValidName(String name, Team currentTeam) {
        if (name.isEmpty()) return false;

        for (Team t : teams) {
            if (t != currentTeam && t.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        return true;
    }

    private String generateUniqueRandomName() {
        String[] suggestions = context.getResources().getStringArray(R.array.team_name_suggestions);

        List<String> currentNames = new ArrayList<>();
        for (Team team : teams) {
            currentNames.add(team.getName());
        }

        List<String> available = new ArrayList<>();
        for (String name : suggestions) {
            if (!currentNames.contains(name)) {
                available.add(name);
            }
        }

        return available.get(new Random().nextInt(available.size()));
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