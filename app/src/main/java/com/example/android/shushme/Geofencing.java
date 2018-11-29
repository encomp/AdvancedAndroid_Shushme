package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class Geofencing implements ResultCallback<Status> {

  private static final String TAG = Geofence.class.getSimpleName();
  private static final int GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000;
  private static final int GEOFENCE_RADIUS = 50;
  private final Context context;
  private final GoogleApiClient googleApiClient;
  private final PendingIntent pendingIntent;
  private ImmutableList<Geofence> geofences;

  Geofencing(Context context, GoogleApiClient googleApiClient) {
    this.context = Preconditions.checkNotNull(context);
    this.googleApiClient = Preconditions.checkNotNull(googleApiClient);
    pendingIntent = createPendingIntent();
    geofences = ImmutableList.copyOf(Lists.newArrayList());
  }

  void registerAllGeofences() {
    if (googleApiClient.isConnected() && geofences.size() > 0) {
      try {
        LocationServices.GeofencingApi.addGeofences(
                googleApiClient, createGeofencingRequest(), pendingIntent)
            .setResultCallback(this);
      } catch (SecurityException exc) {
        Log.e(TAG, exc.getMessage());
      }
    }
  }

  void unRegisterAllGeofences() {
    if (googleApiClient.isConnected()) {
      try {
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent)
            .setResultCallback(this);
      } catch (SecurityException exc) {
        Log.e(TAG, exc.getMessage());
      }
    }
  }

  void updateGeofences(PlaceBuffer placeBuffer) {
    ImmutableList.Builder<Geofence> builder = ImmutableList.builder();
    if (placeBuffer != null && placeBuffer.getCount() > 0) {
      for (Place place : placeBuffer) {
        builder.add(
            new Geofence.Builder()
                .setRequestId(place.getId())
                .setExpirationDuration(GEOFENCE_TIMEOUT)
                .setCircularRegion(
                    place.getLatLng().latitude, place.getLatLng().longitude, GEOFENCE_RADIUS)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
      }
    }
    geofences = builder.build();
  }

  private GeofencingRequest createGeofencingRequest() {
    return new GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofences(geofences)
        .build();
  }

  private PendingIntent createPendingIntent() {
    Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  @Override
  public void onResult(@NonNull Status status) {
    Log.e(TAG, "Unable to perform geofencing :" + status.getStatusCode());
  }
}
