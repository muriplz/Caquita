extends Resource
class_name Inventory

@export var id: int
@export var user_id: int
@export var items: Array[InventoryItem] = []
@export var width: int
@export var height: int


static func from_dict(dict: Dictionary) -> Inventory:
	var inventory = Inventory.new()
	inventory.id = dict.get("id", 0)
	inventory.user_id = dict.get("user_id", 0)
	
	var items_array = dict.get("items", [])
	inventory.items.clear()
	for item_dict in items_array:
		inventory.items.append(InventoryItem.from_dict(item_dict))
		
	inventory.height = dict.get("height", 3)
	inventory.width = dict.get("width", 2)
	
	return inventory
