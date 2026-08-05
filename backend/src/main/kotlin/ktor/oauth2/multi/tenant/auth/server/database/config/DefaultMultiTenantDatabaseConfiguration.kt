package ktor.oauth2.multi.tenant.auth.server.database.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ktor.oauth2.multi.tenant.auth.server.database.migration.V4__seed_role_user
import ktor.oauth2.multi.tenant.auth.server.persistence.entity.Tenant
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.jdbc.JdbcTemplate
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class DefaultMultiTenantDatabaseConfiguration(
    val databaseConfigurationHolder: DatabaseConfigurationHolder,
) : MultiTenantDatabaseConfiguration {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[MultiTenantDatabaseConfiguration] -> init")
    }

    private val tenantDatabase: ConcurrentMap<String, MutableMap<String, Database>> = ConcurrentHashMap()

    private val pools: ConcurrentMap<String, MutableMap<String, HikariDataSource>> = ConcurrentHashMap()

    override fun init() {
        databaseConfigurationHolder.databases.forEach {
            connect(
                key = it.key,
                url = it.value.url,
                username = it.value.username,
                password = it.value.password,
                schema = it.value.schema,
                driver = it.value.driver,
            )

            setUpMasterDatabase(it.key)

            tenantDatabase[it.key]?.get(it.value.schema)?.let { db ->
                transaction(db) {
                    Tenant.all().forEach { tenant ->
                        log.info(
                            "[MultiTenantDatabaseConfiguration] -> Initializing tenant database" +
                                " for tenant: {} with schema: {}",
                            tenant.name,
                            tenant.companyKey,
                        )

                        val dbConfig =
                            DatabaseConfig {
                                sqlLogger = Slf4jSqlDebugLogger
                                useNestedTransactions = true
                                defaultFetchSize = null
                                defaultIsolationLevel = -1
                                warnLongQueriesDuration = null
                                maxEntitiesToStoreInCachePerEntity = Int.MAX_VALUE
                                keepLoadedReferencesOutOfTransaction = true
                            }

                        tenantDatabase[it.key]?.set(
                            tenant.companyKey,
                            Database.connect(
                                init(
                                    it.key,
                                    it.value.url,
                                    it.value.username,
                                    it.value.password,
                                    tenant.companyKey,
                                    it.value.driver,
                                ),
                                databaseConfig = dbConfig,
                            ),
                        )

                        log.info("Successfully connected to schema: ${tenant.companyKey} for tenant: ${tenant.name}")

                        setUpTenantDatabase(it.key, tenant.companyKey)
                    }
                }
            }
        }
    }

    private fun init(
        key: String,
        url: String,
        username: String,
        password: String,
        schema: String,
        driver: String,
    ): HikariDataSource {
        val config: HikariConfig = hikariConfigGenerator(url, username, password, schema, driver)
        val existing: HikariDataSource? = pools[key]?.get(schema)
        if (existing != null && existing.isClosed) {
            existing.close()
        }
        val ds = HikariDataSource(config)
        pools[key]?.set(schema, ds) ?: run {
            pools[key] = mutableMapOf()
            pools[key]?.set(schema, ds)
        }
        return ds
    }

    private fun hikariConfigGenerator(
        url: String,
        username: String,
        password: String,
        schema: String,
        driver: String,
    ): HikariConfig {
        val config = HikariConfig()
        config.jdbcUrl = url
        config.username = username
        config.password = password
        config.schema = schema
        config.driverClassName = driver
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return config
    }

    fun dataSource(
        key: String,
        name: String = "public",
    ): HikariDataSource {
        val ds: HikariDataSource? = pools[key]?.get(name)
        if (ds != null && !ds.isClosed) {
            return ds
        } else {
            throw IllegalStateException("DataSource ($name) is absent.")
        }
    }

    private fun connect(
        key: String,
        url: String?,
        username: String?,
        password: String?,
        schema: String?,
        driver: String?,
    ) {
        if (url.isNullOrEmpty() || username.isNullOrEmpty() || password.isNullOrEmpty() || schema.isNullOrEmpty() ||
            driver.isNullOrEmpty()
        ) {
            throw RuntimeException("Database does not exist")
        }
        val dbConfig =
            DatabaseConfig {
                sqlLogger = Slf4jSqlDebugLogger
                useNestedTransactions = true
                defaultFetchSize = null
                defaultIsolationLevel = -1
                warnLongQueriesDuration = null
                maxEntitiesToStoreInCachePerEntity = Int.MAX_VALUE
                keepLoadedReferencesOutOfTransaction = true
            }
        tenantDatabase[key]?.set(
            schema,
            Database.connect(init(key, url, username, password, schema, driver), databaseConfig = dbConfig),
        ) ?: run {
            tenantDatabase[key] = mutableMapOf()
            tenantDatabase[key]?.set(
                schema,
                Database.connect(
                    datasource = init(key, url, username, password, schema, driver),
                    databaseConfig = dbConfig,
                ),
            )
        }
    }

    override fun createDatabaseSchema(schema: String) {
        val jdbcTemplate =
            JdbcTemplate(
                dataSource(databaseConfigurationHolder.databases.keys.first()).connection,
            )
        jdbcTemplate.executeStatement("CREATE SCHEMA $schema;")

        log.info("Successfully created schema: $schema")

        val dbConfig =
            DatabaseConfig {
                sqlLogger = Slf4jSqlDebugLogger
                useNestedTransactions = true
                defaultFetchSize = null
                defaultIsolationLevel = -1
                warnLongQueriesDuration = null
                maxEntitiesToStoreInCachePerEntity = Int.MAX_VALUE
                keepLoadedReferencesOutOfTransaction = true
            }

        tenantDatabase[databaseConfigurationHolder.databases.keys.first()]?.set(
            schema,
            Database.connect(
                init(
                    databaseConfigurationHolder.databases.keys.first(),
                    databaseConfigurationHolder.databases.values.first().url,
                    databaseConfigurationHolder.databases.values.first().username,
                    databaseConfigurationHolder.databases.values.first().password,
                    schema,
                    databaseConfigurationHolder.databases.values.first().driver,
                ),
                databaseConfig = dbConfig,
            ),
        )

        log.info("Successfully connected to schema: $schema")

        setUpTenantDatabase(
            databaseConfigurationHolder.databases.keys.first(),
            schema,
        )

        log.info("Successfully set up tenant database for schema: $schema")
    }

    override fun getTenantDatabase(
        key: String,
        schema: String,
    ): Database {
        return tenantDatabase[key]?.get(schema) ?: throw RuntimeException("Database does not exist")
    }

    fun setUpMasterDatabase(key: String) {
        Flyway.configure().dataSource(dataSource(key)).defaultSchema("public")
            .locations("classpath:oauth_db")
            .load()
            .migrate()

        Flyway.configure().dataSource(dataSource(key)).defaultSchema("public")
            .locations("classpath:oauth_db", "classpath:db/master", "classpath:db/tenant")
            .javaMigrations(V4__seed_role_user()).load().migrate()
    }

    fun setUpTenantDatabase(
        key: String,
        schema: String,
    ) {
        Flyway.configure().dataSource(dataSource(key)).defaultSchema(schema)
            .locations("classpath:oauth_db")
            .load()
            .migrate()

        Flyway.configure().dataSource(dataSource(key)).defaultSchema(schema)
            .locations("classpath:oauth_db", "classpath:db/tenant")
            .javaMigrations(V4__seed_role_user()).load().migrate()
    }
}
