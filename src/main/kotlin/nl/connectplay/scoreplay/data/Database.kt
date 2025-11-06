package nl.connectplay.scoreplay.data

import org.mariadb.jdbc.MariaDbPoolDataSource
import java.sql.Connection

class Database {

    // local DB only
    // pool the database connections to reuse connections
    private var _pool: MariaDbPoolDataSource = MariaDbPoolDataSource(
        "jdbc:mariadb://db.connectplay.local/score_play?user=root&password="
    )

    val connection: Connection? get() = _pool.connection

    fun close() {
        _pool.close()
    }
}