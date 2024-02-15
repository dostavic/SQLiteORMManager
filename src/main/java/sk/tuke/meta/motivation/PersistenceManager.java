package sk.tuke.meta.motivation;

import java.util.List;
import java.util.Optional;

public interface PersistenceManager<T> {
    /**
     * Get specific object from database.
     *
     * @param id Identifier of the object
     * @return New object based on the database row, or empty if specified
     * identifier is not present in database.
     */
    Optional<T> get(long id);

    /**
     * Get all objects from the database.
     *
     * @return List of new object based on database contents.
     */
    List<T> getAll();

    /**
     * Save object state into database.
     * If object had <code>id</code> field different to zero then database
     * update would be performed, otherwise insert would be performed and
     * generated id would be stored in the <code>id</code> field.
     *
     * @param object Object to save.
     */
    void save(T object);
}
