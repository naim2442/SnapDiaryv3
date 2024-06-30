package com.example.snapdiaryv3;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingHelper {

    public static void displayLocationName(Context context, Double latitude, Double longitude, TextView textViewLocation) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationName = address.getAddressLine(0); // You can customize how you display the address here
                textViewLocation.setText(locationName);
            } else {
                textViewLocation.setText("Location not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
