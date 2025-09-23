class_name CarvingCell
extends TextureRect

@export var grid_x: int = 0
@export var grid_y: int = 0
@export var terrain_type: String = "dirt:grown_grass": set = set_terrain_type

var current_item: CarvingItem = null
var item_visual: TextureRect = null
var background_texture: TextureRect = null

signal cell_clicked(x: int, y: int)

func _ready():
	gui_input.connect(_on_gui_input)
	create_background()
	set_terrain_type(terrain_type)

func create_background():
	background_texture = TextureRect.new()
	var texture = load("res://assets/textures/carving/dirt/rock.png")
	background_texture.texture = texture
	background_texture.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
	background_texture.size = size
	background_texture.z_index = -10
	add_child(background_texture)

func _on_gui_input(event: InputEvent):
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		cell_clicked.emit(grid_x, grid_y)

func set_terrain_type(new_terrain: String):
	terrain_type = new_terrain
	texture = get_terrain_texture(terrain_type)
	update_item_visual()

func get_terrain_texture(terrain: String) -> Texture2D:
	var texture_path = ""
	match terrain:
		"dirt:grown_grass":
			texture_path = "res://assets/textures/carving/dirt/grown_grass.png"
		"dirt:grass":
			texture_path = "res://assets/textures/carving/dirt/grass.png"
		"dirt:dirt":
			texture_path = "res://assets/textures/carving/dirt/dirt.png"
		"dirt:coarse_dirt":
			texture_path = "res://assets/textures/carving/dirt/coarse_dirt.png"
		"dirt:gravel":
			texture_path = "res://assets/textures/carving/dirt/gravel.png"
		"empty":
			return null
		_:
			texture_path = "res://assets/textures/carving/dirt/unknown.png"
	
	return load(texture_path)

func set_item(item: CarvingItem):
	current_item = item
	update_item_visual()

func clear_item():
	current_item = null
	if item_visual:
		item_visual.queue_free()
		item_visual = null

func update_item_visual():
	if item_visual:
		item_visual.queue_free()
		item_visual = null
	
	if current_item and terrain_type == "empty":
		var item_kind = ItemKindStore.get_item_kind(current_item.item)
		if item_kind:
			item_visual = TextureRect.new()
			
			var texture_path = get_item_texture_path(current_item.item)
			var texture = load(texture_path)
			item_visual.texture = texture
			
			var shape = item_kind.shape
			var dims = get_actual_shape_dimensions(shape)
			item_visual.size = Vector2(dims.x * size.x, dims.y * size.y)
			
			var relative_pos = get_relative_position_in_item(shape)
			item_visual.position = relative_pos
			
			item_visual.z_index = -5
			add_child(item_visual)

func get_item_texture_path(item_id: String) -> String:
	return "res://assets/textures/items/%s.png" % [item_id.replace(":", "/")]

func get_actual_shape_dimensions(shape: Array) -> Vector2:
	var max_width = 0
	var max_height = 0
	
	for y in range(shape.size()):
		for x in range(shape[y].size()):
			if shape[y][x] == 1:
				max_width = max(max_width, x + 1)
				max_height = max(max_height, y + 1)
	
	return Vector2(max_width, max_height)

func get_relative_position_in_item(shape: Array) -> Vector2:
	if not current_item:
		return Vector2.ZERO
	
	var cell_offset_x = grid_x - current_item.anchorX  
	var cell_offset_y = grid_y - current_item.anchorY
	
	return Vector2(-cell_offset_x * size.x, -cell_offset_y * size.y)
