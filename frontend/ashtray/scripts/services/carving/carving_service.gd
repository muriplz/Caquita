class_name CarvingService

static var API_URL: String = Static.API_URL + "api/v1/carving"

static func generate(lat: float, lon: float, callback: Callable) -> void:
	var data = {"lat": lat, "lon": lon}
	make_request("POST", "", data, func(response):
		if response != null:
			CarvingStore.set_carving_site(response)
		callback.call(response)
	)

static func carve(tool: String, x: int, y: int, callback: Callable) -> void:
	var data = {"item": tool, "x": x, "y": y}
	make_request("POST", "/carve", data, func(response):
		if response != null:
			CarvingStore.update_carving_site(response)
		callback.call(response)
	)

static func make_request(method: String, endpoint: String, data: Dictionary, callback: Callable) -> void:
	var http_request = HTTPRequest.new()
	Engine.get_main_loop().current_scene.add_child(http_request)
	
	var url = API_URL + endpoint
	var headers = ["Content-Type: application/json"]
	headers.append_array(UserStore.get_auth_header())
	var json_data = JSON.stringify(data)
	
	http_request.request_completed.connect(func(_result: int, response_code: int, _headers: PackedStringArray, body: PackedByteArray):
		var json = JSON.new()
		var parse_result = json.parse(body.get_string_from_utf8())
		
		if parse_result == OK and response_code == 200:
			callback.call(json.data)
		else:
			callback.call(null)
		
		http_request.queue_free()
	, CONNECT_ONE_SHOT)
	
	match method:
		"POST":
			http_request.request(url, headers, HTTPClient.METHOD_POST, json_data)
		"PUT":
			http_request.request(url, headers, HTTPClient.METHOD_PUT, json_data)
		"DELETE":
			http_request.request(url, headers, HTTPClient.METHOD_DELETE, json_data)
		"PATCH":
			http_request.request(url, headers, HTTPClient.METHOD_PATCH, json_data)
