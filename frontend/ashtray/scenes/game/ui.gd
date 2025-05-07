extends Control

@onready var panel = $PanelContainer
@onready var button = $Button

func _ready():
	button.pressed.connect(_on_button_pressed)

func _on_button_pressed():
	panel.visible = !panel.visible
