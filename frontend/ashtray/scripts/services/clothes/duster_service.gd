class_name DusterService

static var API_URL: String = Static.API_URL + "api/v1/wardrobe/duster"

static func equip(col: int, row: int) -> void:
	var data = {"col": col, "row": row}
	make_request("POST", "", data, func(response):
		if response != null:
			WardrobeStore.set_wardrobe(response)
	)

static func swap(col1: int, row1: int, col2: int, row2: int) -> void:
	var data = {"col1": col1, "row1": row1, "col2": col2, "row2": row2}
	make_request("PATCH", "", data, func(response):
		if response != null:
			WardrobeStore.set_wardrobe(response)
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
