extends Node

var API_URL := Static.API_URL + "api/v1/map"

func get_tile(x: int, y: int, tile: Sprite3D) -> void:
	var http_request := HTTPRequest.new()
	add_child(http_request)

	http_request.request_completed.connect(
		func(result, response_code, headers, body):
			_on_get_tile_response(result, response_code, body, tile)
	)

	var url := "%s/%d/%d/%d" % [API_URL, 16, x, y]
	print("Requesting:", url)

	var headers := ["Content-Type: application/json"]
	http_request.request(url, headers, HTTPClient.METHOD_GET, "")
	

func _on_get_tile_response(result: int, response_code: int, body: PackedByteArray, tile: Sprite3D) -> void:
	if response_code == 200:
		var image := Image.new()
		var err := image.load_png_from_buffer(body)
		if err == OK:
			tile.call("set_texture_from_image", image)
			print("Tile loaded")
			ToastManager.show("Tile Loaded")
		else:
			print("Error loading image from buffer:", err)
	else:
		print("HTTP error:", response_code)
