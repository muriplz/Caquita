extends Camera3D

const SENSITIVITY: float       = 1
const GROUND_PLANE: Plane      = Plane(Vector3.UP, 0)
const MIN_PIVOT_DIST: float    = 0.1

@onready var player: Node3D     = $".."

var distance: float
var height_offset: float
var yaw: float

var dragging: bool              = false
var drag_cam_transform: Transform3D
var last_hit: Vector3

func _ready() -> void:
	var offset = global_transform.origin - player.global_transform.origin
	distance      = Vector2(offset.x, offset.z).length()
	height_offset = offset.y
	yaw           = atan2(offset.x, offset.z)

func _process(delta: float) -> void:
	_update_camera_transform()

func _input(event: InputEvent) -> void:
	# unify “press” for mouse or touch
	if (event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT) or (event is InputEventScreenTouch):
		var pressed = event.pressed
		var pos     = event.position

		if pressed:
			# snapshot camera transform
			drag_cam_transform = global_transform
			# initial pick on y=0 plane
			var old = global_transform
			global_transform = drag_cam_transform
			var from = project_ray_origin(pos)
			var dir  = project_ray_normal(pos)
			global_transform = old

			var hit = GROUND_PLANE.intersects_ray(from, dir)
			if hit:
				dragging = true
				last_hit = hit
		else:
			dragging = false
		return

	# unify “move” for mouse or touch‐drag
	if dragging and (event is InputEventMouseMotion or event is InputEventScreenDrag):
		var pos    = event.position
		var rel_x  = event.relative.x

		# use frozen camera for raycast
		var old = global_transform
		global_transform = drag_cam_transform
		var from = project_ray_origin(pos)
		var dir  = project_ray_normal(pos)
		global_transform = old

		var hit = GROUND_PLANE.intersects_ray(from, dir)
		var p   = player.global_transform.origin

		if hit == null or hit.distance_to(p) < MIN_PIVOT_DIST:
			# fallback to raw delta
			yaw -= rel_x * SENSITIVITY * 0.01
		else:
			# incremental ground‐plane drag
			var v0 = Vector2(last_hit.x - p.x, last_hit.z - p.z).normalized()
			var v1 = Vector2(hit.x      - p.x, hit.z      - p.z).normalized()
			var a0 = atan2(v0.y, v0.x)
			var a1 = atan2(v1.y, v1.x)
			var dθ = wrapf(a0 - a1, -PI, PI)
			yaw += dθ * SENSITIVITY
			last_hit = hit

func _update_camera_transform() -> void:
	var x = sin(yaw) * distance
	var z = cos(yaw) * distance
	global_transform.origin = player.global_transform.origin + Vector3(x, height_offset, z)
	look_at(player.global_transform.origin, Vector3.UP)
