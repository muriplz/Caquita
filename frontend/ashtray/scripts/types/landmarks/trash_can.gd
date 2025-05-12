class_name TrashCan
extends RefCounted

var id: int
var lat: float
var lon: float
var name: String
var description: String
var author: int

var broken: bool
var ashtray: bool; var windblown: bool; var flooded: bool; var overwhelmed: bool
var poopbag: bool; var art: bool

func _init(trash_can_data: Dictionary = {}):
	if trash_can_data.is_empty():
		return
		
	id = int(trash_can_data.get("id", 0))
	lat = float(trash_can_data.get("lat", 0))
	lon = float(trash_can_data.get("lon", 0))
	name = trash_can_data.get("name", "")
	description = trash_can_data.get("description", "")
	author = int(trash_can_data.get("author", 0))
	
	broken = bool(trash_can_data.get("broken", false))
	
	ashtray = bool(trash_can_data.get("ashtray", false))
	windblown = bool(trash_can_data.get("windblown", false))
	flooded = bool(trash_can_data.get("flooded", false))
	overwhelmed = bool(trash_can_data.get("overwhelmed", false))
	poopbag = bool(trash_can_data.get("poopbag", false))
	art = bool(trash_can_data.get("art", false))


func to_dict() -> Dictionary:
	return {
		"id": id,
		"lat": lat,
		"lon": lon,
		"name": name,
		"description": description,
		"author": author,
		"broken": broken,
		"ashtray": ashtray,
		"windblown": windblown,
		"flooded": flooded,
		"overwhelmed": overwhelmed,
		"poopbag": poopbag,
		"art": art
	}
