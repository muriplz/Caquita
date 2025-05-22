extends Resource
class_name TrashCan

@export var id: int
@export var lat: float
@export var lon: float
@export var name: String
@export var description: String
@export var author: int

@export var broken: bool
@export var ashtray: bool
@export var windblown: bool
@export var flooded: bool
@export var overwhelmed: bool
@export var poopbag: bool
@export var art: bool

static func from_dict(dict: Dictionary) -> TrashCan:
	var trash_can = TrashCan.new()
	trash_can.id = dict.get("id", 0)
	trash_can.lat = dict.get("lat", 0)
	trash_can.lon = dict.get("lon", 0)
	trash_can.name = dict.get("name", "")
	trash_can.description = dict.get("description", "")
	trash_can.author = dict.get("author", 0)
	
	trash_can.broken = dict.get("broken", false)
	trash_can.ashtray = dict.get("ashtray", false)
	trash_can.windblown = dict.get("windblown", false)
	trash_can.flooded = dict.get("flooded", false)
	trash_can.overwhelmed = dict.get("overwhelmed", false)
	trash_can.poopbag = dict.get("poopbag", false)
	trash_can.art = dict.get("art", false)
	
	return trash_can
