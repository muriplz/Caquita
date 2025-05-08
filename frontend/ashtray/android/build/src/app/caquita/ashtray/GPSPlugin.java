package app.caquita.ashtray;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Collections;
import java.util.Set;

public class GPSPlugin extends GodotPlugin implements LocationListener {
    private final LocationManager lm;
    private final Context ctx;
    private Location lastLocation;

    public GPSPlugin(Godot godot) {
        super(godot);
        ctx = godot.getActivity();
        lm = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public String getPluginName() {
        return "GPSPlugin";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(new SignalInfo("location_updated", Double.class, Double.class));
    }

    @UsedByGodot
    public void start() {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            System.exit(0);
            return;
        }

        boolean hasGps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean hasNet = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!hasGps && !hasNet) {
            Toast.makeText(ctx, "No location provider available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasGps) {
            Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                emitSignal("location_updated", last.getLatitude(), last.getLongitude());
            }
        }

        if (hasGps) {
            lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,    // every 2s
                    1f,                // every 1 meter
                    this,
                    Looper.getMainLooper()
            );
        }

        if (hasNet) {
            lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000,    // every 2s
                    5f,                // every 5 meters
                    this,
                    Looper.getMainLooper()
            );
        }
    }

    @UsedByGodot
    public void stop() {
        lm.removeUpdates(this);
        lastLocation = null;
    }

    @UsedByGodot
    public Dictionary get_last_location() {
        Dictionary d = new Dictionary();
        if (lastLocation != null) {
            d.put("latitude",  lastLocation.getLatitude());
            d.put("longitude", lastLocation.getLongitude());
        }
        return d;
    }

    @Override
    public void onLocationChanged(Location loc) {
        lastLocation = loc;
        new Handler(Looper.getMainLooper()).post(() ->
                emitSignal("location_updated", loc.getLatitude(), loc.getLongitude())
        );
    }
    // Unused stubs:
    @Override public void onStatusChanged(String s, int i, Bundle b) {}
    @Override public void onProviderEnabled(String s) {}
    @Override public void onProviderDisabled(String s) {}
}
