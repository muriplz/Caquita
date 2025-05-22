extends Resource
class_name Inventory

@export var id: int
@export var user_id: int
@export var items: Array
@export var width: int
@export var height: int


static func from_dict(dict: Dictionary) -> Inventory:
	var inventory = Inventory.new()
	inventory.id = dict.get("id", 0)
	inventory.user_id = dict.get("user_id", 0)
	inventory.items = dict.get("items", [])
	print(inventory.items)
	inventory.height = dict.get("height", 3)
	inventory.width = dict.get("width", 2)
	
	return inventory
