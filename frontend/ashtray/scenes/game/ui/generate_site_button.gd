extends Button

@onready var carving_site: Control = $"../../../CarvingSite"
@onready var ui: CanvasLayer = $"../../.."

func _ready():
	pressed.connect(self._on_button_pressed)


func _on_button_pressed() -> void:
	CarvingService.generate(41.3851, 2.1734)
	ui.toggle_screen(carving_site)
