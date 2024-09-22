package com.earthrevealed.immaru.r2dbc

import com.benasher44.uuid.Uuid
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.datetime.Instant
import java.time.LocalDateTime
import java.time.ZoneId

suspend fun <T> ConnectionFactory.useConnection(executeUsing: suspend Connection.() -> T): T {
    val connection = create().awaitSingle()

    return try {
        executeUsing(connection)
    } finally {
        connection.close().awaitFirstOrNull()
    }
}

suspend fun Connection.useTransaction(executeUsing: suspend Connection.() -> Unit) {
    beginTransaction().awaitFirstOrNull()
    try {
        executeUsing()

        commitTransaction().awaitFirstOrNull()
    } catch (throwable: Throwable) {
        rollbackTransaction().awaitFirstOrNull()
        throw throwable
    }
}

fun <T> Statement.bindNullable(name: String, value: T?, clazz: Class<T>): Statement {
    return if (value == null) {
        this.bindNull(name, clazz)
    } else {
        this.bind(name, value as Any)
    }
}

fun Row.getUuid(fieldName: String) = get(fieldName, Uuid::class.java)!!
fun Row.getString(fieldName: String) = get(fieldName, String::class.java)!!
fun Row.getTimestamp(fieldName: String) =
    get(fieldName, LocalDateTime::class.java)!!.atZone(ZoneId.systemDefault()).let {
        Instant.fromEpochMilliseconds(it.toInstant().toEpochMilli())
    }

fun Row.getInt(fieldName: String) =
    get(fieldName, java.lang.Integer::class.java)!!.toInt()
