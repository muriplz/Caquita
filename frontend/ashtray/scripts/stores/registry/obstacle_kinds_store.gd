extends Node

signal obstacle_kinds_ready

var API_URL: String
var obstacle_kinds: Array[ObstacleKind] = []
@onready var http_request := HTTPRequest.new()

func _ready():
	API_URL = Static.API_URL + "api/v1/obstacle-kinds"
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	http_request.request(API_URL)

func _on_response(_result, code, _headers, body) -> void:
	if code != 200:
		return

	var text = body.get_string_from_utf8()
	var parser = JSON.new()
	var err = parser.parse(text)
	if err != OK:
		push_error("Failed to parse item kinds JSON: %s" % parser.get_error_message())
		return

	var data = parser.get_data()
	if typeof(data) != TYPE_ARRAY:
		push_error("Expected JSON array but got %s" % typeof(data))
		return

	obstacle_kinds.clear()
	for entry in data:
		# ensure entry is a Dictionary
		if typeof(entry) == TYPE_DICTIONARY:
			obstacle_kinds.append(ObstacleKind.from_dict(entry))
	emit_signal("obstacle_kinds_ready")

# API
func get_all_obstacle_kinds() -> Array[ObstacleKind]:
	return obstacle_kinds

func get_obstacle_kind(id: String) -> ObstacleKind:
	for ok in obstacle_kinds:
		if ok.id == id: return ok
	return null
