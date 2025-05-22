extends Panel

var i
var j
@export var normal_color: Color = Color(1,1,1,1)
@export var hover_color: Color = Color(0.8,0.8,0.8,1)

var _hovered: bool = false

func _process(delta: float) -> void:
	var mp = get_viewport().get_mouse_position()
	var rect = Rect2(global_position, size)
	var inside = rect.has_point(mp)
	if inside and not _hovered:
		_hovered = true
		modulate = hover_color
		get_parent().get_parent().hovered_cell = self
	elif not inside and _hovered:
		_hovered = false
		modulate = normal_color
		get_parent().get_parent().hovered_cell = null
