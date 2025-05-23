extends OrbitCamera
class_name OrbitZoom

@export var zoom_curve := Curve.new()
@export var min_distance := 3.0
@export var max_distance := 30.0
@export var min_height := 1.0
@export var max_height := 10.0
@export var zoom_speed := 0.02

var _zoom_t : = 0.0
var zoom_t: float:
	set(v):
		_zoom_t = clamp(v, 0.0, 1.0)
		_apply_zoom()
	get:
		return _zoom_t

var _touches := {}        # index → position
var _pinch_dist: float = 0.0
var _pinching:    bool  = false

func _ready() -> void:
	set_process_unhandled_input(true)
	_zoom_t = clamp((distance - min_distance) / (max_distance - min_distance), 0.0, 1.0)
	_apply_zoom()

func _apply_zoom() -> void:
	distance      = lerp(min_distance, max_distance, _zoom_t)
	height_offset = lerp(min_height,   max_height,   zoom_curve.sample(_zoom_t))
	_update_camera_transform()

func _unhandled_input(e: InputEvent) -> void:
	if e is InputEventScreenTouch:
		if e.pressed:
			_touches[e.index] = e.position
		else:
			_touches.erase(e.index)
			_pinching = false
		return

	if e is InputEventScreenDrag and _touches.size() == 2:
		_touches[e.index] = e.position
		var keys = _touches.keys()
		var p0 = _touches[keys[0]]
		var p1 = _touches[keys[1]]
		var d  = p0.distance_to(p1)
		if not _pinching:
			_pinch_dist = d
			_pinching   = true
		else:
			zoom_t    -= (d - _pinch_dist) * zoom_speed * 0.01
			_pinch_dist = d
			get_viewport().set_input_as_handled()
		return

	if e is InputEventMagnifyGesture:
		zoom_t -= (e.factor - 1.0) * zoom_speed
		get_viewport().set_input_as_handled()
		return

	if e is InputEventMouseButton and e.pressed:
		if e.button_index == MOUSE_BUTTON_WHEEL_UP:
			zoom_t -= zoom_speed
			get_viewport().set_input_as_handled()
		elif e.button_index == MOUSE_BUTTON_WHEEL_DOWN:
			zoom_t += zoom_speed
			get_viewport().set_input_as_handled()
