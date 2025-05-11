extends Node

func _ready():
	var font = load("res://assets/fonts/Minecraftia.ttf")
	var font_data = FontFile.new()
	font_data.font_data = font
	
	var theme = Theme.new()
	theme.default_font = font_data
	get_tree().root.theme = theme
