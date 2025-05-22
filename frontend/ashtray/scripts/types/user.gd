extends Resource
class_name User

@export var id: int
@export var username: String
@export var creation: float
@export var connection: float
@export var trust: int
@export var avatar: String
@export var token: String

enum Trust { DEFAULT, TRUSTED, CONTRIBUTOR, MODERATOR, ADMINISTRATOR }

static func from_dict(dict: Dictionary) -> User:
	var user = User.new()
	user.id = dict.get("id", 0)
	user.username = dict.get("username", "")
	user.creation = dict.get("creation", 0)
	user.connection = dict.get("connection", 0)
	
	user.trust = Trust[dict.get("trust", "DEFAULT")]
		
	user.avatar = dict.get("avatar", "")
	user.token = dict.get("token", "")
	
	return user
