extends Control

const CELL_SCENE = preload("res://scenes/game/ui/inventory/cell.tscn")
const INVENTORY_ITEM_SCENE = preload("res://scenes/game/ui/inventory/inventory_item.tscn")

const CELL_SIZE := Vector2(120, 120)

var hovered_cell

func _ready() -> void:
	InventoryStore.connect("inventory_ready", Callable(self, "_on_inventory_ready"))
	InventoryStore.sync()
	

func _on_inventory_ready() -> void:
	var cols = InventoryStore.inventory.width
	var rows = InventoryStore.inventory.height

	var total_w = cols * CELL_SIZE.x + cols
	var total_h = rows * CELL_SIZE.y + rows
	$NinePatchRect.custom_minimum_size = Vector2(total_w, total_h)
	
	create_cells(cols, rows)
	populate_items()
	

func populate_items() -> void: 
	for inventory_item in InventoryStore.inventory.items:
		var inventory_item_scene = INVENTORY_ITEM_SCENE.instantiate()
		
		var shape = ItemKindStore.get_item_kind(inventory_item.id).shape
		var dims = ShapeUtils.get_dimensions(shape)
		
		inventory_item_scene.size = Vector2(
			dims.x * CELL_SIZE.x,
			dims.y * CELL_SIZE.y
		)
		
		inventory_item_scene.item = InventoryItem.from_dict(inventory_item)

		$NinePatchRect.add_child(inventory_item_scene)


func create_cells(cols, rows) -> void:
	for y in range(rows):
		for x in range(cols):
			var cell = CELL_SCENE.instantiate()
			cell.size = CELL_SIZE
			var offset = Vector2(x * CELL_SIZE.x, y * CELL_SIZE.y)
			cell.i = x
			cell.j = y
			cell.position = offset

			$NinePatchRect.add_child(cell)
