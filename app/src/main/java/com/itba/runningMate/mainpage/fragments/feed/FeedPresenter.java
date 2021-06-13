package com.itba.runningMate.mainpage.fragments.feed;

import com.itba.runningMate.domain.Run;
import com.itba.runningMate.mainpage.fragments.feed.cards.PastRunsCard;
import com.itba.runningMate.repository.run.RunRepository;
import com.itba.runningMate.utils.file.CacheFileProvider;
import com.itba.runningMate.utils.schedulers.SchedulerProvider;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class FeedPresenter {

    private final WeakReference<FeedView> view;
    private final WeakReference<PastRunsCard> pastRunsCard;
    private final RunRepository repo;
    private final SchedulerProvider schedulerProvider;
    private final CacheFileProvider cacheFileProvider;

    private final CompositeDisposable disposables; // I need 2 disposables at least


    public FeedPresenter(RunRepository repo, SchedulerProvider schedulerProvider, CacheFileProvider cacheFileProvider,
                         PastRunsCard pastRunsCard, FeedView view) {
        this.view = new WeakReference<>(view);
        this.pastRunsCard = new WeakReference<>(pastRunsCard);
        this.repo = repo;
        this.schedulerProvider = schedulerProvider;
        this.cacheFileProvider = cacheFileProvider;
        disposables = new CompositeDisposable();
    }

    public void onViewAttached() {
        disposables.add(repo.getRunLazy()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(this::receivedRunList, this::onRunListError));
    }

    public void onViewDetached() {
        disposables.dispose();
    }

    private void onRunListError(Throwable throwable) {
        Timber.d("Failed to retrieve runs from db");
        if (view.get() != null) {
            view.get().setPastRunCardsNoText();
        }
    }

    private void receivedRunList(List<Run> runs) {
        Timber.i("Runs %d", runs.size());
        if (view.get() != null){
            if (runs == null || runs.isEmpty()){
                view.get().setPastRunCardsNoText();
                view.get().disappearRuns(0);
                return;
            }
            int maxVal = Math.min(runs.size(), 3);
            for (int i = 1; i <= maxVal; i++) {
                //add data to view
                view.get().addRunToCard(i - 1, runs.get(i - 1));
            }

            // disappear the run cards where they should not be
            view.get().disappearRuns(maxVal);
        }
    }

    public void onRunClick(long id) {
        if (view.get() != null) {
            pastRunsCard.get().launchRunDetails(id);
        }
    }

    public void goToPastRunsActivity() {
        if (view.get() != null){
            view.get().goToPastRunsActivity();
        }
    }
}
