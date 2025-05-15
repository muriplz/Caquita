extends Control

var screens = []

func _ready() -> void:
	screens = [
		$Menu,
		$Profile,
		$Inventory
	]
	
func toggle_screen(screen) -> void:
	if screen.visible:
		screen.visible = false
	else:
		for scr in screens:
			scr.visible = (scr == screen)
