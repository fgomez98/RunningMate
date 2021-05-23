package com.itba.runningMate.mainpage.fragments.running.services.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.itba.runningMate.Constants;
import com.itba.runningMate.R;
import com.itba.runningMate.running.RunningActivity;
import com.itba.runningMate.utils.functional.Function;
import com.itba.runningMate.utils.run.RunMetrics;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

import static com.itba.runningMate.Constants.LOCATION_UPDATE_FASTEST_INTERVAL;
import static com.itba.runningMate.Constants.LOCATION_UPDATE_INTERVAL;
import static com.itba.runningMate.Constants.STOP_WATCH_UPDATE_INTERVAL;

public class TrackingService extends Service {

    private static final String HANDLER_THREAD_NAME = "LocationServiceHandlerThread";

    private NotificationManagerCompat notificationManager;

    private HandlerThread serviceHandlerThread;
    private Handler serviceHandler;
    private Handler mainHandler;

    private FusedLocationProviderClient fusedLocationClient;
    private List<WeakReference<OnTrackingMetricsUpdateListener>> onTrackingMetricsUpdateListeners;
    private List<WeakReference<OnTrackingLocationUpdateListener>> onTrackingLocationsUpdateListeners;

    private boolean isTracking;

    private LatLng lastLocation;
    private List<LatLng> trackedLocations;
    private float elapsedDistance;
    private long startTimeMillis;
    private long lastTimeUpdateMillis;
    private long elapsedMillis;
    private long pace;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            handleLocationUpdate(locationResult);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TrackingServiceBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onTrackingLocationsUpdateListeners = new LinkedList<>();
        onTrackingMetricsUpdateListeners = new LinkedList<>();
        isTracking = false;
        notificationManager = NotificationManagerCompat.from(this);
        serviceHandlerThread = new HandlerThread(HANDLER_THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND);
        serviceHandlerThread.start();
        serviceHandler = new Handler(serviceHandlerThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
        initLocationUpdates();
        Timber.i("Tracking service is up");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        serviceHandlerThread.quit();
        Timber.i("Tracking service is down");
    }

    public void startTracking() {
        startForegroundService();
        trackedLocations = new LinkedList<>();
        if (lastLocation != null) {
            trackedLocations.add(lastLocation);
        }
        elapsedMillis = 0L;
        lastTimeUpdateMillis = 0L;
        elapsedDistance = 0F;
        pace = 0L;
        isTracking = true;
        startTimeMillis = System.currentTimeMillis();
//        if (areListeners(onTrackingMetricsUpdateListeners)) {
        serviceHandler.post(this::stopWatch);
//        }
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void stopTracking() {
        stopForegroundService();
        isTracking = false;
    }

    public void setOnTrackingUpdateListener(OnTrackingUpdateListener listener) {
        setOnTrackingLocationUpdateListener(listener);
        setOnTrackingMetricsUpdateListener(listener);
    }

    public void setOnTrackingLocationUpdateListener(OnTrackingLocationUpdateListener listener) {
        onTrackingLocationsUpdateListeners.add(new WeakReference<>(listener));
    }

    public void setOnTrackingMetricsUpdateListener(OnTrackingMetricsUpdateListener listener) {
        onTrackingMetricsUpdateListeners.add(new WeakReference<>(listener));
        if (isTracking) {
            serviceHandler.post(this::stopWatch);
        }
    }

    public void removeTrackingUpdateListener(OnTrackingUpdateListener listener) {
        removeTrackingLocationUpdateListener(listener);
        removeTrackingMetricsUpdateListener(listener);
    }

    public void removeTrackingLocationUpdateListener(OnTrackingLocationUpdateListener listener) {
        removeListeners(onTrackingLocationsUpdateListeners, listener);
    }

    public void removeTrackingMetricsUpdateListener(OnTrackingMetricsUpdateListener listener) {
        removeListeners(onTrackingMetricsUpdateListeners, listener);
    }

    private <T> void removeListeners(List<WeakReference<T>> listeners, T listener) {
        Iterator<WeakReference<T>> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<T> wr = iterator.next();
            if (wr.get() == listener) {
                iterator.remove();
            }
        }
    }

    private <T> boolean areListeners(List<WeakReference<T>> listeners) {
        Iterator<WeakReference<T>> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<T> wr = iterator.next();
            if (wr.get() == null) {
                iterator.remove();
            } else {
                return true;
            }
        }
        return false;
    }

    private <T> void callListeners(List<WeakReference<T>> listeners, Function<T> function) {
        Iterator<WeakReference<T>> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<T> wr = iterator.next();
            if (wr.get() == null) {
                iterator.remove();
            } else {
                function.apply(wr.get());
            }
        }
    }

    public List<LatLng> getTrackedLocations() {
        return trackedLocations;
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, RunningActivity.class);
        /*
            Agregar una accion al notificationIntent para decirle a nuestra landing activity que
            hacer. Tambien podes usar el paramentro flag del pendingIntnet para agreagar mas data
            (ej. updatear la vista no re crearla)
         */
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_LOCATION_SERVICE_CHANNEL__ID)
                        .setContentText(getText(R.string.notification_tracking_service))
                        .setSmallIcon(R.drawable.ic_stat_notify_tracking_service)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .build();

        startForeground(Constants.NOTIFICATION_LOCATION_SERVICE_ID, notification);
    }

    private void stopForegroundService() {
        stopForeground(true);
    }

    public LatLng getLastLocation() {
        return lastLocation;
    }

    private void handleLocationUpdate(@NonNull LocationResult locationResult) {
        Location location = locationResult.getLastLocation();
        if (lastLocation != null && areEqualLocations(lastLocation.latitude, lastLocation.longitude, location.getLatitude(), location.getLongitude())) {
            return;
        }
        lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (isTracking) {
            trackedLocations.add(lastLocation);
            if (trackedLocations.size() >= 2) {
                LatLng prev = trackedLocations.get(trackedLocations.size() - 2);
                elapsedDistance += RunMetrics.calculateDistance(prev.latitude, prev.longitude, location.getLatitude(), location.getLongitude());
                pace = RunMetrics.calculatePace(elapsedDistance, elapsedMillis);
                callbackDistanceUpdate(elapsedDistance);
                callbackPaceUpdate(pace);
            }
        }
        callbackLocationUpdate(lastLocation.latitude, lastLocation.longitude);
    }

    private void callbackLocationUpdate(double latitude, double longitude) {
        callListeners(onTrackingLocationsUpdateListeners, l -> l.onLocationUpdate(latitude, longitude));
    }

    private void callbackDistanceUpdate(float distance) {
        callListeners(onTrackingMetricsUpdateListeners, l -> l.onDistanceUpdate(distance));
    }

    private void callbackPaceUpdate(long pace) {
        callListeners(onTrackingMetricsUpdateListeners, l -> l.onPaceUpdate(pace));
    }

    private void callbackStopWatchUpdate(long elapsedTime) {
        callListeners(onTrackingMetricsUpdateListeners, l -> l.onStopWatchUpdate(elapsedTime));
    }

    @SuppressLint("MissingPermission")
    private void initLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        /*
            TODO: chequear que los settings sean los apropiados
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnFailureListener( ... );
        */
    }

    private void stopWatch() {
        if (isTracking && areListeners(onTrackingMetricsUpdateListeners)) {
            elapsedMillis = System.currentTimeMillis() - startTimeMillis;

            /*
                lastTimeUpdateMillis is the time of the last time update,
                we just want to send updates when a second has elapsed
            */
            if (elapsedMillis >= lastTimeUpdateMillis + 1000L) {
                mainHandler.post(() -> callbackStopWatchUpdate(elapsedMillis));
                lastTimeUpdateMillis += 1000L;
            }
            serviceHandler.postDelayed(this::stopWatch, STOP_WATCH_UPDATE_INTERVAL);
        }
    }

    private boolean areEqualLocations(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        return Double.compare(latitudeA, latitudeB) == 0 && Double.compare(longitudeA, longitudeB) == 0;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public float getElapsedDistance() {
        return elapsedDistance;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public long getPace() {
        return pace;
    }

}