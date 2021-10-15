package com.comp6442.route42.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.comp6442.route42.R;
import com.comp6442.route42.data.ActiveMapViewModel;
import com.comp6442.route42.data.model.Activity;
import com.comp6442.route42.data.model.BaseActivity;
import com.comp6442.route42.data.model.MockLocation;
import com.comp6442.route42.data.repository.FirebaseStorageRepository;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.Timer;

import timber.log.Timber;


public class ActiveMapFragment extends MapFragment {
  private final boolean mockMode;
  Timer timer = new Timer();
  private boolean requestingLocationUpdates = false;
  private ActiveMapViewModel activeMapViewModel;
  private TextView activityMetricsText;
  private LocationCallback locationCallBack;
  private FloatingActionButton activityButton;

//     class MockDataUpdate extends TimerTask {
//        @Override
//        public void run() {
//            Timber.i("setting mock location ");
//            try {
//                Timber.i("getDeviceLocation: getting the devices current location");
////                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(deviceLocation::setValue);
//            } catch (SecurityException e) {
//                Timber.w("Unable to get current location");
//                Toast.makeText(getActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
//            } catch (RuntimeException e) {
//                Timber.e(e);
//                Toast.makeText(getActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
//            }
//        }
//        void setViewModelDeviceLocation() {
//
//        }
//    }

  public ActiveMapFragment(boolean mockMode) {
    super(R.id.map_fragment2, R.raw.style_json_activity_map);
    this.mockMode = mockMode;
  }

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
    activeMapViewModel = new ViewModelProvider(requireActivity()).get(ActiveMapViewModel.class);
    locationCallBack = new LocationCallback() {
      @Override
      public void onLocationResult(@NonNull LocationResult locationResult) {
        super.onLocationResult(locationResult);
        if (mockMode)
          activeMapViewModel.setMockDeviceLocation();
        else
          activeMapViewModel.setDeviceLocation(locationResult.getLastLocation());
        renderMap(locationResult.getLastLocation());
        Timber.i("location result ready");
      }

      @Override
      public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);
        Timber.i("location available");
      }
    };
    return inflater.inflate(R.layout.active_map_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (mockMode) {
      @SuppressLint("MissingPermission") Observer<Location> deviceLocationObserver = updatedLocation -> {
        // update the mock location in the provider
//                    Timber.i("updated mock location to " + updatedLocation.toString());
//                    Task<Void> updateFusedProvider = fusedLocationProviderClient.setMockLocation(updatedLocation);
//                   updateFusedProvider.addOnFailureListener(err-> {
//                      Timber.e(err);
//                   });
      };
      activeMapViewModel.setMockDeviceLocation();
      activeMapViewModel.getDeviceLocation().observe(getViewLifecycleOwner(), deviceLocationObserver);
    }

    //set active icon
    ImageView activityIconView = view.findViewById(R.id.activity_icon);
    Glide.with(activityIconView.getContext())
            .load(Activity.Activity_Type.getIconResource(getArguments().getInt("activity")))
            .into(activityIconView);

    //set metrics
    activityMetricsText = view.findViewById(R.id.activity_metrics_text);
    activityButton = view.findViewById(R.id.activity_button);
    activityButton.setOnClickListener(click -> {
      activityBtnClickHandler();
    });
  }

  private void endUserActivity() {
    GoogleMap.SnapshotReadyCallback snapshotCallback = bitmap -> {
      try {
        String baseFilename = "activity_route";
        String localFilename = baseFilename + ".png";
        String storageFilename = baseFilename + new Date().toString() + ".png";
        activeMapViewModel.setSnapshotFileName(storageFilename);
        FileOutputStream out = getContext().openFileOutput(localFilename, 0);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        FirebaseStorageRepository.getInstance().uploadSnapshotFromLocal(localFilename, storageFilename, getContext().getFilesDir().getPath());
        Bundle bundle = new Bundle();
        bundle.putString("uid", getArguments().getString("uid"));
        bundle.putString("img_path", "/" + localFilename);
        Fragment fragment = new CreatePostFragment();
        fragment.setArguments(bundle);
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit();
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    googleMap.setOnMapLoadedCallback(() -> {
      Timber.i("clicked activity button");
      googleMap.snapshot(snapshotCallback);
      googleMap.setOnMapLoadedCallback(null);
    });
  }

  @Override
  protected void initializeMap() {
    mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment2);
    assert mapFragment != null;
    mapFragment.getMapAsync(this);
  }

  @SuppressLint("MissingPermission")
  @Override
  protected void renderMap(Location location) {
    Timber.i("Received location from fusedLocationProvider: %s", location);

    // Set the map's camera position to the current location of the device.
    if (location != null) {
      LatLng locationLatLng = MockLocation.latLngFromLocation(location);

      // add current location marker
      googleMap.clear();
      googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(locationLatLng).title("User"));

      // track user using camera
      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
              locationLatLng, 20));
    }

    // add activity route polyline
    if (activeMapViewModel.hasPastLocations()) {
      googleMap.addPolyline(
              new PolylineOptions()
                      .endCap(new RoundCap())
                      .clickable(false)
                      .addAll(activeMapViewModel.getPastLocations())
                      .width(25)
                      .color(Color.BLUE)
      );
    }

    // update activity
    if (activeMapViewModel.getLastUpdateTime() != null) {
      activeMapViewModel.updateElapsedTime();
    }
    activeMapViewModel.setLastUpdateTime(new Date());

    Activity userActivityData = new BaseActivity(
            activeMapViewModel.getPastLocations(),
            activeMapViewModel.getElapsedTime()
    );
    activeMapViewModel.setActivityData(userActivityData);
    activityMetricsText.setText(userActivityData.toString());
  }

  public void activityBtnClickHandler() {
    String[] dialogItems;
    if (!requestingLocationUpdates) dialogItems = new String[]{"Start", "End Activity"};
    else dialogItems = new String[]{"Pause", "End Activity"};

    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(
            new ContextThemeWrapper(requireActivity(), R.style.AlertDialog_AppCompat)
    );
    dialogBuilder.setTitle("Select Action")
            .setItems(dialogItems, (dialogInterface, i) -> {
              if (i == 1) endUserActivity();
              else if (i == 0) {
                if (requestingLocationUpdates) stopLocationUpdates();
                else startLocationUpdates();
              }
            }).create().show();
  }

  private void stopLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    requestingLocationUpdates = false;
    activeMapViewModel.setLastUpdateTime(null);
  }

  private void startLocationUpdates() {
    try {
      Timber.i("getDeviceLocation: getting the devices current location");
      if (mockMode) {
        Timber.i("setting mock mode to true");
        fusedLocationProviderClient.setMockMode(mockMode);
        fusedLocationProviderClient.setMockLocation(activeMapViewModel.getDeviceLocation().getValue());
      }
      requestingLocationUpdates = true;
      LocationRequest locationRequest = LocationRequest.create();
      long intervalMillis = 1000;
      locationRequest.setInterval(intervalMillis).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    } catch (SecurityException e) {
      Timber.w("Unable to get current location");
      Toast.makeText(getActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
    } catch (RuntimeException e) {
      Timber.e(e);
      Toast.makeText(getActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (requestingLocationUpdates) {
      startLocationUpdates();
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    stopLocationUpdates();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    timer.cancel();
    stopLocationUpdates();
  }
}