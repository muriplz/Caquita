extends Resource
class_name CarvingItem

@export var item: String
@export var erre: float
@export var anchorX: int
@export var anchorY: int

static func from_dict(dict: Dictionary) -> CarvingItem:
	var carving_item = CarvingItem.new()
	
	carving_item.item = dict.get("item", "")
	carving_item.erre = dict.get("erre", 0.5)
	carving_item.anchorX = dict.get("anchorX", 0)
	carving_item.anchorY = dict.get("anchorY", 0)
	
	return carving_item
