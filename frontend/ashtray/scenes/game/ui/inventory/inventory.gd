extends Control
const CELL_SCENE = preload("res://scenes/game/ui/inventory/cell.tscn")
const INVENTORY_ITEM_SCENE = preload("res://scenes/game/ui/inventory/inventory_item.tscn")
const CELL_SIZE := Vector2(120, 120)
const PADDING := 20
var hovered_cell

func get_cell_position(col: int, row: int) -> Vector2:
	return Vector2(
		PADDING + col * CELL_SIZE.x,
		PADDING + row * CELL_SIZE.y
	)

func _ready() -> void:
	InventoryStore.connect("inventory_ready", Callable(self, "_on_inventory_ready"))
	InventoryStore.connect("inventory_updated", Callable(self, "_on_inventory_updated"))

func rebuild_inventory() -> void:
	InventoryStore.sync()
	await InventoryStore.inventory_ready

func _on_inventory_ready() -> void:
	clear_inventory()
	build_inventory()

func _on_inventory_updated() -> void:
	update_existing_items()

func clear_inventory() -> void:
	for child in $NinePatchRect.get_children():
		child.queue_free()

func build_inventory() -> void:
	var cols = InventoryStore.inventory.width
	var rows = InventoryStore.inventory.height
	var total_w = cols * CELL_SIZE.x + PADDING * 2
	var total_h = rows * CELL_SIZE.y + PADDING * 2
	$NinePatchRect.custom_minimum_size = Vector2(total_w, total_h)
	
	create_cells(cols, rows)
	populate_items()

func populate_items() -> void:
	for inventory_item in InventoryStore.get_inventory().items:
		var inventory_item_scene = INVENTORY_ITEM_SCENE.instantiate()

		var shape = ItemKindStore.get_item_kind(inventory_item.id).shape
		var dims = ShapeUtils.get_dimensions(shape)
		
		inventory_item_scene.size = Vector2(
			dims.x * CELL_SIZE.x,
			dims.y * CELL_SIZE.y
		)
		
		inventory_item_scene.item = inventory_item
		
		var anchor = ShapeUtils.get_anchor(inventory_item_scene.item)
		inventory_item_scene.position = get_cell_position(anchor.col, anchor.row)
		
		$NinePatchRect.add_child(inventory_item_scene)

func create_cells(cols, rows) -> void:
	for y in range(rows):
		for x in range(cols):
			var cell = CELL_SCENE.instantiate()
			cell.size = CELL_SIZE
			cell.position = get_cell_position(x, y)
			cell.i = x
			cell.j = y
			$NinePatchRect.add_child(cell)

func update_existing_items():
	for child in $NinePatchRect.get_children():
		if child.has_method("update_position_from_anchor"):
			var updated_item = find_updated_item_data(child.item.id, child.item.erre)
			if updated_item:
				child.item = updated_item
				child.anchor = ShapeUtils.get_anchor(child.item)
				child.update_position_from_anchor()

func find_updated_item_data(item_id: String, item_erre: float):
	for inventory_item in InventoryStore.inventory.items:
		if inventory_item.id == item_id and inventory_item.erre == item_erre:
			return inventory_item
	return null
