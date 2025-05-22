extends Control

func _ready() -> void:
	validate()
		
func validate() -> void:
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_validate_response.bind(http_request))
	
	var headers = ["Cookie: auth=" + UserStore.get_token()]
	
	http_request.request(Static.API_URL + "api/v1/auth/validate", headers, HTTPClient.METHOD_POST, "")

func _on_validate_response(_result, response_code, _headers, body, request_node):
	if response_code == 200:
		var json = JSON.parse_string(body.get_string_from_utf8())
		if json != null:
			UserStore.store_user(json)
			get_tree().change_scene_to_file("res://scenes/game/game.tscn")
	
	request_node.queue_free()
