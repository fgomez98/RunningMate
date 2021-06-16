package com.itba.runningMate.mainpage.fragments.feed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.itba.runningMate.R;
import com.itba.runningMate.di.DependencyContainer;
import com.itba.runningMate.di.DependencyContainerLocator;
import com.itba.runningMate.domain.Run;
import com.itba.runningMate.mainpage.fragments.feed.cards.GoalsCard;
import com.itba.runningMate.mainpage.fragments.feed.cards.OnRunClickListener;
import com.itba.runningMate.mainpage.fragments.feed.cards.OnSeeAllClickListener;
import com.itba.runningMate.mainpage.fragments.feed.cards.PastRunsCard;

public class FeedFragment extends Fragment implements FeedView, OnRunClickListener, OnSeeAllClickListener {

    private FeedPresenter presenter;

    private PastRunsCard pastRunsCard;
    private GoalsCard goalsCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createPresenter();

        pastRunsCard = view.findViewById(R.id.past_run_card);
        goalsCard = view.findViewById(R.id.goals_card);

        pastRunsCard.setElementListener(this);
        pastRunsCard.setSeeAllListener(this);
        goalsCard.setSeeAllListener(this);
    }

    private void createPresenter() {
        final DependencyContainer container = DependencyContainerLocator.locateComponent(getContext());

        presenter = new FeedPresenter(
                container.getRunRepository(),
                container.getSchedulerProvider(),
                this);
    }

    @Override
    public void launchPastRunsActivity() {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("runningmate")
                .encodedAuthority("pastruns");
        startActivity(new Intent(Intent.ACTION_VIEW, uriBuilder.build()));
    }

    @Override
    public void launchRunDetailActivity(long runId) {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("runningmate")
                .encodedAuthority("run")
                .appendQueryParameter("run-id", Long.toString(runId));
        startActivity(new Intent(Intent.ACTION_VIEW, uriBuilder.build()));
    }

    @Override
    public void launchAchievementsActivity() {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("runningmate")
                .encodedAuthority("achievements");
        startActivity(new Intent(Intent.ACTION_VIEW, uriBuilder.build()));
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onViewAttached();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onViewDetached();
    }

    @Override
    public void setPastRunCardsNoText() {
        pastRunsCard.setPastRunCardsNoText();
    }

    @Override
    public void addRunToCard(int i, Run run) {
        pastRunsCard.addRunToCard(i, run);
    }

    @Override
    public void disappearRuns(int i) {
        pastRunsCard.disappearRuns(i);
    }

    @Override
    public void disappearNoText() {
        pastRunsCard.disappearNoText();
    }

    @Override
    public void setGoalTitle(int title) {
        goalsCard.setTitle(title);
    }

    @Override
    public void setGoalSubtitle(int subtitle) {
        goalsCard.setSubtitle(subtitle);
    }

    @Override
    public void setGoalImage(int image) {
        goalsCard.setImage(image);
    }

    @Override
    public void onRunClick(long id) {
        presenter.onPastRunClick(id);
    }

    @Override
    public void onSeeAllPastRunsClick() {
        presenter.goToPastRunsActivity();
    }

    @Override
    public void onSeeAllAchievementsClick() {
        presenter.goToAchievementsActivity();
    }

}
