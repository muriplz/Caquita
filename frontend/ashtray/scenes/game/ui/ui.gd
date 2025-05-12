extends Control

@onready var menu = $PanelContainer
@onready var menu_button = $MarginContainer/MenuButton

func _ready():
	menu_button.pressed.connect(_on_button_pressed)

func _on_button_pressed():
	menu.visible = !menu.visible
	ToastManager.show(str(InventoryStore.get_inventory().height))
