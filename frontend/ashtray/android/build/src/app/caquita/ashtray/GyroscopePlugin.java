package app.caquita.ashtray;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Collections;
import java.util.Set;

public class GyroscopePlugin extends GodotPlugin implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor gyroscope;
    private final Context ctx;
    private boolean isRunning = false;

    public GyroscopePlugin(Godot godot) {
        super(godot);
        ctx = godot.getActivity();
        sensorManager = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public String getPluginName() {
        return "GyroscopePlugin";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(new SignalInfo("gyroscope_updated", Float.class, Float.class, Float.class));
    }

    @UsedByGodot
    public void start() {
        if (gyroscope == null) {
            emitSignal("gyroscope_updated", 0f, 0f, 0f);
            return;
        }
        if (!isRunning) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
            isRunning = true;
        }
    }

    @UsedByGodot
    public void stop() {
        if (isRunning) {
            sensorManager.unregisterListener(this, gyroscope);
            isRunning = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // event.values[0] = rotation rate around x (rad/s)
        // event.values[1] = rotation rate around y
        // event.values[2] = rotation rate around z
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        emitSignal("gyroscope_updated", x, y, z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
