extends Control

@onready var screens := [
		$Menu,
		$Profile,
		$Inventory
	]
	
func toggle_screen(screen: Control) -> void:
	var will_open = not screen.visible

	for scr in screens:
		scr.visible = false
	if will_open:
		screen.visible = true
