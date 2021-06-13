package com.itba.runningMate.mainpage.fragments.feed.cards;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.itba.runningMate.R;
import com.itba.runningMate.domain.Run;
import com.itba.runningMate.mainpage.fragments.feed.FeedPresenter;
import com.itba.runningMate.pastruns.runs.ui.OnRunClickListener;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class PastRunsCard extends CardView implements View.OnClickListener {

    private FeedPresenter presenter;

    private TextView pastRunsEmptyMessage;
    private List<RunElementView> runs;

    private WeakReference<OnRunClickListener> listener;

    public PastRunsCard(@NonNull @NotNull Context context) {
        super(context);
        prepareFromConstructor(context);
    }

    private void prepareFromConstructor(Context context) {
        inflate(context, R.layout.card_past_runs, this);
        runs = new ArrayList<>();
        RunElementView run = findViewById(R.id.past_run_card_1);
        //run.setOnClickListener((OnClickListener) (a) -> presenter.goToDetails(a));
        runs.add(run);
        runs.add(findViewById(R.id.past_run_card_2));
        runs.add(findViewById(R.id.past_run_card_3));

        pastRunsEmptyMessage = findViewById(R.id.past_run_empty_card);


    }

    public PastRunsCard(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        prepareFromConstructor(context);
    }

    public PastRunsCard(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareFromConstructor(context);
    }

    public void setPresenter(FeedPresenter fp) {
        presenter = fp;
    }

    public void setPastRunCardsNoText() {
        pastRunsEmptyMessage.setVisibility(View.VISIBLE);
    }

    public void setPastRuns(List<Run> list) {
        if (list == null || list.isEmpty()) return;
        pastRunsEmptyMessage.setVisibility(View.GONE);

    }

    public void addRunToCard(int i, Run run) {
        runs.get(i).setVisibility(VISIBLE);
        runs.get(i).bind(run);
    }

    public void disappearRuns(int i) {
        if (i >= 3) return;
        while (i < 3) {
            runs.get(i++).setVisibility(GONE);
        }
    }

//    public void setRunDetailsListener(OnClickListener listener){
//        this.listener = new WeakReference<>(listener);
//    }

    public void launchRunDetails(long id) {

    }

    @Override
    public void onClick(View v) {
        presenter.goToPastRunsActivity();
    }


}
