/*
 * This file is part of Planetesimals, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <https://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins.planetesimals;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.getDataType;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.using;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

public class Database {

    // private static final String DB_NAME = "planetesimals";
    private static final Name PLANETS_TABLE_NAME = name("planets");
    public static final Table<Record> PLANETS_TABLE = table(PLANETS_TABLE_NAME);
    public static final Field<Integer> CHUNK_X_FIELD =
            field(name("cx"), Integer.class);
    public static final Field<Integer> CHUNK_Y_FIELD =
            field(name("cy"), Integer.class);
    public static final Field<Integer> CHUNK_Z_FIELD =
            field(name("cz"), Integer.class);
    public static final Field<Integer> PLANET_X_FIELD =
            field(name("x"), Integer.class);
    public static final Field<Integer> PLANET_Y_FIELD =
            field(name("y"), Integer.class);
    public static final Field<Integer> PLANET_Z_FIELD =
            field(name("z"), Integer.class);
    public static final Field<Integer> PLANET_RADIUS_FIELD =
            field(name("radius"), Integer.class);

    private DataSource data;
    private DSLContext db;

    public void init() {
        SqlService sql = Sponge.getServiceManager().provide(SqlService.class)
                .orElseThrow(() -> new IllegalStateException("NoSQL?"));
        String jdbcUrl = Planetesimals.getInstance().getJDBCUrl();
        String[] dbParts = jdbcUrl.split(":");
        if (dbParts.length < 2 || !dbParts[1].equals("h2")) {
            throw new IllegalStateException("Not H2. Dunno what to do.");
        }
        try {
            this.data = sql.getDataSource(jdbcUrl);
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't load DB", e);
        }
        this.db = using(this.data, SQLDialect.H2);
        if (getDB().meta().getTables().stream().noneMatch(
                t -> t.getName().equalsIgnoreCase(PLANETS_TABLE.getName()))) {
            getDB().createTable(PLANETS_TABLE)
                    .column(CHUNK_X_FIELD, getDataType(Integer.class))
                    .column(CHUNK_Y_FIELD, getDataType(Integer.class))
                    .column(CHUNK_Z_FIELD, getDataType(Integer.class))
                    .column(PLANET_X_FIELD, getDataType(Integer.class))
                    .column(PLANET_Y_FIELD, getDataType(Integer.class))
                    .column(PLANET_Z_FIELD, getDataType(Integer.class))
                    .column(PLANET_RADIUS_FIELD, getDataType(Integer.class))
                    .execute();
        }
    }

    public DataSource getData() {
        return this.data;
    }

    public DSLContext getDB() {
        return this.db;
    }

}
