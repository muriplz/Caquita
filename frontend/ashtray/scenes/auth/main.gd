extends Control

signal validation_complete(success)
var VALIDATE_API_URL: String
var gps := Engine.get_singleton("GPSPlugin")

func _ready() -> void:
	VALIDATE_API_URL = Static.API_URL + "api/v1/auth/validate"
	validate()
	setup_gps()
	
		
func validate() -> void:
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_validate_response.bind(http_request))
	
	var headers = ["Cookie: auth=" + UserStore.get_token()]
	
	http_request.request(VALIDATE_API_URL, headers, HTTPClient.METHOD_POST, "")

func _on_validate_response(result, response_code, headers, body, request_node):
	if response_code == 200:
		var json = JSON.parse_string(body.get_string_from_utf8())
		if json != null:
			UserStore.store_user(json)
			get_tree().change_scene_to_file("res://scenes/game/game.tscn")
	
	request_node.queue_free()
	
func setup_gps():
	if OS.get_name() == "Android":
		gps.connect("location_updated", Callable(self, "_on_location_updated"))
		gps.start()
	# TODO: elif os_name == "iOS":
	
func _on_location_updated(lat: float, lon: float) -> void:
	ToastManager.show("Location updated: (%s, %s)" % [lat, lon], 2)
	
