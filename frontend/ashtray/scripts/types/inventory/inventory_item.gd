extends Resource
class_name InventoryItem

enum Orientation { UP, DOWN, LEFT, RIGHT }

@export var id: String = ""
@export var cells: Array[Cell] = []
@export var orientation: int = Orientation.UP

static func from_dict(dict: Dictionary) -> User:
	var inventory_item = InventoryItem.new()
	inventory_item.id = dict.get("id", "")
	inventory_item.cells = dict.get("cells", [])
	
	var o_name = dict.get("orientation", "")
	if Orientation.has(o_name):
		inventory_item.orientation = Orientation[o_name]
	
	return inventory_item
