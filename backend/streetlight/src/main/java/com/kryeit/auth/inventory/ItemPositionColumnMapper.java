package com.kryeit.auth.inventory;

import com.kryeit.content.items.Item;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ItemPositionColumnMapper implements ColumnMapper<Map<Item, Position>> {
    @Override
    public Map<Item, Position> map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        PGobject pgObject = (PGobject) r.getObject(columnNumber);
        String json = pgObject.getValue();
        return JsonUtils.deserializeItemPositions(json);
    }
}
