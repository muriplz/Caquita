extends Node3D

@export var render_distance: int = 3

var _loaded_tiles: Dictionary = {}
var _current_center: Vector2i = Vector2i(0, 0)

@onready var _gps_node: Node = $"../GPS"

func _ready() -> void:
	_gps_node.connect("world_position_changed", Callable(self, "_on_world_position_changed"))

func _on_world_position_changed(world_pos: Vector3) -> void:
	ToastManager.show("HEYY")
	var new_center: Vector2i = CoordConversion.world_to_tile(world_pos)
	ToastManager.show("%s" % [new_center])
	if new_center == _current_center:
		return
	_current_center = new_center
	_update_visible_tiles()

func _update_visible_tiles() -> void:
	var want_tiles := {}
	for dx in range(-render_distance, render_distance + 1):
		for dy in range(-render_distance, render_distance + 1):
			want_tiles[Vector2i(_current_center.x + dx, _current_center.y + dy)] = true

	# Unload tiles no longer needed
	for coord in _loaded_tiles.keys():
		if not want_tiles.has(coord):
			var mi: MeshInstance3D = _loaded_tiles[coord]
			remove_child(mi)
			mi.queue_free()
			_loaded_tiles.erase(coord)

	# Start HTTPRequest for new tiles
	for coord in want_tiles.keys():
		if not _loaded_tiles.has(coord):
			_load_tile(coord.x, coord.y)

func _load_tile(tx: int, ty: int) -> void:
	var url := "%sapi/v1/map/%d/%d/%d" % [
		Static.API_URL, CoordConversion.ZOOM, tx, ty
	]
	var http_request := HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(
		Callable(self, "_on_request_completed").bind(tx, ty, http_request)
	)
	var err := http_request.request(url)
	if err != OK:
		push_error("Failed to start HTTP request for tile %d,%d (err %d)" % [tx, ty, err])
		http_request.queue_free()

func _on_request_completed(
		result: int,
		response_code: int,
		_headers: PackedStringArray,
		body: PackedByteArray,
		tx: int,
		ty: int,
		http_request: HTTPRequest
	) -> void:
	# Clean up the HTTPRequest node
	http_request.queue_free()

	if result != OK or response_code != 200:
		push_error("Tile download failed for %d,%d: result %d, HTTP %d" % [tx, ty, result, response_code])
		return

	var img := Image.new()
	if img.load_jpg_from_buffer(body) != OK:
		push_error("Failed to decode JPG for tile %d,%d" % [tx, ty])
		return
	
	var tex := ImageTexture.create_from_image(img)
	_create_tile(tx, ty, tex)

func _create_tile(tx: int, ty: int, tex: Texture2D) -> void:
	var base: Vector3 = CoordConversion.tile_to_world(tx, ty)
	base.x += CoordConversion.TILE_SIZE * 0.5
	base.z += CoordConversion.TILE_SIZE * 0.5

	var mesh := PlaneMesh.new()
	mesh.size = Vector2(CoordConversion.TILE_SIZE, CoordConversion.TILE_SIZE)

	var mi := MeshInstance3D.new()
	mi.mesh = mesh

	var mat := StandardMaterial3D.new()
	mat.shading_mode = StandardMaterial3D.SHADING_MODE_UNSHADED
	mat.albedo_texture = tex

	mi.material_override = mat
	add_child(mi)
	mi.global_transform = Transform3D(Basis(), base)

	_loaded_tiles[Vector2i(tx, ty)] = mi
