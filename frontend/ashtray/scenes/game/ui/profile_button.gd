extends TextureButton

@onready var profile = $"../Profile"
@onready var ui: CanvasLayer = $".."

func _ready() -> void:
	texture_normal = load("res://assets/textures/ui/avatar/%s.png" % UserStore.get_user().avatar)

	custom_minimum_size = Vector2(100, 100)
	size = Vector2(100, 100)

	stretch_mode = STRETCH_KEEP_ASPECT_COVERED

	pressed.connect(self._on_button_pressed)

func _on_button_pressed() -> void:
	ui.toggle_screen(profile)
