class_name CarvingCell
extends TextureRect

@export var grid_x: int = 0
@export var grid_y: int = 0
@export var terrain_type: String = "dirt:grown_grass": set = set_terrain_type

var current_item: CarvingItem = null
var current_obstacle: CarvingObstacle = null
var item_visual: TextureRect = null
var obstacle_visual: TextureRect = null
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
	update_visuals()

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

# Item Management
func set_item(item: CarvingItem):
	current_item = item
	update_visuals()

func clear_item():
	current_item = null
	if item_visual:
		item_visual.queue_free()
		item_visual = null

# Obstacle Management
func set_obstacle(obstacle: CarvingObstacle):
	current_obstacle = obstacle
	update_visuals()

func clear_obstacle():
	current_obstacle = null
	if obstacle_visual:
		obstacle_visual.queue_free()
		obstacle_visual = null

# Visual Updates
func update_visuals():
	update_item_visual()
	update_obstacle_visual()

func update_item_visual():
	if item_visual:
		item_visual.queue_free()
		item_visual = null
	
	if current_item and should_render_item():
		var item_kind = ItemKindStore.get_item_kind(current_item.item)
		if item_kind and is_item_partially_revealed(current_item, item_kind):
			item_visual = TextureRect.new()
			
			var texture_path = get_item_texture_path(current_item.item)
			var texture = load(texture_path)
			item_visual.texture = texture
			
			var shape = item_kind.shape
			var dims = get_actual_shape_dimensions(shape)
			item_visual.size = Vector2(dims.x * size.x, dims.y * size.y)
			
			item_visual.position = get_render_offset()
			item_visual.rotation = -rotation
			item_visual.pivot_offset = item_visual.size / 2
			item_visual.z_index = -3
			add_child(item_visual)

func update_obstacle_visual():
	if obstacle_visual:
		obstacle_visual.queue_free()
		obstacle_visual = null
	
	if current_obstacle and should_render_obstacle():
		var obstacle_kind = ObstacleKindsStore.get_obstacle_kind(current_obstacle.obstacle)
		if obstacle_kind:
			obstacle_visual = TextureRect.new()
			
			var texture_path = get_obstacle_texture_path(current_obstacle.obstacle)
			var texture = load(texture_path)
			obstacle_visual.texture = texture
			
			var shape = obstacle_kind.shape
			var dims = get_actual_shape_dimensions(shape)
			obstacle_visual.size = Vector2(dims.x * size.x, dims.y * size.y)
			
			obstacle_visual.position = get_obstacle_render_offset()  # Use offset instead of Vector2.ZERO
			obstacle_visual.z_index = -2
			add_child(obstacle_visual)

# Rendering Logic
func should_render_item() -> bool:
	if not current_item:
		return false
		
	var item_kind = ItemKindStore.get_item_kind(current_item.item)
	if not item_kind:
		return false
		
	var shape = item_kind.shape
	
	# Find the first cell in the shape that has value 1
	for sy in range(shape.size()):
		for sx in range(shape[sy].size()):
			if shape[sy][sx] == 1:
				var first_valid_x = int(current_item.anchorX) + sx
				var first_valid_y = int(current_item.anchorY) + sy
				return grid_x == first_valid_x and grid_y == first_valid_y
	
	return false

func get_render_offset() -> Vector2:
	if not current_item:
		return Vector2.ZERO
		
	var item_kind = ItemKindStore.get_item_kind(current_item.item)
	if not item_kind:
		return Vector2.ZERO
		
	var shape = item_kind.shape
	
	# Find offset from current cell to anchor
	for sy in range(shape.size()):
		for sx in range(shape[sy].size()):
			if shape[sy][sx] == 1:
				var first_valid_x = int(current_item.anchorX) + sx
				var first_valid_y = int(current_item.anchorY) + sy
				if grid_x == first_valid_x and grid_y == first_valid_y:
					return Vector2(
						(int(current_item.anchorX) - grid_x) * size.x,
						(int(current_item.anchorY) - grid_y) * size.y
					)
	
	return Vector2.ZERO

func is_item_partially_revealed(item: CarvingItem, item_kind) -> bool:
	var shape = item_kind.shape
	var grid = get_parent()
	
	for sy in range(shape.size()):
		for sx in range(shape[sy].size()):
			if shape[sy][sx] == 1:
				var check_x = int(item.anchorX) + sx
				var check_y = int(item.anchorY) + sy
				
				if check_x < grid.grid_width and check_y < grid.grid_height and check_x >= 0 and check_y >= 0:
					if grid.cells[check_y][check_x].terrain_type == "empty":
						return true
	return false

func should_render_obstacle() -> bool:
	if not current_obstacle:
		return false
		
	var obstacle_kind = ObstacleKindsStore.get_obstacle_kind(current_obstacle.obstacle)
	if not obstacle_kind:
		return false
		
	var shape = obstacle_kind.shape
	
	# Find the first cell in the shape that has value 1
	for sy in range(shape.size()):
		for sx in range(shape[sy].size()):
			if shape[sy][sx] == 1:
				var first_valid_x = int(current_obstacle.anchorX) + sx
				var first_valid_y = int(current_obstacle.anchorY) + sy
				return grid_x == first_valid_x and grid_y == first_valid_y
	
	return false

func get_obstacle_render_offset() -> Vector2:
	if not current_obstacle:
		return Vector2.ZERO
		
	var obstacle_kind = ObstacleKindsStore.get_obstacle_kind(current_obstacle.obstacle)
	if not obstacle_kind:
		return Vector2.ZERO
		
	var shape = obstacle_kind.shape
	
	# Find offset from current cell to anchor
	for sy in range(shape.size()):
		for sx in range(shape[sy].size()):
			if shape[sy][sx] == 1:
				var first_valid_x = int(current_obstacle.anchorX) + sx
				var first_valid_y = int(current_obstacle.anchorY) + sy
				if grid_x == first_valid_x and grid_y == first_valid_y:
					return Vector2(
						(int(current_obstacle.anchorX) - grid_x) * size.x,
						(int(current_obstacle.anchorY) - grid_y) * size.y
					)
	
	return Vector2.ZERO
	
# Utility Functions
func get_item_texture_path(item_id: String) -> String:
	return "res://assets/textures/items/%s.png" % [item_id.replace(":", "/")]

func get_obstacle_texture_path(obstacle_id: String) -> String:
	return "res://assets/textures/carving/obstacles/%s.png" % [obstacle_id.replace("obstacle:", "")]

func get_actual_shape_dimensions(shape: Array) -> Vector2:
	var max_width = 0
	var max_height = 0
	
	for y in range(shape.size()):
		for x in range(shape[y].size()):
			if shape[y][x] == 1:
				max_width = max(max_width, x + 1)
				max_height = max(max_height, y + 1)
	
	return Vector2(max_width, max_height)
