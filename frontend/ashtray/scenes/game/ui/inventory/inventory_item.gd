extends TextureRect

var item: InventoryItem
var anchor: Cell

var _dragging := false


func _ready() -> void:
	var tex = load(get_texture_path())
	set_texture(tex)
	anchor = ShapeUtils.get_anchor(item)


func get_texture_path():
	return "res://assets/textures/items/%s.png" % [item.id.replace(":", "/")]


func _gui_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		_dragging = event.pressed
		if _dragging:
			get_viewport().set_input_as_handled()
	elif event is InputEventMouseMotion and _dragging:
		position += event.relative
		get_viewport().set_input_as_handled()
	elif event is InputEventScreenTouch:
		_dragging = event.pressed
		if _dragging:
			get_viewport().set_input_as_handled()
	elif event is InputEventScreenDrag and _dragging:
		position += event.relative
		get_viewport().set_input_as_handled()
