extends OrbitCamera
class_name OrbitZoom

@export var zoom_curve:   Curve = Curve.new()
@export var min_distance: float = 3.0
@export var max_distance: float = 30.0
@export var min_height:   float = 1
@export var max_height:   float = 10.0
@export var zoom_speed:   float = 0.02

var _zoom_t: float = 0.0
var zoom_t: float = 0.0:
	set(v):
		_zoom_t = clamp(v, 0.0, 1.0)
		_apply_zoom()
	get:
		return _zoom_t

func _ready() -> void:
	set_process_unhandled_input(true)
	_zoom_t = clamp((distance - min_distance) / (max_distance - min_distance), 0.0, 1.0)
	_apply_zoom()

func _apply_zoom() -> void:
	distance = lerp(min_distance, max_distance, _zoom_t)
	var h = zoom_curve.sample(_zoom_t)
	height_offset = lerp(min_height, max_height, h)
	_update_camera_transform()

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.pressed:
		if event.button_index == MOUSE_BUTTON_WHEEL_UP:
			zoom_t -= zoom_speed
			get_viewport().set_input_as_handled()
		elif event.button_index == MOUSE_BUTTON_WHEEL_DOWN:
			zoom_t += zoom_speed
			get_viewport().set_input_as_handled()
	elif event is InputEventMagnifyGesture:
		zoom_t -= (event.factor - 1.0) * zoom_speed
		get_viewport().set_input_as_handled()
