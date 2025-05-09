extends Node3D

signal world_position_changed(world_pos: Vector3)

@onready var _player: Node3D = $"../Player"
var gps = Engine.get_singleton("GPSPlugin")
var init = false

func _ready() -> void:
	if gps:
		gps.connect("location_updated", Callable(self, "_on_location_updated"))
		gps.start()
	else:
		# Only happens when on dev env
		ToastManager.show("Hey muri :)")
		_on_location_updated(-33.8688, 151.2093)


func _on_location_updated(lat: float, lon: float) -> void:
	if not init:
		init = true
		CoordConversion.set_origin(lat, lon)
	
	var world_pos: Vector3 = CoordConversion.latlon_to_world(lat, lon)
	emit_signal("world_position_changed", world_pos)


func _move_player(world_pos: Vector3) -> void:
	var t := _player.global_transform
	t.origin = world_pos
	_player.global_transform = t
