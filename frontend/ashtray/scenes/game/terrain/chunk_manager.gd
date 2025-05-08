# ChunkManager.gd
extends Node3D

const ZOOM := 16
const CHUNK_SIZE := 16
const RENDER_DISTANCE := 15
const MAX_CONCURRENT := 4

var loaded_chunks := {}       # Map<Vector2i, Sprite3D>
var queue := []               # Array<Vector2i>
var active_requests := []     # Array<HTTPRequest>

@onready var gps_handler := $GPS

func _ready():
	gps_handler.connect("world_position_changed", Callable(self, "update_chunks"))

func update_chunks(world_position: Vector3) -> void:
	var cx := int(world_position.x / CHUNK_SIZE)
	var cz := int(world_position.z / CHUNK_SIZE)
	var desired := {}
	for x in range(cx - RENDER_DISTANCE, cx + RENDER_DISTANCE + 1):
		for z in range(cz - RENDER_DISTANCE, cz + RENDER_DISTANCE + 1):
			var coord := Vector2i(x, z)
			desired[coord] = true
			if not loaded_chunks.has(coord) and not queue.has(coord):
				queue.append(coord)
	for coord in loaded_chunks.keys():
		if not desired.has(coord):
			loaded_chunks[coord].queue_free()
			loaded_chunks.erase(coord)
	_process_queue()

func _process_queue() -> void:
	while queue.size() > 0 and active_requests.size() < MAX_CONCURRENT:
		var coord: Vector2i = queue.pop_front()
		var request := HTTPRequest.new()
		add_child(request)
		var cb := Callable(self, "_on_tile_loaded").bind(coord, request)
		request.connect("request_completed", cb)
		request.request("%s/api/v1/map/%d/%d/%d" % [Static.API_URL, ZOOM, coord.x, coord.y])
		active_requests.append(request)

func _on_tile_loaded(result, status_code, headers, body, coord: Vector2i, request: HTTPRequest) -> void:
	active_requests.erase(request)
	request.queue_free()
	if status_code == 200:
		var img := Image.new()
		if img.load_png_from_buffer(body) == OK:
			var tex := ImageTexture.create_from_image(img)
			var spr := Sprite3D.new()
			spr.texture = tex
			spr.rotation_degrees.x = -90
			spr.position = Vector3(coord.x * CHUNK_SIZE, 0, coord.y * CHUNK_SIZE)
			add_child(spr)
			loaded_chunks[coord] = spr
	_process_queue()
