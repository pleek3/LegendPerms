package de.legend.legendperms.database;

/**
 * Created by YannicK S. on 28.05.2023
 */
public class DatabaseUpdate extends AsyncDatabaseUpdate {

    /**
     * Lädt die Daten.
     */
    public void loadData() {

    }

    /**
     * Speichert die Daten.
     */
    public void saveData() {

    }

    /**
     * Löscht die Daten.
     */
    public void deleteData() {

    }


    /**
     * Löscht die Daten asynchron.
     */
    public void deleteDataAsync() {
        executeAsync(this::deleteData);
    }

    /**
     * Lädt die Daten asynchron.
     */
    public void loadDataAsync() {
        executeAsync(this::loadData);
    }

    /**
     * Speichert die Daten asynchron.
     */
    public void saveDataAsync() {
        executeAsync(this::saveData);
    }
}
