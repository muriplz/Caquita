extends TextureRect

var item: InventoryItem
var anchor: Cell
var _dragging := false
var _original_position: Vector2
var _tween: Tween
var _drag_start_cell: Vector2

func _ready() -> void:
	var tex = load(get_texture_path())
	set_texture(tex)
	anchor = ShapeUtils.get_anchor(item)
	_original_position = position

func is_valid_drag_position(event_pos: Vector2) -> bool:
	var inventory = get_parent().get_parent()
	
	var item_cell = Vector2(
		int(event_pos.x / inventory.CELL_SIZE.x),
		int(event_pos.y / inventory.CELL_SIZE.y)
	)
	
	var shape = ItemKindStore.get_item_kind(item.id).shape
	
	if item_cell.y >= 0 and item_cell.y < shape.size() and item_cell.x >= 0 and item_cell.x < shape[item_cell.y].size():
		return shape[item_cell.y][item_cell.x] == 1
	
	return false
	
func get_texture_path():
	return "res://assets/textures/items/%s.png" % [item.id.replace(":", "/")]

func _gui_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		if event.pressed:
			if is_valid_drag_position(event.position):
				start_drag(event.position)
				get_viewport().set_input_as_handled()
		else:
			if _dragging:  # Only handle release if we started the drag
				stop_drag()
				get_viewport().set_input_as_handled()
	elif event is InputEventMouseMotion and _dragging:
		position += event.relative
		get_viewport().set_input_as_handled()
	elif event is InputEventScreenTouch:
		if event.pressed:
			if is_valid_drag_position(event.position):
				start_drag(event.position)
				get_viewport().set_input_as_handled()
		else:
			if _dragging:  # Only handle release if we started the drag
				stop_drag()
				get_viewport().set_input_as_handled()
	elif event is InputEventScreenDrag and _dragging:
		position += event.relative
		get_viewport().set_input_as_handled()

func start_drag(event_pos: Vector2):
	_dragging = true
	_original_position = position
	z_index = 100
	
	var inventory = get_parent().get_parent()
	
	var item_local_cell = Vector2(
		int(event_pos.x / inventory.CELL_SIZE.x),
		int(event_pos.y / inventory.CELL_SIZE.y)
	)
	
	var anchor_cell = Vector2(anchor.col, anchor.row)
	_drag_start_cell = anchor_cell + item_local_cell

func stop_drag():
	_dragging = false
	z_index = 0
	
	var mp = get_viewport().get_mouse_position()
	var inventory = get_parent().get_parent()
	
	for child in inventory.get_node("NinePatchRect").get_children():
		if child.has_method("_process"):  # This identifies cells
			var rect = Rect2(child.global_position, child.size)
			if rect.has_point(mp):
				var drop_cell = Vector2(child.i, child.j)
				var offset_from_anchor = _drag_start_cell - Vector2(anchor.col, anchor.row)
				var new_anchor_pos = drop_cell - offset_from_anchor
				
				var new_anchor = Cell.new()
				new_anchor.col = int(new_anchor_pos.x)
				new_anchor.row = int(new_anchor_pos.y)
				
				attempt_move(new_anchor)
				return
	
	animate_to_position(_original_position)

func attempt_move(new_anchor: Cell):
	InventoryService.move(anchor, new_anchor, func(response):
		if response:
			InventoryStore.update_inventory(response)
		else:
			animate_to_position(_original_position)
	)

func animate_to_position(target_pos: Vector2):
	if _tween:
		_tween.kill()
	_tween = create_tween()
	_tween.tween_property(self, "position", target_pos, 0.2)

func update_position_from_anchor():
	var inventory = get_parent().get_parent()
	var target_pos = inventory.get_cell_position(anchor.col, anchor.row)
	animate_to_position(target_pos)
	_original_position = target_pos
