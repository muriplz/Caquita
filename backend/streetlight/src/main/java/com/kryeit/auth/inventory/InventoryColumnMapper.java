package com.kryeit.auth.inventory;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryColumnMapper implements ColumnMapper<GridInventory> {
    @Override
    public GridInventory map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        long id = r.getLong("id");
        long userId = r.getLong("user_id");
        int width = r.getInt("width");
        int height = r.getInt("height");

        PGobject pgObject = (PGobject) r.getObject("item_placements");
        String json = pgObject.getValue();

        return JsonUtils.deserialize(json, id, userId);
    }
}