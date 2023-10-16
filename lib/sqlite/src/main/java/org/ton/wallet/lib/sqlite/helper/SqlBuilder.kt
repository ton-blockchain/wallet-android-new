package org.ton.wallet.lib.sqlite.helper

class SqlTableBuilder(
    private val name: String
) {

    private val entities = ArrayList<SqlEntityBuilder>()

    fun addColumn(name: String, type: SqlColumnBuilder.Type, action: (SqlColumnBuilder.() -> Unit)? = null) = apply {
        val columnBuilder = SqlColumnBuilder(name, type)
        action?.invoke(columnBuilder)
        entities.add(columnBuilder)
    }

    fun buildCreateSql(): String {
        val builder = StringBuilder("CREATE TABLE IF NOT EXISTS ").append(name).append(" ")
        for (i in entities.indices) {
            if (i == 0) {
                builder.append(" (")
            }
            builder.append(entities[i].buildSql())
            if (i == entities.size - 1) {
                builder.append(");")
            } else {
                builder.append(", ")
            }
        }
        return builder.toString()
    }
}

interface SqlEntityBuilder {

    fun buildSql(): String
}

class SqlColumnBuilder(
    private val name: String,
    private val type: Type,
    private var reference: SqlColumnReference? = null,
    var isPrimaryKey: Boolean = false,
    var isAutoIncrement: Boolean = false,
    var isNotNull: Boolean = false,
    var isUnique: Boolean = false,
    var default: String? = null
): SqlEntityBuilder {

    fun addReference(table: String, column: String, action: (SqlColumnReference.() -> Unit)? = null) = apply {
        reference = SqlColumnReference(table, column)
        action?.invoke(reference!!)
    }

    override fun buildSql(): String {
        val builder = StringBuilder(name).append(" ").append(type.toString())
        if (isNotNull) {
            builder.append(" NOT NULL")
        }
        if (isPrimaryKey) {
            builder.append(" PRIMARY KEY")
        }
        if (isAutoIncrement) {
            builder.append(" AUTOINCREMENT")
        }
        if (isUnique) {
            builder.append(" UNIQUE")
        }
        if (!default.isNullOrEmpty()) {
            builder.append(" DEFAULT ").append(default)
        }
        reference?.let { builder.append(it.buildSql()) }
        return builder.toString()
    }

    enum class Type {
        BLOB,
        INTEGER,
        REAL,
        TEXT
    }
}

class SqlColumnReference(
    private val table: String,
    private val column: String,
    var onDelete: Action? = null,
    var onUpdate: Action? = null
): SqlEntityBuilder {

    override fun buildSql(): String {
        return StringBuilder(" REFERENCES ").append(table).append("(").append(column).append(")")
            .append(onDelete?.let { " ON DELETE ${it.value}" } ?: "")
            .append(onUpdate?.let { " ON UPDATE ${it.value}" } ?: "")
            .toString()
    }

    enum class Action(val value: String) {
        Cascade("CASCADE"),
        NoAction("NO ACTION"),
        Restrict("RESTRICT"),
        SetDefault("SET DEFAULT"),
        SetNull("SET NULL");
    }
}
