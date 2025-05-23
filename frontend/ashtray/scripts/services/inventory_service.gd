class_name InventoryService

static var API_URL: String = Static.API_URL + "api/v1/inventory"

static func rotate(anchor: Cell, clockwise: bool, callback: Callable) -> void:
	var data = {"anchor": anchor.to_dict(), "clockwise": clockwise}
	_make_request("POST", "", data, callback)

static func add(item_id: String, anchor: Cell, callback: Callable) -> void:
	var data = {"item_id": item_id, "anchor": anchor.to_dict()}
	_make_request("PUT", "", data, callback)

static func remove(anchor: Cell, callback: Callable) -> void:
	var data = {"anchor": anchor.to_dict()}
	_make_request("DELETE", "", data, callback)

static func move(anchor: Cell, new_anchor: Cell, callback: Callable) -> void:
	var data = {"anchor": anchor.to_dict(), "newAnchor": new_anchor.to_dict()}
	_make_request("PATCH", "", data, callback)

static func can_place(item_id: String, anchor: Cell, callback: Callable) -> void:
	var data = {"item_id": item_id, "anchor": anchor.to_dict()}
	_make_request("POST", "/can-place", data, callback)

static func _make_request(method: String, endpoint: String, data: Dictionary, callback: Callable) -> void:
	var http_request = HTTPRequest.new()
	Engine.get_main_loop().current_scene.add_child(http_request)
	
	var url = API_URL + endpoint
	var headers = ["Content-Type: application/json"]
	headers.append_array(UserStore.get_auth_header())
	var json_data = JSON.stringify(data)
	
	http_request.request_completed.connect(func(_result: int, _response_code: int, _headers: PackedStringArray, body: PackedByteArray):
		var json = JSON.new()
		var parse_result = json.parse(body.get_string_from_utf8())
		
		if parse_result == OK:
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
