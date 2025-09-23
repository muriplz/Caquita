extends Control

var i
var j

var _hovered: bool = false

func _process(_delta: float) -> void:
	var mp = get_viewport().get_mouse_position()
	var rect = Rect2(global_position, size)
	var inside = rect.has_point(mp)
	if inside and not _hovered:
		_hovered = true
		get_parent().get_parent().hovered_cell = self
	elif not inside and _hovered:
		_hovered = false
		get_parent().get_parent().hovered_cell = null
