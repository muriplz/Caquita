extends Button

@onready var menu = $"../../Menu"
@onready var ui: Control = $"../.."

func _ready():
	pressed.connect(self._on_button_pressed)


func _on_button_pressed() -> void:
	ui.toggle_screen(menu)
