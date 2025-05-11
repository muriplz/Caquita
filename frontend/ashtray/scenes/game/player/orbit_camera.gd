# orbit_camera.gd
extends Camera3D
class_name OrbitCamera

const SENSITIVITY: float    = 1.0
const GROUND_PLANE: Plane   = Plane(Vector3.UP, 0)
const MIN_PIVOT_DIST: float = 0.1

@export var distance: float      = 10.0
@export var height_offset: float = 5.0

@onready var player: Node3D = $".."

var yaw: float
var dragging: bool
var drag_cam_transform: Transform3D
var last_hit: Vector3

func _ready() -> void:
	set_process_input(true)
	set_process_unhandled_input(true)
	var offset = global_transform.origin - player.global_transform.origin
	distance      = Vector2(offset.x, offset.z).length()
	height_offset = offset.y
	yaw           = atan2(offset.x, offset.z)

func _process(delta: float) -> void:
	_update_camera_transform()

func _input(event: InputEvent) -> void:
	if (event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT) \
	or (event is InputEventScreenTouch):
		if event.pressed:
			drag_cam_transform = global_transform
			var old_tf = global_transform
			global_transform = drag_cam_transform
			var hit = GROUND_PLANE.intersects_ray(
				project_ray_origin(event.position),
				project_ray_normal(event.position)
			)
			global_transform = old_tf
			if hit:
				dragging = true
				last_hit = hit
		else:
			dragging = false
		return

	if dragging and (event is InputEventMouseMotion or event is InputEventScreenDrag):
		var old_tf = global_transform
		global_transform = drag_cam_transform
		var hit = GROUND_PLANE.intersects_ray(
			project_ray_origin(event.position),
			project_ray_normal(event.position)
		)
		global_transform = old_tf

		var p = player.global_transform.origin
		if not hit or hit.distance_to(p) < MIN_PIVOT_DIST:
			yaw -= event.relative.x * SENSITIVITY * 0.01
		else:
			var v0 = Vector2(last_hit.x - p.x, last_hit.z - p.z).normalized()
			var v1 = Vector2(hit.x      - p.x, hit.z      - p.z).normalized()
			yaw += wrapf(atan2(v0.y, v0.x) - atan2(v1.y, v1.x), -PI, PI) * SENSITIVITY
			last_hit = hit

func _update_camera_transform() -> void:
	var x = sin(yaw) * distance
	var z = cos(yaw) * distance
	global_transform.origin = player.global_transform.origin + Vector3(x, height_offset, z)
	look_at(player.global_transform.origin, Vector3.UP)
