extends Resource
class_name InventoryItem

enum Orientation { UP, DOWN, LEFT, RIGHT }

@export var id: String
@export var cells: Array
@export var orientation: Orientation
@export var erre: float

static func from_dict(dict: Dictionary) -> InventoryItem:
	var inventory_item = InventoryItem.new()
	inventory_item.id = dict.get("id", "")
	inventory_item.cells = dict.get("cells", [])
	inventory_item.orientation = Orientation[dict.get("orientation", "UP")]
	inventory_item.erre = dict.get("erre")
	
	return inventory_item
