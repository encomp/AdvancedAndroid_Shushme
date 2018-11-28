package com.example.android.shushme;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity
    implements ConnectionCallbacks, OnConnectionFailedListener {

  // Constants
  public static final String TAG = MainActivity.class.getSimpleName();
  private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
  private static final int PLACE_PICKER_REQUEST = 7679;

  // Member variables
  private PlaceListAdapter mAdapter;
  private RecyclerView mRecyclerView;

  /**
   * Called when the activity is starting
   *
   * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Set up the recycler view
    mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new PlaceListAdapter(this);
    mRecyclerView.setAdapter(mAdapter);

    // Build up the LocationServices API client
    // Uses the addApi method to request the LocationServices API
    // Also uses enableAutoManage to automatically when to connect/suspend the client
    GoogleApiClient client =
        new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .addApi(Places.GEO_DATA_API)
            .enableAutoManage(this, this)
            .build();
  }

  /**
   * * Called when the Google API Client is successfully connected
   *
   * @param connectionHint Bundle of data provided to clients by Google Play services
   */
  @Override
  public void onConnected(@Nullable Bundle connectionHint) {
    Log.i(TAG, "API Client Connection Successful!");
  }

  /**
   * * Called when the Google API Client is suspended
   *
   * @param cause cause The reason for the disconnection. Defined by constants CAUSE_*.
   */
  @Override
  public void onConnectionSuspended(int cause) {
    Log.i(TAG, "API Client Connection Suspended!");
  }

  /**
   * * Called when the Google API Client failed to connect to Google Play Services
   *
   * @param result A ConnectionResult that can be used for resolving the error
   */
  @Override
  public void onConnectionFailed(@NonNull ConnectionResult result) {
    Log.e(TAG, "API Client Connection Failed!");
  }

  /**
   * * Button Click event handler to handle clicking the "Add new location" Button
   *
   * @param view
   */
  public void onAddPlaceButtonClicked(View view) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG)
          .show();
      return;
    }
    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
    try {
      Intent intent = intentBuilder.build(this);
      startActivityForResult(intent, PLACE_PICKER_REQUEST);
    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
      Log.e(TAG, e.getMessage());
    }
    Toast.makeText(
            this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_LONG)
        .show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
      Place place = PlacePicker.getPlace(this, data);
      if (place == null) {
        Log.i(TAG, "No place selected.");
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    // Initialize location permissions checkbox
    CheckBox locationPermissions = (CheckBox) findViewById(R.id.location_permission_checkbox);
    if (ActivityCompat.checkSelfPermission(
            MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      locationPermissions.setChecked(false);
    } else {
      locationPermissions.setChecked(true);
      locationPermissions.setEnabled(false);
    }
  }

  public void onLocationPermissionClicked(View view) {
    ActivityCompat.requestPermissions(
        MainActivity.this,
        new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
        PERMISSIONS_REQUEST_FINE_LOCATION);
  }
}
