# CameraController.gd
extends Camera3D
class_name CameraController

# --- constants (tweak these!)
const SENSITIVITY: float    = 1.1               # drag→angle multiplier
const GROUND_PLANE: Plane   = Plane(Vector3.UP, 0)

@onready var player: Node3D = $".."

# --- runtime vars
var distance: float          # horizontal distance from player
var height_offset: float     # vertical offset from player.y
var yaw: float               # current heading around player

var dragging: bool = false
var drag_start_hit: Vector3
var drag_start_yaw: float

func _ready() -> void:
	# compute initial horizontal circle & height
	var offset = global_transform.origin - player.global_transform.origin
	distance      = Vector2(offset.x, offset.z).length()
	height_offset = offset.y
	yaw           = atan2(offset.x, offset.z)

func _process(delta: float) -> void:
	_update_camera_transform()

func _input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		if event.pressed:
			var from = project_ray_origin(event.position)
			var dir  = project_ray_normal(event.position)
			var hit  = GROUND_PLANE.intersects_ray(from, dir)
			if hit != null:
				dragging         = true
				drag_start_hit   = hit
				drag_start_yaw   = yaw
		else:
			dragging = false
		return

	if dragging and event is InputEventMouseMotion:
		var from = project_ray_origin(event.position)
		var dir  = project_ray_normal(event.position)
		var hit  = GROUND_PLANE.intersects_ray(from, dir)
		if hit == null:
			return

		# compute normalized 2D directions from player to hit points
		var p = player.global_transform.origin
		var v0 = Vector2(drag_start_hit.x - p.x, drag_start_hit.z - p.z).normalized()
		var v1 = Vector2(hit.x - p.x,           hit.z - p.z).normalized()
		# signed angle delta between them
		var angle0 = atan2(v0.y, v0.x)
		var angle1 = atan2(v1.y, v1.x)
		var Δθ     = wrapf(angle0 - angle1, -PI, PI)

		yaw = drag_start_yaw + Δθ * SENSITIVITY

		# no need to call _update here; it'll run in your next _process
		# but you can if you want immediate feedback:
		_update_camera_transform()

func _update_camera_transform() -> void:
	# rebuild horizontal-offset + fixed height
	var x = sin(yaw) * distance
	var z = cos(yaw) * distance
	global_transform.origin = player.global_transform.origin + Vector3(x, height_offset, z)
	look_at(player.global_transform.origin, Vector3.UP)
