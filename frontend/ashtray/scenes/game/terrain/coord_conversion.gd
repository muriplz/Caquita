extends Node

const ZOOM: int = 16
const TILE_SIZE: float = 64.0

# Default
static var origin_latlon: Vector2 = Vector2(-33.8688, 151.2093)
static var origin_tile: Vector2i = Vector2i()

static func set_origin(lat: float, lon: float) -> void:
	origin_latlon = Vector2(lat, lon)
	origin_tile    = latlon_to_tile(lat, lon)

func _ready() -> void:
	set_origin(origin_latlon.x, origin_latlon.y)


static func latlon_to_tile(lat: float, lon: float) -> Vector2i:
	var n := 1 << ZOOM
	var x := int((lon + 180.0) / 360.0 * n)
	var lat_rad := deg_to_rad(lat)
	var y := int((1.0 - log(tan(lat_rad) + 1.0 / cos(lat_rad)) / PI) / 2.0 * n)
	return Vector2i(x, y)


static func tile_to_latlon(x: int, y: int) -> Vector2:
	var n := 1 << ZOOM
	var lon := float(x) / n * 360.0 - 180.0
	var lat_rad := atan(sinh(PI * (1.0 - 2.0 * float(y) / n)))
	var lat := deg_to_rad(lat_rad)
	return Vector2(lat, lon)


static func tile_to_world(x: int, y: int) -> Vector3:
	var rel := Vector2i(x, y) - origin_tile
	return Vector3(rel.x * TILE_SIZE, 0, rel.y * TILE_SIZE)


static func world_to_tile(world_pos: Vector3) -> Vector2i:
	var local := Vector2i(
		int(world_pos.x / TILE_SIZE),
		int(world_pos.z / TILE_SIZE)
	)
	return origin_tile + local


static func latlon_to_world(lat: float, lon: float) -> Vector3:
	var tile := latlon_to_tile(lat, lon)
	return tile_to_world(tile.x, tile.y)


static func world_to_latlon(world_pos: Vector3) -> Vector2:
	var tile := world_to_tile(world_pos)
	return tile_to_latlon(tile.x, tile.y)
