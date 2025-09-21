extends Node

signal trash_cans_ready

var API_URL: String
var trash_cans: Array = []

func _ready():
	API_URL = Static.API_URL + "api/v1/landmarks/trash_cans"
	
func sync():
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	var headers = ["Cookie: auth=%s" % UserStore.get_token()]
	http_request.request(API_URL, headers)

func _on_response(_result, code, _headers, body):
	if code != 200:
		return
	trash_cans = JSON.parse_string(body.get_string_from_utf8())
	emit_signal("trash_cans_ready")

func get_trash_cans() -> Array[TrashCan]:
	return trash_cans.map(func(item): return TrashCan.from_dict(item))
