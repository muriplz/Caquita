extends Button

var LOGIN_API_URL: String
var REGISTER_API_URL: String
var username_field: LineEdit
var password_field: LineEdit

func _ready() -> void:
	LOGIN_API_URL = Static.API_URL + "api/v1/auth/login"
	REGISTER_API_URL = Static.API_URL + "api/v1/auth/register"
	
	username_field = get_node("../Username")
	password_field = get_node("../Password")
	
	pressed.connect(_on_button_pressed)

func _on_button_pressed() -> void:
	var username = username_field.text
	var password = password_field.text
	login(username, password)

func login(username: String, password: String) -> void:
	if (username.length() < 3 or username.length() > 13):
		ToastManager.show("Username must be between 3 and 13 characters")
		return
	if (password.length() < 6 or password.length() > 32):
		ToastManager.show("Password must be between 6 and 32 characters")
		return
	
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_login_response)
	
	var headers = ["Content-Type: application/json"]
	var body = JSON.stringify({"username": username, "password": password})
	
	http_request.request(LOGIN_API_URL, headers, HTTPClient.METHOD_POST, body)

func _on_login_response(result, response_code, headers, body):
	if response_code == 200:
		var json = JSON.parse_string(body.get_string_from_utf8())
		if json != null:
			ToastManager.show("Login successful!")
			UserStore.store_user(json)
			get_tree().change_scene_to_file("res://scenes/game/game.tscn")
	elif response_code == 460:
		ToastManager.show("Account not found. Creating account...")
		register(username_field.text, password_field.text)
	else:
		ToastManager.show(body.get_string_from_utf8())
	
func register(username: String, password: String) -> void:
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_register_response)
	
	var headers = ["Content-Type: application/json"]
	var body = JSON.stringify({"username": username, "password": password})
	
	http_request.request(REGISTER_API_URL, headers, HTTPClient.METHOD_POST, body)
		
func _on_register_response(result, response_code, headers, body):
	if response_code == 200:
		var json = JSON.parse_string(body.get_string_from_utf8())
		if json != null:
			ToastManager.show("Registration successful! Logging in...")
			# After registration, attempt login again
			login(username_field.text, password_field.text)
	else:
		ToastManager.show(body.get_string_from_utf8())
