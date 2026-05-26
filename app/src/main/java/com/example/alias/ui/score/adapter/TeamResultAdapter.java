package com.example.alias.ui.score.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Team;

import java.util.List;

public class TeamResultAdapter extends RecyclerView.Adapter<TeamResultAdapter.TeamResultViewHolder> {

    private final List<Team> teams;

    public TeamResultAdapter(List<Team> teams) {
        this.teams = teams;
    }

    @NonNull
    @Override
    public TeamResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_result, parent, false);

        return new TeamResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamResultViewHolder holder, int position) {
        Team team = teams.get(position);
        int place = calculatePlace(position);

        holder.tvTeamName.setText(team.getName());
        holder.tvTeamScore.setText(String.valueOf(team.getScore()));

        holder.tvPlaceLabel.setText(
                holder.itemView.getContext().getString(R.string.place_label, place));

        holder.tvPlace.setText(getPlaceIcon(place));
        holder.tvPlace.setTextSize(place <= 4 ? 24 : 18);
        holder.itemView.setBackgroundResource(getPlaceBackground(place));
    }

    private String getPlaceIcon(int place) {
        switch (place) {
            case 1:
                return "🥇";
            case 2:
                return "🥈";
            case 3:
                return "🥉";
            case 4:
                return "🎖️";
            default:
                return place + ".";
        }
    }

    private int getPlaceBackground(int place) {
        switch (place) {
            case 1:
                return R.drawable.bg_result_team_winner;
            case 2:
                return R.drawable.bg_result_team_silver;
            case 3:
                return R.drawable.bg_result_team_bronze;
            case 4:
                return R.drawable.bg_result_team_fourth;
            default:
                return R.drawable.bg_result_team_default;
        }
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    private int calculatePlace(int position) {
        if (position == 0) {
            return 1;
        }

        int place = 1;

        for (int i = 1; i <= position; i++) {
            if (teams.get(i).getScore() < teams.get(i - 1).getScore()) {
                place = i + 1;
            }
        }

        return place;
    }

    static class TeamResultViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlace;
        TextView tvPlaceLabel;
        TextView tvTeamName;
        TextView tvTeamScore;

        TeamResultViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlace = itemView.findViewById(R.id.tvPlace);
            tvPlaceLabel = itemView.findViewById(R.id.tvPlaceLabel);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTeamScore = itemView.findViewById(R.id.tvTeamScore);
        }
    }
}
