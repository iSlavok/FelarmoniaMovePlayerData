package online.slavok.felarmoniaMovePlayerData.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class Auth(
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

    fun isPremium(nickname: String): Boolean {
        return getConnection().use { connection ->
            connection.prepareStatement("SELECT is_premium FROM auth WHERE nickname = ?;").use { ps ->
                ps.setString(1, nickname)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        rs.getBoolean("is_premium")
                    } else {
                        false
                    }
                }
            }
        }
    }
}