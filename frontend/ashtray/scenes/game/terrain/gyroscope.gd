extends Node3D

signal gyroscope_changed(x: float, y: float, z: float)

@onready var _player: Node3D = $"../Player"
var gyroscope = Engine.get_singleton("GyroscopePlugin")

func _ready() -> void:
	if gyroscope:
		gyroscope.connect("gyroscope_updated", Callable(self, "_on_gyroscope_updated"))
		gyroscope.start()

func _on_gyroscope_updated(x: float, y: float, z: float) -> void:
	emit_signal("gyroscope_changed", x, y, z)
	ToastManager.show("Gyro %s, %s, %s" % [x, y, z])

func _move_player(world_pos: Vector3) -> void:
	var t := _player.global_transform
	t.origin = world_pos
	_player.global_transform = t
