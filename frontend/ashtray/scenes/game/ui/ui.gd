extends Control

@onready var panel = $PanelContainer
@onready var menu_button = $MarginContainer/MenuButton

func _ready():
	menu_button.pressed.connect(_on_button_pressed)

func _on_button_pressed():
	panel.visible = !panel.visible
