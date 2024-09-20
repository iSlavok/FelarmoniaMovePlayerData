package online.slavok.felarmoniaMovePlayerData.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class Whitelist(
    private val mysqlUrl: String,
) {
    private val dataSource = createHikariDataSource()

    private fun createHikariDataSource(): HikariDataSource {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val config = HikariConfig()
        config.jdbcUrl = mysqlUrl
        config.maximumPoolSize = 10
        config.connectionTimeout = 5000
        return HikariDataSource(config)
    }

    private fun getConnection(): Connection {
        return dataSource.connection
    }

    fun changeNickname(from: String, to: String): Boolean {
        return getConnection().use { connection ->
            connection.prepareStatement("UPDATE whitelist SET nickname = ? where nickname = ?;").use { ps ->
                ps.setString(1, to)
                ps.setString(2, from)
                ps.execute()
            }
        }
    }
}