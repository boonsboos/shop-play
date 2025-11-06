package nl.connectplay.scoreplay.data

import nl.connectplay.scoreplay.options.DBOptions
import org.mariadb.jdbc.MariaDbPoolDataSource
import java.sql.Connection

class Database(private val dbOptions: DBOptions) {

    // local DB only
    // pool the database connections to reuse connections
    private var _pool: MariaDbPoolDataSource

    init {
        _pool = MariaDbPoolDataSource(dbOptions.connString)
    }

    val connection: Connection? get() = _pool.connection

    fun close() {
        _pool.close()
    }
}