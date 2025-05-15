extends Resource
class_name User

@export var id: int = 0
@export var username: String = ""
@export var creation: float = 0
@export var connection: float = 0
@export var trust: int = Trust.DEFAULT
@export var avatar: String = ""
@export var token: String = ""

enum Trust { DEFAULT, TRUSTED, CONTRIBUTOR, MODERATOR, ADMINISTRATOR }

static func from_dict(dict: Dictionary) -> User:
	var user = User.new()
	user.id = dict.get("id", 0)
	user.username = dict.get("username", "")
	user.creation = dict.get("creation", 0)
	user.connection = dict.get("connection", 0)
	
	var t_name = dict.get("trust", "")
	if Trust.has(t_name):
		user.trust = Trust[t_name]
		
	user.avatar = dict.get("avatar", "")
	user.token = dict.get("token", "")
	
	return user
