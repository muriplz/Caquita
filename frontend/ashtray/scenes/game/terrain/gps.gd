extends Node3D

signal world_position_changed(world_pos: Vector3)

const ZOOM := 16
const TILE_SIZE := 16

@onready var gps = Engine.get_singleton("GPSPlugin")

func _ready():
	if OS.get_name() == "Android":
		gps.connect("location_updated", Callable(self, "_on_location_updated"))
		gps.start()

func _on_location_updated(lat: float, lon: float) -> void:
	var world_pos = _latlon_to_world(lat, lon)
	emit_signal("world_position_changed", world_pos)

func _latlon_to_world(lat: float, lon: float) -> Vector3:
	var rad_lat = deg_to_rad(lat)
	var n = pow(2, ZOOM)
	var xt = (lon + 180.0) / 360.0 * n
	var yt = (1.0 - log(tan(rad_lat) + 1.0 / cos(rad_lat)) / PI) / 2.0 * n
	return Vector3(xt * TILE_SIZE, 0, yt * TILE_SIZE)
