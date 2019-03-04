package io.euruapp.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.euruapp.model.User

@Dao /*CRUD*/
interface RoomAppDao {

    /**
     * Create
     */
    @Insert
    fun createUser(vararg user: User)

    /**
     * Read
     */
    @Query("SELECT * FROM users WHERE `key` = :key")
    fun getCurrentUser(key: String): LiveData<User>

    /**
     * Update
     */
    @Update
    fun updateUser(vararg user: User)

    /**
     * Delete
     */
    @Delete
    fun deleteUser(vararg user: User)

}