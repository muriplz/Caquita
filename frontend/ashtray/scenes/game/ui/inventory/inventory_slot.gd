extends Panel

@export var slot_coords: Vector2 = Vector2.ZERO
@export var normal_color: Color = Color(1,1,1,1)
@export var hover_color:  Color = Color(0.8,0.8,0.8,1)

func _ready() -> void:
	modulate = normal_color
	connect("mouse_entered", Callable(self, "_on_mouse_entered"))
	connect("mouse_exited",  Callable(self, "_on_mouse_exited"))

func _on_mouse_entered() -> void:
	modulate = hover_color

func _on_mouse_exited() -> void:
	modulate = normal_color
