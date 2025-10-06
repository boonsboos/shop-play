package nl.connectplay.scoreplay.data

import java.sql.Connection
import java.sql.DriverManager

class Database {

    // local DB only
    val connection: Connection? = DriverManager.getConnection(
        "jdbc:mariadb://localhost:3306/score_play?user=root&password="
    )
}