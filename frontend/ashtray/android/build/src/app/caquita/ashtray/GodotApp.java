package app.caquita.ashtray;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.godotengine.godot.Godot;
import org.godotengine.godot.GodotActivity;
import org.godotengine.godot.plugin.GodotPlugin;

import java.util.Collections;
import java.util.Set;

public class GodotApp extends GodotActivity {
	private static final int GPS_PERMISSION_REQUEST_CODE = 123;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ensureGPS();
		super.onCreate(savedInstanceState);
		// make toast with all plugins enabled

		new Handler(Looper.getMainLooper()).post(() -> {

			Set<GodotPlugin> plugins = getHostPlugins(getGodot());

			Toast.makeText(this, "Plugins enabled: " + plugins.iterator().next().getPluginName(), Toast.LENGTH_LONG).show();
		});
	}

	@Override
	public Set<GodotPlugin> getHostPlugins(Godot engine) {
		return Collections.singleton(new GPSPlugin(engine));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		ensureGPS();
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void ensureGPS() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(
					this,
					new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
					GPS_PERMISSION_REQUEST_CODE
			);
		}
	}
}
