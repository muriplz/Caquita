extends Control

const SLOT_SCENE = preload("res://scenes/game/ui/inventory/inventory_slot.tscn")
const INVENTORY_ITEM_SCENE = preload("res://scenes/game/ui/inventory/inventory_item.tscn")

const CELL_SIZE = Vector2(120, 120)

func _ready() -> void:
	InventoryStore.connect("inventory_ready", Callable(self, "_on_inventory_ready"))
	
func _on_inventory_ready() -> void:
	var inventory = InventoryStore.get_inventory()
	var cols = inventory.width
	var rows = inventory.height

	var total_w = cols * CELL_SIZE.x + cols - 1
	var total_h = rows * CELL_SIZE.y + rows - 1

	var origin = -Vector2(total_w, total_h) * 0.5

	for y in range(rows):
		for x in range(cols):
			var slot = SLOT_SCENE.instantiate()
			slot.size = CELL_SIZE
			var offset = Vector2(x * CELL_SIZE.x, y * CELL_SIZE.y)
			slot.slot_coords = Vector2(x, y)
			slot.position = origin + offset

			add_child(slot)
			
	for inventory_item in inventory.items:
		var inventory_item_scene = INVENTORY_ITEM_SCENE.instantiate()
		
		inventory_item_scene.id = inventory_item.id
		
		var shape = ItemKindStore.get_item_kind(inventory_item.id).shape
		
		var orientation = inventory_item.orientation as InventoryItem.Orientation
		inventory_item_scene.inventory = inventory

		var cells = inventory_item.cells
		
		add_child(inventory_item_scene)
