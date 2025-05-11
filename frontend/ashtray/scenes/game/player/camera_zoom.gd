# camera_zoom.gd
extends Camera3D
class_name CameraZoom

@export var zoom_speed: float    = 2.0   # world-units per wheel “notch” or pinch
@export var min_distance: float  = 2.0
@export var max_distance: float  = 20.0

@onready var player = $".."

# backing store for our public property:
var _distance: float = 5.0

var distance: float = 5.0:
	set(new_val):
		_distance = clamp(new_val, min_distance, max_distance)
		if has_method("_update_camera_transform"):
			call("_update_camera_transform")
	get:
		return _distance

func _ready() -> void:
	if player:
		var offset = global_transform.origin - player.global_transform.origin
		_distance = offset.length()

func _input(event: InputEvent) -> void:
	# desktop wheel
	if event is InputEventMouseButton:
		if event.button_index == MOUSE_BUTTON_WHEEL_UP and event.pressed:
			distance -= zoom_speed
		elif event.button_index == MOUSE_BUTTON_WHEEL_DOWN and event.pressed:
			distance += zoom_speed

	# mobile pinch
	elif event is InputEventMagnifyGesture:
		distance /= event.factor
