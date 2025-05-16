extends TextureRect

const CELL_SIZE = Vector2(120, 120)

var id: String
var anchor: Cell

var inventory: Inventory

const BASE_VALUE = 24

func _ready() -> void:
	var tex = load("res://assets/textures/items/%s.png" % [id.replace(":", "/")])
	set_texture(tex)
	
	var cols = inventory.width
	var rows = inventory.height

	var total_w = cols * CELL_SIZE.x + cols - 1
	var total_h = rows * CELL_SIZE.y + rows - 1

	var origin = -Vector2(total_w, total_h) * 0.5
