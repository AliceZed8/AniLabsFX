package service;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DbService {
    private static final DbService instance = new DbService();

    private static final String DB_URL = "jdbc:sqlite:app.db";
    private Connection connection;



    private DbService() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to create DB Service");
        }
    }

    public static DbService getInstance() {
        return instance;
    }

    private void initDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS watch_history (
                    anime_id INTEGER NOT NULL,
                    translator_id INTEGER NOT NULL,
                    episode_num INTEGER NOT NULL,
                    position_ns INTEGER NOT NULL,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (anime_id, translator_id, episode_num)
                );
            """);
    }

    // Сохранить или обновить позицию просмотра
    public void saveWatchProgress(Integer animeId, Integer translatorId, Integer episodeNum, Long positionNs) {
        String sql = """
            INSERT INTO watch_history (anime_id, translator_id, episode_num, position_ns, updated_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(anime_id, translator_id, episode_num)
            DO UPDATE SET position_ns = excluded.position_ns, updated_at = CURRENT_TIMESTAMP;
        """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, animeId);
            preparedStatement.setInt(2, translatorId);
            preparedStatement.setInt(3, episodeNum);
            preparedStatement.setLong(4, positionNs);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to save watch progress: anime_id=" + animeId
                    + " translator_id=" + translatorId
                    + " episode_num=" + episodeNum
                    + " position_ns=" + positionNs
            );
        }
    }




    public Map<Integer, Long> getWatchProgress(Integer animeId, Integer translatorId) {
        String sql = """
            SELECT episode_num, position_ns FROM watch_history
            WHERE anime_id = ? AND translator_id = ?;
        """;
        Map<Integer, Long> progressMap = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, animeId);
            preparedStatement.setInt(2, translatorId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Integer episodeNum = rs.getInt("episode_num");
                    Long positionNs = rs.getLong("position_ns");
                    progressMap.put(episodeNum, positionNs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to get watch progress for anime_id=" + animeId + " translator_id=" + translatorId);
        }

        return progressMap;
    }


    // Получить позицию просмотра
    public Optional<Long> getWatchProgress(Integer animeId, Integer translatorId, Integer episodeNum) {
        String sql = """
            SELECT position_ns FROM watch_history
            WHERE anime_id = ? AND translator_id = ? AND episode_num = ?;
        """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, animeId);
            preparedStatement.setInt(2, translatorId);
            preparedStatement.setInt(3, episodeNum);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("position_ns"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to get watch progress for anime_id=" + animeId
                    + " translator_id=" + translatorId
                    + " episode_num=" + episodeNum);
        }
        return Optional.empty();
    }
}
