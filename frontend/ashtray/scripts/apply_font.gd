extends Node

func _ready():
	var font = load("res://assets/fonts/Minecraftia.ttf")
	var font_data = FontFile.new()
	font_data.font_data = font
	
	if get_tree().root.theme:
		get_tree().root.theme.default_font = font_data
		
	else:
		var theme = Theme.new()
		theme.default_font = font_data
		get_tree().root.theme = theme
