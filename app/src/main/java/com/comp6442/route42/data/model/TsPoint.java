package com.comp6442.route42.data.model;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class TsPoint {

  private Timestamp ts;
  private Double latitude;
  private Double longitude;

  public TsPoint() {
  }

  public TsPoint(@NonNull Timestamp ts, @NonNull Double latitude, @NonNull Double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.ts = ts;
  }

  @NonNull
  public Timestamp getTs() {
    return ts;
  }

  @NonNull
  public Double getLatitude() {
    return latitude;
  }

  @NonNull
  public Double getLongitude() {
    return longitude;
  }

  @NonNull
  @Override
  public String toString() {
    return "TsPoint{" +
            "ts=" + ts +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
  }
}