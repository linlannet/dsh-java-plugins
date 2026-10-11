package net.linlan.dsh.dameng.service;

import net.linlan.dsh.dameng.config.DamengAdminProperties;
import net.linlan.dsh.dameng.model.ApiException;
import net.linlan.dsh.dameng.model.QueryResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueryService {
    private final DamengAdminProperties properties;
    private final ConnectionStore connectionStore;
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    public QueryService(DamengAdminProperties properties, ConnectionStore connectionStore) {
        this.properties = properties;
        this.connectionStore = connectionStore;
    }

    public synchronized QueryResult execute(String connectionId, String sql, boolean allowWrite) {
        List<String> statements = splitStatements(sql);
        if (statements.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SQL 不能为空");
        }
        boolean readOnly = statements.stream().allMatch(this::isReadOnly);
        if (!readOnly && !allowWrite) {
            throw new ApiException(HttpStatus.FORBIDDEN, "检测到写操作，请在界面上明确确认后执行");
        }
        try {
            Connection connection = connection(connectionId);
            long started = System.nanoTime();
            int affectedRows = 0;
            QueryResult last = QueryResult.empty(statements.get(statements.size() - 1), readOnly, 0, 0);
            connection.setAutoCommit(!allowWrite);
            for (String statement : statements) {
                try (Statement jdbcStatement = connection.createStatement()) {
                    jdbcStatement.setQueryTimeout(properties.queryTimeoutSeconds());
                    jdbcStatement.setMaxRows(readOnly ? properties.maxRows() : 0);
                    boolean hasResultSet = jdbcStatement.execute(statement);
                    if (hasResultSet) {
                        last = readResultSet(statement, readOnly, jdbcStatement.getResultSet());
                    } else {
                        affectedRows += jdbcStatement.getUpdateCount();
                        last = QueryResult.empty(statement, readOnly, elapsed(started), affectedRows);
                    }
                }
            }
            if (allowWrite) {
                connection.commit();
            }
            return new QueryResult(last.columns(), last.rows(), affectedRows,
                    elapsed(started), readOnly, sql);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Map<String, Object> overview(String connectionId) {
        try {
            Connection connection = connection(connectionId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("database", connection.getSchema());
            result.put("product", connection.getMetaData().getDatabaseProductName());
            result.put("version", connection.getMetaData().getDatabaseProductVersion());
            result.put("url", connection.getMetaData().getURL());
            return result;
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Map<String, Object>> tables(String connectionId, String database) {
        try {
            Connection connection = connection(connectionId);
            String schema = database == null || database.isBlank()
                    ? connection.getSchema()
                    : safeIdentifier(database);
            return toMaps("""
                    SELECT TABLE_NAME AS name, 'TABLE' AS type, '' AS remarks
                    FROM ALL_TABLES
                    WHERE OWNER = '%s'
                    ORDER BY TABLE_NAME
                    """.formatted(schema.toUpperCase(Locale.ROOT)), connection);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Map<String, Object> tableDetail(String connectionId, String table) {
        try {
            Connection connection = connection(connectionId);
            String schema = connection.getSchema().toUpperCase(Locale.ROOT);
            String tableName = safeIdentifier(table).toUpperCase(Locale.ROOT);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("columns", toMaps("""
                    SELECT COLUMN_NAME AS Field, DATA_TYPE AS Type, DATA_LENGTH AS Length,
                           CASE WHEN NULLABLE = 'Y' THEN 'YES' ELSE 'NO' END AS Null,
                           DATA_DEFAULT AS Default
                    FROM ALL_TAB_COLUMNS
                    WHERE OWNER = '%s' AND TABLE_NAME = '%s'
                    ORDER BY COLUMN_ID
                    """.formatted(schema, tableName), connection));
            result.put("indexes", toMaps("""
                    SELECT I.INDEX_NAME AS Key_name, C.COLUMN_NAME AS Column_name,
                           CASE WHEN I.UNIQUENESS = 'UNIQUE' THEN 0 ELSE 1 END AS Non_unique
                    FROM ALL_INDEXES I
                    JOIN ALL_IND_COLUMNS C ON I.INDEX_NAME = C.INDEX_NAME AND I.OWNER = C.INDEX_OWNER
                    WHERE I.TABLE_OWNER = '%s' AND I.TABLE_NAME = '%s'
                    """.formatted(schema, tableName), connection));
            try {
                result.put("ddl", toMaps("""
                        SELECT DBMS_METADATA.GET_DDL('TABLE', '%s', '%s') AS DDL FROM DUAL
                        """.formatted(tableName, schema), connection));
            } catch (SQLException e) {
                result.put("ddl", "无法获取 DDL: " + e.getMessage());
            }
            return result;
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Map<String, Object> performance(String connectionId) {
        try {
            Connection connection = connection(connectionId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("processes", toMaps("""
                    SELECT SESS_ID AS Id, STATE AS State, CLNT_IP AS Host, SQL_TEXT AS Info
                    FROM V$SESSIONS
                    WHERE STATE = 'ACTIVE'
                    """, connection));
            result.put("status", toMaps("""
                    SELECT NAME AS Variable_name, STAT_VAL AS Value
                    FROM V$SYSSTAT
                    """, connection));
            result.put("variables", toMaps("""
                    SELECT PARA_NAME AS Variable_name, PARA_VALUE AS Value
                    FROM V$PARAMETER
                    """, connection));
            return result;
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public QueryResult explain(String connectionId, String sql) {
        String clean = stripTrailingSemicolon(sql.trim());
        if (!isReadOnly(clean)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EXPLAIN 只支持 SELECT/SHOW/DESC 语句");
        }
        try {
            Connection connection = connection(connectionId);
            long started = System.nanoTime();
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("EXPLAIN " + clean)) {
                return readResultSet("EXPLAIN " + clean, true, rows);
            }
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void test(String connectionId) {
        try (Connection ignored = DriverManager.getConnection(
                jdbcArguments(connectionId).get("url"),
                jdbcArguments(connectionId).get("username"),
                jdbcArguments(connectionId).get("password"))) {
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Map<String, String> jdbcArguments(String connectionId) {
        return connectionStore.jdbcArguments(connectionId);
    }

    public void invalidate(String connectionId) {
        Connection connection = connections.remove(connectionId);
        if (connection != null) {
            closeQuietly(connection);
        }
    }

    private Connection connection(String connectionId) throws SQLException {
        Connection existing = connections.get(connectionId);
        if (existing != null && !existing.isClosed()) {
            return existing;
        }
        Map<String, String> arguments = connectionStore.jdbcArguments(connectionId);
        Connection connection = DriverManager.getConnection(arguments.get("url"),
                arguments.get("username"), arguments.get("password"));
        Connection previous = connections.put(connectionId, connection);
        if (previous != null) {
            closeQuietly(previous);
        }
        return connection;
    }

    private QueryResult readResultSet(String sql, boolean readOnly, ResultSet rows) throws SQLException {
        ResultSetMetaData metadata = rows.getMetaData();
        List<String> columns = new ArrayList<>();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            columns.add(metadata.getColumnLabel(index));
        }
        List<List<Object>> values = new ArrayList<>();
        while (rows.next() && values.size() < properties.maxRows()) {
            List<Object> row = new ArrayList<>(columns.size());
            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                row.add(rows.getObject(index));
            }
            values.add(row);
        }
        return new QueryResult(columns, values, 0, 0, readOnly, sql);
    }

    private List<Map<String, Object>> toMaps(String sql, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            QueryResult result = readResultSet(sql, true, rows);
            List<Map<String, Object>> maps = new ArrayList<>();
            for (List<Object> row : result.rows()) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int index = 0; index < result.columns().size(); index++) {
                    map.put(result.columns().get(index), row.get(index));
                }
                maps.add(map);
            }
            return maps;
        }
    }

    private boolean isReadOnly(String sql) {
        String normalized = stripLeadingComments(sql).toLowerCase(Locale.ROOT);
        return normalized.startsWith("select") || normalized.startsWith("show")
                || normalized.startsWith("desc") || normalized.startsWith("describe")
                || normalized.startsWith("explain");
    }

    private String stripLeadingComments(String sql) {
        String value = sql.trim();
        while (value.startsWith("/*")) {
            int end = value.indexOf("*/");
            if (end < 0) {
                break;
            }
            value = value.substring(end + 2).trim();
        }
        while (value.startsWith("--")) {
            int end = value.indexOf('\n');
            if (end < 0) {
                break;
            }
            value = value.substring(end + 1).trim();
        }
        return value;
    }

    private String stripTrailingSemicolon(String sql) {
        return sql.endsWith(";") ? sql.substring(0, sql.length() - 1) : sql;
    }

    private List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        for (int index = 0; index < sql.length(); index++) {
            char character = sql.charAt(index);
            current.append(character);
            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                }
                continue;
            }
            if (character == '\'' || character == '"' || character == '`') {
                quote = character;
            } else if (character == ';') {
                addStatement(statements, current);
            }
        }
        addStatement(statements, current);
        return statements;
    }

    private void addStatement(List<String> statements, StringBuilder current) {
        String statement = stripTrailingSemicolon(current.toString().trim());
        current.setLength(0);
        if (!statement.isBlank()) {
            statements.add(statement);
        }
    }

    private String safeIdentifier(String value) {
        if (!value.matches("[A-Za-z0-9_$]+")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "非法表名");
        }
        return value;
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
