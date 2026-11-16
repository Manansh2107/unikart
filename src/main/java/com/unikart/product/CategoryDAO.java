
package com.unikart.product;

import com.unikart.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {

        List<Category> categories = new ArrayList<>();

        String sql = "SELECT category_id, category_name, description "
                + "FROM categories ORDER BY category_id";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Category category = new Category();

                category.setCategoryId(result.getInt("category_id"));
                category.setCategoryName(result.getString("category_name"));
                category.setDescription(result.getString("description"));

                categories.add(category);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching categories!", e);
        }

        return categories;
    }
}
