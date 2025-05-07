# User.gd
class_name User
extends RefCounted

var id: int
var username: String
var creation: float
var connection: float
var trust: String
var avatar: String
var token: String

func _init(user_data: Dictionary = {}):
	if user_data.is_empty():
		return
		
	id = int(user_data.get("id", 0))
	username = user_data.get("username", "")
	
	# Simply convert whatever we receive to float
	creation = float(user_data.get("creation", 0))
	connection = float(user_data.get("connection", 0))
		
	trust = user_data.get("trust", "")
	avatar = user_data.get("avatar", "")
	token = user_data.get("token", "")

func to_dict() -> Dictionary:
	return {
		"id": id,
		"username": username,
		"creation": creation,
		"connection": connection,
		"trust": trust,
		"avatar": avatar,
		"token": token
	}
