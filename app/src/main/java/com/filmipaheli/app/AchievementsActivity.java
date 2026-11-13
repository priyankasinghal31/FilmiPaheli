package com.filmipaheli.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Lists every achievement, earned ones highlighted. */
public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        Edge.apply(this);
        Prefs prefs = Prefs.get(this);
        Achievements.checkAndAnnounce(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Feedback.get(this).tap();
            finish();
        });
        ((TextView) findViewById(R.id.achProgress)).setText(
                getString(R.string.ach_progress, prefs.achievementCount(), Achievements.ALL.size()));

        LinearLayout list = findViewById(R.id.achList);
        for (Achievements.Item item : Achievements.ALL) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_achievement, list, false);
            boolean done = prefs.hasAchievement(item.id);
            ((TextView) row.findViewById(R.id.achTitle)).setText(item.titleRes);
            ((TextView) row.findViewById(R.id.achDesc)).setText(item.descRes);
            TextView badge = row.findViewById(R.id.achBadge);
            badge.setText(done ? "✓" : "🔒");
            badge.setBackgroundResource(done ? R.drawable.bg_day_done : R.drawable.bg_day);
            TextView reward = row.findViewById(R.id.achReward);
            reward.setText("+" + item.reward);
            row.setAlpha(done ? 1f : 0.65f);
            list.addView(row);
        }
    }
}
